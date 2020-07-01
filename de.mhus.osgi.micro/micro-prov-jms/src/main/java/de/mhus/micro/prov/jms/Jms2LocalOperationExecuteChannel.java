/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.micro.prov.jms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.base.service.ServerIdent;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.Successful;
import de.mhus.lib.core.operation.util.MapValue;
import de.mhus.lib.core.pojo.DefaultFilter;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.pojo.PojoParser;
import de.mhus.lib.core.util.SerializedValue;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.micro.core.api.JmsApi;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.osgi.api.jms.JmsDataChannel;
import de.mhus.osgi.jms.services.AbstractJmsDataChannel;

@Component(service = JmsDataChannel.class, immediate = true)
public class Jms2LocalOperationExecuteChannel extends AbstractJmsDataChannel {

    public static CfgString CFG_QUEUE_NAME =
            new CfgString(
                    Jms2LocalOperationExecuteChannel.class,
                    "queue",
                    "sop.operation." + M.l(ServerIdent.class));
    private static CfgBoolean CFG_IS_ACCESS_CONTROL =
            new CfgBoolean(Jms2LocalOperationExecuteChannel.class, "accessControl", true);
    private String ident = M.l(ServerIdent.class).getIdent();

    static Jms2LocalOperationExecuteChannel instance;
    private JmsApi jmsApi;

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    @Override
    protected JmsChannel createChannel() throws JMSException {

        name = getServiceName();
        ServerJms out =
                new ServerJms(new JmsDestination(getQueueName(), false)) {

                    @Override
                    public void receivedOneWay(Message msg) throws JMSException {
                        Jms2LocalOperationExecuteChannel.this.received(msg);
                    }

                    @Override
                    public Message received(Message msg) throws JMSException {
                        return Jms2LocalOperationExecuteChannel.this.received(msg);
                    }
                };

        if (out != null && CFG_IS_ACCESS_CONTROL.value())
            ((ServerJms) out).setInterceptorIn(new TicketAccessInterceptor());
        return out;
    }

    protected String getQueueName() {
        return CFG_QUEUE_NAME.value();
    }

    @Reference
    public void setJmsApi(JmsApi api) {
        this.jmsApi = api;
    }

    protected String getJmsConnectionName() {
        return jmsApi.getDefaultConnectionName();
        // return "sop";
    }

    protected OperationResult doExecute(String path, VersionRange version, IConfig request)
            throws NotFoundException {

        log().d("execute operation", path, request);

        OperationApi api = M.l(OperationApi.class);
        OperationResult res =
                api.doExecute(
                        path,
                        version,
                        null,
                        request,
                        OperationApi.LOCAL_ONLY,
                        OperationApi.RAW_RESULT);

        log().d("operation result", path, res, res == null ? "" : res.getResult());
        return res;
    }

    protected List<String> getPublicOperations() {
        LinkedList<String> out = new LinkedList<String>();
        OperationApi admin = M.l(OperationApi.class);
        for (OperationDescriptor desc : admin.findOperations("*", null, null)) {
            if (!JmsOperationProvider.PROVIDER_NAME.equals(desc.getProvider())) {
                try {
                    out.add(desc.getPath() + ":" + desc.getVersionString());
                } catch (Throwable t) {
                    log().d(desc, t);
                }
            }
        }

        return out;
    }

    protected OperationDescriptor getOperationDescription(String path, VersionRange version)
            throws NotFoundException {
        OperationApi admin = M.l(OperationApi.class);
        OperationDescriptor desc = admin.findOperation(path, version, null);
        if (desc == null) return null;
        return desc;
    }

    @Override
    public String getConnectionName() {
        connectionName = jmsApi.getDefaultConnectionName();
        return connectionName;
    }

    /** Receive operation calls and return the answer */
    @SuppressWarnings("rawtypes")
    protected Message received(Message msg) throws JMSException {

        String path = msg.getStringProperty(JmsApi.PARAM_OPERATION_PATH);
        if (path == null) return null;
        String version = msg.getStringProperty(JmsApi.PARAM_OPERATION_VERSION);
        IConfig request = null;
        if (msg instanceof MapMessage) {
            request = MJms.getMapProperties((MapMessage) msg);
        } else if (msg instanceof ObjectMessage) {
            Serializable obj = ((ObjectMessage) msg).getObject();
            if (obj == null) {

            } else if (obj instanceof MConfig) {
                request = (MConfig) obj;
            } else if (obj instanceof Map) {
                request = new MProperties((Map) obj);
            }
        }

        if (request == null) request = new MConfig(); // empty

        OperationResult res = null;
        if (path.equals(JmsApi.OPERATION_LIST)) {
            String list = MString.join(getPublicOperations().iterator(), ",");
            res = new Successful(JmsApi.OPERATION_LIST, "list", OperationResult.OK, "list", list);
        } else if (path.equals(JmsApi.OPERATION_INFO)) {
            String id = request.getString(JmsApi.PARAM_OPERATION_ID, null);
            if (id == null)
                res = new NotSuccessful(JmsApi.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
            else {
                try {
                    OperationDescriptor des =
                            getOperationDescription(
                                    id, version == null ? null : new VersionRange(version));
                    res =
                            new Successful(
                                    JmsApi.OPERATION_INFO,
                                    "list",
                                    OperationResult.OK,
                                    "group",
                                    des.getAddress().getGroup(),
                                    "id",
                                    des.getAddress().getName(),
                                    "form",
                                    des.getForm() == null ? "" : des.getForm().toString(),
                                    "title",
                                    des.getTitle());
                } catch (NotFoundException nfe) {
                    res =
                            new NotSuccessful(
                                    JmsApi.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
                }
            }
        } else
            try {
                res =
                        doExecute(
                                path,
                                version == null ? null : new VersionRange(version),
                                request);
            } catch (NotFoundException nfe) {
                res = new NotSuccessful(path, "not found", OperationResult.NOT_FOUND);
            }
        Message ret = null;
        boolean consumed = false;

        // check if map message is possible and create a result message if possible
        if (res != null && res.getResult() != null && res.getResult() instanceof Map) {
            // Map Message is allowed if all values are primitives. If not use object Message
            consumed = true;
            ret = getServer().createMapMessage();
            ret.setStringProperty("_encoding", "map");

            Map<?, ?> map = (Map<?, ?>) res.getResult();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (MJms.isMapProperty(value))
                    MJms.setMapProperty(
                            String.valueOf(entry.getKey()), entry.getValue(), (MapMessage) ret);
                else {
                    consumed = false;
                    ret = null;
                    break;
                }
            }
        }

        // create result message and fill with result data from operation
        if (consumed) {
            // already done
        } else if (res != null && res.getResult() != null) {
            if (res.getResult() instanceof SerializedValue) {
                ret = getServer().createObjectMessage();
                ret.setStringProperty("_encoding", "serialized");
                ((ObjectMessage) ret).setObject(((SerializedValue) res.getResult()).getValue());
            } else if (res.getResult() instanceof MapValue) {
                ret = getServer().createMapMessage();
                ret.setStringProperty("_encoding", "mapvalue");
                MJms.setMapProperties(
                        (Map<?, ?>) ((MapValue) res.getResult()).getValue(), (MapMessage) ret);
            } else if (res.getResult() instanceof InputStream) {
                ret = getServer().createBytesMessage();
                ret.setStringProperty("_encoding", "stream");
                try (InputStream is = (InputStream) res.getResult()) {
                    byte[] buffer = new byte[1024 * 10];
                    while (true) {
                        int size = is.read(buffer);
                        if (size == -1) break;
                        if (size == 0) MThread.sleep(200);
                        else {
                            ((BytesMessage) ret).writeBytes(buffer, 0, size);
                        }
                    }
                } catch (IOException e) {
                    throw new JMSException(e.toString());
                }
            } else if (res.getResult() instanceof File) {
                ret = getServer().createBytesMessage();
                ret.setStringProperty("_encoding", "file");
                ret.setStringProperty("_file", ((File) res.getResult()).getName());
                try {
                    File f = (File) res.getResult();
                    try (FileInputStream is = new FileInputStream(f)) {
                        byte[] buffer = new byte[1024 * 10];
                        while (true) {
                            int size = is.read(buffer);
                            if (size == -1) break;
                            if (size == 0) MThread.sleep(200);
                            else {
                                ((BytesMessage) ret).writeBytes(buffer, 0, size);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new JMSException(e.toString());
                }
            } else if (res.getResult() instanceof byte[]) {
                ret = getServer().createBytesMessage();
                ret.setStringProperty("_encoding", "byte[]");
                ((BytesMessage) ret).writeBytes((byte[]) res.getResult());
            } else if (res.getResult() instanceof Serializable) {
                ret = getServer().createObjectMessage();
                ret.setStringProperty("_encoding", "serialized");
                ((ObjectMessage) ret).setObject((Serializable) res.getResult());
            } else {
                ret = getServer().createMapMessage();
                ret.setStringProperty("_encoding", "pojo");
                try {
                    IProperties prop =
                            MPojo.pojoToProperties(
                                    res.getResult(),
                                    new PojoModelFactory() {

                                        @Override
                                        public PojoModel createPojoModel(Class<?> pojoClass) {
                                            PojoModel model =
                                                    new PojoParser()
                                                            .parse(pojoClass, "_", null)
                                                            .filter(
                                                                    new DefaultFilter(
                                                                            true, false, false,
                                                                            false, true))
                                                            .getModel();
                                            return model;
                                        }
                                    });
                    MJms.setMapProperties(prop, (MapMessage) ret);
                } catch (IOException e) {
                    log().w(path, res, e);
                    ret.setStringProperty("_error", e.getMessage());
                }
            }
        } else {
            ret = getServer().createTextMessage(null);
            ret.setStringProperty("_encoding", "empty");
        }

        // fill metadata of result message
        if (res == null) {
            ret.setLongProperty("rc", OperationResult.INTERNAL_ERROR);
            ret.setStringProperty("msg", "null");
            ret.setBooleanProperty("successful", false);
        } else {
            ret.setLongProperty("rc", res.getReturnCode());
            ret.setStringProperty("msg", res.getMsg());
            ret.setBooleanProperty("successful", res.isSuccessful());
            OperationDescription next = res.getNextOperation();
            if (next != null) {
                ret.setStringProperty("next.path", next.getPath());
                MProperties prop = new MProperties(next.getParameters());
                MJms.setProperties("next.p.", prop, ret);
            }
        }
        ret.setStringProperty("path", path);
        ret.setStringProperty("source", ident);
        ret.setStringProperty("host", MSystem.getHostname());
        ret.setStringProperty("reply_source", msg.getStringProperty("source"));
        ret.setStringProperty("reply_host", msg.getStringProperty("host"));

        return ret;
    }

    protected String getServiceName() {
        return getClass().getCanonicalName();
    }

    protected ServerJms getServer() {
        return (ServerJms) getChannel();
    };
}
