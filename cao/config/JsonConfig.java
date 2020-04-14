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
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.directory.WritableResourceNode;
import de.mhus.lib.errors.MException;

public class JsonConfig extends IConfig {

    private static final long serialVersionUID = 1L;
    private ObjectNode node;
    protected String name;
    private WritableResourceNode<IConfig> parent;
    // private int index = -1;

    public JsonConfig(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nodex = mapper.readValue(json, JsonNode.class);
        if (nodex instanceof ObjectNode) node = (ObjectNode) nodex;
        else {
            node = ((ArrayNode) nodex).objectNode();
            node.put("default", nodex);
        }
    }

    public JsonConfig(ObjectNode node) {
        this(null, null, node);
    }

    public JsonConfig(String name, IConfig parent, JsonNode node) {
        if (node instanceof ObjectNode) this.node = (ObjectNode) node;
        else if (node instanceof ArrayNode) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.node = (ObjectNode) mapper.readValue("{}", JsonNode.class);
            } catch (IOException e) {
            }
            this.node.put("array", node);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.node = (ObjectNode) mapper.readValue("{}", JsonNode.class);
            } catch (IOException e) {
            }
            this.node.put("value", node);
        }
        this.name = name;
        this.parent = parent;
    }

    public JsonConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        node = (ObjectNode) mapper.readValue("{}", JsonNode.class);
    }

    public JsonConfig(String name, JsonConfig jsonConfig, TextNode textNode) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        node = (ObjectNode) mapper.readValue("{}", JsonNode.class);
        node.put("value", textNode);
        this.name = name;
    }

    @Override
    public IConfig getNode(String name) {
        JsonNode child = node.get(name);
        if (child == null) return null;
        if (child.isObject() || child.isArray()) return new JsonConfig(name, this, child);
        return null;
    }

    @Override
    public List<IConfig> getNodes(String name) {
        JsonNode child = node.get(name);
        if (child == null || !child.isArray()) return MCollection.getEmptyList();
        LinkedList<IConfig> out = new LinkedList<>();
        for (int i = 0; i < child.size(); i++) {
            out.add(new JsonConfig(name, this, child.get(i)));
        }
        return out;
    }

    @Override
    public List<IConfig> getNodes() {
        LinkedList<IConfig> out = new LinkedList<>();
        for (Map.Entry<String, JsonNode> entry : MCollection.iterate(node.getFields())) {
            JsonNode child = entry.getValue();
            String childName = entry.getKey();
            if (child != null && child.isArray()) {
                for (int i = 0; i < child.size(); i++)
                    out.add(new JsonConfig(childName, this, child.get(i)));
            } else if (child != null && child.isObject())
                out.add(new JsonConfig(childName, this, child));
        }
        return out;
    }

    @Override
    public List<String> getNodeKeys() {
        LinkedList<String> out = new LinkedList<String>();
        for (Iterator<String> i = node.getFieldNames(); i.hasNext(); ) {
            String name = i.next();
            JsonNode child = node.get(name);
            if (child.isArray() || child.isObject()) out.add(name);
        }
        return out;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object getProperty(String name) {
        JsonNode child = node.get(name);
        if (child == null) return null;
        return child.getValueAsText();
    }

    @Override
    public List<String> getPropertyKeys() {
        LinkedList<String> out = new LinkedList<String>();
        for (Iterator<String> i = node.getFieldNames(); i.hasNext(); ) {
            String name = i.next();
            JsonNode child = node.get(name);
            if (!child.isArray() && !child.isObject()) out.add(name);
        }
        return out;
    }

    @Override
    public boolean isProperty(String name) {
        JsonNode child = node.get(name);
        return (child != null && !child.isArray() && !child.isObject());
    }

    @Override
    public void removeProperty(String name) {
        JsonNode child = node.get(name);
        if (child != null && !child.isArray() && !child.isObject()) getNode().remove(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        JsonNode child = node.get(name);
        if (child == null || !child.isArray() && !child.isObject())
            getNode().put(name, MCast.objectToString(value));
    }

    public ObjectNode getNode() {
        return node;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public WritableResourceNode<IConfig> createConfig(String key) throws MException {

        // find array node, to append new config
        if (node.get(key) != null && !node.get(key).isArray()) {
            node.remove(key);
        }
        ArrayNode array = (ArrayNode) node.get(key);
        if (array == null) { // if not, create one
            array = node.arrayNode();
            node.put(key, array);
        }

        if (!(array instanceof ArrayNode)) {
            throw new MException(key + " is not an array");
        }

        // create new object node in array
        ObjectNode out = array.objectNode();
        array.add(out);

        return new JsonConfig(key, this, out);
    }

    @Override
    public int moveConfig(IConfig config, int newPos) throws MException {
        if (!(config instanceof JsonConfig)) throw new MException("not JsonConfig");

        // find array node, to append new config
        JsonNode array = node.get(config.getName());
        if (array == null) throw new MException("config set not found");

        int pos = findPosOf((ArrayNode) array, (JsonConfig) config);
        if (pos < 0) throw new MException("could not find child");

        // make it simple if only one element is in list
        if (((ArrayNode) array).size() == 1) {
            if (newPos == MOVE_FIRST || newPos == MOVE_LAST || newPos == 0) return 0;
            throw new MException("out of range");
        }

        if (newPos == MOVE_FIRST) {
            ((ArrayNode) array).remove(pos);
            ((ArrayNode) array).insert(0, ((JsonConfig) config).node);
            return 0;
        }
        if (newPos == MOVE_LAST) {
            ((ArrayNode) array).remove(pos);
            ((ArrayNode) array).add(((JsonConfig) config).node);
            return ((ArrayNode) array).size() - 1;
        }

        if (newPos == MOVE_DOWN) {
            if (pos == ((ArrayNode) array).size() - 1) throw new MException("out of range");

            ((ArrayNode) array).remove(pos);
            ((ArrayNode) array).insert(pos + 1, ((JsonConfig) config).node);
            return pos + 1;
        }

        if (newPos == MOVE_UP) {
            if (pos == 0) throw new MException("out of range");

            ((ArrayNode) array).remove(pos);
            ((ArrayNode) array).insert(pos - 1, ((JsonConfig) config).node);
            return pos - 1;
        }

        if (pos == newPos) return pos;

        ((ArrayNode) array).remove(pos);
        ((ArrayNode) array).insert(newPos, ((JsonConfig) config).node);

        return newPos;
    }

    @Override
    public void removeConfig(IConfig config) throws MException {

        if (!(config instanceof JsonConfig)) return;

        // find array node, to append new config
        JsonNode array = node.get(config.getName());
        if (array == null) return;

        int pos = findPosOf((ArrayNode) array, (JsonConfig) config);
        if (pos < 0) throw new MException("could not find child");
        ((ArrayNode) array).remove(pos);
    }

    protected int findPosOf(ArrayNode array, JsonConfig config) {

        // if (config.index >= 0) return config.index;

        int cnt = 0;
        for (JsonNode item : array) {
            if (item == config.node) return cnt;
            cnt++;
        }

        return -1;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public WritableResourceNode<IConfig> getParent() {
        return parent;
    }

    public void write(OutputStream os) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.writeValue(os, node);
    }

    public void write(Writer os) throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.writeValue(os, node);
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
        node.removeAll();
    }
}
