package de.mhus.aqua.mod;

import java.io.File;

import de.mhus.lib.config.IConfig;

public abstract class Publisher {

	public abstract void publish(IConfig config, File dir);
	
}
