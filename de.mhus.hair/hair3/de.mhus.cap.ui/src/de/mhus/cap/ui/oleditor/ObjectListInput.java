package de.mhus.cap.ui.oleditor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoList;

public class ObjectListInput implements IEditorInput {

	private CaoList list;
	private CaoElement element;
	private String name;
	private String toolTipText;
	private String title;

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name == null ? "?" : name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return toolTipText == null ? "" : toolTipText;
	}

	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public void setList(CaoList list) {
		this.list = list;
	}
	
	public CaoList getList() {
		return list;
	}

	public CaoElement getElement() {
		return element;
	}

	public void setElement(CaoElement element) {
		this.element = element;
	}

	public void setTitle(String in) {
		title = in;
	}
	
	public String getTitle() {
		return title;
	}

}
