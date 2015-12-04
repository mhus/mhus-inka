/*
 *  Copyright (C) 2002-2004 Mike Hummel
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package de.mhu.lib.apps.jadjar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;

import de.mhu.lib.AFile;
import de.mhu.lib.AThread;
import de.mhu.lib.log.AL;
import de.mhu.lib.sf.AScriptPanel;
import de.mhus.lib.sf.IMScript;
import de.mhus.lib.sf.ScriptLogger;

public class JadJar implements IMScript {

	private int lastLine;
	private LinkedList<String> stack = new LinkedList<String>();
	private int minLine;
	private int maxLine;
	private Hashtable<Integer,String> lineCache = new Hashtable<Integer,String>();
	private boolean isStatic;
	private boolean debug = false;
	private AL log;
	private FileObject tmpDir;
	private FileObject jarFile;
	private FileObject jadExe;
	private long timeout = 10000;
	private boolean cleanup = true;
	private boolean order = true;
	private boolean isBeginning;

	public static void main( String[] args ) throws IOException {
		// // AScriptControl.showFrame( JadJar.class.getName() );

		//		AScriptPanel.showFrame( JadJar.class.getName() ).setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		Properties p = new Properties();
		try {
			FileInputStream fis = new FileInputStream("jadjar.properties");
			p.load(fis);
			fis.close();
		} catch (Exception e) {}
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Temp Directory");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setSelectedFile(new File(p.getProperty("tmp", ".")));
		if ( chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File tmp = chooser.getSelectedFile();
		if (tmp==null) return;
		p.setProperty("tmp", tmp.getAbsolutePath());
		
		chooser.setSelectedFile(new File(p.getProperty("jad", ".")));
		chooser.setDialogTitle("jad Binary");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if ( chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File jad = chooser.getSelectedFile();
		if (jad==null) return;
		p.setProperty("jad", jad.getAbsolutePath());
		
		chooser.setSelectedFile(new File(p.getProperty("jar", ".")));
		chooser.setDialogTitle("JAR Package");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if ( chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File jar = chooser.getSelectedFile();
		if (jar==null) return;
		p.setProperty("jar", jar.getAbsolutePath());
		
		try {
			FileOutputStream fos = new FileOutputStream("jadjar.properties");
			p.store(fos, "");
			fos.close();
		} catch (Exception e) {}
		
		
		
		JadJar inst = new JadJar();
		inst.setJadExe(jad.getAbsolutePath());
		inst.setDebug(true);
		inst.setCleanup(true);
		inst.setJarFile(jar.getAbsolutePath());
		inst.setTmpDir(tmp.getAbsolutePath());

		inst.log = new AL(JadJar.class);
		inst.run();
		
	}
	
	public void run() throws IOException {
		
//		String jadExe = "c:/dev/bin/jad.exe";
//		String tmpDir = "c:/Temp";
//		String jarFile = "c:/Temp/ucf-server-impl.jar";
		
		File tmp = new File( tmpDir + "/jad" );
		tmp.mkdirs();
		
		
		// remove all
		AFile.deleteDir( tmp );
		
		tmp.mkdirs();
		
		// unpack jar
		ZipFile jar = new ZipFile( jarFile.getURL().getFile() );
		
		
		for ( Enumeration<?> i = jar.entries(); i.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry)i.nextElement();
			if ( ! entry.isDirectory() ) {
				log.info( ">>> Unzip " + entry.getName() );
				String path = entry.getName();
				String name = null;
				int pos = path.lastIndexOf( '/' );
				if ( pos < 0 ) {
					name = path;
					path = "";
				} else {
					name = path.substring( pos+1 );
					path = path.substring( 0, pos );
				}
				File outDir = new File( tmp, path );
				outDir.mkdirs();
				File outFile = new File( outDir, name );
				
				InputStream is = jar.getInputStream( entry );
				AFile.copyFile( is, new FileOutputStream( outFile ) );
			}
			
		}
		
		
		
		// decompile
		
		for ( Enumeration<?> i = jar.entries(); i.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry)i.nextElement();
			String name = entry.getName();
			
			/*
			if ( ! name.equals( "com/documentum/djcb/IDfLogger.class") )
				continue;
			*/
			
			if ( ! entry.isDirectory() && name.endsWith( ".class" ) && name.indexOf( "$" ) < 0 ) {
				clear();
				log.info( ">>> JAD " + name );
				
				decompile(tmp ,name);
				
			}
		}
		
		
		// create src zip
		String jarName =  jarFile.getURL().getFile();
		ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( jarName.substring( 0, jarName.length() - 4 ) + "_jad_src.zip" ) );
		
		for ( Enumeration<?> i = jar.entries(); i.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry)i.nextElement();
			String name = entry.getName();
			String dst = null;
			if ( ! entry.isDirectory() && name.endsWith( ".class" ) && name.indexOf( "$" ) < 0 ) {
				dst = name.substring( 0, name.length() - 5 ) + "java";
			} else
			if ( ! entry.isDirectory() &&  !name.endsWith( ".class" ) ) {
				dst = name;
			} else
			if ( entry.isDirectory() ) {
				
			}
			
			if ( dst != null ) {
				log.info( ">>> ZIP " + dst );
				ZipEntry en = new ZipEntry( dst );
				zip.putNextEntry( en );
				AFile.copyFile( new FileInputStream( new File( tmp, dst  ) ) , zip );
				zip.closeEntry();
			}
			
			
		}
		
		zip.close();
		
		if ( cleanup ) {
			AFile.deleteDir( tmp );
		}
		
	}
	
	private void decompile(File tmp, String name) throws IOException {
		
		if ( debug ) log.debug( "Excute: " + jadExe + " -nonlb -8 -& -p -lnc -dead " + tmp.getAbsolutePath() + '/' + name );
		Process p = Runtime.getRuntime().exec( new String[] { jadExe.getURL().getFile(), "-nonlb", "-8", "-&", "-p", "-lnc", "-dead", tmp.getAbsolutePath() + '/' + name } );
		
		MyInputStream inStream = new MyInputStream( p.getInputStream() );
		BufferedReader in = new BufferedReader( new InputStreamReader( inStream ));
		BufferedReader inErr = new BufferedReader( new InputStreamReader(p.getErrorStream()));

		// PrintWriter out = new PrintWriter(System.out);
		// PrintWriter err = new PrintWriter(System.err);
		
		String text = "";
		long timeoutTrigger= System.currentTimeMillis() + timeout ;
		while ( ( text = in.readLine() ) != null ) {
			/*
			out.println(text); 
			out.flush();
			*/
			
			if ( order ) {
				if ( text.startsWith( "//" ) )
					inStream.eof = false;
				foundLine( text );
				timeoutTrigger= System.currentTimeMillis() + timeout;
			

				while ( inErr.ready() )  {
					text = inErr.readLine();
					if ( text != null )  {
						if ( text.length() != 0 ) {
							addStack( "/*ERR " + text + "*/" );
							timeoutTrigger= System.currentTimeMillis() + timeout;
						}
					}
				}

				/*
				if ( inStream.isEOF() ) {
					log.warn( "EOF" );
					p.destroy();
					break;
				}
				*/
				if ( timeout != 0 && System.currentTimeMillis() > timeoutTrigger ) {
					log.warn( "Timeout" );
					p.destroy();
					break;
				}
			} else {
				addLine( false, maxLine+1, text );
			}
			
        }

		// dummy last line to flush stack
		setLine( maxLine + stack.size() + 1, "" );
		
		// write java file
		
		File dst = new File( tmp.getAbsolutePath() + '/' + name.substring( 0, name.length() - 5 ) + "java" );
		FileOutputStream os = new FileOutputStream( dst );
		PrintWriter outFile = new PrintWriter( os );
		for ( int nr = minLine; nr <= 0; nr++ ) {
			text = (String)lineCache.get( new Integer( nr ) );
			if ( text == null ) {
			} else {
				outFile.print( text );
			}
			outFile.flush();					
		}
		for ( int nr = 1; nr <= maxLine; nr++ ) {
			text = (String)lineCache.get( new Integer( nr ) );
			if ( text == null ) {
				outFile.println();
			} else {
				outFile.println( text );
			}
			outFile.flush();
		}
		os.close();
	}

	private void foundLine(String text) {
		
		// System.out.println( "#" + text );
		// find linenr
		int linenr = -1;
		if ( text.startsWith( "/*" ) ) {
			int pos = text.indexOf("*/", 3 );
			if ( pos > 0 ) {
				try {
					linenr = Integer.parseInt( text.substring( 2, pos ).trim() );
				}  catch ( NumberFormatException nfe ) {
					log.warn( "NFE: " + text.substring( 2, pos ).trim() );
					linenr = -1;
				}
				text = "        " + text.substring( pos+2 );
			}
		}
		
		if ( debug ) log.debug( linenr + ": " + text );
		
		// ignore //
		if ( linenr == -1 && text.startsWith( "//" ) ) {
			// ignore it
		} else
		// the beginning is different
		if ( isBeginning ) {
			if ( text.trim().length()!=0 ) { // ignore empty
				lastLine++;
				setLine(lastLine,text);
				if ( text.trim().startsWith("public") && text.indexOf(" class " ) > 0 )
					isBeginning = false;
			}
		} else
		// go into static mode
		if ( linenr == -1 && text.trim().equals( "static  {" ) ) {
			isStatic = true;
			addStack( text );
		} else
		// append closed blocks
		if ( linenr == -1 && text.trim().equals( "}" ) ) {
			isStatic = false;
			lastLine++;
			lastLine+=stack.size();
			if ( text.equals("}") ) { // end of class
				lastLine = maxLine + stack.size() + 1;
			}
			setLine( lastLine, text );
		} else
		// ignore empty lines
		if ( text.trim().length() == 0 ) {
			// ignore it
		} else
		// write static with line direct
		if ( linenr != -1 && isStatic ) {
			addLine( false, linenr, text );
		} else
		// append unknown to stack
		if ( isStatic || linenr == -1 ) {
			addStack( text );
		} else 
		// default is to write the line
		{
			lastLine = linenr;
			setLine( linenr, text );
		}
		
	}

	private void addStack(String text) {
		if ( debug ) log.debug( "# STACK" );
		stack.add( text );
	}

	private void setLine(int line, String text) {
		if ( stack.size() != 0 ) {
			int cnt = stack.size()+1;
			for ( Iterator<String> i = stack.iterator(); i.hasNext(); ) {
				addLine( true, line - cnt, i.next() );
				cnt--;
			}
			stack.clear();
		}

		addLine( false, line, text );
		
	}

	private void addLine(boolean fromStack, int line, String text ) {

		if ( debug ) log.debug( ( fromStack ? "~" : "#" ) + " L " + line + ' ' + text );
		
		minLine = Math.min( minLine, line );
		maxLine = Math.max( maxLine, line );

		String l = (String)lineCache.get( new Integer( line ) );
		if ( l == null ) {
			lineCache.put( new Integer( line ), text );
		} else {
			lineCache.put( new Integer( line ), l + " /**/ " + text.trim() );
		}
		
	}
	
	private void clear() {
		minLine = 0;
		maxLine = 0;
		stack.clear();
		lastLine = 0;
		lineCache.clear();
		isStatic = false;
		isBeginning = true;
	}

	public void execute(ScriptLogger logger) throws Exception {
		run();
	}

	public void finish(ScriptLogger logger) throws Exception {
		
	}

	public void init(ScriptLogger logger) throws Exception {
		log = logger;
	}

	public String getTmpDir() {
		return tmpDir.getName().getBaseName();
	}

	public void setTmpDir(String tmpDir) throws FileSystemException {
		this.tmpDir = VFS.getManager().resolveFile(tmpDir);
		if ( this.tmpDir.getType() == FileType.FILE)
			this.tmpDir = this.tmpDir.getParent();
	}

	public String getJarFile() {
		return jarFile.getName().getBaseName();
	}

	public void setJarFile(String jarFile) throws FileSystemException {
		this.jarFile = VFS.getManager().resolveFile(jarFile);
	}

	public String getJadExe() {
		return jadExe.getName().getBaseName();
	}

	public void setJadExe(String jadExe) throws FileSystemException {
		this.jadExe = VFS.getManager().resolveFile(jadExe);
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	private static class MyInputStream extends InputStream {
		
		public InputStream src = null;
		private boolean eof = false;
		
		public MyInputStream( InputStream src ) {
			this.src = src;
		}

		public boolean isEOF() {
			return eof;
		}
		
		public int available() throws IOException {
			return src.available();
		}

		public void close() throws IOException {
			src.close();
		}

		public boolean equals(Object obj) {
			return src.equals(obj);
		}

		public int hashCode() {
			return src.hashCode();
		}

		public void mark(int readlimit) {
			src.mark(readlimit);
		}

		public boolean markSupported() {
			return src.markSupported();
		}

		public int read() throws IOException {
			int ret = src.read();
			if ( ret < 0 ) eof  = true;
			return ret;
		}

		public void reset() throws IOException {
			src.reset();
		}

		public long skip(long n) throws IOException {
			return src.skip(n);
		}

		public String toString() {
			return src.toString();
		}
		
	}

	public String getTimeout() {
		return String.valueOf( timeout / 1000 );
	}

	public void setTimeout(String timeout) {
		this.timeout = Long.parseLong( timeout ) * 1000;
	}

	public boolean isCleanup() {
		return cleanup;
	}

	public void setCleanup(boolean cleanup) {
		this.cleanup = cleanup;
	}
	
	public void setOrder( boolean in ) {
		order  = in;
	}
	
	public boolean isOrder() {
		return order;
	}
	
}
