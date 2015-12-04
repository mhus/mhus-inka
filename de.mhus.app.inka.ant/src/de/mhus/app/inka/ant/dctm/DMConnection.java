/*
 *  Hair2 License
 *
 *  Copyright (C) 2008 Mike Hummel 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.mhus.app.inka.ant.dctm;

import java.util.Hashtable;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.DfPrincipalException;
import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfPreferences;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import de.mhus.lib.MPassword;
import de.mhus.lib.MSql;

public class DMConnection {

	public static IDfClientX clientx = new DfClientX();
	// private static Hashtable connections = new Hashtable();

	private IDfSessionManager sMgr = null;
	private IDfSession session = null;
	private IDfClient client = null;
	private String sessionId;
	private String userName;
	private String host;
	private int port;

	private IDfLoginInfo loginInfoObj;

	private String docBase;

	public DMConnection(String pUser, String pPass, String pDocBase)
			throws DfException {

		try {

			userName = pUser;
			docBase = pDocBase;
			String userPass = MPassword.decode(pPass);

			client = clientx.getLocalClient();
			IDfTypedObject config = client.getClientConfig();
			host = config.getString("primary_host");
			port = config.getInt("primary_port");

			// create a Session Manager object
			sMgr = client.newSessionManager();

			// create an IDfLoginInfo object named loginInfoObj
			loginInfoObj = clientx.getLoginInfo();
			loginInfoObj.setUser(userName);
			loginInfoObj.setPassword(userPass);
			loginInfoObj.setDomain(null);

			// bind the Session Manager to the login info
			synchronized (sMgr) {
				sMgr.setIdentity(pDocBase, loginInfoObj);
				session = sMgr.newSession(pDocBase);
			}
			// System.out.println( "Session ID: " + session.getSessionId() );

			sessionId = session.getSessionId();
			// connections.put(sessionId, this);

		} catch (DfException e) {
			e.printStackTrace();
			disconnect();
			throw e;
		}
	}

	protected DMConnection(DMConnection parent, boolean transaction)
			throws DfException {
		this.sMgr = parent.sMgr;
		this.host = parent.host;
		this.docBase = parent.docBase;
		this.loginInfoObj = parent.loginInfoObj;
		this.client = parent.client;
		synchronized (sMgr) {
			if (transaction)
				sMgr.beginTransaction();
			this.session = sMgr.getSession(docBase);
		}
		sessionId = session.getSessionId();
	}

	public void disconnect() {
		if (isConnected()) {
			// connections.remove(sessionId);
			sMgr.release(session);
			sMgr = null;
			session = null;
		}
	}

	public boolean isConnected() {
		return (sMgr != null && session != null);
	}

	public IDfSession getSession() {
		return session;
	}

	public IDfCollection getChilds(IDfId id) throws Exception {
		IDfPersistentObject obj = session.getObject(id);
		if (!(obj instanceof IDfFolder)) {
			// throw new Exception( "Object is not folder or cabinet: " + id );
			return null;
		}
		IDfFolder folder = (IDfFolder) obj;
		if (folder == null) {
			throw new Exception(
					"Folder or cabinet does not exist in the Docbase: " + id);
		}

		// Get the folder contents
		return folder.getContents("object_name,r_object_type,r_object_id");
	}

	public IDfPersistentObject getExistingObject(String strObjId) throws DfException {
		if (strObjId == null)
			return null;
		IDfId myId = clientx.getId(strObjId);
		return getExistingObject(myId);
	}

	public IDfPersistentObject getExistingObject(IDfId myId) throws DfException {
		if (myId == null)
			return null;
		IDfPersistentObject sysObj = session.getObject(myId);
		if (sysObj == null) {
			// System.out.println("--- Object can not be found: " + myId );
			return null;
		} else {
			// System.out.println("--- Object named '" + sysObj.getObjectName()
			// + "' was found.");
			return sysObj;
		}
	}

	public IDfPersistentObject getExistingObject(String path, String lang)
			throws DfException {
		int pos = path.lastIndexOf('/');

		String dql = null;
		if (pos < 0) {
			dql = "SELECT r_object_id FROM dm_cabinet WHERE object_name='"
					+ MSql.escape(path) + " AND language_code='"
					+ MSql.escape(lang) + "'";
		} else {
			String folder = path.substring(0, pos);
			String name = path.substring(pos + 1);
			dql = "SELECT r_object_id FROM dm_sysobject WHERE FOLDER('"
					+ MSql.escape(folder) + "') AND object_name='"
					+ MSql.escape(name) + "' AND language_code='"
					+ MSql.escape(lang) + "'";
		}
		// System.out.println( dql );
		IDfQuery query = createQuery(dql);
		IDfCollection res = query.execute(getSession(), IDfQuery.EXEC_QUERY);
		IDfId objectId = null;
		if (res.next())
			objectId = res.getId("r_object_id");
		res.close();

		return getExistingObject(objectId);

	}

	public IDfPersistentObject getPersistentObject(String strObjId)
			throws DfException {
		IDfId myId = clientx.getId(strObjId);
		return getPersistentObject(myId);
	}

	public IDfPersistentObject getPersistentObject(IDfId myId)
			throws DfException {
		IDfPersistentObject sysObj = (IDfPersistentObject) session
				.getObject(myId);
		if (sysObj == null) {
			System.out.println("--- Object can not be found: " + myId);
			return null;
		} else {
			// System.out.println("--- Object named '" + sysObj.getObjectId() +
			// "' was found.");
			return sysObj;
		}
	}

	public IDfQuery createQuery(String dql) {
		IDfQuery query = clientx.getQuery();
		if (dql != null)
			query.setDQL(dql);
		return query;
	}

	public IDfCollection executeQuery(String dql) throws DfException {
		IDfQuery query = clientx.getQuery();
		query.setDQL(dql);
		return query.execute(session, IDfQuery.READ_QUERY);
	}

	// public static DMConnection findConnection(String dctmSession) {
	// return (DMConnection) connections.get(dctmSession);
	// }

	public String getUserName() {
		return userName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDocbaseName() throws DfException {
		return getSession().getDocbaseName() + '@' + getHost() + ':'
				+ getPort();
	}

	public static String escape(String in) {
		if (in == null)
			return null;
		if (in.indexOf('\'') < 0)
			return in;
		return in.replaceAll("'", "''");
	}

	public boolean isTransaction() throws DfException {
		return session.isTransactionActive();
	}

	public void abordTransaction() throws DfException {
		sMgr.abortTransaction();
	}

	public void commitTransaction() throws DfException {
		sMgr.commitTransaction();
	}

	public DMConnection cloneConnection(boolean beginTransaction)
			throws DfException {
		// System.out.println("!!! Create DMConnection clone !!!");
		return new DMConnection(this, beginTransaction);
	}

	public void finalize() {
		disconnect();
	}
	
}
