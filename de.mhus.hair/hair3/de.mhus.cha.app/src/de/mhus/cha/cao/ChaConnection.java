package de.mhus.cha.cao;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Hashtable;

import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.ConnectionException;
import de.mhus.cap.app.gui.CapGuiDriver;
import de.mhus.cap.core.Access;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.util.Rfc1738;

public class ChaConnection extends CaoConnection<Access> {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(ChaConnection.class);
	
	private String path;
	private FileOutputStream lock;
	private FileFilter defaultFileFilter;
	private int cnt;
	private Hashtable<String, String> idIndex;

	public ChaConnection(ChaDriver fsDriver, String url) throws CaoException {
		super(fsDriver);
		ChaConfiguration config = new ChaConfiguration();
		config.fromUrl(url);
		this.path = config.getPath();
		
		defaultFileFilter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !pathname.isHidden() && !pathname.getName().startsWith("_") && !pathname.getName().startsWith(".");
			}
		};
		
		//create lock
		try {
			lock = new FileOutputStream( new File(this.path + "/lock") );
		} catch (FileNotFoundException e) {
			throw new ConnectionException("Can't obtain lock");
		}
		
		// load id index
		reloadIdIndex();
		
		ChaApplication app = new ChaApplication(this, path + "/data");
		applications.put(CaoDriver.APP_CONTENT, app);
		
		app = new ChaApplication(this, path + "/meta");
		applications.put("meta", app);
		
		config.setGui(path + "/config");
		CapGuiDriver.createApplicationFor(config,applications);
	}

	private void reloadIdIndex() {
		idIndex = new Hashtable<String, String>();
		
		loadIdIndex( new File(path + "/data") );
		loadIdIndex( new File(path + "/meta") );
		
	}
	
	private void loadIdIndex(File path) {
		for (File item : path.listFiles(getDefaultFileFilter())) {
//			if (idIndex.containsKey(item.getName())) {
//				log.warn("Id already exists " + item.getName() + " '" + idIndex.get(item.getName()) + "' and '" + item.getAbsolutePath() +"'");
//			}
			addIdPath(item.getName(), item.getAbsolutePath());
			loadIdIndex(item);
		}
	}

	/**
	 * Set the absolute folder path for an id. Be carefull with this function, it's only for internal usage.
	 * 
	 * @param id
	 * @param path
	 */
	public void changeIdPath(String id, String path) {
		log.trace("IdPath " + id + " '" + path + "'");
		if ( !idIndex.containsKey(id))
			log.warn("id not defined " + id);
		if (path == null)
			idIndex.remove(id);
		else
			idIndex.put(id, path);
	}
	
	public void addIdPath(String id, String path) {
		log.trace("addIdPath " + id + " '" + path + "'");
		if (path != null) {
			if (idIndex.containsKey(id))
				log.warn("id already defined " + id);
			idIndex.put(id, path);
		}
	}
	
	/**
	 * Returns the folder path to given id.
	 * 
	 * @param id
	 * @return absolute path as string or null if the id is not found
	 */
	public String getIdPath(String id) {
		return idIndex.get(id);
	}
	
	public FileFilter getDefaultFileFilter() {
		return defaultFileFilter;
	}

	public String createUID() {
		cnt++;
		return String.valueOf(System.currentTimeMillis()) + "-" + cnt; //TODO
	}


}
