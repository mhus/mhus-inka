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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.errors.MException;

/**
 * Load the configuration from a properties file. The implementation do not support inner
 * configurations.
 *
 * @author mhu
 */
public class EmptyConfig extends IConfig implements IFlatConfig {

    private static final long serialVersionUID = 1L;
    protected Properties properties = new Properties();
    protected boolean changed = false;
    protected String name;

    public EmptyConfig() {}

    public EmptyConfig(String fill) {
        properties = new Properties();
        if (fill != null) {
            try {
                readConfig(new StringReader(fill));
            } catch (IOException e) {
            }
        }
    }

    public void writeConfig(Writer os) throws IOException {
        properties.store(os, "");
        changed = false;
    }

    public void readConfig(Reader file) throws IOException {
        properties.load(file);
    }

    public void configRemoved() {}

    @Override
    public IConfig getNode(String key) {
        return null;
    }

    @Override
    public List<IConfig> getNodes(String key) {
        return MCollection.getEmptyList();
    }

    @Override
    public List<IConfig> getNodes() {
        return MCollection.getEmptyList();
    }

    @Override
    public List<String> getNodeKeys() {
        // return (String[]) properties.keySet().toArray(new String[properties.size()]);
        return MCollection.getEmptyList();
    }

    @Override
    public List<String> getPropertyKeys() {
        LinkedList<String> out = new LinkedList<>();
        for (Object key : properties.keySet()) out.add(key.toString());
        return out;
    }

    @Override
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    @Override
    public boolean isProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
        changed = true;
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.setProperty(key, MCast.objectToString(value));
        changed = true;
    }

    public boolean isConfigChanged() {
        return changed;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IConfig createConfig(String key) throws MException {
        throw new MException("not supported");
    }

    @Override
    public int moveConfig(IConfig config, int newPos) throws MException {
        throw new MException("not supported");
    }

    @Override
    public void removeConfig(IConfig config) throws MException {
        throw new MException("not supported");
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public IConfig getParent() {
        return null;
    }

    @Override
    public InputStream getInputStream(String key) {
        return null;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public void clear() {}
}
