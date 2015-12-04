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

package de.mhu.hair.plugin.ui;

import java.awt.BorderLayout;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class TypeAttrPlugin extends AbstractHotSelectMenu implements Plugin,
		ApiObjectHotSelect {

	private PluginNode node;
	private PluginConfig config;
	private TypeAttrPanel attrs;
	private DMConnection con;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		config = pConfig;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);

		initUI();

		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(this,
				pConfig.getNode());

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			initHotSelectMenu(pNode, pConfig, this);
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			showObj(obj.getType());
		}

	}

	private void initUI() {
		setLayout(new BorderLayout());

		attrs = new TypeAttrPanel(null);
		add(attrs, BorderLayout.CENTER);

	}

	protected void showObj(IDfType type) {

		try {
			attrs.show(type);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroyPlugin() throws Exception {
		destroyHotSelectMenu();
	}

	public void apiObjectDepricated0() {

		attrs.getLabel().setIcon(
				de.mhu.lib.resources.ImageProvider.getInstance().getIcon(
						"mhu:action:throbber"));
	}

	public void apiObjectHotSelected0(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject pObj[])
			throws Exception {
		if (pObj == null || pObj.length != 1)
			attrs.clear();
		else
			showObj(pObj[0].getType());
		attrs.getLabel().setIcon(null);
	}

}
