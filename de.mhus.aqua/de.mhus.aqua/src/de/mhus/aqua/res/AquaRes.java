package de.mhus.aqua.res;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MCast;
import de.mhus.lib.config.IConfig;

/**
 * Represent a resource in the aqua framework. The resource can be loaded from the ResourceApplication.
 * 
 * @author mikehummel
 *
 */
public abstract class AquaRes {

	private String name;
	private float version;
	protected IConfig config;
	private IConfig map;

	public void setConfig(IConfig config) {
		this.config = config;
		this.map = config.getConfig("map");
		setName(config.getProperty("name"));
		setVersion(MCast.tofloat(config.getProperty("version", "0"),0));
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}
	
	public float getVersion() {
		return version;
	}
	
	public abstract void process(AquaRequest request) throws Exception;
	
	public String getPath() {
		return Activator.getAqua().getConfig().getExtracted("WEB_PATH","") + "/res/_" + getName() + "_" + getVersion(); //TODO cache
	}
	
	public String toString() {
		return getPath();
	}

	public String getPathFor(String path) {
		if(map!=null)
			path = map.getProperty(path, path);
		return getPath() + path;
	}
	
}
