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

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class LookAndFeelPlugin implements Plugin {

	public void destroyPlugin() throws Exception {

	}

	public void initPlugin(PluginNode node, PluginConfig config)
			throws Exception {

		System.out.println("OS LuF: "
				+ UIManager.getSystemLookAndFeelClassName());
		LookAndFeel[] aux = UIManager.getAuxiliaryLookAndFeels();
		if (aux != null) {
			for (LookAndFeel a : aux)
				System.out.println("LuS: " + a.getName());
		}
		// UIManager.setLookAndFeel( )

	}

}
