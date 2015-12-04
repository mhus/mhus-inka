package de.mhus.app.web.filebrowser.plug.fs;

import java.io.File;
import java.util.LinkedList;
import java.util.Properties;


import de.mhus.app.web.filebrowser.api.FileBrowserNode;
import de.mhus.app.web.filebrowser.api.Space;
import de.mhus.lib.MString;

public class FileSpace extends Space {

	public String name;
	public File path;
	public String title;
	
	public FileSpace(File file, String title) {
		this.path = file;
		this.title = title;
	}

	public FileSpace() {
	}

	public void fillList(String subPath, LinkedList<FileBrowserNode> list) {
		File file = new File (path + "/" + subPath);
		File[] l = file.listFiles();
		if (l == null) return;
		for (File sub : l) {
			if (!sub.isHidden() && !sub.getName().startsWith(".")) {
				list.add(new FileNode(this,sub));
			}
		}
	}

	public String findPath(File file) {
		String ret = file.getAbsolutePath().substring( (int)path.getAbsolutePath().length() );
		ret = ret.replace('\\', '/');
		if (ret.startsWith("/"))
			ret = ret.substring(1);
		return ret;
	}

	@Override
	public String toLink() {
		return req.getContextPath() + "/" + getName();
	}

	public boolean isRoot(File file) {
		return file.equals(path);
	}

	public FileBrowserNode getNode(String subPath) {
		if (MString.isEmpty(subPath)) 
			return this;
		File file = new File (path + "/" + subPath);
		if (file.exists())
			return new FileNode(this,file);
		return null;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public String getPath() {
		return path.getPath();
	}

	@Override
	public void fillBreadcrumb(LinkedList<FileBrowserNode> breadcrumb) {
		breadcrumb.add(this);
	}

	@Override
	public void initialize(String spaceName, Properties spaceProps) {
		this.name = spaceName;
		this.title = spaceProps.getProperty("title", name);
		this.path = new File(spaceProps.getProperty("path"));
	}

	public void setPath(File file) {
		this.path = file;
	}
	
	public String toString() {
		return name + "@" + path;
	}
	
}
