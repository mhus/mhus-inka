package de.mhus.app.inka.sync;

import java.util.TreeMap;

import de.mhus.lib.config.IConfig;
import de.mhus.lib.util.CompareDir;
import de.mhus.lib.util.CompareDirEntry;

public abstract class IFile implements IConfigurable, CompareDir.Listener {

	protected IConfig config;
	protected Sync sync;
	
	@Override
	public void initialize(Sync sync, IConfig config) {
		this.config = config;
		this.sync   = sync;
		doInit();
	}
	
	protected abstract void doInit();
	
	public abstract TreeMap<String, CompareDirEntry> getStructure();
	
}
