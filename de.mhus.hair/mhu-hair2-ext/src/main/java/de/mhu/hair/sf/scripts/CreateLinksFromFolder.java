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
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.AString;

public class CreateLinksFromFolder implements ScriptIfc {

	private boolean testMode = true;
	private String sourceFolder;
	private String destFolder;
	private DMConnection con;
	private ALogger logger;
	private IDfFolder src;
	private String[] ignoreList = new String[0];
	private boolean overwrite = false;

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] targets, ALogger pLogger) throws Exception {

		con = pCon;
		logger = pLogger;

		src = (IDfFolder) con.getSession().getFolderByPath(sourceFolder);

		logger.setMaximum(targets.length);
		for (int i = 0; i < targets.length; i++) {
			logger.setValue(i);
			check(targets[i], ObjectTool.getPath(targets[i]));
		}

	}

	private void check(IDfPersistentObject object, String path) {

		logger.out.println("--- Check " + path + "/" + destFolder);

		try {
			IDfFolder dest = (IDfFolder) con.getSession().getFolderByPath(
					path + "/" + destFolder);

			IDfQuery query = con
					.createQuery("select r_object_id from dm_sysobject where any i_folder_id='"
							+ src.getObjectId() + "'");
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);

			while (res.next()) {

				IDfSysObject wantLink = con.getExistingObject(res
						.getString("r_object_id"));
				String wantLinkName = wantLink.getObjectName();

				// ignore ?
				boolean ignore = false;
				for (int i = 0; i < ignoreList.length; i++)
					ignore = AString.compareSQLLikePattern(wantLinkName,
							ignoreList[i]);

				if (!ignore) {
					IDfFolder exists = (IDfFolder) con.getSession()
							.getFolderByPath(
									path + "/" + destFolder + "/"
											+ wantLinkName);
					if (exists != null) {

						logger.out
								.println("+++ WARNING: folder already exists");
						if (!overwrite)
							ignore = true;
					}
				}

				if (!ignore) {
					if (testMode) {
						logger.out.println("*** Will link " + wantLinkName);
					} else {
						logger.out.println("*** Link " + wantLinkName);
						wantLink.link(dest.getObjectId().getId());
						wantLink.save();
					}
				}
			}
			res.close();

		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.err.println("*** ERROR: " + e);
		}

	}

	public void setTestMode(boolean in) {
		testMode = in;
	}

	public void setSourceFolder(String in) {
		sourceFolder = in;
	}

	public void setDestFolder(String in) {
		destFolder = in;
	}

	public void setIgnoreList(String in) {
		ignoreList = in.split(";");
	}

	public void setOverwrite(boolean in) {
		overwrite = in;
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		// TODO Auto-generated method stub

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}
}
