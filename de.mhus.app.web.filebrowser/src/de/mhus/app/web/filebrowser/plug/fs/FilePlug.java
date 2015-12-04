package de.mhus.app.web.filebrowser.plug.fs;

import de.mhus.app.web.filebrowser.api.FileBrowserContext;
import de.mhus.app.web.filebrowser.api.FileBrowserPlug;

public class FilePlug implements FileBrowserPlug {

	OpenAction open = new OpenAction();
	
	@Override
	public void start(FileBrowserContext context) {
		context.registerSpaceType("fs", FileSpace.class);
		context.registerAction(open);
	}

	@Override
	public void stop(FileBrowserContext context) {
		context.unregisterAction(open);
		context.unregisterSpaceType("fs");
	}

}
