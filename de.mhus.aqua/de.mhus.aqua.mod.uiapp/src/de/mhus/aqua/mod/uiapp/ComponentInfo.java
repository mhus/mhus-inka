package de.mhus.aqua.mod.uiapp;

import de.mhus.lib.config.IConfig;

public class ComponentInfo {

	private String title;
	private String id;
	private IConfig config;

	public ComponentInfo(String id, String title, IConfig config) {
		this.title = title;
		this.id = id;
		this.config = config;
	}
	
	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public IConfig getConfig() {
		return config;
	}
	
}
