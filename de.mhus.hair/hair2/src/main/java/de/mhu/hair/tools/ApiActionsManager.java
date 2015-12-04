package de.mhu.hair.tools;

import org.w3c.dom.Element;

import de.mhu.hair.api.Api;

public interface ApiActionsManager extends Api {

	public void addAction( String id, String clazz, Element config ) throws Exception;
	public void removeAction( String id ) throws Exception;
	
	public void addMenu() throws Exception;
	public void removeMenu() throws Exception;
	
	public void addToolbar() throws Exception;
	public void removeToolbar() throws Exception;
	
	public void addPopup() throws Exception;
	public void removePopup() throws Exception;
	
}
