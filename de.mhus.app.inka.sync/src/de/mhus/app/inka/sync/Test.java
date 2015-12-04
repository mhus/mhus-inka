package de.mhus.app.inka.sync;

import java.util.TreeMap;

import de.mhus.lib.logging.Log;
import de.mhus.lib.util.CompareDir;
import de.mhus.lib.util.CompareDirEntry;

public class Test implements CompareDir.Listener {

	private static Log log = Log.getLog(Test.class);
	
	@Override
	public void start(TreeMap<String, CompareDirEntry> pCurrent,
			TreeMap<String, CompareDirEntry> pLast) {
		log.i("start");
	}

	@Override
	public boolean finish(TreeMap<String, CompareDirEntry> pCurrent,
			TreeMap<String, CompareDirEntry> pLast) {
		log.i("finish");
		return true;
	}

	@Override
	public boolean updateObject(String path, CompareDirEntry curVal,
			CompareDirEntry lastVal) {
		log.i("update",path);
		return true;
	}

	@Override
	public boolean createObject(String path, CompareDirEntry curVal) {
		log.i("create",path);
		return true;
	}

	@Override
	public boolean deleteObject(String path, CompareDirEntry lastVal) {
		log.i("delete",path);
		return true;
	}

}
