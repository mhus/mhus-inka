package de.mhus.aqua.mod;

import java.io.File;

import de.mhus.lib.config.IConfig;
import de.mhus.lib.io.Unzip;

public class ZipPublisher extends Publisher {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(ZipPublisher.class);

	@Override
	public void publish(IConfig config, File dir) {
		
		String strategy = config.getString("strategy", "overwrite");

		File src = new File(config.getExtracted("src"));
		File dst = new File(dir,config.getExtracted("dest"));
	
		if ("once".equals(strategy) && dst.exists()) {
			log.t("destination already exists",strategy,src,dst);
			return;
		}

		try {
			Unzip.unzip(src, dst, null);
		} catch (Exception e) {
			log.w(src,dst,e);
		}
		
		
	}

}
