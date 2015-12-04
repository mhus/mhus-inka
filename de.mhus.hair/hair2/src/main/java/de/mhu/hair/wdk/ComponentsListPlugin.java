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

package de.mhu.hair.wdk;

import java.awt.event.MouseEvent;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.wdk.ListContainer.Container;

public class ComponentsListPlugin implements Plugin {

	private ListTablePanel panel;
	private StructurePlugin structure;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		panel = new ListTablePanel("Components", new ListTablePanel.Listener() {

			public void clickedEvent(Container selected, MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() > 1) {
					// TODO Connection.open(
					// ((StructurePlugin.ComponentContainer
					// )selected).getFile().getAbsolutePath() );

				}
			}

		});
		structure = (StructurePlugin) pNode.getSingleApi(StructurePlugin.class);
		panel.showList(structure.getComponentsAsList());

		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(panel,
				pConfig.getNode());

	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub

	}

}
