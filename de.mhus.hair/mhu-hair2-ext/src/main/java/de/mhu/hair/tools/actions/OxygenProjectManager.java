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

import javax.swing.JPanel;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;

public class OxygenProjectManager implements ActionIfc, Listener {

	private Element config;
	private PluginNode node;
	private DMConnection con;
	private JPanel main;
	private boolean visiblePlugin = false;

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		config = pConfig;
		node = pNode;
		con = pCon;
	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return con != null;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		setPluginVisible(true);

	}

	private void setPluginVisible(boolean b) throws Exception {

		if (visiblePlugin == b)
			return;
		visiblePlugin = b;
		if (visiblePlugin) {
			if (main == null)
				initUI();
			((ApiLayout) node.getSingleApi(ApiLayout.class)).setComponent(main,
					config, this);
		} else {
			((ApiLayout) node.getSingleApi(ApiLayout.class))
					.removeComponent(main);
		}

	}

	private void initUI() {

		main = new JPanel();

	}

	public void windowClosed(Object source) {
		try {
			setPluginVisible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return null;
	}

}
