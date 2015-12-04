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

package de.mhu.hair.plugin.bof;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfDborEntry;
import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDbor;
import com.documentum.fc.client.IDfDborEntry;
import com.documentum.fc.client.IDfEnumeration;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.xml.XmlTool;

public class BofLoaderPlugin implements Plugin {

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		IDfClient m_client = DfClient.getLocalClient();
		IDfDbor dbor = m_client.getDbor();
		DMConnection con = (DMConnection) pNode
				.getSingleApi(DMConnection.class);
		String docbase = con.getSession().getDocbaseName();

		NodeList list = XmlTool.getLocalElements(pConfig.getNode(), "unload");
		for (int i = 0; i < list.getLength(); i++) {
			Element bo = (Element) list.item(i);
			String name = bo.getAttribute("name");
			if (bo.getAttribute("docbase").length() == 0
					|| bo.getAttribute("docbase").equals(docbase)) {
				System.out.println(">>> unload BOR: " + name);
				try {
					dbor.unregister(name);
				} catch (DfServiceException se) {
					se.printStackTrace();
				}
			}
		}

		list = XmlTool.getLocalElements(pConfig.getNode(), "load");
		for (int i = 0; i < list.getLength(); i++) {
			Element bo = (Element) list.item(i);
			String name = bo.getAttribute("name");
			if (bo.getAttribute("docbase").length() == 0
					|| bo.getAttribute("docbase").equals(docbase)) {
				System.out.println(">>> load BOR: " + name);
				/*
				 * try { System.out.println( "--- unload" ); dbor.unregister(
				 * name ); } catch ( DfServiceException se ) {
				 * se.printStackTrace(); }
				 */
				try {
					// System.out.println( "--- load" );
					IDfDborEntry entry = new DfDborEntry();
					entry.setName(name);
					entry.setServiceBased(false); // true for SBO, false for TBO
					entry.setJavaClass(bo.getAttribute("class"));
					entry.setVersion(bo.getAttribute("version")); // i.e. "1.0"

					dbor.register(entry);
				} catch (DfServiceException se) {
					se.printStackTrace();
				}
			}
		}

		for (IDfEnumeration enu = dbor.getAll(); enu.hasMoreElements();) {
			IDfDborEntry entry = (IDfDborEntry) enu.nextElement();
			System.out.println("DBOR ENTRY: " + entry.getName() + " ("
					+ entry.getVersion() + "): " + entry.getJavaClass());
		}

	}

	public void destroyPlugin() throws Exception {
	}

}
