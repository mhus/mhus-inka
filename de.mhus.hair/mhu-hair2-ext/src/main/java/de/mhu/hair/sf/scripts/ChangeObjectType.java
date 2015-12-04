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

package de.mhu.hair.sf.scripts;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;

public class ChangeObjectType implements ScriptIfc {

	private String superType;
	private String newType;
	private ALogger logger;
	private DMConnection con;
	private boolean cancelCheckout;
	private boolean unImmutable;
	private boolean recoverImmutable;
	private boolean recover;
	private boolean deleteRecovered;
	private boolean useTransaction;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		logger = pLogger;
		con = pCon;

		for (int i = 0; i < pTargets.length; i++) {
			logger.out.println(">>> " + ObjectTool.getPath(pTargets[i]));
			try {
				changeType((IDfSysObject) pTargets[i]);
			} catch (Exception e) {
				logger.out.println("*** ERROR: " + e);
				e.printStackTrace();
			}
		}

	}

	private void changeType(IDfSysObject object) throws Exception {

		String strObjId = object.getObjectId().getId();
		IDfSessionManager sesMan = con.getSession().getSessionManager();
		IDfSession transSession = null;
		boolean error = false;

		if (useTransaction) {
			if (!sesMan.isTransactionActive()) {
				sesMan.beginTransaction();
				transSession = sesMan.newSession(con.getSession()
						.getDocbaseName());
			} else {
				throw new Exception("ERROR: Cannot start Transaction.");
			}
		} else {
			transSession = con.getSession();
		}

		try {
			boolean immutable = false;
			boolean deleted = false;

			IDfSysObject obj = (IDfSysObject) transSession.getObject(new DfId(
					strObjId));

			// restore if needed
			if (recover && obj.isDeleted()) {
				if (!transSession
						.apiExec(
								"execsql",
								"update dm_sysobject_s set i_is_deleted=0, i_vstamp=i_vstamp+1 where r_object_id='"
										+ obj.getObjectId() + "'")) {
					throw new Exception("Can't restore: " + obj.getObjectId());
				}
				obj.fetch(null);
			}

			// disable immutable flag
			// otherwise no change will effected
			if (unImmutable && obj.isImmutable()) {
				immutable = true;
				obj.setBoolean("r_immutable_flag", false);
				obj.save();
			}

			if (cancelCheckout && obj.isCheckedOut()) {
				transSession.apiExec("unlock", obj.getString("r_object_id"));
				obj.fetch(null);
			}
			// change to type to super type
			String dqlChangeQuery = "CHANGE " + obj.getTypeName()
					+ " (all) OBJECT TO " + superType;
			dqlChangeQuery += " WHERE r_object_id='" + strObjId + "'";

			IDfQuery dql = con.createQuery(dqlChangeQuery);

			// if test mode activated nothing happens
			IDfCollection collaction = dql.execute(transSession,
					IDfQuery.EXEC_QUERY);
			collaction.close();

			// change super type to needed type
			dqlChangeQuery = "CHANGE " + superType + " (all) OBJECT TO "
					+ newType;
			dqlChangeQuery += " WHERE r_object_id='" + strObjId + "'";
			dql.setDQL(dqlChangeQuery);

			// if test mode activated nothing happens
			collaction = dql.execute(transSession, IDfQuery.EXEC_QUERY);
			collaction.close();

			if (recoverImmutable && immutable) {
				obj.setBoolean("r_immutable_flag", true);
				immutable = false;
				obj.save();
			}

			if (deleteRecovered && deleted) {
				transSession
						.apiExec(
								"execsql",
								"update dm_sysobject_s set i_is_deleted=0, i_vstamp=i_vstamp+1 where r_object_id='"
										+ obj.getObjectId() + "'");
				obj.fetch(null);
			}

		} catch (Exception e) {
			logger.out.println("*** ERROR: " + e);
			e.printStackTrace();
			error = true;

		}

		// close session

		try {
			if (useTransaction) {
				if (error)
					sesMan.abortTransaction();
				else
					sesMan.commitTransaction();

				if (transSession != null)
					sesMan.release(transSession);
			}
			transSession = null;
		} catch (DfException e) {
			if (useTransaction) {
				sesMan.abortTransaction();
				if (transSession != null)
					sesMan.release(transSession);
			}
			transSession = null;
			throw new DfException(
					"ERROR: Commit failed. Last ObjectID procceded: "
							+ strObjId);
		}

	}

	public void setSuperType(String in) {
		superType = in;
	}

	public void setNewType(String in) {
		newType = in;
	}

	public void setCancelcheckout(boolean in) {
		cancelCheckout = in;
	}

	public void setUnimmutable(boolean in) {
		unImmutable = in;
	}

	public void setRecoverimmutable(boolean in) {
		recoverImmutable = in;
	}

	public void setRecover(boolean in) {
		recover = in;
	}

	public void setDelete(boolean in) {
		deleteRecovered = in;
	}

	public void setTransaction(boolean in) {
		useTransaction = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
