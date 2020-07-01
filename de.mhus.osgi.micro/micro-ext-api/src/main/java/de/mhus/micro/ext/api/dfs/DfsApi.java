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
import java.util.Collection;
import java.util.Map;

import de.mhus.lib.core.util.MUri;

public interface DfsApi {

    String SCHEME_DFQ = "dfq";

    /**
     * Search and return a file in the file space.
     *
     * @param uri
     * @return Info or null
     */
    default FileInfo getFileInfo(String uri) {
        return getFileInfo(MUri.toUri(uri));
    }

    /**
     * Search and return a file in the file space.
     *
     * @param uri
     * @return Info or null
     */
    FileInfo getFileInfo(MUri uri);

    /**
     * Request and return a file queue handle to the prepared file.
     *
     * @param uri Uri to the requested file
     * @return File queue handle
     */
    MUri exportFile(MUri uri);

    /**
     * Request a list of directory entries (if supported by the remote dfs). If the entry is a sub
     * directory it will end with an slash.
     *
     * @param uri
     * @return a list of entries and the corresponding URI
     */
    Map<String, MUri> getDirectoryList(MUri uri);

    Collection<String> listProviders();

    void importFile(MUri queueUri, MUri target) throws IOException;

    void deleteFile(MUri uri) throws IOException;

    void createDirectories(MUri uri) throws IOException;
}
