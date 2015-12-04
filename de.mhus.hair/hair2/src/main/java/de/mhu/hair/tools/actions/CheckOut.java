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

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;

public class CheckOut implements ActionIfc {

	public void actionPerformed(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		for (int i = 0; i < target.length; i++) {
			if (!((IDfSysObject) target[i]).isCheckedOut()) {
				((IDfSysObject) target[i]).checkout();
				String file = ((IDfSysObject) target[i])
						.getFile(((IDfSysObject) target[i]).getObjectName());
				System.out.println("CheckOut to: " + file);
			}
		}

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		if (target == null)
			return false;
		for (int i = 0; i < target.length; i++)
			if (!((IDfSysObject) target[i]).isCheckedOut())
				return true;
		return false;
	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		// TODO Auto-generated method stub

	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}

}
