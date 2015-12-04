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

import java.text.DateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.DateFormatter;

import org.apache.commons.fileupload.FileItem;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Form implements FormIfc {

	protected FormResult result  = new FormResult();
	private   String     formId  = null;
	private   PageIfc    pageIfc = null;
	
	/**
	 * 
	 */
	public Form() {
	}

	/**
	 * Send back an error.
	 * 
	 * @param _msg Error Message
	 */
	public void error( String _msg ) {
		result = new FormResult( _msg );
	}
	
	/**
	 * If the parameter is empty, the method set the error message and throws an exception.
	 * 
	 * @param _parameter Propand
	 * @param _error Error message on check failure
	 * @throws Exception To signalize failure
	 */
	public void checkNotEmpty( String _parameter, String _error ) throws Exception {
		if ( _parameter == null || _parameter.equals( "" ) ) {
			error( _error );
			throw new Exception( _parameter + " is empty" );
		}
	}
	
	/**
	 * If parameter is not an integer, the method set the error message and throws an exception.
	 * 
	 * @param _parameter Propand
	 * @param _error Error message on failure
	 * @return Integer value
	 * @throws Exception To signalize failure
	 */
	public int checkIsInteger( String _parameter, String _error ) throws Exception {
		try {
			return new Integer( _parameter ).intValue();
		} catch ( Exception e ) {
			error( _error );
			throw e;
		}
	}
	
	public void checkIsEmail( String _parameter, String _error ) throws Exception {
		checkNotEmpty( _parameter, _error );
		if ( _parameter.indexOf( '@' ) < 1 ) {
			error( _error );
			throw new Exception( _parameter + " no email" );
		}
		String domain = _parameter.substring( _parameter.indexOf( '@' ) );
		if ( domain.indexOf( '.' ) < 2 ) {
			error( _error );
			throw new Exception( _parameter + " no domain" );
		}
		String ttl = domain.substring( domain.indexOf( '.' ) );
		if ( ttl.length() < 2 ) {
			error( _error );
			throw new Exception( _parameter + " no tld" );
		}

	}

	public Date checkIsDate( String _parameter, String _error ) throws Exception {
		try {
			return DateFormat.getInstance().parse( _parameter );
		} catch ( Exception e ) {
			error( _error );
			throw e;
		}
	}

	public void checkNotEmpty( FileItem _fi, String _error ) throws Exception {
		if ( _fi == null || _fi.getSize() <= 0 ) {
			error( _error );
			throw new Exception( "is empty" );
		}
	}
	
	public void checkContentType( FileItem _fi, String _type, String _error ) throws Exception {
		if ( ! _fi.getContentType().startsWith( _type ) ) {
			error( _error );
			throw new Exception( "wrong content type" );
		}
	}

	/**
	 * Called to verify values ( after calling all setter methods ).
	 * 
	 * @return internal result...
	 * @see de.mhu.shore.ifc.FormIfc#verify()
	 */
	public FormResult verify( HttpServletRequest _request, HttpServletResponse _response ) {
		
		return result;
	}

	/**
	 * Set the formular id (tag: form.id). Set by jsp, before calling init.
	 * 
	 * @see de.mhu.shore.ifc.FormIfc#setFormId(java.lang.String)
	 */
	public void setFormId(String _id) {
		formId = _id;
	}
	
	public String getFormId() {
		return formId;
	}

	/**
	 * Called by jsp after setting formId.
	 * @see de.mhu.shore.ifc.FormIfc#initForm(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void initForm(HttpServletRequest _request, HttpServletResponse _response) {
		
	}

	/* (non-Javadoc)
	 * @see de.mhu.shore.ifc.FormIfc#setPage(de.mhu.shore.ifc.PageIfc)
	 */
	public void setPage(PageIfc _page) {
		pageIfc = _page;
	}
	
	public PageIfc getPage() {
		return pageIfc;
	}
	
	
}
