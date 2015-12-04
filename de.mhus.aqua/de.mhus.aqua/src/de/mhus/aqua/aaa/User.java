package de.mhus.aqua.aaa;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaDriver;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public class User implements IUser {
	
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
		.getLog(User.class);

	private String id;
	private UserRights rights;
	private String name;
	private JsonConfig config;
	private boolean isAdmin;

	public User(String id) throws Exception {
		this.id = id;
		relaod();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IUserRights getRights() {
		return rights;
	}
	
	@Override
	public boolean isAdmin() {
		return isAdmin;
	}
	
	@Override
	public IConfig getConfig() {
		return config;
	}
	
		
		public void relaod() throws Exception {
			
			rights = Activator.getAqua().getUserRights(getId());
			
			AquaConnection con = Activator.getAqua().getCaoConnection();
			CaoApplication<AquaSession> app = con.getApplication(Activator.getAqua().getRootSession(), AquaDriver.APP_AAA);
			CaoElement<AquaSession> element = app.queryTree(AquaConnection.TREE_USER, Activator.getAqua().getRootSession(), id);
			name = element.getString("name");
			try {
				config = new JsonConfig(element.getString("config"));
			} catch (IOException e) {
				log.t(id,e.toString());
				config = new JsonConfig();
			}
			isAdmin = element.getBoolean("is_admin",false);
			
		}

		public User getUser() {
			return User.this;
		}
		
		public String toString() {
			return "[" + getId() + "," + name + "]";
		}
		

}
