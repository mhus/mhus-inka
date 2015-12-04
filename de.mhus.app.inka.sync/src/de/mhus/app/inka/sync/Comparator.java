package de.mhus.app.inka.sync;

import de.mhus.lib.config.IConfig;
import de.mhus.lib.util.CompareDir;

public class Comparator extends CompareDir implements IConfigurable {

	protected IConfig config;
	protected Sync sync;
	
	@Override
	public void initialize(Sync sync, IConfig config) {
		this.config = config;
		this.sync   = sync;
		setNeedAllFolders(true);
		doInit();
	}

	protected void doInit() {}
	
}
