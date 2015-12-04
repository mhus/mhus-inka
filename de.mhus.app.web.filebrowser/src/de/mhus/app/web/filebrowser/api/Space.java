package de.mhus.app.web.filebrowser.api;

import java.util.LinkedList;
import java.util.Properties;

public abstract class Space extends FileBrowserNode {

	public abstract void fillList(String subPath, LinkedList<FileBrowserNode> list);

	public abstract FileBrowserNode getNode(String subPath);

	public abstract void initialize(String spaceName, Properties spaceProps);

}
