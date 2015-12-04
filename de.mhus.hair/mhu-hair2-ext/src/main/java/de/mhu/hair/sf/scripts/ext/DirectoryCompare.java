package de.mhu.hair.sf.scripts.ext;

import java.io.File;
import java.util.TreeMap;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.sf.scripts.ext.CompareStructureDiff.Listener;

public class DirectoryCompare implements ScriptIfc {

	private File left;
	private File right;
	private boolean checkTimestamp;
	private boolean checkSize;
	private ALogger log;
	private Listener listener;
	private CompareStructureDiff comp;

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
	}

	public void execute(PluginNode node, DMConnection con,
			IDfPersistentObject[] targets, ALogger logger) throws Exception {
	}

	public void initialize(PluginNode node, DMConnection con, ALogger logger)
			throws Exception {
		
		log = logger;
		
		if ( ! left.isDirectory() ) {
			logger.out.println( "*** left is no directory" );
			return;
		}
		
		if ( ! right.isDirectory() ) {
			logger.out.println( "*** right is no directory" );
			return;
		}
		
		listener = new CompareStructureDiff.Listener<File>() {

			public void createObject(String path, File curVal) {
				log.out.println( "<<< " + curVal.getAbsolutePath() );
			}

			public void deleteObject(String path, File lastVal) {
				log.out.println( ">>> " + lastVal.getAbsolutePath() );				
			}

			public boolean equalsMapValues(File curVal, File oldVal) {
				if ( curVal.isDirectory() && oldVal.isDirectory() ) {
					compare(curVal, oldVal);
					return true;
				}
				if ( curVal.isDirectory() && ! oldVal.isDirectory() ) return false;
				if ( !curVal.isDirectory() && oldVal.isDirectory() ) return false;
				
				if ( checkSize ) {
					if ( curVal.length() != oldVal.length() ) return false;
				}
				
				if ( checkTimestamp ) {
					if ( curVal.lastModified() != oldVal.lastModified() ) return false;
				}
				
				return true;
			}

			public void finish(TreeMap<String, File> current,
					TreeMap<String, File> last) {
				
			}

			public void start(TreeMap<String, File> current,
					TreeMap<String, File> last) {
			}

			public void updateObject(String path, File curVal, File lastVal) {
				log.out.println( "--- " + curVal.getAbsolutePath() );				
			}
			
		};
		
		comp = new CompareStructureDiff<File>();
		
		compare(left, right );
		
	}

	private void compare(File l, File r) {
		
		File[] ll = l.listFiles();
		File[] lr = r.listFiles();
		
		TreeMap<String, File> sll = new TreeMap<String, File>();
		TreeMap<String, File> slr = new TreeMap<String, File>();
		
		for ( File i : ll ) sll.put(i.getName(), i);
		for ( File i : lr ) slr.put(i.getName(), i);
		
		comp.compare(sll, slr, listener );
		
	}

	public void setLeftDir(String in) {
		left = new File(in);
	}
	
	public void setRightDir(String in) {
		right = new File(in);
	}
	
	public void setCheckTimestamp(boolean in) {
		checkTimestamp = in;
	}
	
	public void setCheckSize(boolean in) {
		checkSize = in;
	}
	
}
