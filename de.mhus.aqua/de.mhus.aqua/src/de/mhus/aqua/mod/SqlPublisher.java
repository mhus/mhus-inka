package de.mhus.aqua.mod;

import java.io.File;

import de.mhus.aqua.Activator;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.caosql.ASConnection;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStructureUtil;

public class SqlPublisher extends Publisher {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(SqlPublisher.class);

	@Override
	public void publish(IConfig config, File dir) {
		try {
			String dbName = config.getString("database", ASConnection.DB_NAME);
			DbConnection db = Activator.getAqua().getDbPool().getConnection(dbName);
			DbStructureUtil.createStructure(config, db);
		} catch (Exception e) {
			log.w(e);
		}
	}

	
	
}
