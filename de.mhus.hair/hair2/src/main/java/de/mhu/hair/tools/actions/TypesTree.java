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

package de.mhu.hair.tools.actions;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.plugin.ui.TypeAttrPanel;
import de.mhu.hair.plugin.ui.TypesTreePanel;
import de.mhu.hair.plugin.ui.TypesTreePanel.Listener;

public class TypesTree implements ActionIfc {

	private Element config;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		return con != null;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);

		final TypeAttrPanel attr = new TypeAttrPanel(null);
		TypesTreePanel tree = new TypesTreePanel((DMConnection) node
				.getSingleApi(DMConnection.class), (ApiTypes) node
				.getSingleApi(ApiTypes.class), null, null, new Listener() {

			public void selectedEvent(IDfType value, int mode, JComponent src,
					int x, int y) {
				if (value == null)
					return;
				try {
					System.out.println("+++ Event: " + mode + " "
							+ value.getName());
				} catch (DfException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (mode == Listener.MODE_HOT_SELECT)
					try {
						attr.show(value);
					} catch (DfException e) {
						e.printStackTrace();
					}
			}

		});
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(tree);
		split.setRightComponent(attr);
		layout.setComponent(split, config);

	}

	public void initAction(PluginNode node, DMConnection con, Element pConfig) {
		config = pConfig;
	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}

}
