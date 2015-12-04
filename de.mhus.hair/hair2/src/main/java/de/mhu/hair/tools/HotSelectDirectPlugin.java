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

package de.mhu.hair.tools;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiObjectWorkerFactory;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class HotSelectDirectPlugin implements Plugin, ApiObjectWorkerFactory {

	private PluginNode node;
	private DMConnection con;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		con = (DMConnection) node.getSingleApi(DMConnection.class);
		node.addApi(ApiObjectWorkerFactory.class, this);

	}

	public void destroyPlugin() throws Exception {
		node.removeApi(this);
	}

	public String getTitleFor(DMConnection pCon, IDfPersistentObject[] pObj) {
		return "Hot Select Object";
	}

	public boolean canCreateFor(DMConnection pCon, IDfPersistentObject[] pObj) {
		if ( pObj == null || pObj.length == 0 ) return false;
		return true;
	}

	public void createWorker(DMConnection pCon, IDfPersistentObject[] pObj)
			throws Exception {
		if (!canCreateFor(pCon, pObj))
			return;
		ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
				.getApi(ApiObjectHotSelect.class);
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++)
			try {
				list[i].apiObjectDepricated();
			} catch (Throwable t) {
				t.printStackTrace();
			}

		for (int i = 0; i < list.length; i++)
			try {
				list[i].apiObjectHotSelected(con, null, pObj);
			} catch (Throwable t) {
				t.printStackTrace();
			}
	}

}
