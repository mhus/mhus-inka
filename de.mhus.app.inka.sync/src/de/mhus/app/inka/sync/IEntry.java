package de.mhus.app.inka.sync;

import java.io.InputStream;
import java.io.OutputStream;

import de.mhus.lib.util.CompareDirEntry;

public abstract class IEntry extends CompareDirEntry {

	@Override
	public boolean compareWithEntry(CompareDirEntry other) {
		if (other instanceof IEntry) {
			IEntry entry = (IEntry) other;
			if (isFolder() && entry.isFolder()) return true;
			
			if (isFolder() || entry.isFolder()) return false;
			
			return getSize() == entry.getSize() && getModified() == entry.getModified();
			
		}
		return false;
	}

	public abstract long getSize();
	
	public abstract long getModified();

	// public abstract OutputStream getOutputStream() throws Exception;
	
	public abstract InputStream getInputStream() throws Exception;
	
}
