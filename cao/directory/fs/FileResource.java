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
package de.mhus.lib.core.directory.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.errors.MException;

public class FileResource extends ResourceNode<FileResource> {

    private static final long serialVersionUID = 1L;

    public enum KEYS {
        NAME,
        LENGTH,
        MODIFIED,
        TYPE,
        HIDDEN
    };

    public enum TYPE {
        FILE,
        DIRECTORY,
        UNKNOWN
    }

    public static final long UNKNOWN_LENGTH = -1;

    private FileResourceRoot root;
    private File file;
    private FileResource parent;
    private HashMap<String, FileResource> cache = new HashMap<>();

    public FileResource(FileResourceRoot root, FileResource parent, File file) {
        if (root == null) root = (FileResourceRoot) this;
        this.root = root;
        this.file = file;
        this.parent = parent;
    }

    @Override
    public Collection<String> getPropertyKeys() {

        KEYS[] v = KEYS.values();
        LinkedList<String> out = new LinkedList<>();
        for (int i = 0; i < v.length; i++) out.add(v[i].name().toLowerCase());

        return out;
    }

    @Override
    public FileResource getNode(String key) {
        if (key == null) return null;
        if (key.equals("..") || key.equals(".")) return null;
        if (key.indexOf('/') > -1 || key.indexOf('\\') > -1) return null; // only direct children
        // TODO special chars ?!!

        FileResource cached = cache.get(key);
        if (cached != null) {
            if (cached.isValid()) return cached;
            cache.remove(key);
        }
        File f = new File(file, key);
        if (!f.exists()) return null;
        cached = new FileResource(root, this, f);
        cache.put(key, cached);

        return cached;
    }

    @Override
    public Collection<FileResource> getNodes() {
        LinkedList<FileResource> out = new LinkedList<>();
        for (String sub : file.list()) {
            FileResource n = getNode(sub);
            if (n != null) out.add(n);
        }
        return out;
    }

    @Override
    public Collection<FileResource> getNodes(String key) {
        FileResource n = getNode(key);
        if (n == null) return new EmptyList<>();
        LinkedList<FileResource> out = new LinkedList<>();
        out.add(n);
        return out;
    }

    @Override
    public Collection<String> getNodeKeys() {
        LinkedList<String> out = new LinkedList<>();
        for (String sub : file.list()) {
            FileResource n = getNode(sub);
            if (n != null) out.add(sub);
        }
        return out;
    }

    @Override
    public String getName() throws MException {
        return file.getName();
    }

    @Override
    public InputStream getInputStream(String key) {
        if (file.isDirectory()) return null;
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FileResource getParent() {
        return parent;
    }

    @SuppressWarnings("deprecation")
    @Override
    public URL getUrl() {
        try {
            return file.toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getProperty(String name) {

        if (name.equals("file")) return file;
        if (name.equals("filepath")) return file.getAbsolutePath();

        try {
            KEYS key = KEYS.valueOf(name.toUpperCase());
            switch (key) {
                case NAME:
                    return file.getName();
                case LENGTH:
                    return file.length();
                case MODIFIED:
                    if (file.isFile()) return file.lastModified();
                    else return UNKNOWN_LENGTH;
                case TYPE:
                    if (file.isFile()) return TYPE.FILE;
                    if (file.isDirectory()) return TYPE.DIRECTORY;
                    return TYPE.UNKNOWN;
                case HIDDEN:
                    return file.isHidden();
            }
        } catch (IllegalArgumentException e) {
        }
        return null;
    }

    @Override
    public boolean isProperty(String name) {
        try {
            KEYS key = KEYS.valueOf(name.toUpperCase());
            return key != null;
        } catch (Throwable t) {
        }
        return false;
    }

    @Override
    public void removeProperty(String key) {}

    @Override
    public void setProperty(String key, Object value) {}

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isValid() {
        return file != null && file.exists();
    }

    @Override
    public boolean hasContent() {
        return file.isFile();
    }

    @Override
    public Collection<String> getRenditions() {
        if (hasContent()) {
            return new EmptyList<>();
        }
        return null;
    }

    @Override
    public void clear() {}

    @Override
    public IProperties getRenditionProperties(String rendition) {
        return null;
    }
}
