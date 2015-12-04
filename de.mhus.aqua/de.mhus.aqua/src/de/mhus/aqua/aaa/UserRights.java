package de.mhus.aqua.aaa;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.api.IUserRights;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaDriver;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public class UserRights implements IUserRights {


	protected Set<String> groups = new HashSet<String>();
	protected Set<String> roles = new HashSet<String>();
	protected String userId;

	public UserRights(AquaConnection con, String userId) throws Exception {
		
		this.userId = userId;
		
		CaoApplication<AquaSession> app = con.getApplication(Activator.getAqua().getRootSession(),AquaDriver.APP_AAA);
		CaoList<AquaSession> list = app.queryList(AquaConnection.LIST_RIGHTS, Activator.getAqua().getRootSession(), userId);
		
		for (CaoElement<AquaSession> res : list.getElements()) {
			String name = res.getString("name");
			int rg = res.getInt("rg",0);
			if (rg == 0) {
				roles.add(name);
			} else
			if (rg == 1) {
				groups.add(name);
			}
		}
		
	}
	
	@Override
	public boolean containsRole(String role) {
		return roles.contains(role);
	}
	
	@Override
	public boolean containsGroup(String group) {
		return groups.contains(group);
	}
	
	@Override
	public boolean contains(int rg, String rgName) {
		switch (rg) {
		case ROLE: return containsRole(rgName);
		case GROUP: return containsGroup(rgName);
		case USER: return rgName.equals(userId);
		case ROLE_PATTERN:
			for (String role : roles)
				if (MString.compareFsLikePattern(role, rgName)) return true;
			return false;
		case GROUP_PATTERN:
			for (String group : groups)
				if (MString.compareFsLikePattern(group, rgName)) return true;
			return false;
		case USER_PATTERN:
			return MString.compareFsLikePattern(userId, rgName);
		default: return false;
		}
	}
	
	public String[] toStringArray() {
		LinkedList<String> out = new LinkedList<String>();
		for (String group : groups) {
			out.add("Group:" + group);
		}
		for (String role : roles) {
			out.add("Role:" + role);
		}
		return out.toArray(new String[out.size()]);
	}
	
}
