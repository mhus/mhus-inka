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
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.mhu.hair.HairDebug;
import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectChangedTool;

public class OptionsMenuPlugin implements Plugin {

	private JMenu menu;
	private JCheckBoxMenuItem cbiAutoUpdate;
	private JCheckBoxMenuItem cbiTreeHotSelect;
	private JCheckBoxMenuItem cbiDebug;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		ApiMenuBar menuApi = (ApiMenuBar) pNode.getSingleApi(ApiMenuBar.class);

		menu = new JMenu(pConfig.getNode().getAttribute("title"));
		cbiAutoUpdate = new JCheckBoxMenuItem("Auto Update");
		cbiAutoUpdate.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ObjectChangedTool.SEND_CHANGES = !ObjectChangedTool.SEND_CHANGES;
			}

		});
		menu.add(cbiAutoUpdate);

		cbiTreeHotSelect = new JCheckBoxMenuItem("Tree Hot Select");
		cbiTreeHotSelect.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				TreePlugin.TREE_HOT_SELECT = !TreePlugin.TREE_HOT_SELECT;
			}

		});
		menu.add(cbiTreeHotSelect);

		cbiDebug = new JCheckBoxMenuItem("Debug Trace");
		cbiDebug.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				HairDebug.TRACE = !HairDebug.TRACE;
			}

		});
		menu.add(cbiDebug);
		
		menu.addMenuListener(new MenuListener() {

			public void menuSelected(MenuEvent e) {
				cbiAutoUpdate.setSelected(ObjectChangedTool.SEND_CHANGES);
				cbiTreeHotSelect.setSelected(TreePlugin.TREE_HOT_SELECT);
				cbiDebug.setSelected(HairDebug.TRACE);
			}

			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub

			}

			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub

			}

		});

		menuApi.addMenuItem(pConfig.getNode().getAttribute("location"), menu);

	}

	public void destroyPlugin() throws Exception {

	}

}
