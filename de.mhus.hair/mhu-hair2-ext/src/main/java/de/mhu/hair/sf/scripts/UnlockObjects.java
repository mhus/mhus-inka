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

import java.util.Iterator;
import java.util.Vector;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;

public class UnlockObjects implements ScriptIfc {

	private DMConnection con;
	private ALogger logger;
	private boolean testMode = true;
	private ApiTypes typesApi;

	public void execute(PluginNode node, DMConnection pCon,
			IDfPersistentObject[] targets, ALogger pLogger) throws Exception {

		con = pCon;
		logger = pLogger;
		typesApi = (ApiTypes) node.getSingleApi(ApiTypes.class);

		logger.setMaximum(targets.length);
		for (int i = 0; i < targets.length; i++) {
			logger.setValue(i);
			check(targets[i], i + ":");
		}

	}

	private void check(IDfPersistentObject object, String path)
			throws DfException {

		if (!typesApi.isTypeOf("dm_folder", object.getType()))
			return;

		path = path + "/" + ((IDfSysObject) object).getObjectName();
		logger.out.println("--- Check " + path);

		IDfQuery query = con
				.createQuery("select r_object_id from dm_sysobject where any i_folder_id='"
						+ object.getObjectId() + "'");
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.READ_QUERY);

		Vector unlock = new Vector();
		Vector parse = new Vector();

		while (res.next()) {
			IDfSysObject child = con.getExistingObject(res
					.getString("r_object_id"));
			if (child.isCheckedOut())
				unlock.add(child);

			parse.add(child);
		}
		res.close();

		for (Iterator i = unlock.iterator(); i.hasNext();) {
			IDfSysObject child = (IDfSysObject) i.next();
			if (testMode) {
				logger.out.println("*** Will unlock: "
						+ ObjectTool.getPath(child));
			} else {
				logger.out.println("*** Unlock: " + ObjectTool.getPath(child));
				child.cancelCheckout();
				child.save();
			}

		}

		for (Iterator i = parse.iterator(); i.hasNext();) {
			IDfSysObject child = (IDfSysObject) i.next();
			check(child, path);
		}

	}

	public void setTestMode(boolean in) {
		testMode = in;
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
