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
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class UncloseCollection implements ScriptIfc {

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {
		IDfQuery query = pCon.createQuery("SELECT count(*) FROM dm_sysobject");
		query.execute(pCon.getSession(), IDfQuery.READ_QUERY);

	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
