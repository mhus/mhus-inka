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

import org.w3c.dom.Element;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDbor;
import com.documentum.fc.client.IDfDborEntry;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfGlobalModuleRegistry;
import com.documentum.fc.client.IDfModuleDescriptor;
import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;

public class PrintDborInfoAction implements ActionIfc {

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		// TODO Auto-generated method stub

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public boolean isEnabled(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	public void actionPerformed(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		// TEST
		IDfClient m_client = DfClient.getLocalClient();
		IDfDbor dbor = m_client.getDbor();

		for (IDfEnumeration enu = dbor.getAll(); enu.hasMoreElements();) {
			IDfDborEntry entry = (IDfDborEntry) enu.nextElement();
			System.out.println("DBOR ENTRY: " + entry.getName() + " ("
					+ entry.getVersion() + "): " + entry.getJavaClass());
		}
		IDfGlobalModuleRegistry gmr = m_client.getModuleRegistry();
		IDfEnumeration mods = gmr.getServiceDescriptors();

		while (mods.hasMoreElements()) {
			IDfModuleDescriptor md = (IDfModuleDescriptor) mods.nextElement();
			System.out.println("MODULE ENTRY: " + md.getObjectName());
		}
	}

	public String getTitle() {
		return null;
	}

}
