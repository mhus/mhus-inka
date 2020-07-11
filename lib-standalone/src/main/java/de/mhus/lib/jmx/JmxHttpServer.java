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
package de.mhus.lib.jmx;

import javax.management.ObjectName;

import com.sun.jdmk.comm.AuthInfo;
import com.sun.jdmk.comm.HtmlAdaptorServer;

import de.mhus.lib.annotations.jmx.JmxManaged;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.jmx.MJmx;
import de.mhus.lib.core.jmx.MRemoteManager;
import de.mhus.lib.errors.MException;

@JmxManaged(descrition = "Jmx Http Server")
public class JmxHttpServer extends MJmx {

    private HtmlAdaptorServer server;
    private IConfig config;

    public void setConfig(IConfig config) {
        this.config = config;
    }

    @JmxManaged
    public void openServer() throws MException {
        if (config == null) { // auto load config
            config = MApi.getCfg(this);
        }
        if (config == null || server != null) return;
        server = new HtmlAdaptorServer(config.getInt("port", 1098));

        for (IConfig cuser : config.getArray("user")) {
            server.addUserAuthenticationInfo(
                    new AuthInfo(
                            cuser.getExtracted("name"),
                            MPassword.decode(cuser.getExtracted("password"))));
        }

        // TODO load user auth infos into server
        // server.setMBeanServer(mbs);
        try {
            M.l(MRemoteManager.class)
                    .register(new ObjectName("adaptor:proptocol=HTTP"), server, false, false);
            //			mbs.registerMBean(server, new ObjectName("adaptor:proptocol=HTTP"));
            server.setMBeanServer(M.l(MRemoteManager.class).getMBeanServer());
        } catch (Exception e) {
            log().w(e);
            server = null;
            return;
        }
        server.start();
    }

    @JmxManaged
    public void closeServer() {
        if (server == null) return;
        server.stop();
        server = null;
    }
}
