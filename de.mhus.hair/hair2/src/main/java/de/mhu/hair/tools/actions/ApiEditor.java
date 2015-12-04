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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.xml.XmlTool;

public class ApiEditor implements ActionIfc {

	private Element config;
	private Properties predefined;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return con != null;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		de.mhu.hair.plugin.ui.ApiEditor editor = new de.mhu.hair.plugin.ui.ApiEditor(
				node, predefined);
		layout.setComponent(editor, config);
	}

	public void initAction(PluginNode node, DMConnection con, Element pConfig) {
		config = pConfig;
		NodeList list = XmlTool.getLocalElements(config, "pre");
		predefined = new Properties();
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			predefined.setProperty(e.getAttribute("name"), e
					.getAttribute("expr"));
		}
		if (!"".equals(config.getAttribute("file"))) {
			Properties out = loadPredefined(new File(config
					.getAttribute("file")));
			if (out != null)
				predefined = out;
		}
	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public static Properties loadPredefined(File file) {
		if (file == null)
			return new Properties();
		try {
			Properties p = new Properties();

			p.load(new FileInputStream(file));
			return p;

		} catch (Exception e1) {
			
			System.out.println( "*** " + e1 );
		}

		return null;

	}

	public String getTitle() {
		return config.getAttribute("title");
	}

}
