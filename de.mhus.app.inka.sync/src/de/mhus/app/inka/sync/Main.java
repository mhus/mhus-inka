package de.mhus.app.inka.sync;

import de.mhus.lib.MArgs;
import de.mhus.lib.MSingleton;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;

public class Main {

	public static void main(String[] args) throws Exception {
		
		MSingleton.instance().setArguments(args);
		MArgs arg = MSingleton.instance().getArguments();
		
//		ConfigUtil.dump(MSingleton.instance().getConfig(), System.out);
		
		String[] def = arg.getValues(MArgs.DEFAULT);
		
		IConfig config = MSingleton.instance().getConfig();
		
		IConfig compare = config.getConfig("compare");
		if (compare == null) config.createConfig("compare");
		
		IConfig source1 = null;
		IConfig source2 = null;
		IConfig[] sources = config.getConfigBundle("source");
		if (sources.length < 1) {
			source1 = config.createConfig("source");
			if (def.length < 1) {
				printUsage();
				return;
			}
			source1.setString("directory", def[0]);
		} else
			source1 = sources[0];
		
		if (sources.length < 2) {
			source2 = config.createConfig("source");
			if (def.length < 2) {
				printUsage();
				return;
			}
			source2.setString("directory", def[1]);
		} else
			source2 = sources[1];
				
		Sync sync = new Sync(config);
		sync.action();
		
	}

	private static void printUsage() {
		System.out.println("Usage: sync <from> <to> [options]");
	}
}
