package de.mhus.aqua.mod.uiapp.wui;

public interface ITreeNode {

	public String getTitle();
	public String getIconClass();
	public boolean hasChildren();
	public boolean isExpanded();
	public String getId();
	
}
