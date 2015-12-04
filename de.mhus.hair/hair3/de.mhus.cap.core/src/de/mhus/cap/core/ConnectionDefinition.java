package de.mhus.cap.core;

import java.util.UUID;

import org.eclipse.swt.graphics.Image;

import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.util.MutableElement;
import de.mhus.lib.cao.util.NoneApplication;
import de.mhus.lib.config.IConfig;

public class ConnectionDefinition extends MutableElement {

	public static final String TITLE = "title";
	public static final String SERVICE = "service";
	public static final String URL = "url";
	public static final String ID = "id";
	private Image imageOpen;
	private Image imageClose;
	
	public ConnectionDefinition(IConfig config) throws MException {
		super(NoneApplication.getInstance());
		
		setTitle(config.getExtracted("title"));
		setService(config.getExtracted("service"));
		setUrl(config.getExtracted("url"));
		String id  = config.getExtracted("id");
		if (id == null)
			id = UUID.randomUUID().toString();
		setId(id);
		
		imageOpen = Activator.getImageDescriptor("/icons/database_go.png").createImage();
		imageClose = Activator.getImageDescriptor("/icons/database.png").createImage();

	}

	public void setService(String service) throws MException {
		setString(SERVICE, service);
	}

	public String getUrl() throws CaoException {
		return getString(URL);
	}

	public String getService() throws CaoException {
		return getString(SERVICE);
	}

	public void setUrl(String url) throws MException {
		setString(URL,url);
	}

	public String getTitle() throws CaoException {
		return getString(TITLE);
	}

	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other instanceof ConnectionDefinition) {
			try {
				return getId().equals( ((ConnectionDefinition)other).getId() );
			} catch (CaoException e) {
			}
		}
		return super.equals(other);
	}

	public void setTitle(String title) throws MException {
		setString(TITLE,title);
	}

	public void fill(IConfig config) throws MException {
		config.setString("title", getTitle());
		config.setString("url", getUrl());
		config.setString("service", getService());
		config.setString("id", getId());
		
	}

	public Image getImage(boolean connected) {
		return connected ? imageOpen : imageClose;
	}
}
