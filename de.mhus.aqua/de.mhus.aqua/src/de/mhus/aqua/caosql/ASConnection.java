package de.mhus.aqua.caosql;

import de.mhus.aqua.Activator;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaDriver;
import de.mhus.lib.MXml;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbPool;

public class ASConnection extends AquaConnection {

	public static final String DB_NAME = "database";
	public static final String QUERY_NODE = "node_select";
	public static final String QUERY_NODE_BY_ID = "node_select_by_id";
	public static final String QUERY_CONTAINER = "container_select";
	public static final String QUERY_CONTAINER_CHANGE = "container_change";
	public static final String QUERY_CONTAINER_CREATE = "container_create";
	public static final String QUERY_NODE_CHILDREN = "node_select_children";
	public static final String QUERY_USER = "user_select";
	public static final String QUERY_USER_RIGHTS = "user_rights_select";
	public static final String QUERY_ACL = "acl_select";
	public static final String QUERY_ACL_RULE = "acl_rule_select";
	public static final String QUERY_APPLICATION_CONFIG_CHANGE = "application_config_change";
	public static final String QUERY_CONTAINER_REMOVE = "container_remove";

	private static Log log = Log.getLog(ASConnection.class);
	
	private DbPool pool;
	private IConfig config;

	public ASConnection(AquaDriver driver,String url) throws CaoException {
		super(driver);
		try {
			config = new XmlConfig(MXml.loadXml(url).getDocumentElement());
			pool = Activator.getAqua().getDbPool();
			pool.getConnection(config.getExtracted("database")); // test connection - open one connetion
			applications.put(CaoDriver.APP_CONTENT, new ASContentApplication(this));
			applications.put(AquaDriver.APP_AAA, new ASAaaApplication(this));
			
		} catch (Exception e) {
			throw new CaoException(e);
		}	
	}

	public DbPool getPool() {
		return pool;
	}

}
