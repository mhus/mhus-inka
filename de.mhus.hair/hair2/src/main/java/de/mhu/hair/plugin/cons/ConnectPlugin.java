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

package de.mhu.hair.plugin.cons;

import java.util.Map;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.common.DfPreferences;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiDocbases;
import de.mhu.lib.xml.XmlTool;

public class ConnectPlugin implements Plugin {

	private PluginNode node;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		Element conf = pConfig.getNode();
//		Element bofConf = (Element) XmlTool.getLocalElements(conf, "bof").item(
//				0);
		Element docConf = (Element) XmlTool.getLocalElements(conf, "docbase")
				.item(0);
 
//		if (bofConf.getAttribute("docbase").length() != 0) {
//			System.out.println("--- Connect to bof "
//					+ bofConf.getAttribute("docbase") + " as "
//					+ bofConf.getAttribute("user"));
			
//			DfPreferences.access().setGlobalRegistryRepository( bofConf.getAttribute("docbase") );
//			DfPreferences.access().setGlobalRegistryUsername( bofConf.getAttribute("user") );
//			DfPreferences.access().setGlobalRegistryPassword( bofConf.getAttribute("pass") );
			
//			DfPreferences.access().setModuleRegistry(
//					bofConf.getAttribute("docbase"),
//					bofConf.getAttribute("user"), bofConf.getAttribute("pass"));
//		}

		System.out.println("--- Connect to " + docConf.getAttribute("docbase")
				+ " as " + docConf.getAttribute("user"));
		DMConnection con = new DMConnection(docConf.getAttribute("user"),
				docConf.getAttribute("pass"), docConf.getAttribute("docbase"));
		pNode.addApi(DMConnection.class, con);

		System.out.println(">>> Connected to " + con.getDocbaseName() + " as "
				+ con.getUserName());
		Map params = pConfig.getProperties();
		params.put("docbase", con.getSession().getDocbaseName());

		if (conf.getAttribute("module").length() != 0) {

			PluginNode child = node.createChild(node.getConfigName() + "/"
					+ conf.getAttribute("module"));
			child.addApi(DMConnection.class, con);

			child.start(params, false);
		}

	}

	public void destroyPlugin() throws Exception {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
