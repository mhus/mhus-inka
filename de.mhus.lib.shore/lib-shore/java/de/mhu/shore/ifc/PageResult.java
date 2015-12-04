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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author hummel
 * 

 * 
 */
public class PageResult {

	public static final int ERROR   = 1;
	public static final int READY   = 2;
	public static final int FORWARD = 3;
	
	private boolean   redirect = false;
	private int       type     = READY;
	private URL       url      = null;
	private String    errorMsg = null;

	public PageResult( int _type ) {
		type = _type;
	}
	
	public PageResult( int _type, String _target ) {
		this( _type );
		if ( type == ERROR ) {
			errorMsg = _target;
		}
		if ( type == FORWARD )
				try {
					url = new URL( _target );
				} catch (MalformedURLException e) {
					type = ERROR;
				}
	}
	
	public PageResult( int _type, boolean _redirect, String _target ) {
		this( _type, _target );
		setRedirect( _redirect );
	}

	public void setRedirect( boolean _redirect ) {
		redirect = _redirect;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
	
	public boolean isError() {
		return ( type == ERROR || ( type == FORWARD && url == null) );
	}
		
	public boolean goAhead() {
		return ( type == READY );
	}
	
	public boolean goForward() {
		return ( type == FORWARD );
	}
	
	public boolean isRedirect() {
		return redirect;
	}
	
	public URL getForwardURL() {
		return url;
	}
	
}
