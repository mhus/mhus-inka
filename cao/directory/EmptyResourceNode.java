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
package de.mhus.lib.core.directory;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;

public class EmptyResourceNode<E extends ResourceNode<?>> extends ResourceNode<E> {

    private static final long serialVersionUID = 1L;
    private String name;

    public EmptyResourceNode() {}

    public EmptyResourceNode(String name) {
        this.name = name;
    }

    @Override
    public List<String> getPropertyKeys() {
        return MCollection.getEmptyList();
    }

    @Override
    public E getNode(String key) {
        return null;
    }

    @Override
    public List<E> getNodes() {
        return MCollection.getEmptyList();
    }

    @Override
    public List<E> getNodes(String key) {
        return MCollection.getEmptyList();
    }

    @Override
    public List<String> getNodeKeys() {
        return MCollection.getEmptyList();
    }

    @Override
    public String getName() throws MException {
        return name;
    }

    @Override
    public InputStream getInputStream(String key) {
        return null;
    }

    @Override
    public ResourceNode<?> getParent() {
        return null;
    }

    @Override
    public URL getUrl() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public boolean isProperty(String name) {
        return false;
    }

    @Override
    public void removeProperty(String key) {}

    @Override
    public void setProperty(String key, Object value) {
        throw new NotSupportedException("empty resource");
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public Collection<String> getRenditions() {
        return null;
    }

    @Override
    public void clear() {}

    @Override
    public IProperties getRenditionProperties(String rendition) {
        return null;
    }
}
