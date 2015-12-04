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

package de.mhu.hair.sf.scripts.db;

import java.util.Vector;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfId;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class DBChangeId implements ScriptIfc {

	private String newId;
	private String oldId = "";

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		try {
			IDfPersistentObject obj = pCon.getPersistentObject(newId);

			pLogger.out.println(">>> Object Id already exists: " + newId);
			return;

		} catch (Exception ex) {
		}

		new DfId(newId);

		if ("".equals(oldId))
			oldId = pTargets[0].getObjectId().toString();

		IDfType type = pTargets[0].getType();
		Vector types = new Vector();
		while (type != null) {
			types.add(type.getName());
			pLogger.out.println("--- Found Type: " + type.getName());
			type = type.getSuperType();
		}

		pLogger.out.println(">>> DMI_OBJECT_TYPE");
		try {
			boolean res = pCon.getSession().apiExec(
					"execsql",
					"UPDATE DMI_OBJECT_TYPE SET R_OBJECT_ID='" + newId
							+ "' WHERE R_OBJECT_ID='" + oldId + "'");
			pLogger.out.println("--- DMI_OBJECT_TYPE: " + res);
		} catch (Exception e) {
			pLogger.out.println("*** DMI_OBJECT_TYPE: " + e);
			return;
		}

		for (int i = 0; i < types.size(); i++) {
			String name = (String) types.elementAt(i);
			pLogger.out.println(">>> " + name);
			name = name.toUpperCase();
			try {
				boolean res = pCon.getSession().apiExec(
						"execsql",
						"UPDATE " + name + "_S SET R_OBJECT_ID='" + newId
								+ "' WHERE R_OBJECT_ID='" + oldId + "'");
				pLogger.out.println("--- Single: " + res);
			} catch (Exception e) {
				pLogger.out.println("*** Single: " + e);
			}
			try {
				boolean res = pCon.getSession().apiExec(
						"execsql",
						"UPDATE " + name + "_R SET R_OBJECT_ID='" + newId
								+ "' WHERE R_OBJECT_ID='" + oldId + "'");
				pLogger.out.println("--- Repeating: " + res);
			} catch (Exception e) {
				pLogger.out.println("*** Repeating: " + e);
			}
		}

		/*
		 * obj.setString( "r_object_id", newId ); obj.save();
		 * 
		 * pLogger.out.println( "DONE!" );
		 */
	}

	public void setNewId(String in) {
		newId = in;
	}

	public void setOldId(String in) {
		oldId = in;
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
