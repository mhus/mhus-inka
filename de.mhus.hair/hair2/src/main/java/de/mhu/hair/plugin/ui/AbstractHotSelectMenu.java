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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public abstract class AbstractHotSelectMenu extends JPanel implements Plugin,
		ApiObjectHotSelect {

	private IDfPersistentObject[] parents;
	private IDfPersistentObject[] targets;
	private boolean visibleFrame;
	private JCheckBoxMenuItem menuItem;
	private PersistentManager manager;
	private JComponent component;
	private PluginNode node;
	private PluginConfig config;
	private DMConnection con;
	private AbstractFrame.Listener frameListener = new AbstractFrame.Listener() {

		public void windowClosed(Object source) {
			setMenuVisible(false);
		}
		
	};

	protected void initHotSelectMenu(PluginNode pNode, PluginConfig pConfig,
			JComponent pComponent) {
		if (menuItem != null)
			return;
		component = pComponent;
		node = pNode;
		config = pConfig;

		ApiMenuBar menuBar = (ApiMenuBar) pNode.getSingleApi(ApiMenuBar.class);
		String title = pConfig.getNode().getAttribute("title");
		String location = pConfig.getNode().getAttribute("location");

		if (pConfig.getNode().getAttribute("persistent").length() != 0)
			manager = ((ApiPersistent) pNode.getSingleApi(ApiPersistent.class))
					.getManager(pConfig.getNode().getAttribute("persistent"));

		menuItem = new JCheckBoxMenuItem(title);
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setMenuVisible(menuItem.isSelected());
			}

		});

		visibleFrame = ((ApiLayout) node.getSingleApi(ApiLayout.class))
				.isComponent(component);
		if (manager != null) {
			boolean visible = "1".equals(manager.getProperty("menu.visible",
					config.getProperty("visible.default", "1")));
			menuItem.setSelected(visible);
			setMenuVisible(visible);
		}

		menuBar.addMenuItem(location, menuItem);

	}

	protected void destroyHotSelectMenu() {
		if (menuItem == null)
			return;
		ApiMenuBar menuBar = (ApiMenuBar) node.getSingleApi(ApiMenuBar.class);
		String location = config.getNode().getAttribute("location");
		menuBar.removeMenuItem(location);
		menuBar = null;
	}

	public void setMenuVisible(boolean visible) {

		if (visible == visibleFrame)
			return;
		visibleFrame = visible;

		try {

			if (manager != null) {
				manager.setProperty("menu.visible", visible ? "1" : "0");
				manager.save();
			}

			if (visible) {
				((ApiLayout) node.getSingleApi(ApiLayout.class)).setComponent(
						component, config.getNode(), frameListener);

				apiObjectDepricated0();
				if (targets != null)
					apiObjectHotSelected0(con, parents, targets);

			} else {
				((ApiLayout) node.getSingleApi(ApiLayout.class))
						.removeComponent(component);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void apiObjectDepricated() {
		targets = null;
		if (visibleFrame)
			apiObjectDepricated0();
	}

	public void apiObjectHotSelected(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
			throws Exception {
		con = pCon;
		parents = pParents;
		targets = pObj;
		if (visibleFrame) {
			apiObjectHotSelected0(pCon, pParents, pObj);
		}
	}

	protected abstract void apiObjectDepricated0();

	protected abstract void apiObjectHotSelected0(DMConnection con,
			IDfPersistentObject[] parents2, IDfPersistentObject[] obj)
			throws Exception;

}
