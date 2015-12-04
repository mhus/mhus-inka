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

import java.io.FileOutputStream;
import java.util.Properties;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class ExportMimeTypes implements ScriptIfc {

	private String exportFile;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		Properties out = new Properties();

		String dql = "select name,mime_type from dm_format where mime_type != ' '";
		IDfQuery query = pCon.createQuery(dql);
		IDfCollection res = query.execute(pCon.getSession(),
				IDfQuery.READ_QUERY);
		while (res.next()) {
			out.setProperty(res.getString("name"), res.getString("mime_type"));
		}
		res.close();
		// out.setProperty( "default", out.getProperty( "html" ) );

		FileOutputStream fos = new FileOutputStream(exportFile);
		out.store(fos, "");
		fos.close();
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

	}

	public void setExportFile(String in) {
		exportFile = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}
}
