package de.mhu.com.morse.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import de.mhu.lib.AFile;
import de.mhu.lib.AThread;
import de.mhu.lib.io.FileListIterator;

public class ObjectFileStore {

	private File root;
	private String rootPath;
	
	private static FileFilter filter = new FileFilter() {

		public boolean accept( String name) {
			return ( ! name.startsWith( "." ) && name.length() == 2 );
		}

		public boolean accept(File pathname) {
			return accept( pathname.getName() );
		}
		
	};

	public ObjectFileStore( File pRoot ) {
		root = pRoot;
		root.mkdirs();
		rootPath = root.getAbsolutePath();
	}
	
	public void clear() {
		File[] list = root.listFiles( filter );
		for ( int i = 0; i < list.length; i++ )
			AFile.deleteDir( list[i] );
	}
	
	public Writer getWriter( String id ) throws MorseException, IOException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		f.getParentFile().mkdirs();
		
		return new OutputStreamWriter( new FileOutputStream( f ), "UTF-8" );
		
	}
	
	public OutputStream getOutputStream( String id ) throws MorseException, IOException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		f.getParentFile().mkdirs();
		
		return new FileOutputStream( f );
		
	}
	
	public boolean exists( String id ) throws MorseException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		return f.exists() && f.isFile();
	}	
	
	public boolean delete( String id ) throws MorseException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		return f.delete();
	}
	
	public boolean touch( String id ) throws MorseException, IOException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		f.getParentFile().mkdirs();
		
		return f.createNewFile();
	}
	
	public Reader getReader( String id ) throws MorseException, IOException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		
		return new InputStreamReader( new FileInputStream( f ), "UTF-8" );
		
	}
	
	public InputStream getInputStream( String id ) throws MorseException, FileNotFoundException {
		ObjectUtil.assetId( id );
		
		File f = getFileForId( root, id );
		
		return new FileInputStream( f );
		
	}
	
	public Iterator<String> idIterator() {
		return new MyIdIterator();
	}
	
	public static File getFileForId( File root, String id) {
		return new File( root, new String( getPathForId( id ) ) );
	}

	public static char[] getPathForId( String id ) {
		return new char[ ] { 
				id.charAt( 0 ), id.charAt( 1 ), '/', 
				id.charAt( 2 ), id.charAt( 3 ), '/', 
				id.charAt( 4 ), id.charAt( 5 ), '/', 
				id.charAt( 6 ), id.charAt( 7 ), '/',
				id.charAt( 8 ), id.charAt( 9 ), '/',
				id.charAt( 10 ), id.charAt( 11 ), '/',
				id.charAt( 12 ), id.charAt( 13 ), '/',
				id.charAt( 14 ), id.charAt( 15 ), '/',
				id.charAt( 16 ), id.charAt( 17 ), '/',
				id.charAt( 18 ), id.charAt( 19 ), '/',
				id.charAt( 19 ), id.charAt( 21 ), '/',
				id.charAt( 22 ), id.charAt( 23 ), '/',
				id.charAt( 24 ), id.charAt( 25 ), '/',
				id.charAt( 26 ), id.charAt( 27 ), '/',
				id.charAt( 28 ), id.charAt( 29 ), '/',
				id.charAt( 30 ), id.charAt( 31 )
				};		
	}
	
	class MyIdIterator implements Iterator<String> {
		
		private FileListIterator parent;
		private String current;

		MyIdIterator() {
			parent = new FileListIterator( root, filter );
			findNext();
		}

		private void findNext() {
			while ( parent.hasNext() ) {
				File currentFile = parent.next();
				if ( currentFile.isFile() ) {
					String s = currentFile.getAbsolutePath().substring( 0, rootPath.length() + 1 );
					if ( s.length() == 47 ) {
						char[] b = new char[ ] {
								s.charAt( 0 ), s.charAt( 1 ),
								s.charAt( 3 ), s.charAt( 4 ),
								s.charAt( 6 ), s.charAt( 7 ),
								s.charAt( 9 ), s.charAt( 10 ),
								s.charAt( 12 ), s.charAt( 13 ),
								s.charAt( 15 ), s.charAt( 16 ),
								s.charAt( 18 ), s.charAt( 19 ),
								s.charAt( 21 ), s.charAt( 22 ),
								s.charAt( 24 ), s.charAt( 25 ),
								s.charAt( 27 ), s.charAt( 28 ),
								s.charAt( 30 ), s.charAt( 31 ),
								s.charAt( 33 ), s.charAt( 34 ),
								s.charAt( 36 ), s.charAt( 37 ),
								s.charAt( 39 ), s.charAt( 40 ),
								s.charAt( 42 ), s.charAt( 43 ),
								s.charAt( 45 ), s.charAt( 46 )
						};
						current = new String( b );
						return;	
					}
				}
			}
			current = null;
		}

		public boolean hasNext() {
			return ( current != null );
		}

		public String next() {
			String next = current;
			findNext();
			return next;
		}

		public void remove() {
			
		}
		
	}

	public boolean lock(String id) throws IOException {
		File f = new File( getFileForId( root, id ), ".lock" );
		int cnt = 0;
		while ( ! f.createNewFile() ) {
			AThread.sleep( 100 );
			cnt++;
			if ( cnt > 10 )
				return false;
		}
		return true;
	}

	public void unlock(String id) {
		File f = new File( getFileForId( root, id ), ".lock" );
		if ( f.exists() )
			f.delete();
	}

	public File getRoot() {
		return root;
	}

	public String getRelativePath(String id) {
		return new String( getPathForId( id ) );
	}
	
	
}
