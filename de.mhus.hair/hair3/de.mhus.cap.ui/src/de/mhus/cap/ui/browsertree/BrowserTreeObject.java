package de.mhus.cap.ui.browsertree;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;

public abstract class BrowserTreeObject implements IAdaptable {

	private BrowserTreeObject parent;

	public abstract String getLabel();

	public void setParent(BrowserTreeObject parent) {
		this.parent = parent;
	}
	public BrowserTreeObject getParent() {
		return parent;
	}
	
	public String toString() {
		return getLabel();
	}
	
	public abstract BrowserTreeObject [] getChildren();

	public abstract boolean hasChildren();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public abstract Image getImage();

}
