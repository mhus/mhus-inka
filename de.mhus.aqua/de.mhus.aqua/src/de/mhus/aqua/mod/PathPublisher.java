package de.mhus.aqua.mod;

import java.io.File;
import java.io.FileFilter;

import de.mhus.lib.MFile;
import de.mhus.lib.config.IConfig;

public class PathPublisher extends Publisher {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(PathPublisher.class);

	@Override
	public void publish(IConfig config, File dir) {
		
		final String strategy = config.getString("strategy", "overwrite");
		
		final File src = new File(config.getExtracted("src"));
		final File dst = new File(dir,config.getExtracted("dest"));
		
		if ("once".equals(strategy) && dst.exists()) {
			log.t("destination already exists",strategy,src,dst);
			return;
		}
		
		log.t("publish",src,dst);
		MFile.copyDir(src, dst, new FileFilter() {

			@Override
			public boolean accept(File cur) {
				if (cur.isDirectory()) return true;
				if ("update".equals(strategy)) {
					File d = new File(dst, cur.getAbsolutePath().substring(src.getAbsolutePath().length()));
					if (d.exists()) return false; // also check crc ??
				}
				return true;
			}
			
		});
	}

}
