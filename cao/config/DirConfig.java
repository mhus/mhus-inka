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
package de.mhus.lib.core.config;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.errors.MException;

public class DirConfig extends PropertiesConfig {

    private static final long serialVersionUID = 1L;
    private File dir;

    public DirConfig(File dir) {
        this.dir = dir;
        File f = new File(dir, "_.properties");
        if (f.exists() && f.isFile()) {
            try {
                FileReader r = new FileReader(f);
                readConfig(r);
                r.close();
            } catch (Exception e) {
                log().d(dir, e);
            }
        }
    }

    @Override
    public List<String> getPropertyKeys() {
        return new EmptyList<>();
    }

    @Override
    public IConfig getNode(String key) {
        try {
            String keyDot = key + ".";
            for (File f : dir.listFiles())
                if (f.isDirectory() && f.getName().equals(key)
                        || !f.isDirectory() && f.getName().startsWith(keyDot))
                    return MConfig.createConfigFor(f);
        } catch (Throwable e) {

        }
        return null;
    }

    @Override
    public List<IConfig> getNodes(String key) {
        LinkedList<IConfig> out = new LinkedList<>();
        String keyDot = key + ".";
        for (File f : dir.listFiles())
            try {
                if (f.isDirectory() && f.getName().equals(key)
                        || !f.isDirectory() && f.getName().startsWith(keyDot))
                    out.add(MConfig.createConfigFor(f));
            } catch (Throwable e) {

            }

        return out;
    }

    @Override
    public List<IConfig> getNodes() {
        LinkedList<IConfig> out = new LinkedList<>();
        for (File f : dir.listFiles())
            try {
                if (f.isDirectory() && !f.getName().startsWith(".") && !f.isHidden())
                    out.add(MConfig.createConfigFor(f));
                else if (!f.isDirectory() && !f.isHidden()) {
                    IConfig conf = MConfig.createConfigFor(f);
                    if (conf != null) out.add(conf);
                }
            } catch (Throwable e) {

            }

        return out;
    }

    @Override
    public List<String> getNodeKeys() {
        LinkedList<String> out = new LinkedList<String>();

        for (File f : dir.listFiles()) {
            if (f.isDirectory() && !out.contains(f.getName())) out.add(f.getName());
            else if (!f.isDirectory() && !f.getName().startsWith("_")) {
                String name = f.getName();
                int pos = name.indexOf('.');
                if (pos > 0) name = name.substring(0, pos);
                if (!out.contains(name)) out.add(name);
            }
        }

        return out;
    }

    @Override
    public String getName() {
        return dir.getName();
    }

    @Override
    public IConfig createConfig(String key) throws MException {
        return null;
    }

    @Override
    public int moveConfig(IConfig config, int newPos) throws MException {
        return 0;
    }

    @Override
    public void removeConfig(IConfig config) throws MException {}

    @Override
    public IConfig getParent() {
        File parent = dir.getParentFile();
        if (parent == null) return null;
        return new DirConfig(parent);
    }

    @Override
    public boolean isEditable() {
        // TODO Auto-generated method stub
        return false;
    }

    public File getDirectory() {
        return dir;
    }
}
