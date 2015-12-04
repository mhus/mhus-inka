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

package de.mhu.shore.plugins;

import java.util.Date;
import java.util.Hashtable;

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuString;
import de.mhu.shore.*;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShorePageBorder implements Plugin {

	/* (non-Javadoc)
	 * @see de.mhu.shore.Plugin#execute(java.lang.String, java.util.Hashtable)
	 */
	public void execute( PluginHelper _helper ) throws Exception {

		String[] names = MhuString.split( _helper.getParameter( "names" ), "," );
		
		String   date  = new Date().toString();

		for ( int i = 0; i < names.length; i++ )
			names[i] = names[i].trim();

		String content = _helper.getContent();
		
		// Insert header
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "pageborder/Header.tpl" ) );
			Hashtable h = new Hashtable();
			// insert include names

		
			for ( int i = 0; i < names.length; i++ ) {
				h.put( "name", names[i] );
				temp.loopAppend( "include", h );
			}

			
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "file"   , _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "date"   , date );
			// _helper.setContent( temp.toString().trim() + _helper.getContent() );
			 
			content = temp.toString().trim() + content;
			
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}

		// Insert footer
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "pageborder/Footer.tpl" ) );
			Hashtable h = new Hashtable();
			// insert include names

			for ( int i = 0; i < names.length; i++ ) {
				h.put( "name", names[i] );
				temp.loopAppend( "include", h );
			}

			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "file"   , _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "date"   , date );
			//_helper.setContent( _helper.getContent() + temp.toString().trim() );
			content = content + temp.toString().trim();
					
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
		//System.out.println( content );
		
		_helper.setContent( content );

	}
	
}
