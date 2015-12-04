package de.mhus.app.web.filebrowser.plug.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import de.mhus.app.web.filebrowser.api.FileBrowserNode;
import de.mhus.app.web.filebrowser.api.ProvideInputStream;
import de.mhus.lib.MXml;

public class FileNode extends FileBrowserNode implements ProvideInputStream {

	private FileSpace space;
	private File file;
	private String path;

	public FileNode(FileSpace space, File file) {
		this.space = space;
		this.file = file;
		this.path = space.findPath(file);
	}

	@Override
	public String toLink() {
		return req.getContextPath() + "/" + space.getName() + "/" + path;
	}

	@Override
	public String getTitle() {
		return file.getName();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public long getLength() {
		return file.length();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String getPath() {
		return file.getAbsolutePath();
	}

	@Override
	public void fillBreadcrumb(LinkedList<FileBrowserNode> breadcrumb) {
		File parentFile = file.getParentFile();
		if (space.isRoot(parentFile)) {
			space.fillBreadcrumb(breadcrumb);
		} else {
			FileNode parentNode = new FileNode(space,parentFile);
			parentNode.fillBreadcrumb(breadcrumb);
		}
		breadcrumb.add(this);
	} 
	
}
