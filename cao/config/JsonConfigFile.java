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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.errors.MException;

public class JsonConfigFile extends JsonConfig {

    private static final long serialVersionUID = 1L;
    private File file;

    public JsonConfigFile(File file) throws Exception {
        super(MFile.readFile(file));
        this.file = file;
    }

    public JsonConfigFile(InputStream is) throws Exception {
        super(MFile.readFile(new InputStreamReader(is, MString.CHARSET_UTF_8))); // read utf
        this.file = null;
    }

    public boolean canSave() {
        return file != null && file.canWrite();
    }

    public void save() throws MException {
        try {
            if (!canSave()) return;
            log().t("save config", this);
            FileOutputStream os = new FileOutputStream(file);
            write(os);
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
