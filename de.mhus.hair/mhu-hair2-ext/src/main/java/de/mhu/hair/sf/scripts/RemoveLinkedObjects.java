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

public class RemoveLinkedObjects implements ScriptIfc {

	private DMConnection con;
	private ALogger logger;
	private boolean testMode = true;
	private ApiTypes typesApi;
	private boolean massMode;
	private boolean unlockMode;

	public void execute(PluginNode node, DMConnection pCon,
			IDfPersistentObject[] targets, ALogger pLogger) throws Exception {

		con = pCon;
		logger = pLogger;
		typesApi = (ApiTypes) node.getSingleApi(ApiTypes.class);

		logger.setMaximum(targets.length);
		for (int i = 0; i < targets.length; i++) {
			logger.setValue(i);
			check(ObjectTool.getPath(targets[i]) + '/', targets[i], i + ":");
		}

	}

	private void check(String rootPath, IDfPersistentObject object, String path)
			throws DfException {

		if (!typesApi.isTypeOf("dm_folder", object.getType()))
			return;

		path = path + "/" + ((IDfSysObject) object).getObjectName();
		logger.out.println("--- Check " + path);

		IDfQuery query = con
				.createQuery("select r_object_id from dm_sysobject (all) where any i_folder_id='"
						+ object.getObjectId() + "'");
		IDfCollection res = query
				.execute(con.getSession(), IDfQuery.READ_QUERY);

		Vector unlink = new Vector();
		Vector parse = new Vector();

		while (res.next()) {
			IDfSysObject child = con.getExistingObject(res
					.getString("r_object_id"));
			if (child.getValueCount("i_folder_id") > 1)
				unlink.add(child);
			else
				parse.add(child);
		}
		res.close();

		for (Iterator i = unlink.iterator(); i.hasNext();) {

			try {
				IDfSysObject child = (IDfSysObject) i.next();

				if (unlockMode && child.isCheckedOut()) {
					if (testMode) {
						logger.out.println("*** Will unlock: "
								+ ObjectTool.getPath(child));
					} else {
						logger.out.println("--- Unlock: "
								+ ObjectTool.getPath(child));
						child.cancelCheckout();
					}
				}

				if (testMode) {
					logger.out.println("*** Will unlink: "
							+ ObjectTool.getPath(child));
				} else {

					if (massMode) {
						logger.out.println("--- Unlink: "
								+ ObjectTool.getPath(child));
						Vector unlinkList = new Vector();
						int cnt = child.getValueCount("i_folder_id");
						for (int f = 0; f < cnt; f++) {
							IDfPersistentObject folder = con
									.getPersistentObject(child
											.getRepeatingString("i_folder_id",
													f));
							int cnt2 = folder.getValueCount("r_folder_path");
							for (int f2 = 0; f2 < cnt2; f2++) {
								String f2Path = folder.getRepeatingString(
										"r_folder_path", f2);
								logger.out.println("1:" + f2Path);
								logger.out.println("2:" + rootPath);
								if (f2Path.startsWith(rootPath)) {
									unlinkList.add(folder.getObjectId()
											.toString());
									f2 = cnt2;
								}
							}
						}

						for (int f = 0; f < unlinkList.size(); f++) {
							String f2Path = (String) unlinkList.elementAt(f);
							logger.out.println("*** " + f2Path);
							child.unlink(f2Path);
						}

					} else {
						logger.out.println("*** Unlink: "
								+ ObjectTool.getPath(child));
						child.unlink(object.getObjectId().toString());
					}

					child.save();
				}
			} catch (Exception e) {
				logger.out.println("*** ERROR: " + e);
				e.printStackTrace();
			}

		}

		for (Iterator i = parse.iterator(); i.hasNext();) {
			IDfSysObject child = (IDfSysObject) i.next();
			check(rootPath, child, path);
		}

	}

	public void setTestMode(boolean in) {
		testMode = in;
	}

	public void setMassMode(boolean in) {
		massMode = in;
	}

	public void setUnlockMode(boolean in) {
		unlockMode = in;
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
