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

import javax.swing.JMenuBar;
import javax.swing.JPanel;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class InnerFramePlugin extends AbstractFrame implements ApiToolbar,
		ApiLayout {

	private ApiLayout layout;
	private JPanel panel;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		super.initPlugin(pNode, pConfig);

		layout = (ApiLayout) pNode.getParentNode()
				.getSingleApi(ApiLayout.class);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(this, BorderLayout.CENTER);

		layout.setComponent(panel, pConfig.getNode(), new ApiLayout.Listener() {

			public void windowClosed(Object source) {
				node.getParentNode().removeChild(node);
			}

		});

	}

	protected void setMenuBarInternal(JMenuBar menuBar2) {
		panel.add(menuBar2, BorderLayout.NORTH);
	}

}
