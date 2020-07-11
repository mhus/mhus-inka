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

import java.util.Map;

import de.mhus.lib.server.Task;

public class Command extends Task {

    @Override
    public void pass() throws Exception {
        for (int i = 0; options.isProperty("cmd" + i); i++) {
            String cmd = options.getString("cmd" + i, null);
            execute(cmd, i);
        }
    }

    private void execute(String cmd, int i) {
        if (cmd.equals("objects.clear")) {
            base.objects().clear();
        } else if (cmd.equals("options.clear")) {
            base.getOptions().clear();
        } else if (cmd.equals("objects.print")) {
            for (Map.Entry<String, Object> o : base.objects().entrySet())
                System.out.println(o.getKey() + "=" + o.getValue());
        }
    }
}
