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

import java.util.Map;

import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.jmx.MJmx;

public abstract class Task extends MJmx {

    protected IConfig config;
    protected TaskConfig base;
    protected MProperties options;

    public static void main(String[] args) throws Exception {
        String taskName = Thread.currentThread().getStackTrace()[1].getClassName();
        Main.main(new String[] {taskName});
    }

    public boolean initAndRun() throws Exception {
        TaskConfig initializer = new TaskConfig();
        initializer.init();
        init(initializer);
        return run();
    }

    public boolean run() {

        if (options == null) options = new MProperties();

        if (base.getOptions() != null)
            for (Map.Entry<String, String> entry : base.getOptions().entrySet()) {
                if (!options.isProperty(entry.getKey()))
                    options.setProperty(entry.getKey(), entry.getValue());
            }

        boolean ret = true;
        log().i("-----------------------------------------------");
        log().i(">>> EXECUTE", getClass().getCanonicalName(), options);
        //		MApi.instance().getConsole().println("EXECUTE: " + getClass().getCanonicalName());
        MStopWatch watch = new MStopWatch(getClass().getCanonicalName());
        watch.start();
        try {
            pass();
        } catch (Throwable t) {
            t.printStackTrace();
            ret = false;
        }
        watch.stop();
        log().i("-----------------------------------------------");
        log().i("TIME: " + watch.getCurrentTimeAsString());
        //		MApi.instance().getConsole().println("TIME: " + getClass().getCanonicalName() + ": " +
        // watch.getCurrentTimeAsString());
        return ret;
    }

    public void init(TaskConfig tc) {
        this.base = tc;

        this.config = base.config();
    }

    public abstract void pass() throws Exception;

    public void setOptions(Map<String, String> options) {
        this.options = new MProperties();
        if (options != null)
            for (Map.Entry<String, String> entry : options.entrySet())
                this.options.setProperty(entry.getKey(), entry.getValue());
    }
}
