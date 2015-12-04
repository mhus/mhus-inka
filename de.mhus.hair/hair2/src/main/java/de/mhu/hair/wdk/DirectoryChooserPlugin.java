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

import java.io.File;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class DirectoryChooserPlugin extends JFrame implements Plugin {

	private PersistentManager config;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		config = ((ApiPersistent) pNode.getSingleApi(ApiPersistent.class))
				.getManager("wdk_chooser");

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(config.getProperty("dir", ".")));
		chooser.setName("Select WDK webapps directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File dir = null;

		boolean ok = false;
		while (!ok) {

			ok = true;

			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				((ApiSystem) pNode.getSingleApi(ApiSystem.class)).exit(0);

			dir = chooser.getSelectedFile();
			if (dir == null)
				((ApiSystem) pNode.getSingleApi(ApiSystem.class)).exit(0);

			File webXml = new File(dir, "WEB-INF/web.xml");
			if (!webXml.exists() || !webXml.isFile()) {
				JOptionPane.showMessageDialog(null, "web.xml not found");
				ok = false;
			}

		}

		config.setProperty("dir", dir.toString());
		config.save();

		PluginNode child = pNode.createChild(pNode.getConfigName() + "/"
				+ pConfig.getNode().getAttribute("next_plugin"));

		Hashtable params = new Hashtable();
		params.put("dir", dir.getAbsoluteFile());

		child.start(params, true);

	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub

	}

}
