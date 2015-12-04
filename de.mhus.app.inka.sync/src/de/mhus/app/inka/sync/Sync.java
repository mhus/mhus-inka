package de.mhus.app.inka.sync;

import java.util.TreeMap;

import de.mhus.lib.MSingleton;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.Log;
import de.mhus.lib.util.CompareDir;
import de.mhus.lib.util.CompareDirEntry;

public class Sync {

	private static Log log = Log.getLog(Sync.class);
	
	private Comparator comparator;
	private IFile source1;
	private IFile source2;
	private IFile destination;

	private boolean test;

	public Sync(IConfig config) throws Exception {
		
		test = config.getBoolean("test", false);
		
		IConfig cComparator = config.getConfig("compare");
		comparator = (Comparator) MSingleton.instance().getActivator().createObject(cComparator.getExtracted("type", Comparator.class.getCanonicalName()) );
		comparator.initialize(this, cComparator);

		IConfig[] cSource = config.getConfigBundle("source");

		source1 = (IFile) MSingleton.instance().getActivator().createObject(cSource[0].getExtracted("type", DirFile.class.getCanonicalName()) );
		source1.initialize(this, cSource[0]);
		
		source2 = (IFile) MSingleton.instance().getActivator().createObject(cSource[1].getExtracted("type", DirFile.class.getCanonicalName()) );
		source2.initialize(this, cSource[1]);
		
		IConfig cDestination = config.getConfig("destination");
		destination = new DirFile();
		if (cDestination == null || !cDestination.isProperty("type"))
			destination = source2;
		else {
			destination = (IFile) MSingleton.instance().getActivator().createObject(cDestination.getExtracted("type", DirFile.class.getCanonicalName()) );
			destination.initialize(this, cDestination);
		}
	}
	
	public void action() {
		
		log.i("Load Source 1");
		TreeMap<String, CompareDirEntry> map1 = source1.getStructure();
		log.d("1 Entries ", map1.size());
		if (log.isTrace()) for (String e : map1.keySet()) log.t("1 ",e);

		log.i("Load Source 2");
		TreeMap<String, CompareDirEntry> map2 = source2.getStructure();
		log.d("2 Entries ", map2.size());
		if (log.isTrace()) for (String e : map2.keySet()) log.t("2 ",e);

		CompareDir.Listener listener = destination;
		if (test) listener = new Test();
		comparator.compare(map1, map2, listener);
		
	}

}
