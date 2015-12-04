package de.mhus.aqua.mod.uiapp.wui;

public interface ITreeSource extends ISource {
	
	public ITreeNode[] getRoots();
	
	public ITreeNode[] getChildren(String id);
	
}
