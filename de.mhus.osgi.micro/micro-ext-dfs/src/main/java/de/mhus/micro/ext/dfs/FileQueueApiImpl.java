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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.base.service.IdentUtil;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.io.QuotaFileOutputStream;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.MutableUri;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationUtil;
import de.mhus.micro.ext.api.dfs.DfsApi;
import de.mhus.micro.ext.api.dfs.FileInfo;
import de.mhus.micro.ext.api.dfs.FileQueueApi;
import de.mhus.micro.ext.api.dfs.FileQueueOperation;

@Component(immediate = true)
public class FileQueueApiImpl extends MLog implements FileQueueApi {

    protected static CfgLong CFG_MAX_QUEUE_SIZE =
            new CfgLong(
                    FileQueueApi.class,
                    "maxQueueSize",
                    1024l * 1024l * 1024l * 50l); // max 50 GB for all files
    protected static CfgLong CFG_MAX_FILE_SIZE =
            new CfgLong(
                    FileQueueApi.class,
                    "maxFileSize",
                    1024l * 1024l * 50l); // max size per file 50 MB
    protected static CfgInt CFG_MAX_FILES =
            new CfgInt(
                    FileQueueApi.class,
                    "maxFiles",
                    10000); // max number of files - 10000 are no working for windows os

    private static FileQueueApiImpl instance;
    private int queueFileCnt;
    private long queueFileSize;
    private long lastManualQueueCleanup;

    public static FileQueueApiImpl instance() {
        return instance;
    }
    
    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    public static File getUploadDir() {
        File dir =  MApi.getFile(MApi.SCOPE.TMP, "filequeue");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public File getFile(UUID id) {
        File dir = getUploadDir();
        //		File pFile = new File(dir, id + ".properties");
        synchronized (this) {
            File dFile = new File(dir, id + ".data");
            if (!dFile.exists()) return null;
            return dFile;
        }
    }

    @Override
    public UUID createQueueFile(String name, long ttl) throws IOException {
        checkFileQueueSize();
        if (ttl <= 0) ttl = DEFAULT_TTL;

        synchronized (this) {
            UUID id = UUID.randomUUID();

            File dir = getUploadDir();
            File pFile = new File(dir, id + ".properties");
            File dFile = new File(dir, id + ".data");
            dFile.createNewFile();

            MProperties prop = new MProperties();
            prop.setString("name", name);
            prop.setLong("created", System.currentTimeMillis());
            prop.setLong("accessed", System.currentTimeMillis());
            prop.setLong("expires", System.currentTimeMillis() + ttl);
            prop.setLong("ttl", ttl);
            prop.setBoolean("queued", true);

            prop.save(pFile);

            queueFileCnt++;

            return id;
        }
    }

    @Override
    public UUID takeFile(InputStream is, long ttl, long modified, String name) throws IOException {
        checkFileQueueSize();

        if (ttl <= 0) ttl = DEFAULT_TTL;

        MProperties prop = new MProperties();
        prop.setString("name", name);
        prop.setLong("modified", modified);

        UUID id = UUID.randomUUID();
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        FileOutputStream os = new FileOutputStream(dFile);
        MFile.copyFile(is, os);
        os.close();

        dFile.setWritable(false, false);

        synchronized (this) {
            prop.setLong("created", System.currentTimeMillis());
            prop.setLong("accessed", System.currentTimeMillis());
            prop.setLong("expires", System.currentTimeMillis() + ttl);
            prop.setLong("ttl", ttl);

            prop.save(pFile);

            queueFileCnt++;
        }
        return id;
    }

    @Override
    public UUID takeFile(File file, boolean copy, long ttl) throws IOException {
        checkFileQueueSize();

        if (ttl <= 0) ttl = DEFAULT_TTL;

        MProperties prop = new MProperties();
        prop.setString("name", file.getName());
        prop.setLong("modified", file.lastModified());

        UUID id = UUID.randomUUID();
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (copy) MFile.copyDir(file, dFile);
        else if (!file.renameTo(dFile))
            throw new IOException("Can't move file " + file + " to " + dFile);

        dFile.setWritable(false, false);

        synchronized (this) {
            prop.setLong("created", System.currentTimeMillis());
            prop.setLong("accessed", System.currentTimeMillis());
            prop.setLong("expires", System.currentTimeMillis() + ttl);
            prop.setLong("ttl", ttl);

            prop.save(pFile);

            queueFileCnt++;
        }
        return id;
    }

    @Override
    public long closeQueueFile(UUID id) throws IOException {

        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());
        synchronized (this) {
            MProperties prop = MProperties.load(pFile);
            if (!prop.getBoolean("queued", false)) throw new IOException("File not queued " + id);

            prop.setBoolean("queued", false);
            prop.setLong("expires", prop.getLong("ttl", DEFAULT_TTL) + System.currentTimeMillis());
            prop.setLong("accessed", System.currentTimeMillis());

            prop.save(pFile);
        }
        dFile.setWritable(false, false);
        return dFile.length();
    }

    @Override
    public long appendQueueFileContent(UUID id, byte[] content) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());
        if (content == null || content.length == 0) return dFile.length();

        synchronized (this) {
            if (
            /* queueFileSize > CFG_MAX_QUEUE_SIZE.value() || the max size of a file is guaranteed !! */
            dFile.length() + content.length > CFG_MAX_FILE_SIZE.value())
                throw new IOException("maximum file size reached " + CFG_MAX_FILE_SIZE.value());
            MProperties prop = MProperties.load(pFile);
            if (!prop.getBoolean("queued", false)) throw new IOException("File not queued " + id);

            FileOutputStream os = new FileOutputStream(dFile, true);
            os.write(content);
            os.close();

            queueFileSize += content.length;
        }
        return dFile.length();
    }

    @Override
    public OutputStream createQueueFileOutputStream(UUID id) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());
        synchronized (this) {
            MProperties prop = MProperties.load(pFile);
            if (!prop.getBoolean("queued", false)) throw new IOException("File not queued " + id);

            FileOutputStream os = new QuotaFileOutputStream(dFile, CFG_MAX_FILE_SIZE.value());

            return os;
        }
    }

    @Override
    public File loadFile(MUri uri) throws IOException, MException {

        if (!DfsApi.SCHEME_DFQ.equals(uri.getScheme()))
            throw new IOException("Wrong scheme " + uri.getScheme() + " for queue");
        if (!MValidator.isUUID(uri.getPath()))
            throw new IOException("Malformed queue file id " + uri.getPath());

        File dir = getUploadDir();
        File pFile = new File(dir, uri.getPath() + ".properties");
        File dFile = new File(dir, uri.getPath() + ".data");

        synchronized (this) {
            if (dFile.exists() && pFile.exists()) {
                // load cached
                MProperties prop = MProperties.load(pFile);
                if (prop.getBoolean("queued", false)) throw new IOException("File queued " + uri);

                prop.setLong("accessed", System.currentTimeMillis());
                try {
                    prop.save(pFile);
                } catch (IOException e) {
                    log().w(e);
                }
                return dFile;
            }
            if (pFile.exists()) pFile.delete();
            if (dFile.exists()) {
                dFile.setWritable(true, false);
                dFile.delete();
            }
        }

        OperationApi api = M.l(OperationApi.class);
        LinkedList<String> tags = new LinkedList<>();
        tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
        OperationDescriptor desc =
                api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
        FileQueueOperation operation =
                OperationUtil.createOperationProxy(FileQueueOperation.class, desc);
        FileInfo info = operation.getFileInfo(UUID.fromString(uri.getPath()));
        File file = operation.getFile(UUID.fromString(uri.getPath()));

        synchronized (this) {
            file.renameTo(dFile);
            dFile.setWritable(false, false);

            MProperties prop = new MProperties();
            prop.setString("name", info.getName());
            prop.setLong("size", info.getSize());
            prop.setLong("modified", info.getModified());
            prop.setLong("accessed", System.currentTimeMillis());
            prop.setLong("ttl", DEFAULT_TTL);
            prop.setLong("expires", System.currentTimeMillis() + DEFAULT_TTL);
            prop.setString("source", uri.toString());
            try {
                prop.save(pFile);
            } catch (IOException e) {
                log().w(e);
            }
        }

        return dFile;
    }

    @Override
    public File loadFile(UUID id) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        synchronized (this) {
            MProperties prop = MProperties.load(pFile);
            if (prop.getBoolean("queued", false)) throw new IOException("File queued " + id);
        }

        return dFile;
    }

    @Override
    public FileInfo getFileInfo(UUID id) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        MProperties prop = MProperties.load(pFile);

        return new FileInfoImpl(
                getUri(id),
                prop.getString("name", ""),
                dFile.length(),
                prop.getLong("modified", 0));
    }

    @Override
    public MUri getUri(UUID id) throws FileNotFoundException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        MProperties prop = MProperties.load(pFile);

        MutableUri uri = new MutableUri(null);
        uri.setScheme(DfsApi.SCHEME_DFQ);
        uri.setLocation(IdentUtil.getServerIdent());
        uri.setPath(id.toString());
        uri.setParams(
                new String[] {String.valueOf(dFile.length()), prop.getString("modified", "")});

        return uri;
    }

    @Override
    public FileInfo getFileInfo(MUri uri) throws IOException, MException {
        if (!DfsApi.SCHEME_DFQ.equals(uri.getScheme()))
            throw new IOException("Wrong scheme " + uri.getScheme() + " for queue");
        if (!MValidator.isUUID(uri.getPath()))
            throw new IOException("Malformed queue file id " + uri.getPath());

        File dir = getUploadDir();
        File pFile = new File(dir, uri.getPath() + ".properties");
        File dFile = new File(dir, uri.getPath() + ".data");

        synchronized (this) {
            if (dFile.exists() && pFile.exists()) {
                return new FileInfoImpl(getUri(UUID.fromString(uri.getPath())));
            }
        }

        OperationApi api = M.l(OperationApi.class);
        LinkedList<String> tags = new LinkedList<>();
        tags.add(OperationDescriptor.TAG_IDENT + "=" + uri.getLocation());
        OperationDescriptor desc =
                api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
        FileQueueOperation operation =
                OperationUtil.createOperationProxy(FileQueueOperation.class, desc);
        FileInfo info = operation.getFileInfo(UUID.fromString(uri.getPath()));
        return info;
    }

    public void cleanupQueue() {
        File dir = getUploadDir();
        synchronized (this) {
            queueFileCnt = 0;
            queueFileSize = 0;
            for (File file : dir.listFiles()) {

                if (!file.getName().startsWith(".") && file.isFile()) {
                    queueFileSize += file.length();
                }
                if (file.getName().endsWith(".properties")) {
                    try {
                        MProperties prop = MProperties.load(file);
                        long expires = prop.getLong("expires", 0);
                        long access = prop.getLong("access", 0);
                        if (expires > 0 && System.currentTimeMillis() > expires
                                || expires == 0
                                        && MPeriod.isTimeOut(access, FileQueueApi.DEFAULT_TTL)
                                || expires == 0 && access == 0) {
                            String id = MString.beforeIndex(file.getName(), '.');
                            log().d("cleanup", id, prop);
                            File dFile = new File(dir, id + ".data");
                            if (dFile.exists()) {
                                dFile.setWritable(true, false);
                                dFile.delete();
                                queueFileSize -=
                                        dFile
                                                .length(); // could be distort size amount, but shit
                                                           // appends
                            }
                            file.delete();
                        } else {
                            queueFileCnt++;
                        }
                    } catch (Throwable t) {
                        log().e(file, t);
                    }
                }
            }
        }
    }

    private void checkFileQueueSize() throws IOException {
        synchronized (this) {
            if (queueFileCnt > CFG_MAX_FILES.value()
                    || queueFileSize > CFG_MAX_QUEUE_SIZE.value()) {
                // if the queue is full a manual cleanup check will executed to - maybe - cleanup
                // resources
                // to avoid overload this will only be executed every 10 sec.
                if (MPeriod.isTimeOut(lastManualQueueCleanup, 10000)) {
                    log().w("Manual File Queue Cleanup", queueFileCnt, queueFileSize);
                    cleanupQueue();
                    lastManualQueueCleanup = System.currentTimeMillis();
                }
            }
            if (queueFileCnt > CFG_MAX_FILES.value())
                throw new IOException("too much files in file queue " + queueFileCnt);
            if (queueFileSize > CFG_MAX_QUEUE_SIZE.value())
                throw new IOException("file queue quota reached " + queueFileSize);
        }
    }

    @Override
    public void touchFile(UUID id, long ttl) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        MProperties prop = MProperties.load(pFile);
        synchronized (this) {
            if (ttl > 0) prop.setLong("ttl", ttl);
            prop.setLong("expires", prop.getLong("ttl", DEFAULT_TTL) + System.currentTimeMillis());
            prop.setLong("accessed", System.currentTimeMillis());

            prop.save(pFile);
        }
    }

    public Set<UUID> getQueuedIdList(boolean queued) {

        HashSet<UUID> out = new HashSet<>();
        // only admin is allowed to watch id list, id is like a token and need to be protected
        if (!AccessUtil.isAdmin())
            throw new AccessDeniedException("only root is allowed to access ids");

        File dir = getUploadDir();
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".properties")) {

                if (!queued) {
                    MProperties prop = MProperties.load(file);
                    if (prop.getBoolean("queued", false)) continue;
                }

                String id = MString.beforeIndex(file.getName(), '.');
                out.add(UUID.fromString(id));
            }
        }
        return out;
    }

    public MProperties getProperties(UUID id) throws FileNotFoundException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        return MProperties.load(pFile);
    }

    public void delete(UUID id) {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");
        File dFile = new File(dir, id + ".data");
        if (pFile.exists()) pFile.delete();
        if (dFile.exists()) {
            queueFileSize -= dFile.length();
            dFile.setWritable(true, false);
            dFile.delete();
        }
        queueFileCnt--;
    }

    public void setParameter(UUID id, String key, String val) throws IOException {
        File dir = getUploadDir();
        File pFile = new File(dir, id + ".properties");

        if (!pFile.exists()) throw new FileNotFoundException(id.toString());

        MProperties prop = MProperties.load(pFile);
        prop.put(key, val);
        prop.save(pFile);
    }

    public Set<String> listProviders() {
        HashSet<String> out = new HashSet<>();
        OperationApi api = M.l(OperationApi.class);
        for (OperationDescriptor desc : api.findOperations(FileQueueOperation.class, null, null)) {
            String ident =
                    OperationUtil.getOption(desc.getTags(), OperationDescriptor.TAG_IDENT, "");
            out.add(ident);
        }
        return out;
    }

    public FileQueueOperation getOperation(String ident) throws MException {
        OperationApi api = M.l(OperationApi.class);
        LinkedList<String> tags = new LinkedList<>();
        tags.add(OperationDescriptor.TAG_IDENT + "=" + ident);
        OperationDescriptor desc =
                api.findOperation(FileQueueOperation.class.getCanonicalName(), null, tags);
        FileQueueOperation operation =
                OperationUtil.createOperationProxy(FileQueueOperation.class, desc);
        return operation;
    }
}
