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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;

/**
 * A file is first queued and after creation it's stored. You can access remote stored files. They
 * will be downloaded and provided as local stored files.
 *
 * @author mikehummel
 */
public interface FileQueueApi {

    long DEFAULT_TTL = 15 * 60 * 1000; // 15 Min.

    /**
     * Create a new entry in the file queue.
     *
     * @param name
     * @param ttl Time to live of the file in the queue
     * @return The file info
     * @throws IOException
     */
    UUID createQueueFile(String name, long ttl) throws IOException;

    /**
     * Take the file and move it into the file queue. The file is closed after it's taken.
     *
     * @param file
     * @param copy
     * @param ttl
     * @return created id
     * @throws IOException
     */
    UUID takeFile(File file, boolean copy, long ttl) throws IOException;

    /**
     * Take file from input stream. Reads the full stream in the new file content. Will not close
     * the stream.
     *
     * @param is Input Stream with data
     * @param ttl
     * @param modified
     * @param name
     * @return created id
     * @throws IOException
     */
    UUID takeFile(InputStream is, long ttl, long modified, String name) throws IOException;

    /**
     * Close a queued file and change state to stored.
     *
     * @param id
     * @return The final file size
     * @throws IOException
     */
    long closeQueueFile(UUID id) throws IOException;

    /**
     * Append content to a queued file.
     *
     * @param id
     * @param content
     * @return The size after appending
     * @throws IOException
     */
    long appendQueueFileContent(UUID id, byte[] content) throws IOException;

    /**
     * Return a output stream for a queued file. File will be removed if existed before.
     *
     * @param id
     * @return a new output stream
     * @throws IOException
     */
    OutputStream createQueueFileOutputStream(UUID id) throws IOException;

    /**
     * Download a file from network (if not local) and return the local file handle.
     *
     * @param uri
     * @return the file
     * @throws NotFoundException
     * @throws MException
     * @throws IOException
     */
    File loadFile(MUri uri) throws MException, IOException;

    /**
     * Return the file handle for a stored file.
     *
     * @param id
     * @return The file
     * @throws IOException
     */
    File loadFile(UUID id) throws IOException;

    /**
     * Return the file information for a local stored file.
     *
     * @param id
     * @return The file info
     * @throws IOException
     */
    FileInfo getFileInfo(UUID id) throws IOException;

    /**
     * Return the file information for a local stored file.
     *
     * @param uri
     * @return The file info
     * @throws MException
     * @throws IOException
     */
    FileInfo getFileInfo(MUri uri) throws IOException, MException;

    /**
     * Return the local uri of the file.
     *
     * @param id
     * @return The uri
     * @throws FileNotFoundException
     */
    MUri getUri(UUID id) throws FileNotFoundException;

    /**
     * Renew the lifetime of a file. Using the ttl to calculate the new expire time.
     *
     * @param id Id of the local file
     * @param ttl The ttl in ms or 0 for the last used ttl.
     * @throws IOException
     */
    void touchFile(UUID id, long ttl) throws IOException;
}
