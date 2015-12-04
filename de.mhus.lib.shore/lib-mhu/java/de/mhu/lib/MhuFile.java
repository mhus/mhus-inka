/*
 *  mhu-lib Generic Application Framework
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

package de.mhu.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MhuFile extends File {

	/**
	 * @param arg0
	 */
	public MhuFile(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MhuFile(String arg0, String arg1) {
		super(arg0, arg1);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MhuFile(File arg0, String arg1) {
		super(arg0, arg1);
		
	}

	public MhuFile(File arg0 ) {
		super(arg0.getAbsolutePath() );
		
	}

	/**
	 * @param arg0
	 */
	public MhuFile(URI arg0) {
		super(arg0);
		
	}

	public void write( String _content ) throws IOException {
		
		FileOutputStream fos = new FileOutputStream( this );
		fos.write( _content.getBytes() );
		fos.close();
		
	}

	public void write( InputStream _if ) throws IOException {
		
		FileOutputStream fos = new FileOutputStream( this );
		long max = Runtime.getRuntime().freeMemory();
		if ( max > 50000 ) max = 50000;
		
		byte[] buffer = new byte[ (int)max ];
		
		while ( _if.available() > 0 ) {
			int stored = _if.read( buffer );
			fos.write( buffer, 0, stored );
		}
		
		fos.close();
		
	}

	public String read() throws IOException {
		
		FileInputStream fis = new FileInputStream( this );
		byte[] buffer = new byte[ fis.available() ];
		fis.read( buffer );
		fis.close();
		
		return new String( buffer );
	}
	
	public void read( OutputStream _os ) throws IOException {

		FileInputStream fis = new FileInputStream( this );
		long max = Runtime.getRuntime().freeMemory();
		if ( max > 50000 ) max = 50000;
		
		byte[] buffer = new byte[ (int)max ];
		
		while ( fis.available() > 0 ) {
			int stored = fis.read( buffer );
			_os.write( buffer, 0, stored );
		}
		
		fis.close();
		
	}

	public long getSize() {
		return this.length();
	}

}
