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

import de.mhus.lib.core.M;
import de.mhus.lib.core.util.MObject;
import de.mhus.lib.server.service.Command;
import de.mhus.lib.server.service.GarbageCollectionTrigger;
import de.mhus.lib.server.service.Quit;
import de.mhus.lib.server.service.Reset;
import de.mhus.lib.server.service.Set;

public class DefaultTasks extends MObject {

    @SuppressWarnings("unchecked")
    public DefaultTasks() {
        Main main = M.l(Main.class);
        main.appendList(new TaskListDefinition("q", "Quit", new Class[] {Quit.class}));

        main.appendList(new TaskListDefinition("r", "Reset", new Class[] {Reset.class}));

        main.appendList(
                new TaskListDefinition(
                        "gc", "Garbage Collection", new Class[] {GarbageCollectionTrigger.class}));

        main.appendList(new TaskListDefinition("set", "Set Options", new Class[] {Set.class}));

        main.appendList(
                new TaskListDefinition(
                        "cmd",
                        "Commands ... cmd0=objects.clear&cmd1=options.clear",
                        new Class[] {Command.class}));
    }
}
