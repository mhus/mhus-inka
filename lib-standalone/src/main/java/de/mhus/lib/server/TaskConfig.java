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
package de.mhus.lib.server;

import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.core.M;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.jmx.MJmx;

public class TaskConfig extends MJmx {

    private IConfig config;
    private Map<String, String> options = new HashMap<String, String>();
    private Map<String, Object> objects = new HashMap<String, Object>();

    public void init() throws Exception {
        init(M.l(IConfig.class));
    }

    public void init(IConfig config) throws Exception {

        //		if (config == null)
        //			config = MApi.instance().getConfig();
        this.config = config;
    }

    public IConfig config() {
        return config;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public Map<String, Object> objects() {
        return objects;
    }

    public Map<String, String> getOptions() {
        return options;
    }
}
