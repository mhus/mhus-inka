package de.mhus.app.web.filebrowser.api;

public interface FileBrowserPlug {

	void start(FileBrowserContext context);
	void stop(FileBrowserContext context);
	
}
