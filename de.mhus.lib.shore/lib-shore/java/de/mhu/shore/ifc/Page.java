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

import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhu.shore.ShoreUtil;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class Page extends Hashtable implements PageIfc {

	protected HttpServletRequest  request  = null;
	protected HttpServletResponse response = null;
	/* (non-Javadoc)
	 * @see de.mhu.shore.ifc.PageIfc#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public PageResult init(HttpServletRequest _request, HttpServletResponse _response) {
		request  = _request;
		response = _response;
		return init();
	}
	
	public abstract PageResult init();

	public void set( String _in ) {
		if ( _in == null ) return;
		
		try {
			int p = 0;
			if ( ( p = _in.indexOf( '=' ) ) >= 0 ) {
				String key = _in.substring( 0, p );
				String val = _in.substring( p+1 );
				
				try {
					Method m = this.getClass().getMethod( "set" + ShoreUtil.toParameterName( key ), new Class[] {String.class} );
					m.invoke( this, new Object[] {val} );
				} catch ( Exception e ) {
					this.put( key, val );
				}
				
			}
		} catch ( Exception e ) {}
	}
	
	public Object get( Object _key ) {

		try {
			if ( _key instanceof String ) {
				String key = (String)_key;
				// handle http values
				if ( key.startsWith( "request." )) {
					return request.getParameter( key.substring( 8 ));
				}
			}
		} catch ( Exception e ) {
		}
		
		// wrap get() to getter methods		
		try {
		
			Method m = this.getClass().getMethod( "get" + ShoreUtil.toParameterName( _key.toString() ), new Class[0] );
			return m.invoke( this, new Object[0] );
			
		} catch ( Exception e ) {
			// System.out.println( "get: " + e);
			return super.get( _key );
		}
		
	}

	/* (non-Javadoc)
	 * @see de.mhu.shore.ifc.PageIfc#finish()
	 */
	public void finish() {
		
		
	}



}
