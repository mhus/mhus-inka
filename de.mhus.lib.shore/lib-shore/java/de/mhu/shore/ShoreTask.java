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

package de.mhu.shore;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShoreTask extends Task {

	private File config  = null;
	private File dest    = null;
	private FileSet fset = null;

	public void setConfigDir( File _config) {
		config = _config;
	}
	
	public void setDestination( File _destination ) {
		dest =  _destination;
	}
	
	public void addFileset(FileSet set)
	{
		fset = set;
	}
	
	public void execute() throws BuildException {
		
		// check parameter
		if ( config == null ) throw new BuildException( "config directory (configDir) not set" );
		if ( dest   == null ) throw new BuildException( "destination directory (destinationDir) not set" );
		if ( fset   == null ) throw new BuildException( "no input files set (fileset)" );
		
		Shore shore = new Shore( config );
				
		try {
	
			DirectoryScanner scanner = fset.getDirectoryScanner( getProject() );
			String[] files = scanner.getIncludedFiles();
			String base    = scanner.getBasedir().getAbsolutePath();
			for ( int i = 0; i < files.length; i++ ) {
				//-- System.out.println( base + "/" + files[i] );
				shore.clean();
				shore.parse( new File( base + "/" + files[i] ) );
				shore.execute();
				shore.write( new File( dest, files[i] ) );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new BuildException( e );
		}
		
	}
	
}
