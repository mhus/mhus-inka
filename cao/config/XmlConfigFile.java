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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;
import de.mhus.lib.errors.MException;

public class XmlConfigFile extends XmlConfig {

    private static final long serialVersionUID = 1L;
    private File file;

    public XmlConfigFile(File file) throws FileNotFoundException, Exception {
        super();
        if (file.exists())
            readConfig(new InputStreamReader(new FileInputStream(file), MString.CHARSET_UTF_8));
        this.file = file;
    }

    public XmlConfigFile(InputStream is) throws FileNotFoundException, Exception {
        super();
        readConfig(new InputStreamReader(is, MString.CHARSET_UTF_8));
        this.file = null;
    }

    public boolean canSave() {
        if (file == null) return false;
        if (!file.exists()) {
            return file.getParentFile().canWrite();
        }
        return file.canWrite();
    }

    public void save() throws MException {
        try {
            if (!canSave()) return;
            log().t("save config", this);
            FileOutputStream os = new FileOutputStream(file);
            MXml.trim(element);
            MXml.saveXml(element, os);
            os.close();
        } catch (Exception e) {
            throw new MException(e);
        }
    }

    @Override
    public String toString() {
        return getClass() + ": " + (file == null ? "?" : file.getAbsolutePath());
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }
}
