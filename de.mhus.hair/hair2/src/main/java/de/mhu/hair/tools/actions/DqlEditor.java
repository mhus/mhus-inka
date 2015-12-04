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
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.lib.xml.XmlTool;

public class DqlEditor implements ActionIfc {

	private Element config;
	private Properties predefined;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return con != null;
	}

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		ApiLayout layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		de.mhu.hair.plugin.ui.DqlEditor editor = new de.mhu.hair.plugin.ui.DqlEditor(
				node, predefined);
		layout.setComponent(editor, config);
	}

	public void initAction(final PluginNode node, DMConnection con, Element pConfig) {
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

		// preload names
		// TODO preload without blocking the system
//		Timer timer = ((ApiSystem) node.getSingleApi(ApiSystem.class)).getTimer();
//		timer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				ApiTypes api = (ApiTypes) node.getSingleApi(ApiTypes.class);
//				api.getNamesList();
//			}
//			
//		}, 1000);
		
		
	}

	public static Properties loadPredefined(File file) {
		if (file == null)
			return new Properties();
		try {
			Properties p = new Properties();

			p.load(new FileInputStream(file));
			return p;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}

}
