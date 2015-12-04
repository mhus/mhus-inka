package de.mhus.app.inka.sync;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;

import de.mhus.lib.MFile;
import de.mhus.lib.io.FileListIterator;
import de.mhus.lib.logging.Log;
import de.mhus.lib.util.CompareDirEntry;

public class DirFile extends IFile implements FileFilter {

	private static Log log = Log.getLog(DirFile.class);
	private File dir;
	private String dirPrefix;
	
	@Override
	protected void doInit() {
		dir = new File(config.getExtracted("directory"));
		dirPrefix = dir.getAbsolutePath() + "/";
	}

	@Override
	public boolean createObject(String arg0, CompareDirEntry arg1) {
		log.t("create",arg0);
		
		if (arg1.isFolder()) return true;
		
		try {
			File f = new File(dirPrefix + arg0);
			
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
			OutputStream os = new FileOutputStream(dirPrefix + arg0);
			InputStream is = ((IEntry)arg1).getInputStream();
			MFile.copyFile(is, os);
			is.close();
			os.close();
			f.setLastModified(((IEntry)arg1).getModified());
		} catch (Exception e) {
			log.e(arg0,e);
			return false;
		}
		return true;
	}

	@Override
	public boolean deleteObject(String arg0, CompareDirEntry arg1) {
		log.t("delete",arg0);
		return new File(dirPrefix + arg0).delete();
	}

	@Override
	public boolean finish(TreeMap<String, CompareDirEntry> arg0,
			TreeMap<String, CompareDirEntry> arg1) {
		
		return true;
	}

	@Override
	public void start(TreeMap<String, CompareDirEntry> arg0,
			TreeMap<String, CompareDirEntry> arg1) {
		
	}

	@Override
	public boolean updateObject(String arg0, CompareDirEntry arg1,
			CompareDirEntry arg2) {
		log.t("update",arg0);

		if (arg1.isFolder()) return true;

		try {
			File f = new File(dirPrefix + arg0);
			OutputStream os = new FileOutputStream(dirPrefix + arg0);
			InputStream is = ((IEntry)arg1).getInputStream();
			MFile.copyFile(is, os);
			is.close();
			os.close();
			f.setLastModified(((IEntry)arg1).getModified());
		} catch (Exception e) {
			log.e(arg0,e);
			return false;
		}
		return true;
	}

	@Override
	public TreeMap<String, CompareDirEntry> getStructure() {
		
		TreeMap<String, CompareDirEntry> out = new TreeMap<String, CompareDirEntry>();
		
		FileListIterator iterator = new FileListIterator(dir, this );

		for (File f : iterator) {
			String path = f.getAbsolutePath();
			if (path.startsWith(dirPrefix)) path = path.substring(dirPrefix.length());
			out.put(path,new MyEntry(f,path));
		}
		return out;
	}

	private static class MyEntry extends IEntry {

		private File file;
		private String path;

		public MyEntry(File f, String path) {
			this.file = f;
			this.path = path;
		}

		@Override
		public boolean isFolder() {
			return file.isDirectory();
		}

		@Override
		public long getSize() {
			return file.length();
		}

		@Override
		public long getModified() {
			return file.lastModified();
		}

//		@Override
//		public OutputStream getOutputStream() throws Exception {
//			return new FileOutputStream(file);
//		}

		@Override
		public InputStream getInputStream() throws Exception {
			return new FileInputStream(file);
		}
		
	}

	@Override
	public boolean accept(File pathname) {
		// include exclude rules etc ....
		if (pathname.isDirectory()) return true;
		return true;
	}
	
}
