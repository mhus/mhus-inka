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

import de.mhu.shore.Plugin;
import de.mhu.shore.PluginHelper;

/**
 * @author hummel
 * 

 * 
 */
public class Util implements Plugin {

	public void execute(PluginHelper _helper) throws Exception {
		
		String action = _helper.getParameter( "action" );
		if ( action == null ) return;
		
		if ( action.equals( "clean" )) {
				System.out.println( "Util: Clean Content" );
			_helper.setContent( "" );
		}
		
		if ( action.equals( "execute" ) ) {
			
			String clazz = _helper.getParameter( "class" );
			_helper.setContent( _helper.getContent() + "<% " + clazz + " obj=new " + clazz + "( request, response );%>" );
			
			
		}
		
	}
	
}
