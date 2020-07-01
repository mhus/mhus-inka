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
import java.util.TreeMap;
import java.util.UUID;

import javax.sql.DataSource;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
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

public class DfsSqlProvider extends OperationToIfcProxy implements DfsProviderOperation {

    private String scheme = "sql";
    private String prefix = "sop_dfs";
    private String acl = "*";
    private String dataSourceName = "db_sop";
    private DbPool pool;
    private SoftHashMap<String, UUID> queueCache = new SoftHashMap<>();
    private DataSource dataSource;
    private DataSourceProvider dsProvider;

    public DfsSqlProvider(String dataSource, String scheme, String prefix, String acl) {
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
        return getEntry(path);
    }

    private EntryData getEntry(String path) {
        init();
        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT name_,path_,created_,modified_,pathlevel_,size_ FROM "
                                    + prefix
                                    + "_entry_ WHERE path_ = $path$");
            MProperties prop = new MProperties();
            prop.setString("path", path);
            res = sth.executeQuery(prop);
            if (res.next()) {
                EntryData entry = new EntryData(res);
                if (res.next()) {
                    log().w("more then one entry for", path);
                }
                sth.close();
                return entry;
            }
            sth.close();
            return null;
        } catch (Exception e) {
            log().e(path, e);
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

        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT name_,path_,content_,modified_ FROM "
                                    + prefix
                                    + "_entry_ WHERE path_ = $path$");
            MProperties prop = new MProperties();
            prop.setString("path", path);
            res = sth.executeQuery(prop);
            if (res.next()) {

                // found

                InputStream is = res.getBinaryStream("content_");
                Date modify = res.getDate("modified_");
                String name = res.getString("name_");
                //				String path2 = res.getString("path");
                id = api.takeFile(is, 0, modify.getTime(), name);
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
        String path = normalizePath(uri.getPath());
        if (!path.endsWith("/")) path = path + "/";
        int pathLevel = MString.countCharacters(path, '/'); // TODO -1 ?

        DbConnection con = null;
        DbResult res = null;
        try {
            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "SELECT name_,path_,modified_ FROM "
                                    + prefix
                                    + "_entry_ WHERE path_ like $path$ AND pathlevel_ = $pathlevel$");
            MProperties prop = new MProperties();
            prop.setString("path", path + "%");
            prop.setInt("pathlevel", pathLevel);
            res = sth.executeQuery(prop);
            while (res.next()) {
                String name2 = res.getString("name_");
                String path2 = res.getString("path_");

                MutableUri u = new MutableUri(null);
                u.setScheme(uri.getScheme());
                u.setLocation(uri.getLocation());
                u.setPath(path2);

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
            // test for directory
            String dirPath = MUri.getFileDirectory(targetPath);
            if (dirPath != null) {
                dirPath = dirPath + "/";
                EntryData dirEntry = getEntry(dirPath);
                if (dirEntry == null) throw new IOException("Directory not found " + dirPath);
            }

            synchronized (queueCache) {
                queueCache.remove(targetPath);
            }

            Date now = new Date();

            EntryData entry = getEntry(target);
            con = pool.getConnection();
            if (entry != null) {
                // update
                DbStatement sth =
                        con.createStatement(
                                "UPDATE "
                                        + prefix
                                        + "_entry_ SET modified_=$modified$, content_=$content$ WHERE path_=$path$");
                MProperties prop = new MProperties();
                prop.setString("path", targetPath);
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
                String targetName = MUri.getFileName(targetPath);
                int pathLevel = MString.countCharacters(targetPath, '/');

                DbStatement sth =
                        con.createStatement(
                                "INSERT INTO "
                                        + prefix
                                        + "_entry_ (name_,path_,pathlevel_,created_,modified_,type_,content_) "
                                        + "VALUES ($name$,$path$,$pathlevel$,$created$,$modified$,0,$content$)");
                MProperties prop = new MProperties();
                prop.setString("name", targetName);
                prop.setString("path", targetPath);
                prop.setInt("pathlevel", pathLevel);
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
        String path = normalizePath(uri.getPath());
        DbConnection con = null;
        try {
            if (!AccessUtil.isAdmin())
                throw new IOException("Not supported"); // TODO use ACL!!!!

            con = pool.getConnection();
            if (path.endsWith("/")) {
                // is directory
                DbStatement sth =
                        con.createStatement(
                                "DELETE FROM " + prefix + "_entry_ WHERE path_ like $path$");
                MProperties prop = new MProperties();
                prop.setString("path", path + "%");
                int res = sth.executeUpdate(prop);
                if (res == 0) {
                    throw new IOException("File not found: " + path);
                }
                sth.close();
                con.commit();
            } else {

                synchronized (queueCache) {
                    queueCache.remove(path);
                }

                // is file
                DbStatement sth =
                        con.createStatement(
                                "DELETE FROM " + prefix + "_entry_ WHERE path_ = $path$");
                MProperties prop = new MProperties();
                prop.setString("path", path);
                int res = sth.executeUpdate(prop);
                if (res == 0) {
                    throw new IOException("File not found: " + path);
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
    public void createDirectories(MUri uri) throws IOException {
        String path = normalizePath(uri.getPath());
        DbConnection con = null;
        try {
            if (!AccessUtil.isAdmin())
                throw new IOException("Not supported"); // TODO use ACL!!!!

            con = pool.getConnection();
            DbStatement sth =
                    con.createStatement(
                            "INSERT INTO "
                                    + prefix
                                    + "_entry_ (name_,path_,pathlevel_,created_,modified_,type_) "
                                    + "VALUES ($name$,$path$,$pathlevel$,$created$,$modified$,1)");

            while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            if (path.length() == 0) return;

            Date now = new Date();
            StringBuilder cur = new StringBuilder().append('/');
            path = path.substring(1); // remove first /
            for (String part : path.split("/")) {
                cur.append(part).append('/');
                String curStr = cur.toString();
                EntryData entry = getEntry(curStr);
                if (entry == null) {
                    int pathLevel = MString.countCharacters(curStr, '/') - 1;
                    MProperties prop = new MProperties();
                    prop.setString("name", part);
                    prop.setString("path", curStr);
                    prop.setInt("pathlevel", pathLevel);
                    prop.setDate("created", now);
                    prop.setDate("modified", now);
                    int res = sth.executeUpdate(prop);
                    if (res != 1) {
                        throw new IOException("Can't insert entry " + curStr);
                    }
                }
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
            URL url = MSystem.locateResource(this, "SqlDfsStorage.xml");
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

        @SuppressWarnings("unused")
        private String name;

        private long size;

        @SuppressWarnings("unused")
        private String path;

        @SuppressWarnings("unused")
        private Date created;

        private Date modified;

        public EntryData(DbResult res) throws Exception {
            this.name = res.getString("name_");
            this.size = res.getLong("size_");
            this.path = res.getString("path_");
            this.created = res.getDate("created_");
            this.modified = res.getDate("modified_");
        }
    }
}
