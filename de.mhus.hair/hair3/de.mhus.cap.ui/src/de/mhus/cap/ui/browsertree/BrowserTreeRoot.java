package de.mhus.cap.ui.browsertree;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class BrowserTreeRoot extends BrowserTreeObject {

	private ArrayList<BrowserTreeObject> children = new ArrayList<BrowserTreeObject>();
	private String name;

	@Override
	public String getLabel() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void addChild(BrowserTreeObject child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(BrowserTreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
	
	@Override
	public BrowserTreeObject [] getChildren() {
		return (BrowserTreeObject [])children.toArray(new BrowserTreeObject[children.size()]);
	}
	
	@Override
	public boolean hasChildren() {
		return children.size()>0;
	}

	@Override
	public Image getImage() {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (hasChildren())
		   imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
	

}
