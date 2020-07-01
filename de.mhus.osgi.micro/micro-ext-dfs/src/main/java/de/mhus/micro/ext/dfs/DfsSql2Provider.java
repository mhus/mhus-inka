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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.sql.DataSource;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.IConfigFactory;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationToIfcProxy;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.MutableUri;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.sql.DataSourceProvider;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.DbResult;
import de.mhus.lib.sql.DbStatement;
import de.mhus.lib.sql.DefaultDbPool;
import de.mhus.micro.ext.api.dfs.DfsProviderOperation;
import de.mhus.micro.ext.api.dfs.FileInfo;
import de.mhus.micro.ext.api.dfs.FileQueueApi;
import de.mhus.osgi.api.services.MOsgi;
import de.mhus.osgi.api.util.DataSourceUtil;

public class DfsSql2Provider extends OperationToIfcProxy implements DfsProviderOperation {

    private static final UUID ROOT_ID = MConstants.EMPTY_UUID;
    private static final EntryData rootEntry = new EntryData();
    private String scheme = "sql";
    private String prefix = "sop_dfs";
    private String acl = "*";
    private String dataSourceName = "db_sop";
    private DbPool pool;
    private SoftHashMap<String, UUID> queueCache = new SoftHashMap<>();
    private DataSource dataSource;
    private DataSourceProvider dsProvider;

    public DfsSql2Provider(String dataSource, String scheme, String prefix, String acl) {
        this.dataSourceName = dataSource;
        this.scheme = scheme;
        this.prefix = prefix;
        this.acl = acl;
    }

    @Override
    public FileInfo getFileInfo(MUri uri) {
        init();
        EntryData entry = getEntry(uri);
        if (entry == null) return null;

        MutableUri u = new MutableUri(uri.toString());
        u.setParams(
                new String[] {
                    String.valueOf(entry.size), String.valueOf(entry.modified.getTime())
                });
        //		HashMap<String,String> query = new HashMap<>();
        //		query.put("created", String.valueOf(entry.created.getTime()));
        //		u.setQuery(query);
        return new FileInfoImpl(u);
    }

    private EntryData getEntry(MUri uri) {
        String path = normalizePath(uri.getPath());
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return getEntry(path);
    }

    private EntryData getEntry(String path) {
        if (path == null || path.equals("/") || path.equals("")) {
            return rootEntry;
        }
        UUID parent = ROOT_ID;
        EntryData current = null;
        for (String name : path.split("/")) {
            if (name.length() == 0) continue;
            if (current != null && current.type == 0) {
                return null; // file is not a directory entry
            }
            current = getEntry(parent, name);
            if (current == null) return null;
            parent = current.id;
        }
        return current;
    }

    private EntryData getEntry(UUID parent, String name) {
        name = normalizeName(name);
        init();
        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT id_,name_,created_,modified_,size_,parent_,type_ FROM "
                                    + prefix
                                    + "_entry_ WHERE parent_ = $parent$ AND name_ = $name$");
            MProperties prop = new MProperties();
            prop.setString("name", name);
            prop.setString("parent", parent.toString());
            res = sth.executeQuery(prop);
            if (res.next()) {
                EntryData entry = new EntryData(res);
                if (res.next()) {
                    log().w("more then one entry for", parent, name);
                }
                sth.close();
                return entry;
            }
            sth.close();
            return null;
        } catch (Exception e) {
            log().e(parent, name, e);
            return null;
        } finally {
            if (res != null)
                try {
                    res.close();
                } catch (Throwable t) {
                    log().e(t);
                }
            ;
            if (con != null) con.close();
        }
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        return name.replace('/', '_').replace('%', '_');
    }

    @Override
    public MUri exportFile(MUri uri) throws IOException {
        init();

        String path = normalizePath(uri.getPath());
        UUID id = null;
        FileQueueApi api = M.l(FileQueueApi.class);
        if (api == null) throw new IOException("FileQueueApi not found");

        // from cache ?
        synchronized (queueCache) {
            id = queueCache.get(path);
        }
        if (id != null) {
            try {
                EntryData entry = getEntry(uri);
                if (entry == null) throw new IOException("Entry not found " + uri);

                FileInfo localInfo = api.getFileInfo(id);
                if (localInfo.getModified() == entry.modified.getTime()) {
                    api.touchFile(id, 0);
                    return MUri.toUri(localInfo.getUri());
                }
            } catch (FileNotFoundException fnf) {

            }
        }

        EntryData entry = getEntry(uri);
        if (entry == null) {
            log().d("entry not found", uri);
            return null;
        }

        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT content_ FROM " + prefix + "_entry_ WHERE id_ = $id$");
            MProperties prop = new MProperties();
            prop.setString("id", entry.id.toString());
            res = sth.executeQuery(prop);
            if (res.next()) {

                // found

                InputStream is = res.getBinaryStream("content_");
                id = api.takeFile(is, 0, entry.modified.getTime(), entry.name);
                synchronized (queueCache) {
                    queueCache.put(path, id);
                }

                // finally
                if (res.next()) {
                    log().w("more then one entry for", path);
                }

                sth.close();
                return api.getUri(id);
            }
            sth.close();
            return null;
        } catch (Exception e) {
            throw new IOException(uri.toString(), e);
        } finally {
            if (res != null)
                try {
                    res.close();
                } catch (Throwable t) {
                    log().e(t);
                }
            ;
            if (con != null) con.close();
        }
    }

    @Override
    public Map<String, MUri> getDirectoryList(MUri uri) {
        init();

        TreeMap<String, MUri> out = new TreeMap<>();

        EntryData entry = getEntry(uri);
        String path = uri.getPath();
        if (!path.endsWith("/")) path = path + "/";

        if (entry == null) {
            log().d("entry not found", uri);
            return out;
        }

        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT name_ FROM " + prefix + "_entry_ WHERE parent_ = $parent$");
            MProperties prop = new MProperties();
            prop.setString("parent", entry.id.toString());
            res = sth.executeQuery(prop);
            while (res.next()) {
                String name2 = res.getString("name_");

                MutableUri u = new MutableUri(null);
                u.setScheme(uri.getScheme());
                u.setLocation(uri.getLocation());
                u.setPath(path + name2);

                out.put(name2, u);
            }
            sth.close();
            return out;
        } catch (Exception e) {
            log().e(uri, e);
            return out;
        } finally {
            if (res != null)
                try {
                    res.close();
                } catch (Throwable t) {
                    log().e(t);
                }
            ;
            if (con != null) con.close();
        }
    }

    private String normalizePath(String path) {
        if (path == null) return null;
        return path.trim().replace('%', '_');
    }

    @Override
    public void importFile(MUri queueUri, MUri target) throws IOException {
        init();

        DbConnection con = null;
        try {
            if (!AccessUtil.isAdmin())
                throw new IOException("Not supported"); // TODO use ACL!!!!

            FileQueueApi api = M.l(FileQueueApi.class);
            if (api == null) throw new IOException("FileQueueApi not found");
            File fromFile = api.loadFile(queueUri);

            String targetPath = normalizePath(target.getPath());
            if (targetPath.endsWith("/"))
                throw new IOException("Target is a directory " + targetPath);

            synchronized (queueCache) {
                queueCache.remove(targetPath);
            }

            Date now = new Date();

            EntryData entry = getEntry(target);
            con = pool.getConnection();
            if (entry != null) {
                if (entry.type != 0) throw new IOException("Entry is not a file " + targetPath);

                // update
                DbStatement sth =
                        con.createStatement(
                                "UPDATE "
                                        + prefix
                                        + "_entry_ SET modified_=$modified$, content_=$content$ WHERE id_=$id$");
                MProperties prop = new MProperties();
                prop.setString("id", entry.id.toString());
                prop.setDate("modified", now);
                prop.put("content", new FileInputStream(fromFile));
                int res = sth.executeUpdate(prop);
                if (res != 1) {
                    throw new IOException("Can't update entry " + target);
                }
                sth.close();
                con.commit();
            } else {
                // create

                EntryData parent = getEntry(MUri.getFileDirectory(targetPath));
                if (parent == null)
                    throw new NotFoundException("Parent directory not found", targetPath);

                String targetName = MUri.getFileName(targetPath);

                DbStatement sth =
                        con.createStatement(
                                "INSERT INTO "
                                        + prefix
                                        + "_entry_ (name_,id_,parent_,created_,modified_,type_,content_) "
                                        + "VALUES ($name$,$id$,$parent$,$created$,$modified$,0,$content$)");
                MProperties prop = new MProperties();
                prop.setString("name", targetName);
                prop.setString("id", UUID.randomUUID().toString());
                prop.setString("parent", parent.id.toString());
                prop.setDate("created", now);
                prop.setDate("modified", now);
                prop.put("content", new FileInputStream(fromFile));
                int res = sth.executeUpdate(prop);
                if (res != 1) {
                    throw new IOException("Can't insert entry " + target);
                }
                sth.close();
                con.commit();
            }

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (con != null) con.close();
        }
    }

    @Override
    public void deleteFile(MUri uri) throws IOException {

        EntryData entry = getEntry(uri);
        if (entry == null) throw new IOException("Entry not found " + uri);

        String path = normalizePath(uri.getPath());
        DbConnection con = null;
        try {
            if (!AccessUtil.isAdmin())
                throw new IOException("Not supported"); // TODO use ACL!!!!

            con = pool.getConnection();
            if (entry.type == 1) {
                // is directory
                for (Entry<String, MUri> sub : getDirectoryList(uri).entrySet()) {
                    deleteFile(sub.getValue());
                }
            }

            synchronized (queueCache) {
                queueCache.remove(path);
            }

            // is file
            DbStatement sth =
                    con.createStatement("DELETE FROM " + prefix + "_entry_ WHERE id_ = $id$");
            MProperties prop = new MProperties();
            prop.setString("id", entry.id.toString());
            int res = sth.executeUpdate(prop);
            if (res == 0) {
                throw new IOException("File not found: " + path);
            }
            sth.close();
            con.commit();

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (con != null) con.close();
        }
    }

    @Override
    public void createDirectories(MUri uri) throws IOException {
        DbConnection con = null;
        try {
            if (!AccessUtil.isAdmin())
                throw new IOException("Not supported"); // TODO use ACL!!!!

            String parentPath = normalizePath(MUri.getFileDirectory(uri.getPath()));
            String name = normalizeName(MUri.getFileName(uri.getPath()));
            EntryData parent = getEntry(parentPath);
            if (parent == null) {
                createDirectories(MUri.toUri("sql:" + parentPath));
                parent = getEntry(parentPath);
                if (parent == null) throw new IOException("can't create " + parentPath);
            }
            EntryData existing = getEntry(parent.id, name);
            if (existing != null) return;

            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "INSERT INTO "
                                    + prefix
                                    + "_entry_ (name_,id_,parent_,created_,modified_,type_) "
                                    + "VALUES ($name$,$id$,$parent$,$created$,$modified$,1)");

            Date now = new Date();
            MProperties prop = new MProperties();
            prop.setString("name", name);
            prop.setString("id", UUID.randomUUID().toString());
            prop.setString("parent", parent.id.toString());
            prop.setDate("created", now);
            prop.setDate("modified", now);
            int res = sth.executeUpdate(prop);
            if (res != 1) {
                throw new IOException("Can't insert entry " + uri);
            }
            sth.close();
            con.commit();

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (con != null) con.close();
        }
    }

    @Override
    protected Class<?> getInterfaceClass() {
        return DfsProviderOperation.class;
    }

    @Override
    protected Object getInterfaceObject() {
        return this;
    }

    @Override
    protected Version getInterfaceVersion() {
        return MOsgi.getBundelVersion(this.getClass());
    }

    @Override
    protected void initOperationDescription(HashMap<String, String> parameters) {
        parameters.put(PARAM_SCHEME, scheme);
        parameters.put(OperationDescription.TAGS, "acl=" + acl);
    }

    private void init() {
        dataSource = DataSourceUtil.getDataSource(dataSourceName);
        if (dataSource == null) throw new MRuntimeException("datasource not found", dataSourceName);
        if (dsProvider != null) {
            dsProvider.setDataSource(dataSource);
            return;
        }

        dsProvider = new DataSourceProvider();
        dsProvider.setDataSource(dataSource);
        pool = new DefaultDbPool(dsProvider);
        try {
            URL url = MSystem.locateResource(this, "Sql2DfsStorage.xml");
            DbConnection con = pool.getConnection();
            IConfig data = M.l(IConfigFactory.class).read(url);
            data.setString("prefix", prefix);
            pool.getDialect().createStructure(data, con, null, false);
            con.close();
        } catch (Exception e) {
            log().e(e);
        }
    }

    public String getScheme() {
        return scheme;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAcl() {
        return acl;
    }

    public String getDataSource() {
        return dataSourceName;
    }

    private static class EntryData {

        UUID id;

        @SuppressWarnings("unused")
        UUID parent;

        private String name;
        private long size;

        @SuppressWarnings("unused")
        private Date created;

        private Date modified;
        private int type;

        public EntryData() {
            id = ROOT_ID;
            parent = null;
            name = "";
            type = 1;
        }

        public EntryData(DbResult res) throws Exception {
            this.id = UUID.fromString(res.getString("id_"));
            this.parent = UUID.fromString(res.getString("parent_"));
            this.name = res.getString("name_");
            this.size = res.getLong("size_");
            this.created = res.getDate("created_");
            this.modified = res.getDate("modified_");
            this.type = res.getInt("type_");
        }
    }
}
