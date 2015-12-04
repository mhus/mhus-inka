package de.mhus.aqua.api;

import de.mhus.lib.config.IConfig;

public interface IUser {

	String getId();

	IUserRights getRights();

	boolean isAdmin();

	IConfig getConfig();

}
