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

package de.mhu.hair.main;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		System.setProperty("apple.laf.useScreenMenuBar", "true");

		try {
			// check java version
			String javaVersion = System.getProperty("java.version");
			System.out.println("--- Java Version: " + javaVersion);
			if (javaVersion.compareTo("1.5") < 0) {
				String out = "You are running Java version " + javaVersion + ".\n"
				+ "*** Hair requires Java 1.5 or later.";
				if ( !GraphicsEnvironment.isHeadless() ) {
					JOptionPane.showMessageDialog(null, out);
				}
				System.err.println("*** ERROR: " + out);
				System.exit(1);
			}
	
			LocalArgsParser.initialize(args);
			LocalArgsParser ap = LocalArgsParser.getInstance();
			
			if ( ap.isSet("hair_classpath_config")) {
				new ClasspathConfigEditor();
				return;
			}
			
			File libBase = new File(".");
			if (ap.isSet("hair_home"))
				libBase = new File(ap.getValues("hair_home")[0]);
	
			if (ap.isSet("development"))
				libBase = new File("dist");
			String dfcPath = ap.getValue("hair_dfc", libBase.getPath() + "/dfc", 0);
	
			File[] libDir = null;
			File[] libFile = null;
			File[] libPath = null;
			
			String cpPath = ap.isSet("hair_classpath_properties") ? ap.getValue("hair_classpath_properties", 0) : "classpath.properties";
			File cpConfig = new File( cpPath );
			if ( cpConfig.exists() ) {
				System.out.println("classpath.properties: " + cpPath );
				Properties p = new Properties();
				p.load(new FileInputStream(cpConfig));
				
				int cnt = 0;
				LinkedList<File> listDir  = new LinkedList<File>();
				LinkedList<File> listFile = new LinkedList<File>();
				LinkedList<File> listPath = new LinkedList<File>();
				
				listDir.add( new File(libBase, "libs") );
				listDir.add( new File(libBase, "plugins") ); 
				for ( Object okey : p.keySet() ) {
					String key = (String)okey;
					if ( key.endsWith(".dir") ) {
						File f = new File(p.getProperty(key));
						if ( f.exists() ) {
							listDir.add( f );
							cnt++;
						}
					}
					if ( key.endsWith(".file") ) {
						File f = new File(p.getProperty(key));
						if ( f.exists() ) {
							listFile.add( f );
							cnt++;
						}
					}
					if ( key.endsWith(".path") ) {
						File f = new File(p.getProperty(key));
						if ( f.exists() ) {
							listPath.add( f );
							cnt++;
						}
					}
					
				}
				
				if ( cnt == 0 ) {
					new ClasspathConfigEditor();
					return;
				}
				
				libDir = listDir.toArray(new File[0]);
				if ( listFile.size() > 0 )
					libFile = listFile.toArray(new File[0]);
				if ( listPath.size() > 0 )
					libPath = listPath.toArray(new File[0]);
				
			} else
			if ( ap.isSet("dctm_lib_dir") ) {
				String[] strDir = ap.getValues("dctm_lib_dir");
				libDir = new File[strDir.length];
				for ( int i = 0; i < strDir.length; i++ )
					libDir[i] = new File(strDir[i]);
				
				String[] strFile = ap.getValues("dctm_lib_file");
				if (strFile != null && strFile.length != 0 ) {
					libFile = new File[strFile.length];
					for ( int i = 0; i < strFile.length; i++ )
						libFile[i] = new File(strFile[i]);
				}
			} else {
//				File dctmDir = null;
//				
//				if ( dctmDir == null && ap.isSet("documentum_shared" ) ) {
//					dctmDir = new File(ap.getValue("documentum_shared", 0));
//				}
//				
//				if ( dctmDir == null && System.getenv("DOCUMENTUM_SHARED") != null ) {
//					File f = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc.jar");
//					if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM_SHARED"));
//				}
//				
//				if ( dctmDir == null && System.getenv("DOCUMENTUM_SHARED") != null ) {
//					File f = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc/dfc.jar");
//					if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM_SHARED") + "/dfc" );
//				}
//				
//				if ( dctmDir == null && System.getenv("DOCUMENTUM") != null ) {
//					File f = new File(System.getenv("DOCUMENTUM") + "/shared/dfc.jar");
//					if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM") + "/shared" );
//				}
//				
//				if ( dctmDir == null && System.getenv("DOCUMENTUM") != null ) {
//					File f = new File(System.getenv("DOCUMENTUM") + "/shared/dfc/dfc.jar");
//					if ( f.exists() ) dctmDir = new File(System.getenv("DOCUMENTUM") + "/shared/dfc" );
//				}
//				
//				libDir = new File[] { 
//						new File(libBase, "libs"), 
//						new File(libBase, "plugins"), 
//						dctmDir };
				
				new ClasspathConfigEditor();
				return;
				
			}
			
			if (ap.getSize("hair_libs") > 0) {
				String[] ar = ap.getValues("hair_libs");
				libDir = new File[ar.length];
				for (int i = 0; i < ar.length; i++)
					libDir[i] = new File(ar[i]);
			}
	
			LinkedList<URL> urls = new LinkedList<URL>();
	
			File hairJarFile = new File(libBase, "hair.jar");
			if (hairJarFile.exists()) {
				urls.add(hairJarFile.toURL());
			}
			urls.add(new File(dfcPath).toURL());
	
			for (int l = 0; l < libDir.length; l++) {
				if ( libDir[l] != null && libDir[l].exists() && libDir[l].isDirectory()) {
					File[] libList = libDir[l].listFiles();
					for (int i = 0; i < libList.length; i++) {
						if (libList[i].getName().endsWith(".jar")) {
							URL url = libList[i].toURL();
							System.out.println("Found LIB: " + url.toString());
							urls.add(url);
						}
					}
				} else {
					if ( libDir[l] != null ) System.out.println("*** NOT A DIRECTORY "
							+ libDir[l].getAbsolutePath());
				}
			}
	
			if ( libFile != null ) {
				for ( File file : libFile ) {
					if ( file.exists() && file.isFile() ) {
						URL url = file.toURL();
						urls.add(url);
					}
				}
			}
			
			if ( libPath != null ) {
				for ( File file : libPath) {
					if ( file.exists() && file.isDirectory() ) {
						URL url = file.toURL();
						urls.add(url);
					}
				}
			}
			
			if (ap.getSize("hair_jar") > 0) {
				String[] ar = ap.getValues("hair_jar");
				for (int i = 0; i < ar.length; i++) {
					if (ar[i].endsWith(".jar")) {
						URL url = new File(ar[i]).toURL();
						System.out.println("Found LIB: " + url.toString());
						urls.add(url);
					}
				}
			}
	
			URL[] urlArray = (URL[]) urls.toArray(new URL[urls.size()]);
			URLClassLoader cl = new URLClassLoader(urlArray, null);
			Thread.currentThread().setContextClassLoader(cl);
			cl.loadClass("de.mhu.hair.main.MainStarter").getConstructor(
					new Class[] { String[].class, URL[].class }).newInstance(
					new Object[] { args, urlArray });
	
		} catch ( Throwable e ) {
			e.printStackTrace();
			if ( !GraphicsEnvironment.isHeadless() ) {
				JOptionPane.showMessageDialog(null, e);
			}
		}
	}
}
