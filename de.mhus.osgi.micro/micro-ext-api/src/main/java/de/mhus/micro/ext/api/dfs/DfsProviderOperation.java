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
package de.mhus.micro.ext.api.dfs;

import java.io.IOException;
import java.util.Map;

import de.mhus.lib.core.util.MUri;

public interface DfsProviderOperation {

    String PARAM_SCHEME = "scheme";

    FileInfo getFileInfo(MUri uri);

    MUri exportFile(MUri uri) throws IOException;

    Map<String, MUri> getDirectoryList(MUri uri);

    void importFile(MUri queueUri, MUri target) throws IOException;

    /**
     * Delete file or directory.
     *
     * @param uri
     * @throws IOException
     */
    void deleteFile(MUri uri) throws IOException;

    /**
     * Create all the missing directories.
     *
     * @param uri
     * @throws IOException
     */
    void createDirectories(MUri uri) throws IOException;
}
