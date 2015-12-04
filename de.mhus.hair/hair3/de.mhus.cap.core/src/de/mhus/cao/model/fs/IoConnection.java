package de.mhus.cao.model.fs;

import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoUser;
import de.mhus.lib.cao.util.EmptyApplication;

public class IoConnection extends CaoConnection {

	private String path;

	public IoConnection(IoDriver fsDriver, IoConfiguration form) throws CaoException {
		super(fsDriver,form.getConfig());
		setUser( new CaoUser(new EmptyApplication(this),System.getProperty("user.name"),true,true) );
		this.path = form.getPath();
//		FsApplication app = new FsApplication(this, path);
//		applications.put(CaoDriver.APP_CONTENT, app);
//		CapGuiDriver.createApplicationFor(config,applications);
	}

	public String getPath() {
		return path;
	}

	@Override
	protected void doDisconnect() {
		path = null;
	}

	@Override
	public boolean isValid() {
		return path != null;
	}

}
