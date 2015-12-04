package de.dctm.bridge;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;

import de.mhu.lib.ASql;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.ALUtilities;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.client.AuthPassword;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.Query;

public class ImportFormats {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		ArgsParser arg = new ArgsParser( args );
		ConfigManager.initialize( arg );
		ALUtilities.configure( arg );
		
		Config dmConfig = ConfigManager.getConfig( "dctm" );
		Config mcConfig = ConfigManager.getConfig( "morse" );
		
		DMConnection dmCon = new DMConnection( dmConfig.getProperty( "user" ), dmConfig.getProperty( "pass" ), dmConfig.getProperty( "docbase" ) );
		
		MConnectionTcp mcCon = new MConnectionTcp( mcConfig.getProperty("host"), 6666 );
		mcCon.setService( mcConfig.getProperty( "service" ) );
		mcCon.setAuth( mcConfig.getProperty( "user" ), new AuthPassword( mcConfig.getProperty( "pass" ) ) );
		mcCon.connect();
		IConnection mcDb = mcCon.getSession().getDbProvider().getDefaultConnection();
		new Query( mcDb, "DELETE FROM mc_format `enable:-btc,-event`" ).execute().close();
		
		IDfCollection rFormats = dmCon.createQuery( "SELECT * FROM dm_format" ).execute( dmCon.getSession(), IDfQuery.READ_QUERY );
		while ( rFormats.next() ) {
			
			try {
				String mql = "INSERT INTO mc_format (name,dos_extension,mime_type,description) VALUES (" +
							"'" + rFormats.getString( "name" ) + "','" +
							rFormats.getString( "dos_extension" ) + "','" +
							rFormats.getString( "mime_type" ) + "','" +
							ASql.escape( rFormats.getString( "description" ) ) + "')";
				new Query( mcDb, mql ).execute().close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		
		mcCon.close();
		dmCon.disconnect();
		
		
	}

}
