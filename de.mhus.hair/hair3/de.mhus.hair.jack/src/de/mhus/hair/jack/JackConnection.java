package de.mhus.hair.jack;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;

import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoUser;
import de.mhus.lib.cao.ConnectionException;
import de.mhus.lib.cao.util.EmptyApplication;

public class JackConnection extends CaoConnection {

	private Session session;
	private JackConfiguration config;

	public JackConnection(JackDriver driver, JackConfiguration form) throws CaoException {
		super(driver,form.getConfig());
		setUser(new CaoUser(new EmptyApplication(this),form.getUser(),true,true));
		config = form;
		try {
			Repository repo = JcrUtils.getRepository(form.getUri());
			if (MString.isEmptyTrim(form.getWorkspace()))
				session = repo.login(new SimpleCredentials(form.getUser(), form.getPassword().toCharArray()));
			else
				session = repo.login(new SimpleCredentials(form.getUser(), form.getPassword().toCharArray()), form.getWorkspace());
		} catch (Throwable t) {
			throw new ConnectionException(t);
		}
		
//		applications.put(CaoDriver.APP_CONTENT, new JackApplication(this));
//		CapGuiDriver.createApplicationFor(config,applications);
	}

	public Session getSession() {
		return session;
	}

	@Override
	protected void doDisconnect() {
		session.logout();
		session = null;
	}

	@Override
	public boolean isValid() {
		return session != null && session.isLive();
	}

	public JackConfiguration getConfiguration() {
		return config;
	}
	
}
