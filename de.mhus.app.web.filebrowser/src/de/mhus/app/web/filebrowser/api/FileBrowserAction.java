package de.mhus.app.web.filebrowser.api;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public interface FileBrowserAction {

	boolean canExecute(FileBrowserNode node);
	
	boolean doExecute(FileBrowserNode node, ServletContext context, HttpServletResponse response) throws ServletException, IOException;

	String getTitle();
}
