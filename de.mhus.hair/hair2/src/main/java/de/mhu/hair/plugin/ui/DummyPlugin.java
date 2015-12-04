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

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.mhu.hair.Build;
import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class DummyPlugin extends JPanel implements Plugin {

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		
		String name = Build.getInstance().getName();
		String ver  = Build.getInstance().getVersion();
		
		add(new JLabel(pConfig.getNode().getAttribute("text") + " " + name + " " + ver ));
		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(this,
				pConfig.getNode());
	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub

	}

}
