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

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.jms.MapMessage;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.w3c.dom.Document;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.base.service.ServerIdent;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.form.ModelUtil;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.micro.core.api.JmsApi;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.osgi.api.jms.JmsUtil;

@Component(immediate = true)
public class JmsApiImpl extends MLog implements JmsApi {

    public static CfgString connectionName = new CfgString(JmsApi.class, "connection", "sop");
    protected static JmsApiImpl instance;

    private ClientJms registerClient;
    HashMap<String, JmsOperationDescriptor> register = new HashMap<>();
    long lastRegistryRequest;

    @Override
    public String getDefaultConnectionName() {
        return connectionName.value();
    }

    @Override
    public void sendLocalOperations() {
        try {
            checkClient();
            MapMessage msg = registerClient.createMapMessage();
            msg.setStringProperty("type", "operations");
            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());

            int cnt = 0;

            for (OperationDescriptor desc :
                    M.l(OperationApi.class).findOperations("*", null, null)) {
                if (!JmsOperationProvider.PROVIDER_NAME.equals(desc.getProvider())) {
                    msg.setString("operation" + cnt, desc.getPath());
                    msg.setString("version" + cnt, desc.getVersionString());
                    String tags = MString.join(desc.getTags().iterator(), ";");
                    if (tags.length() > 0) tags = tags + ";";
                    tags =
                            tags
                                    + OperationDescriptor.TAG_REMOTE
                                    + "=jms;"
                                    + OperationDescriptor.TAG_HOST
                                    + "="
                                    + MSystem.getHostname()
                                    + ";"
                                    + OperationDescriptor.TAG_IDENT
                                    + "="
                                    + M.l(ServerIdent.class).toString();
                    msg.setString("tags" + cnt, tags);
                    msg.setString("title" + cnt, desc.getTitle());
                    msg.setString("uuid" + cnt, desc.getUuid().toString());
                    for (String key : desc.getParameterKeys()) {
                        msg.setString("param" + cnt + "." + key, desc.getParameter(key));
                    }
                    DefRoot form = desc.getForm();
                    if (form != null) {
                        Document doc = ModelUtil.toXml(form);
                        msg.setString("form" + cnt, MXml.toString(doc.getDocumentElement(), false));
                    }
                    cnt++;
                }
            }

            registerClient.sendJms(msg);
        } catch (Throwable t) {
            log().w(t);
        }
    }

    @Override
    public void requestOperationRegistry() {
        try {
            checkClient();
            MapMessage msg = registerClient.createMapMessage();
            msg.setStringProperty("type", "request");
            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());
            registerClient.sendJmsOneWay(msg);
        } catch (Throwable t) {
            log().w(t);
        }
    }

    private void checkClient() {
        if (registerClient.getJmsDestination().getConnection() == null) {
            JmsConnection con = JmsUtil.getConnection(getDefaultConnectionName());
            if (con != null) registerClient.getJmsDestination().setConnection(con);
        }
    }

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
        registerClient = new ClientJms(new JmsDestination(JmsApi.REGISTRY_TOPIC, true));
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;

        if (registerClient != null) registerClient.close();
        registerClient = null;
        register.clear();
    }

    public static class JmsOperationDescriptor extends OperationDescriptor {

        private long lastUpdated;

        public JmsOperationDescriptor(
                UUID uuid,
                OperationAddress address,
                OperationDescription description,
                Collection<String> tags) {
            super(uuid, address, description, tags);
        }

        public long getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated() {
            lastUpdated = System.currentTimeMillis();
        }
    }

//    public boolean registryPublish(RegistryValue entry) {
//        try {
//            checkClient();
//
//            MapMessage msg = registerClient.createMapMessage();
//            msg.setStringProperty("type", "registrypublish");
//            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
//            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());
//            msg.setStringProperty("ident", M.l(ServerIdent.class).getIdent());
//            msg.setStringProperty("scope", "single");
//
//            msg.setString("path0", entry.getPath());
//            msg.setString("value0", entry.getValue());
//            msg.setLong("timeout0", entry.getTimeout());
//            msg.setBoolean("readOnly0", entry.isReadOnly());
//            msg.setBoolean("persistent0", entry.isPersistent());
//            registerClient.sendJms(msg);
//            return true;
//        } catch (Throwable t) {
//            log().w(t);
//        }
//        return false;
//    }

    public boolean registryRemove(String path) {
        try {
            checkClient();

            MapMessage msg = registerClient.createMapMessage();
            msg.setStringProperty("type", "registryremove");
            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());
            msg.setStringProperty("ident", M.l(ServerIdent.class).getIdent());

            msg.setString("path0", path);

            registerClient.sendJms(msg);
            return true;
        } catch (Throwable t) {
            log().w(t);
        }
        return false;
    }

//    public boolean sendLocalRegistry() {
//        try {
//            checkClient();
//
//            String ident = M.l(ServerIdent.class).getIdent();
//            MapMessage msg = registerClient.createMapMessage();
//            msg.setStringProperty("type", "registrypublish");
//            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
//            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());
//            msg.setStringProperty("ident", ident);
//            msg.setStringProperty("scope", "full");
//
//            RegistryManager api = M.l(RegistryManager.class);
//            if (api == null) {
//                log().d("sendLocalRegistry: API not found");
//                return false;
//            }
//            int cnt = 0;
//            for (RegistryValue entry : api.getAll()) {
//                if (entry.getSource().equals(ident)
//                        && !entry.getPath().startsWith(RegistryApi.PATH_LOCAL)) {
//                    msg.setString("path" + cnt, entry.getPath());
//                    msg.setString("value" + cnt, entry.getValue());
//                    msg.setLong("timeout" + cnt, entry.getTimeout());
//                    msg.setBoolean("readOnly" + cnt, entry.isReadOnly());
//                    msg.setBoolean("persistent" + cnt, entry.isPersistent());
//                    cnt++;
//                }
//            }
//            registerClient.sendJms(msg);
//            return true;
//        } catch (Throwable t) {
//            log().w(t);
//        }
//        return false;
//    }

//    public boolean requestRegistry() {
//        try {
//            checkClient();
//            MapMessage msg = registerClient.createMapMessage();
//            msg.setStringProperty("type", "registryrequest");
//            msg.setStringProperty("connection", M.l(JmsApi.class).getDefaultConnectionName());
//            msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.CFG_QUEUE_NAME.value());
//            registerClient.sendJmsOneWay(msg);
//            return true;
//        } catch (Throwable t) {
//            log().w(t);
//        }
//        return false;
//    }

    public boolean isConnected() {
        return registerClient != null && registerClient.getSession() != null;
    }
}
