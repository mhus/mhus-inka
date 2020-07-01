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
package de.mhus.micro.ext.dfs;

import java.util.Date;

import de.mhus.lib.core.M;
import de.mhus.lib.core.util.MUri;
import de.mhus.micro.ext.api.dfs.FileInfo;

public class FileInfoImpl implements FileInfo {

    private static final long serialVersionUID = 1L;
    protected String name;
    protected long size;
    protected long modified;
    private String uri;

    public FileInfoImpl(MUri uri, String name, long size, long modified) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.modified = modified;
    }

    public FileInfoImpl(MUri uri) {
        this.uri = uri.toString();
        name = uri.getPath();
        String[] params = uri.getParams();
        if (params != null) {
            size = M.to(params[0], 0);
            modified = M.to(params[1], new Date()).getTime();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public String getUri() {
        return uri;
    }
}
