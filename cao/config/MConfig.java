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
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.directory.WritableResourceNode;
import de.mhus.lib.core.lang.IBase;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;

public class MConfig implements IBase {

    /**
     * This will search a file with different file extensions
     *
     * @param path Path to file without file extension
     * @param include option to include other config files with __include=
     * @return The config object or null
     * @throws MException
     */
    public static IConfig find(String path, boolean include) throws MException {
        File f = new File(path);
        return find(f.getParentFile(), f.getName(), include);
    }

    /**
     * This will search a file with different file extensions
     *
     * @param parent
     * @param name Name of file without file extension
     * @param include option to include other config files with __include=
     * @return The config object or null
     * @throws MException
     */
    public static IConfig find(File parent, String name, boolean include) throws MException {
        {
            File f = new File(parent, name + ".xml");
            if (f.exists() && f.isFile()) return createConfigFor(f, include);
        }
        {
            File f = new File(parent, name + ".json");
            if (f.exists() && f.isFile()) return createConfigFor(f, include);
        }
        {
            File f = new File(parent, name + ".properties");
            if (f.exists() && f.isFile()) return createConfigFor(f, include);
        }
        {
            File f = new File(parent, name);
            if (f.exists() && f.isDirectory()) return createConfigFor(f, include);
        }
        return null;
    }

    public static IConfig createConfigFor(File file) throws MException {
        return createConfigFor(file, false);
    }

    public static IConfig createConfigFor(File file, boolean include) throws MException {
        if (include) {
            HashSet<String> included = new HashSet<>();
            return create(file, included);
        } else {
            return create(file);
        }
    }

    protected static IConfig create(File file, HashSet<String> included) throws MException {
        IConfig out = create(file);
        included.add(file.getAbsolutePath());
        for (String path : out.getString("__include", "").split(",")) {
            if (MString.isSetTrim(path)) {
                File absolut = MFile.getFile(file, path);
                if (absolut == null) continue;
                String abs = absolut.getAbsolutePath();
                if (!included.contains(abs)) {

                    IConfig nextOne = create(absolut, included);
                    for (String key : nextOne.getPropertyKeys())
                        if (!out.containsKey(key)) out.setProperty(key, nextOne.get(key));

                    included.add(abs);
                }
            }
        }
        return out;
    }

    public static IConfig create(File file) throws MException {

        if (file.isDirectory()) return new DirConfig(file);

        String key = file.getName();

        if (key.endsWith(".xml")) {
            try {
                XmlConfigFile out = new XmlConfigFile(file);
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", file.getAbsolutePath());
                return out;
            } catch (Exception e) {
                throw new MException(file, e);
            }
        }
        if (key.endsWith(".properties")) {
            try {
                PropertiesConfigFile out = new PropertiesConfigFile(file);
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", file.getAbsolutePath());
                return out;
            } catch (Exception e) {
                throw new MException(file, e);
            }
        }
        if (key.endsWith(".json")) {
            try {
                JsonConfigFile out = new JsonConfigFile(file);
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", file.getAbsolutePath());
                return out;
            } catch (Exception e) {
                throw new MException(file, e);
            }
        }
        return null;
    }

    public static IConfig createConfigFor(URI uri) throws MException {
        if (uri == null) return null;

        String key = uri.getPath();
        if (key == null) key = uri.toString();

        if (key.endsWith(".xml")) {
            try {
                XmlConfigFile out = new XmlConfigFile(uri.toURL().openStream());
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", uri.getPath());
                out.setString("__host", uri.getHost());
                out.setInt("__port", uri.getPort());
                out.setString("__user", uri.getUserInfo());
                return out;
            } catch (Exception e) {
                throw new MException(uri, e);
            }
        }
        if (key.endsWith(".properties")) {
            try {
                PropertiesConfigFile out = new PropertiesConfigFile(uri.toURL().openStream());
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", uri.getPath());
                out.setString("__host", uri.getHost());
                out.setInt("__port", uri.getPort());
                out.setString("__user", uri.getUserInfo());
                return out;
            } catch (Exception e) {
                throw new MException(uri, e);
            }
        }
        if (key.endsWith(".json")) {
            try {
                JsonConfigFile out = new JsonConfigFile(uri.toURL().openStream());
                out.setName(MFile.getFileNameOnly(key)); // set the filename as name
                out.setString("__path", uri.getPath());
                out.setString("__host", uri.getHost());
                out.setInt("__port", uri.getPort());
                out.setString("__user", uri.getUserInfo());
                return out;
            } catch (Exception e) {
                throw new MException(uri, e);
            }
        }
        return null;
    }

    public static IConfig createConfigForFile(String key) throws MException {
        if (key == null) return null;

        if (key.endsWith(".xml")) return new XmlConfig();
        if (key.endsWith(".properties")) return new PropertiesConfig();
        if (key.endsWith(".json"))
            try {
                return new JsonConfig();
            } catch (Exception e) {
                throw new MException(key, e);
            }

        return null;
    }

    /**
     * Return a config or null if the string is not understud.
     *
     * @param configString
     * @return A config object if the config is found or null. If no config is recognized it returns
     *     null
     * @throws MException
     */
    public static IConfig toConfig(String configString) throws MException {
        if (MString.isEmptyTrim(configString)) return new HashConfig();
        if (configString.startsWith("[") || configString.startsWith("{")) {
            try {
                return new JsonConfig(configString);
            } catch (Exception e) {
                throw new MException(configString, e);
            }
        }
        if (configString.startsWith("<?")) {
            try {
                return new XmlConfig(MXml.loadXml(configString).getDocumentElement());
            } catch (Exception e) {
                throw new MException(configString, e);
            }
        }

        if (configString.contains("=")) {
            if (configString.contains("&")) return new HashConfig(MUri.explode(configString));
            else return new PropertiesConfig(configString);
        }

        return null;
    }

    public static String[] toStringArray(Collection<IConfig> nodes, String key) {
        LinkedList<String> out = new LinkedList<>();
        for (IConfig item : nodes) {
            String value = item.getString(key, null);
            if (value != null) out.add(value);
        }
        return out.toArray(new String[out.size()]);
    }

    public static IConfig createFromResource(Class<?> owner, String fileName) throws MException {
        try {
            URL url = MSystem.locateResource(owner, fileName);

            if (fileName.endsWith(".xml")) return new XmlConfigFile(url.openStream());
            if (fileName.endsWith(".properties")) return new PropertiesConfigFile(url.openStream());
            if (fileName.endsWith(".json")) return new JsonConfigFile(url.openStream());
        } catch (Throwable t) {
            throw new MException(fileName, t);
        }
        throw new NotFoundException(fileName);
    }

    public static void merge(IConfig from, IConfig to) throws MException {
        for (IConfig node : from.getNodes()) {
            WritableResourceNode<IConfig> n = to.createConfig(node.getName());
            for (String name : node.getPropertyKeys()) {
                n.put(name, node.get(name));
            }
            merge(node, (IConfig) n);
        }
    }
}
