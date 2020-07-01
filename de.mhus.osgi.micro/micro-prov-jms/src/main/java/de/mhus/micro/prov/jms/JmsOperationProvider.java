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
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.base.service.ServerIdent;
import de.mhus.lib.core.operation.NotSuccessful;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.TempFile;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.MJms;
import de.mhus.micro.core.api.JmsApi;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationUtil;
import de.mhus.micro.core.api.OperationsProvider;
import de.mhus.micro.prov.jms.JmsApiImpl.JmsOperationDescriptor;
import de.mhus.osgi.api.jms.JmsUtil;

/**
 * The class provides the remote Operations into the osgi engine.
 *
 * @author mikehummel
 */
@Component(immediate = true, property = "provider=jms")
public class JmsOperationProvider extends MLog implements OperationsProvider {

    protected static final String PROVIDER_NAME = "jms";
    private String ident = M.l(ServerIdent.class).getIdent();

    @Activate
    public void doActivate(ComponentContext ctx) {}

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {}

    @Override
    public void collectOperations(
            List<OperationDescriptor> list,
            String filter,
            VersionRange version,
            Collection<String> providedTags) {
        if (!JmsApi.CFG_ENABLED.value()) return;
        synchronized (JmsApiImpl.instance.register) {
            HashMap<String, JmsOperationDescriptor> register = JmsApiImpl.instance.register;
            for (JmsOperationDescriptor desc : register.values())
                if (OperationUtil.matches(desc, filter, version, providedTags)) list.add(desc);
        }
    }

    @Override
    public OperationResult doExecute(
            String filter,
            VersionRange version,
            Collection<String> providedTags,
            IProperties properties,
            String... executeOptions)
            throws NotFoundException {
        OperationDescriptor d = null;
        synchronized (JmsApiImpl.instance.register) {
            HashMap<String, JmsOperationDescriptor> register = JmsApiImpl.instance.register;
            for (OperationDescriptor desc : register.values()) {
                if (OperationUtil.matches(desc, filter, version, providedTags)) {
                    d = desc;
                    break;
                }
            }
        }
        if (d == null)
            throw new NotFoundException("operation not found", filter, version, providedTags);
        return doExecute(d, properties);
    }

    @Override
    public OperationResult doExecute(
            OperationDescriptor desc, IProperties properties, String... executeOptions)
            throws NotFoundException {

        if (!PROVIDER_NAME.equals(desc.getProvider()))
            throw new NotFoundException("description is from another provider", desc);

        String conName =
                desc.getAddress().partSize() > 1
                        ? desc.getAddress().getPart(1)
                        : JmsApiImpl.instance.getDefaultConnectionName();
        String queueName = desc.getAddress().getPart(0);
        String path = desc.getPath();
        String version = desc.getVersionString();

        JmsConnection con = JmsUtil.getConnection(conName);

        String ticket = AccessUtil.createTrustTicket(JmsApi.TRUST_NAME.value(), AccessUtil.getSubject());
        Locale locale = AccessUtil.getLocale();
        long timeout =
                OperationUtil.getOption(
                        executeOptions,
                        JmsApi.OPT_TIMEOUT,
                        MPeriod.MINUTE_IN_MILLISECOUNDS); // TODO Configurable via execute options

        try {
            return doExecuteOperation(
                    con,
                    queueName,
                    path,
                    version,
                    properties,
                    ticket,
                    locale,
                    timeout,
                    executeOptions);
        } catch (Throwable e) {
            return new NotSuccessful(path, e.getMessage(), OperationResult.INTERNAL_ERROR);
        }
    }

    public OperationResult doExecuteOperation(
            JmsConnection con,
            String queueName,
            String operationName,
            String version,
            IProperties parameters,
            String ticket,
            Locale l,
            long timeout,
            String... options)
            throws Exception {

        if (con == null) throw new JMSException("connection is null");
        try (ClientJms client = new ClientJms(con.createQueue(queueName))) {

            boolean needObject = false;
            if (!OperationUtil.isOption(options, JmsApi.OPT_FORCE_MAP_MESSAGE)) {
                for (Entry<String, Object> item : parameters) {
                    Object value = item.getValue();
                    if (!MJms.isMapProperty(value)) {
                        needObject = true;
                        break;
                    }
                }
            }

            Message msg = null;
            if (needObject) {
                msg = con.createObjectMessage((MProperties) parameters);
            } else {
                msg = con.createMapMessage();
                for (Entry<String, Object> item : parameters) {
                    String name = item.getKey();
                    // if (!name.startsWith("_"))
                    Object value = item.getValue();
                    if (value != null && value instanceof Date)
                        value = MDate.toIsoDateTime((Date) value);
                    else if (value != null
                            && !(value instanceof String)
                            && !value.getClass().isPrimitive()) value = String.valueOf(value);
                    ((MapMessage) msg).setObject(name, value);
                }
                ((MapMessage) msg).getMapNames();
            }

            msg.setStringProperty(JmsApi.PARAM_OPERATION_PATH, operationName);
            msg.setStringProperty(JmsApi.PARAM_OPERATION_VERSION, version);

            if (l == null) l = Locale.getDefault();
            String locale = l.toString();

            msg.setStringProperty(JmsApi.PARAM_AAA_TICKET, ticket);
            msg.setStringProperty(JmsApi.PARAM_LOCALE, locale);

            msg.setStringProperty("source", ident);
            msg.setStringProperty("host", MSystem.getHostname());

            client.setTimeout(timeout);
            // Send Request

            log().d(operationName, "sending Message", queueName, msg, options);

            if (OperationUtil.isOption(options, JmsApi.OPT_ONE_WAY)) {
                client.sendJmsOneWay(msg);
                return null;
            }

            Message answer = client.sendJms(msg);

            // Process Answer

            OperationResult out = new OperationResult();
            out.setOperationPath(operationName);
            if (answer == null) {
                log().d(queueName, operationName, "answer is null");
                out.setSuccessful(false);
                out.setMsg("answer is null");
                out.setReturnCode(OperationResult.INTERNAL_ERROR);
            } else {
                // remote error handling
                String errorMsg = answer.getStringProperty(JmsApi.PARAM_ERROR);
                if (errorMsg != null) {
                    throw new MException("Remote error", errorMsg);
                }

                // check if technical successful
                boolean successful = answer.getBooleanProperty(JmsApi.PARAM_SUCCESSFUL);
                out.setSuccessful(successful);

                if (!successful) out.setMsg(answer.getStringProperty(JmsApi.PARAM_MSG));
                out.setReturnCode(answer.getLongProperty(JmsApi.PARAM_RC));

                // if (successful) { // also errors can have a result object

                if (answer instanceof MapMessage) {
                    MapMessage mapMsg = (MapMessage) answer;
                    out.setResult(MJms.getMapProperties(mapMsg));
                } else if (answer instanceof TextMessage) {
                    out.setMsg(((TextMessage) answer).getText());
                    out.setResult(out.getMsg());
                } else if (answer instanceof BytesMessage) {

                    File tmpFile = TempFile.createTempFile(MSystem.getPid() + "_jms_msg", ".bin");

                    FileOutputStream os = new FileOutputStream(tmpFile);
                    BytesMessage m = (BytesMessage) answer;
                    long length = m.getBodyLength();
                    byte[] buffer = new byte[1024 * 10];
                    long done = 0;
                    while (done < length) {
                        int size = m.readBytes(buffer);
                        if (size > 0) {
                            os.write(buffer, 0, size);
                        }
                        done += size;
                    }
                    os.close();

                    out.setResult(tmpFile);

                } else if (answer instanceof ObjectMessage) {
                    Serializable obj = ((ObjectMessage) answer).getObject();
                    if (obj == null) {
                        out.setResult(null);
                    } else {
                        out.setResult(obj);
                    }
                }
                // }
            }

            return out;
        }
    }

    @Override
    public OperationDescriptor getOperation(OperationAddress addr) throws NotFoundException {
        if (!JmsApi.CFG_ENABLED.value()) return null;
        String connection = JmsApiImpl.instance.getDefaultConnectionName(); // TODO
        String queue = addr.getPart(0);
        String path = addr.getPath();
        String version = addr.getVersionString();
        String ident = connection + "," + queue + "," + path + "," + version;
        synchronized (JmsApiImpl.instance.register) {
            JmsOperationDescriptor res = JmsApiImpl.instance.register.get(ident);
            if (res == null) throw new NotFoundException("operation not found", addr);
            return res;
        }
    }

    @Override
    public void synchronize() {
        if (!JmsApi.CFG_ENABLED.value()) return;
        if (MPeriod.isTimeOut(
                JmsApiImpl.instance.lastRegistryRequest,
                JmsApi.CFG_SYNCHRONIZE_WAIT.value())) {
            long now = System.currentTimeMillis();
            JmsApiImpl.instance.requestOperationRegistry();
            MThread.sleep(30000);

            // remove staled - if not updated in the last 30 seconds
            synchronized (JmsApiImpl.instance.register) {
                JmsApiImpl.instance
                        .register
                        .entrySet()
                        .removeIf(e -> e.getValue().getLastUpdated() < now);
            }
        }
    }
}
