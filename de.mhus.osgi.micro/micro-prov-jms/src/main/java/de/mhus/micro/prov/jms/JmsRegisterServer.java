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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.w3c.dom.Document;

import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.ServerJms;
import de.mhus.micro.core.api.JmsApi;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.prov.jms.JmsApiImpl.JmsOperationDescriptor;
import de.mhus.osgi.api.jms.JmsDataChannel;
import de.mhus.osgi.jms.services.AbstractJmsDataChannel;

/**
 * The class subscribes and watches for incoming operation registry information. They will be send
 * by JmsApiImpl.sendLocalOperations
 *
 * @author mikehummel
 */
@Component(immediate = true, service = JmsDataChannel.class)
public class JmsRegisterServer extends AbstractJmsDataChannel {

    private JmsApi jmsApi;

    @Reference
    public void setJmsApi(JmsApi api) {
        this.jmsApi = api;
        // TODO
//        new MThread(
//                        new Runnable() {
//
//                            @Override
//                            public void run() {
//                                while (true) {
//                                    if (JmsApiImpl.instance.isConnected()) {
//                                        JmsApiImpl.instance.requestRegistry();
//                                        return;
//                                    }
//                                    MThread.sleep(10000);
//                                }
//                            }
//                        })
//                .start();
    }

    @Override
    protected JmsChannel createChannel() {
        return new ServerJms(new JmsDestination(JmsApi.REGISTRY_TOPIC, true)) {

            @Override
            public void receivedOneWay(Message msg) throws JMSException {

                MapMessage m = (MapMessage) msg;
                String type = m.getStringProperty("type");
                if ("request".equals(type)) {
                    JmsApiImpl.instance.lastRegistryRequest = System.currentTimeMillis();
                    JmsApiImpl.instance.sendLocalOperations();
                } else if ("registryrequest".equals(type)) {
                    // JmsApiImpl.instance.sendLocalRegistry();
                }

                if (msg instanceof MapMessage
                        && !Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME
                                .value()
                                .equals(
                                        msg.getStringProperty(
                                                "queue")) // do not process my own messages
                ) {

                    if ("operations".equals(type)) {
                        String queue = m.getStringProperty("queue");
                        String connection = jmsApi.getDefaultConnectionName(); // TODO configurable?
                        int cnt = 0;
                        String path = null;
                        synchronized (JmsApiImpl.instance.register) {
                            long now = System.currentTimeMillis();
                            while (m.getString("operation" + cnt) != null) {
                                path = m.getString("operation" + cnt);
                                String version = m.getString("version" + cnt);
                                String tags = m.getString("tags" + cnt);
                                String title = m.getString("title" + cnt);
                                String form = m.getString("form" + cnt);
                                UUID uuid = UUID.fromString(m.getString("uuid" + cnt));
                                DefRoot model = null;
                                if (form != null) {
                                    try {
                                        Document doc = MXml.loadXml(form);
                                        model = ModelUtil.toModel(doc.getDocumentElement());
                                    } catch (Throwable t) {
                                    }
                                }
                                HashMap<String, String> parameters = null;
                                for (@SuppressWarnings("unchecked")
                                        Enumeration<String> enu = m.getMapNames();
                                        enu.hasMoreElements(); ) {
                                    String key = enu.nextElement();
                                    String start = "param" + cnt + ".";
                                    if (key.startsWith(start)) {
                                        if (parameters == null) parameters = new HashMap<>();
                                        parameters.put(
                                                key.substring(start.length()), m.getString(key));
                                    }
                                }
                                cnt++;
                                String ident =
                                        connection + "," + queue + "," + path + "," + version;
                                JmsOperationDescriptor desc =
                                        JmsApiImpl.instance.register.get(ident);
                                if (desc == null) {
                                    OperationAddress a =
                                            new OperationAddress(
                                                    JmsOperationProvider.PROVIDER_NAME
                                                            + "://"
                                                            + path
                                                            + ":"
                                                            + version
                                                            + "/"
                                                            + queue
                                                            + "/"
                                                            + connection);
                                    OperationDescription d =
                                            new OperationDescription(
                                                    uuid,
                                                    a.getGroup(),
                                                    a.getName(),
                                                    a.getVersion(),
                                                    null,
                                                    title);
                                    d.setForm(model);
                                    d.setParameters(parameters);
                                    desc =
                                            new JmsOperationDescriptor(
                                                    uuid,
                                                    a,
                                                    d,
                                                    tags == null
                                                            ? null
                                                            : MCollection.toTreeSet(
                                                                    tags.split(";")));
                                    JmsApiImpl.instance.register.put(ident, desc);
                                }
                                desc.setLastUpdated();
                            }
                            // remove stare
                            JmsApiImpl.instance
                                    .register
                                    .entrySet()
                                    .removeIf(
                                            entry ->
                                                    entry.getValue()
                                                                    .getAddress()
                                                                    .getPart(0)
                                                                    .equals(queue)
                                                            && entry.getValue().getLastUpdated()
                                                                    < now);
                        }
                    } else if ("registrypublish".equals(type)) {
                      //TODO Deprecated
//                        RegistryManager api = M.l(RegistryManager.class);
//                        int cnt = 0;
//                        String ident = m.getStringProperty("ident");
//                        String scope = m.getStringProperty("scope");
//                        long updated = System.currentTimeMillis();
//                        while (m.getString("path" + cnt) != null) {
//                            String path = m.getString("path" + cnt);
//                            String value = m.getString("value" + cnt);
//                            long timeout = m.getLong("timeout" + cnt);
//                            boolean readOnly = m.getBoolean("readOnly" + cnt);
//                            boolean persistent = m.getBoolean("persistent" + cnt);
//                            api.setParameterFromRemote(
//                                    new RegistryValue(
//                                            value,
//                                            ident,
//                                            updated,
//                                            path,
//                                            timeout,
//                                            readOnly,
//                                            persistent));
//                            cnt++;
//                        }
//                        if ("full".equals(scope)) {
//                            for (RegistryValue value : api.getAll()) {
//                                if (value.getSource().equals(ident) && value.getUpdated() < updated)
//                                    api.removeParameterFromRemote(value.getPath(), ident);
//                            }
//                        }
                    } else if ("registryremove".equals(type)) {
                        //TODO Deprecated
//                        RegistryManager api = M.l(RegistryManager.class);
//                        int cnt = 0;
//                        String ident = m.getStringProperty("ident");
//                        while (m.getString("path" + cnt) != null) {
//                            String path = m.getString("path" + cnt);
//                            api.removeParameterFromRemote(path, ident);
//                            cnt++;
//                        }
                    } else log().d("unknown type", type);
                }
            }

            @Override
            public Message received(Message msg) throws JMSException {
                receivedOneWay(msg);
                return null;
            }
        };
    }

    @Override
    public String getConnectionName() {
        connectionName = jmsApi.getDefaultConnectionName();
        return connectionName;
    }
}
