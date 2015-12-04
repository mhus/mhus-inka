package de.mhu.res.img;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.mhu.lib.resources.IResourceProvider;

public class HairResourceProvider implements IResourceProvider {

	public OutputStream createFile(String path) throws IOException {
		return null;
	}

	public InputStream getInputStream(String path) throws IOException {
		path = "/de/mhu/res/img" + path;
		return this.getClass().getResourceAsStream(path);
	}

	public long getLastModified(String path) throws IOException {
		return 0;
	}

	public boolean isWriteable(String path) {
		return false;
	}

}
