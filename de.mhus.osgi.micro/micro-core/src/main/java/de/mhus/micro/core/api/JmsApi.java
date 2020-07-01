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
package de.mhus.micro.core.api;

import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.cfg.CfgString;

public interface JmsApi {

    public static final CfgString TRUST_NAME =
            new CfgString(JmsApi.class, "aaaTrustName", "default");
    public static final CfgBoolean CFG_ENABLED =
            new CfgBoolean(JmsApi.class, "enabled", true);
    public static final CfgLong CFG_SYNCHRONIZE_WAIT =
            new CfgLong(
                    JmsApi.class,
                    "synchronizeWait",
                    MPeriod.MINUTE_IN_MILLISECOUNDS * 3);
    
    public static final String PARAM_AAA_TICKET = "_mhus_aaa_ticket";
    public static final String PARAM_LOCALE = "_mhus_locale";
    public static final String OPT_FORCE_MAP_MESSAGE = "forceMapMessage";
    public static final String REGISTRY_TOPIC = "sop.registry";
    public static final String OPT_TIMEOUT = "timeout";
    public static final String OPT_ONE_WAY = "oneWay";

    public static final String PARAM_OPERATION_PATH = "path";
    public static final String OPERATION_LIST = "_list";
    public static final String OPERATION_INFO = "_get";
    public static final String PARAM_OPERATION_ID = "id";
    
    public static final String PARAM_SUCCESSFUL = "successful";
    public static final String PARAM_MSG = "msg";
    public static final String PARAM_RC = "rc";
    public static final String PARAM_OPERATION_VERSION = "version";
    public static final String PARAM_ERROR = "_error";
    
    String getDefaultConnectionName();

    void sendLocalOperations();

    void requestOperationRegistry();
}
