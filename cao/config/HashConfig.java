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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.errors.MException;

public class HashConfig extends IConfig {

    private static final long serialVersionUID = 1L;
    private HashMap<String, String> properties = null;
    private HashMap<String, LinkedList<IConfig>> children = null;
    private LinkedList<HashConfig> childrenAll = null;
    private String name;
    private IConfig parent;

    public HashConfig() {
        this(null, null);
    }

    public HashConfig(String name, IConfig parent) {
        this.name = name;
        this.parent = parent;
        properties = new HashMap<String, String>();
        children = new HashMap<String, LinkedList<IConfig>>();
        childrenAll = new LinkedList<HashConfig>();
    }

    public HashConfig(Map<String, String> fill) {
        this(null, null);
        if (fill != null) properties.putAll(fill);
    }

    @Override
    public List<String> getPropertyKeys() {
        return MCollection.toList(properties.keySet());
    }

    @Override
    public IConfig getNode(String key) {
        LinkedList<IConfig> list = children.get(key);
        if (list == null || list.size() == 0) return null;
        return list.getFirst();
    }

    @Override
    public List<IConfig> getNodes(String key) {
        LinkedList<IConfig> list = children.get(key);
        if (list == null || list.size() == 0) return MCollection.getEmptyList();
        return MCollection.toReadOnlyList(list);
    }

    @Override
    public List<IConfig> getNodes() {
        return MCollection.toReadOnlyList(childrenAll);
    }

    @Override
    public List<String> getNodeKeys() {
        LinkedList<String> out = new LinkedList<>();
        out.addAll(children.keySet());
        return out;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IConfig createConfig(String key) throws MException {
        HashConfig child = new HashConfig(key, this);
        LinkedList<IConfig> list = children.get(key);
        if (list == null) {
            list = new LinkedList<>();
            children.put(key, list);
        }
        list.add(child);
        childrenAll.add(child);
        return child;
    }

    public void unlink() throws MException {
        if (parent == null) return;
        parent.removeConfig(this);
        parent = null;
    }

    public IConfig setConfig(String key, HashConfig child) throws MException {
        if (child.parent != null) throw new MException("Config already linked");
        LinkedList<IConfig> list = children.get(key);
        if (list == null) {
            list = new LinkedList<>();
            children.put(key, list);
        }
        child.parent = this;
        child.name = key;
        list.add(child);
        childrenAll.add(child);
        return child;
    }

    // TODO move in childAll list
    @Override
    public int moveConfig(IConfig config, int newPos) throws MException {

        if (!(config instanceof HashConfig)) throw new MException("not a hashconfig");

        LinkedList<IConfig> list = children.get(config.getName());
        if (list == null || list.size() == 0 || !list.contains(config))
            throw new MException("config not in my list");

        if (list.size() == 1) {
            if (newPos == MOVE_FIRST || newPos == MOVE_LAST || newPos == 0) return 0;
            throw new MException("out of range");
        }

        if (newPos == MOVE_FIRST) {
            list.remove(config);
            list.add(0, (HashConfig) config);
            return 0;
        }

        if (newPos == MOVE_LAST) {
            list.remove(config);
            list.add((HashConfig) config);
            return list.size() - 1;
        }

        if (newPos == MOVE_DOWN) {
            int pos = list.indexOf(config);
            if (pos == list.size() - 1) throw new MException("out of range");
            list.remove(config);
            list.add(pos + 1, (HashConfig) config);
            return pos + 1;
        }

        if (newPos == MOVE_UP) {
            int pos = list.indexOf(config);
            if (pos == 0) throw new MException("out of range");
            list.remove(config);
            list.add(pos - 1, (HashConfig) config);
            return pos - 1;
        }

        if (newPos < 0 || newPos >= list.size()) throw new MException("out of range");

        list.remove(config);
        list.add(newPos, (HashConfig) config);

        return newPos;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public void removeConfig(IConfig config) throws MException {

        if (!(config instanceof HashConfig)) return;

        LinkedList<IConfig> list = children.get(config.getName());
        if (list == null) return;
        list.remove(config);
        if (list.size() == 0) children.remove(list);

        childrenAll.remove(config);
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public boolean isProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, MCast.objectToString(value));
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public ResourceNode<IConfig> getParent() {
        return parent;
    }

    @Override
    public Set<String> keys() {
        return properties.keySet();
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
    public void clear() {
        properties.clear();
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name, properties, children);
    }
}
