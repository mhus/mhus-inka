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

package de.mhu.hair.tools;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiObjectSelect;
import de.mhu.hair.api.ApiObjectWorkerFactory;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.xml.XmlTool;

public class FactoryPlugin implements Plugin {

	private PluginNode node;
	private PluginConfig pluginConfig;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig) {

		node = pNode;
		pluginConfig = pConfig;

		NodeList list = XmlTool.getLocalElements(pConfig.getOriginalNode(),
				"listener");
		for (int i = 0; i < list.getLength(); i++) {
			Element l = (Element) list.item(i);
			Worker w = new Worker(l);
		}

	}

	private class Worker implements ApiObjectHotSelect, ApiObjectSelect,
			ApiObjectWorkerFactory {

		private Element config;
		private String title;

		public Worker(Element l) {
			config = l;
			if (l.getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
				node.addApi(ApiObjectHotSelect.class, this);
			}
			if (l.getAttribute("listen").indexOf("_obj.select_") >= 0) {
				node.addApi(ApiObjectSelect.class, this);
			}
			if (l.getAttribute("listen").indexOf("_obj.worker.factory_") >= 0) {
				node.addApi(ApiObjectWorkerFactory.class, this);
				title = l.getAttribute("title");
			}
		}

		public void apiObjectHotSelected(DMConnection pCon,
				IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
				throws Exception {
			if (pObj == null || pObj.length != 1)
				return;
			Map params = pluginConfig.getProperties();
			params.put("objid", pObj[0].getObjectId().toString());
			params.put("objname", ObjectTool.getName(pObj[0]));
			params.put("objpath", ObjectTool.getPath(pObj[0]));
			action(params);
		}

		public void apiObjectSelected(DMConnection pCon,
				IDfPersistentObject[] pObj) throws Exception {
			if (pObj == null || pObj.length != 1)
				return;
			Map params = pluginConfig.getProperties();
			params.put("objid", pObj[0].getObjectId().toString());
			params.put("objname", ObjectTool.getName(pObj[0]));
			params.put("objpath", ObjectTool.getPath(pObj[0]));
			action(params);
		}

		private void action(Map params) {
			try {
				PluginNode child = node.createChild(config);
				child.start(params, true);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public String getTitleFor(DMConnection pCon, IDfPersistentObject[] pObj) {
			return title;
		}

		public boolean canCreateFor(DMConnection pCon,
				IDfPersistentObject[] pObj) {
			if (pObj == null || pObj.length != 1)
				return false;
			return true;
		}

		public void createWorker(DMConnection pCon, IDfPersistentObject[] pObj)
				throws Exception {
			if (pObj == null || pObj.length != 1)
				return;
			Map params = pluginConfig.getProperties();
			params.put("objid", pObj[0].getObjectId().toString());
			params.put("objname", ObjectTool.getName(pObj[0]));
			params.put("objpath", ObjectTool.getPath(pObj[0]));
			action(params);
		}

		public void apiObjectDepricated() {
			// TODO Auto-generated method stub

		}

	}

	public void destroyPlugin() throws Exception {
		// TODO Auto-generated method stub

	}

}
