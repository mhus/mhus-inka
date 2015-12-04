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

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.xml.XmlTool;

public class MenuStructurePlugin implements Plugin {

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		ApiMenuBar menuApi = (ApiMenuBar) pNode.getSingleApi(ApiMenuBar.class);

		NodeList list = XmlTool.getLocalElements(pConfig.getNode(), "menu");
		for (int i = 0; i < list.getLength(); i++) {
			Element menu = (Element) list.item(i);
			menuApi.addMenuItem(menu.getAttribute("location"), new JMenu(menu
					.getAttribute("title")));
		}

		list = XmlTool.getLocalElements(pConfig.getNode(), "item");
		for (int i = 0; i < list.getLength(); i++) {
			Element menu = (Element) list.item(i);
			JMenuItem item = new JMenuItem(menu.getAttribute("title"));
			item.setEnabled(false);
			menuApi.addMenuItem(menu.getAttribute("location"), item);
		}

		list = XmlTool.getLocalElements(pConfig.getNode(), "special");
		for (int i = 0; i < list.getLength(); i++) {
			Element menu = (Element) list.item(i);
			String type = menu.getAttribute("type");
			if (type.equals("window_manager")) {
				JComponent wmm = menuApi.getWindowManagerMenu();
				if (wmm != null)
					menuApi.addMenuItem(menu.getAttribute("location"), wmm);
			} else
			if (type.equals("help")) {
				JComponent help = new HelpMenu();
				menuApi.addMenuItem(menu.getAttribute("location"), help);
			}
		}

	}

	public void destroyPlugin() throws Exception {

	}

}
