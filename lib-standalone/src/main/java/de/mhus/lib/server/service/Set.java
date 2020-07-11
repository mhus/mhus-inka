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
package de.mhus.lib.server.service;

import java.util.Map.Entry;

import de.mhus.lib.server.Task;

public class Set extends Task {

    @Override
    public void pass() throws Exception {

        if (options.getBoolean("__objclear", false)) {
            log().i("objects clear");
            base.objects().clear();
        }
        if (options.getBoolean("__optclear", false)) {
            log().i("options clear");
            base.getOptions().clear();
        }

        for (Entry<String, Object> entry : options) {
            log().i(entry.getKey(), String.valueOf(entry.getValue()));
            base.getOptions().put(entry.getKey(), String.valueOf(entry.getValue()));
        }

        log().i("print", base.getOptions());
    }
}
