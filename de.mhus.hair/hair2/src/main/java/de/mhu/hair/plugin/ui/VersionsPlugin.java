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
import java.awt.event.MouseEvent;
import java.util.Timer;

import javax.swing.JComponent;
import javax.swing.JTable;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.ui.DocumentPlugin.ListListener;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.res.img.LUF;

public class VersionsPlugin extends AbstractHotSelectMenu implements Plugin,
		ApiObjectHotSelect {
	private PluginNode node;
	private DMConnection con;
	private DMList list;

	// select r_object_id,object_name,r_version_label,r_modifier,r_modify_date
	// from dm_sysobject (all) where i_chronicle_id='090115e580b0142f' order by
	// r_modify_date
	public VersionsPlugin() {
		super();
	}

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		node = pNode;

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
			showObj(con, obj);
		}

	}

	private void showObj(DMConnection con2, IDfPersistentObject obj) {

		if (obj == null || !(obj instanceof IDfSysObject)) {
			list.clear();
			return;
		}

		try {
			String dql = "select r_object_id,object_name,r_version_label,r_modifier,r_modify_date,r_creation_date from dm_sysobject (deleted) "
					+ "where i_chronicle_id='"
					+ obj.getString("i_chronicle_id")
					+ "' order by r_creation_date desc";

			IDfQuery query = con2.createQuery(dql);
			IDfCollection res = query.execute(con2.getSession(),
					IDfQuery.EXEC_QUERY);
			list.show(con2, res);
			res.close();
			list.getLabel().setIcon(LUF.DOT_GREEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initUI() {
		setLayout(new BorderLayout());
		list = new DMList("Versions", new DMListUserFieldObjListener(node), new String[] { "object_name","r_modifier","r_modify_date","r_creation_date","r_version_label" }, "r_object_id");
		list.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); 
		DocumentTableCellRenderer renderer = new DocumentTableCellRenderer(list);
		renderer.setColVersion(4);
		list.getTable().setDefaultRenderer(Object.class, renderer);
		add(list, BorderLayout.CENTER);
	}

	public void destroyPlugin() throws Exception {

	}

	protected void apiObjectDepricated0() {
		list.getLabel().setIcon(LUF.DOT_RED);
	}

	protected void apiObjectHotSelected0(DMConnection con,
			IDfPersistentObject[] parents2, IDfPersistentObject[] obj)
			throws Exception {
		if (obj == null || obj.length != 1)
			list.clear();
		else
			showObj(con, obj[0]);
		list.getLabel().setIcon(LUF.DOT_GREEN);
	}

}
