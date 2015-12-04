package de.mhus.app.web.filebrowser.api;

public interface FileBrowserContext {

	void registerAction(FileBrowserAction action);
	void unregisterAction(FileBrowserAction action);
	
	void registerSpaceType(String name, Class<? extends Space> clazz);
	void unregisterSpaceType(String name);
	
}
