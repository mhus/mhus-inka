package de.mhus.cao.model.fs;

import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.ObjectNotFoundException;
import de.mhus.lib.config.IConfig;

public class IoApplication extends CaoApplication {

	private String path;
	
	public IoApplication(IoConnection fsConnection, IConfig config) throws CaoException {
		super(fsConnection,config);
		this.path = fsConnection.getPath();
	}
	
	@Override
	public CaoElement queryElement(String name, CaoAccess access, String... attributes) throws CaoException {
		if (name.equals(CaoDriver.TREE_DEFAULT)) {
			return new IoElement(this,path);
		}
		if (name.equals(CaoDriver.LIST_DEFAULT)) {
			if (!attributes[0].startsWith(path))
				throw new ObjectNotFoundException(attributes[0]);
			return new IoElement(this, attributes[0]);
		}
		return null;
	}

}
