package de.mhus.app.inka.ant.dctm;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;

import de.mhus.lib.MPassword;
import de.mhus.lib.MString;

public class Connect extends Task {

	private static Map<String, DMConnection> sessions = new HashMap<String, DMConnection>();
	
	private String docbase;
	private String user;
	private String password;
	private String session;

	public void execute() throws BuildException {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			DMConnection con = new DMConnection(user, password, docbase);
			sessions.put(session, con);
		} catch (Exception e) {
			throw new BuildException(session + ":" + e.getMessage(),e);
		}
	}
	
	public static DMConnection getSession(String name) {
		return sessions.get(name);
	}
	
	public void setSession(String in) {
		session = in;
	}
	
	public void setDocbase(String in) {
		docbase = in;
	}
	
	public void setUser(String in) {
		user = in;
	}
	
	public void setPassword(String in) {
		password = MPassword.decode(in);
	}
	
	public void setDfc(String in) {
		if (MString.isEmpty(in))
			System.getProperties().remove("dfc.properties.file");
		else
			System.setProperty("dfc.properties.file", in);
	}

	public static IDfPersistentObject findObject(DMConnection con, String tar, String language) throws DfException {
		IDfPersistentObject obj = null;
		if (tar.startsWith("/")) {
			obj = con.getExistingObject( tar, language);
		} else
		if (tar.length() == 16)
			obj = con.getExistingObject(tar);
		else
			obj = con.getSession().getObjectByQualification(tar);
		return obj;
	}
	
}
