package de.mhu.hair.dctm;

import java.io.File;

public class DctmEnvironment {

	public static File findDctmShared( String argShared ) {
		File dctmDir = null;
		
		if ( dctmDir == null && argShared != null ) {
			// dctmDir = new File(ap.getValue("documentum_shared", 0));
			dctmDir = new File(argShared);
		}
		
		if ( dctmDir == null && System.getenv("DOCUMENTUM_SHARED") != null ) {
			File f = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc.jar");
			if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM_SHARED"));
		}
		
		if ( dctmDir == null && System.getenv("DOCUMENTUM_SHARED") != null ) {
			File f = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc/dfc.jar");
			if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc" );
		}
		
		if ( dctmDir == null && System.getenv("DOCUMENTUM") != null ) {
			File f = new File(System.getenv("DOCUMENTUM") + "/shared/dfc.jar");
			if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM") + "/shared" );
		}
		
		if ( dctmDir == null && System.getenv("DOCUMENTUM") != null ) {
			File f = new File(System.getenv("DOCUMENTUM") + "/shared/dfc/dfc.jar");
			if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM") + "/shared/dfc" );
		}

		return dctmDir;
		
	}
	
	
	
}
