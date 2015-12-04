package de.mhus.aqua.caosql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.util.HashMap;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.Acl;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IAcl;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.lib.MException;
import de.mhus.lib.MString;
import de.mhus.lib.MXml;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.ConfigBuilder;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public class ASNode extends ASElement {

	private static Log log = Log.getLog(ASNode.class);
	
	private IConfig appConfig;

	protected ASNode(AquaConnection connection) throws IOException, Exception {
		super(connection);
		try {
			DbConnection db = ((ASConnection)connection).getPool().getConnection(ASConnection.DB_NAME);
			DbStatement sth = db.getStatement(ASConnection.QUERY_NODE_BY_ID);
			HashMap<String, Object> attr = new HashMap<String, Object>();
			attr.put("id", 0);
			ResultSet res = sth.executeQuery(attr);
			if (!res.next()) {
				res.close();
				throw new CaoException("Node not found: 0");
			}
			load(res);
			res.close();
			db.close();
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	public ASNode(AquaConnection connection, int parent, String name)  throws CaoException {
		super(connection);
		try {
			DbConnection db = ((ASConnection)connection).getPool().getConnection(ASConnection.DB_NAME);
			DbStatement sth = db.getStatement(ASConnection.QUERY_NODE);
			HashMap<String, Object> attr = new HashMap<String, Object>();
			attr.put("id", parent);
			attr.put("name", name);
			ResultSet res = sth.executeQuery(attr);
			if (!res.next()) {
				res.close();
				throw new CaoException("Node not found: " + parent +" " + name);
			}
			load(res);
			res.close();
			db.close();
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}
	
	public ASNode(AquaConnection connection, int id) throws CaoException {
		super(connection);
		try {
			DbConnection db = ((ASConnection)connection).getPool().getConnection(ASConnection.DB_NAME);
			DbStatement sth = db.getStatement(ASConnection.QUERY_NODE_BY_ID);
			HashMap<String, Object> attr = new HashMap<String, Object>();
			attr.put("id", id);
			ResultSet res = sth.executeQuery(attr);
			res.next();
			load(res);
			res.close();
			db.close();
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}
	
	protected void load(ResultSet res) throws Exception {
		super.load(res);
		
		String appAttr = getString("config");
		if (MString.isEmpty(appAttr))
			appConfig = getApplication().createDefaultConfig();
		else
			appConfig = new JsonConfig(appAttr);
	
	}
	
	public IConfig getApplicationConfig(IUser user) throws MException {
		DbConnection con = null;
		try {
			con = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
			DbStatement sth = con.getStatement(ASConnection.QUERY_CONTAINER);
			HashMap<String, Object> attr = new HashMap<String, Object>();
			attr.put("node", getId());
			attr.put("user", user.getId());
System.out.println("aa");
			ResultSet res = sth.executeQuery(attr);
			if (res.next()) {
				String configStr = res.getString("config");
				res.close();
				return new JsonConfig(configStr);
			} else {
				res.close();
				// clone config - is not allowed to change config from extern
				IConfig c2 = new HashConfig();
				new ConfigBuilder().cloneConfig(appConfig, c2);
				return c2;
			}
		} catch (Exception e) {
			throw new MException(user,e);
		} finally {
			if (con != null) con.close();
		}
	}

	@Override
	public AquaElement getExtendedNode(String ext) {
		return this;
	}

	@Override
	public ASElement getChild(String name, AquaSession access) throws CaoException {
		try {
			return new ASNode((AquaConnection) getConnection(),Integer.valueOf(getId()),name);
		} catch (Exception e) {
			throw new CaoException(name,e);
		}
	}

	@Override
	public void setApplicationConfig(IUser user, IConfig config) throws CaoException {
		synchronized(this) {
			// TODO check rights ????
			try {
				if (config == null) {
					log.t("remove session for user",user);
					DbConnection con = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
					DbStatement sth = con.getStatement(ASConnection.QUERY_CONTAINER_REMOVE);
					HashMap<String, Object> attr = new HashMap<String, Object>();
					attr.put("node", getId());
					attr.put("user", user.getId());					
					sth.execute(attr);
					con.close();

				} else {
					log.t("save session for user",user);
					if (log.isTrace()) ConfigUtil.dump(config, System.out);
					
					DbConnection con = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
					DbStatement sth = con.getStatement(ASConnection.QUERY_CONTAINER);
					HashMap<String, Object> attr = new HashMap<String, Object>();
					attr.put("node", getId());
					attr.put("user", user.getId());
					
					JsonConfig c2 = null;
					if (config instanceof JsonConfig)
						c2 = (JsonConfig)config;
					else {
						c2 = new JsonConfig();
						new ConfigBuilder().cloneConfig(config, c2);
					}
	
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					c2.write(os);
					
					attr.put("config", os.toString());
					ResultSet res = sth.executeQuery(attr);
					boolean exists = res.next();
					res.close();
					if (exists) {
						sth = con.getStatement(ASConnection.QUERY_CONTAINER_CHANGE);
					} else {
						sth = con.getStatement(ASConnection.QUERY_CONTAINER_CREATE);
					}
					sth.execute(attr);
					con.close();
				}
			} catch (Exception e) {
				throw new CaoException(e);
			}
		}
	}

	@Override
	public void setApplicationConfig(IConfig config) throws CaoException {
		synchronized(this) {
			try {
				log.t("save session");
				if (log.isTrace()) ConfigUtil.dump(config, System.out);
				
				DbConnection con = ((ASConnection)getConnection()).getPool().getConnection(ASConnection.DB_NAME);
				HashMap<String, Object> attr = new HashMap<String, Object>();
				attr.put("id", getId());

				JsonConfig c2 = null;
				c2 = new JsonConfig();
				new ConfigBuilder().cloneConfig(config, c2);

				log.t("save session -------------");
				if (log.isTrace()) ConfigUtil.dump(config, System.out);

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				c2.write(os);
				attr.put("config", os.toString());

				DbStatement sth = con.getStatement(ASConnection.QUERY_APPLICATION_CONFIG_CHANGE);
				sth.execute(attr);
				con.close();
				appConfig = c2;
			} catch (Exception e) {
				throw new CaoException(e);
			}
		}
	}

}
