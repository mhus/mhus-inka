package de.mhus.app.web.filebrowser.vm;

import de.mhus.app.web.filebrowser.api.FileBrowserAction;

public class ActionDefinition {

	private String name;
	private String title;

	public ActionDefinition(String key, FileBrowserAction value) {
		this.name = key;
		this.title = value.getTitle();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
