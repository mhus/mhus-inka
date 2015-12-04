package de.mhus.aqua.core;

import de.mhus.aqua.aaa.User;
import de.mhus.aqua.aaa.UserRights;
import de.mhus.aqua.api.IUser;
import de.mhus.lib.jmx.JmxObject;

public class JmxAqua extends JmxObject implements JmxAquaMBean {

	private Aqua aquaInternal;

	public JmxAqua(Aqua internal) {
		aquaInternal = internal;
	}

	@Override
	public String[] findUserGroups(String userId) throws Exception {
		IUser user = aquaInternal.getUser(userId);
		if (user == null) return null;
		return ((UserRights)user.getRights()).toStringArray();
	}

	
	
}
