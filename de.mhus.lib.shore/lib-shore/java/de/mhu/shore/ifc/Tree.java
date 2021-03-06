/*
 *  mhu-shore JSP creation Framework
 *    shore help you to create JSP files using MVC (Model-View-Controll) design.
 *    mhu-shore is a ant task and generate JSP files from - nearly - simple
 *    HTML files. A special Servlet or active server component is not needed.
 * 
 *  Copyright (C) 2003  Mike Hummel
 *  
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *  
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  WWW: http://code.mikehummel.de/
 *  E-mail: code@mikehummel.de
 */

package de.mhu.shore.ifc;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhu.shore.ifc.tree.Document;

/**
 * @author hummel
 * 

 * 
 */
public abstract class Tree {

	private String name      = null;
	private String imagePath = null;
	private PageIfc pageIfc  = null;
	private Hashtable options = new Hashtable();
	
	public void setName( String _name ) {
		name = _name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setImagePath( String _path ) {
		imagePath = _path;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public void setPage(PageIfc _page) {
		pageIfc = _page;
	}
	
	public PageIfc getPage() {
		return pageIfc;
	}
	
	public Hashtable getOptions() {
		return options;
	}
	
	public abstract Document getDocument( HttpServletRequest _request, HttpServletResponse _response);

}
