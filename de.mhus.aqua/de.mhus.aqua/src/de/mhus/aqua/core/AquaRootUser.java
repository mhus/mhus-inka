package de.mhus.aqua.core;

import de.mhus.aqua.aaa.User;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.api.IUserRights;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;

public class AquaRootUser implements IUser {

	private IConfig config = new HashConfig();
	private IUserRights rights = new IUserRights() {
		
		@Override
		public boolean containsRole(String role) {
			return true;
		}
		
		@Override
		public boolean containsGroup(String group) {
			return true;
		}
		
		@Override
		public boolean contains(int rg, String rgName) {
			return true;
		}
	};
	
	@Override
	public String getId() {
		return "root";
	}

	@Override
	public IUserRights getRights() {
		return rights;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public IConfig getConfig() {
		return config;
	}


}
