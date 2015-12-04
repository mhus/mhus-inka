/*
 *  Hair2 License
 *
 *  Copyright (C) 2008 Mike Hummel 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.mhu.hair;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import de.mhu.lib.plugin.JarClassLoader;
import de.mhu.lib.plugin.JarsClassLoader;


public class Build {

	private String name = "Hair";

	private String version = "2.1.2";

	private static Build instance;
	
	
	
	public static synchronized Build getInstance() {
		if ( instance == null ) 
			instance = new Build();
		return instance;
	}
	
	
	private Build() {
		ClassLoader cl = this.getClass().getClassLoader();
		try {
			
			for ( Enumeration<URL> mfs = cl.getResources("/META-INF/MANIFEST.MF");mfs.hasMoreElements();) {
				Manifest mf = new Manifest(mfs.nextElement().openStream());
				Attributes attr = mf.getMainAttributes(); 
				System.out.println(attr);
			}
			 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	
}
