package de.mhus.aqua.cao;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaSession;
import de.mhus.lib.MXml;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.sql.DbPool;

public class AquaConnection extends CaoConnection<AquaSession> {
	
	public static final String TREE_NODE = "node";
	public static final String TREE_ACL = "acl";
	public static final String LIST_RULES = "rules";
	public static final String TREE_USER = "user";
	public static final String LIST_RIGHTS = "rights";

	public AquaConnection(AquaDriver driver) {
		super(driver);
	}

}
