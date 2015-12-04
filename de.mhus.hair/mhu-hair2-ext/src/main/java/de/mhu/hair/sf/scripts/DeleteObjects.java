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
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;

public class DeleteObjects implements ScriptIfc {

	private boolean resVersions;
	private boolean resUnlink;
	private boolean resStructure;
	private DMConnection con;
	private ALogger logger;
	private boolean unlockMode;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		con = pCon;
		logger = pLogger;
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] target, ALogger pLogger) throws Exception {
		try {
			for (int i = 0; i < target.length; i++) {
				logger.out.println(">>> Delete " + (i + 1) + "/"
						+ target.length + ": " + target[i].getObjectId());

				delete(target[i], resStructure, resUnlink);

			}

			System.out.println("FINISHED DELETE");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void delete(IDfPersistentObject obj, boolean childs, boolean unlink)
			throws DfException {

		try {
			if (obj instanceof IDfFolder) {
				IDfFolder folder = (IDfFolder) obj;
				IDfCollection list = folder.getContents("r_object_id");
				Vector ids = new Vector();
				while (list.next())
					ids.add(con
							.getExistingObject(list.getString("r_object_id")));
				list.close();
				for (Iterator i = ids.iterator(); i.hasNext();) {
					IDfSysObject child = (IDfSysObject) i.next();
					logger.out.println("--- " + ObjectTool.getPath(child));

					if (unlockMode && child.isCheckedOut()) {
						logger.out.println("+++ Unlock");
						child.cancelCheckout();
					}

					if (unlink && child.getValueCount("i_folder_id") > 1) {
						child.unlink(obj.getObjectId().toString());
						child.save();
					} else {
						delete(child, childs, unlink);
					}
				}
			}
		} catch (Exception e) {
			logger.out.println("*** ERROR: " + e);
			e.printStackTrace();
		}

		try {
			if (resVersions)
				if (obj instanceof IDfSysObject) {
					((IDfSysObject) obj).destroyAllVersions();
					// target[i].destroy();
				} else
					obj.destroy();
			else
				obj.destroy();

		} catch (Exception e) {
			logger.out.println("*** ERROR: " + e);
			e.printStackTrace();
		}
	}

	public void setVersions(boolean in) {
		resVersions = in;
	}

	public void setUnlink(boolean in) {
		resUnlink = in;
	}

	public void setRecursive(boolean in) {
		resStructure = in;
	}

	public void setUnlockMode(boolean in) {
		unlockMode = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
