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

package de.mhu.hair.sf.scripts;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class ChangeAttribute implements ScriptIfc {

	private String name;
	private String value;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		pLogger.setMaximum(pTargets.length);
		for (int i = 0; i < pTargets.length; i++) {
			pLogger.setValue(i);
			try {
				pTargets[i].setString(name, value);
				pTargets[i].save();
			} catch (Exception e) {
				pLogger.out.println("***ERROR: " + e + " at "
						+ pTargets[i].getObjectId());
			}
		}
	}

	public void setName(String in) {
		name = in;
	}

	public void setValue(String in) {
		value = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
