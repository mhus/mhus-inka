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

public class TaskListDefinition implements Comparable<TaskListDefinition> {

    private String name;
    private String description;
    private Class<? extends Task>[] tasks;

    public TaskListDefinition(String name, String description, Class<? extends Task>[] tasks) {
        this.name = name;
        this.description = description;
        this.tasks = tasks;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends Task>[] getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return name + ": " + description;
    }

    @Override
    public int compareTo(TaskListDefinition in) {
        return toString().compareTo(String.valueOf(in));
    }
}
