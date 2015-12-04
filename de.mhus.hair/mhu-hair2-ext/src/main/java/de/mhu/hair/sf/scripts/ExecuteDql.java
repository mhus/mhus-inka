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

import java.io.File;
import java.io.FileWriter;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.lib.io.CSVWriter;

public class ExecuteDql implements ScriptIfc {

	private String dql;
	private boolean printResult;
	private File csvFile;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		IDfQuery query = pCon.createQuery(dql);
		IDfCollection res = query.execute(pCon.getSession(),
				IDfQuery.EXEC_QUERY);

		FileWriter fw = null;
		CSVWriter writer = null;

		if (csvFile != null) {
			fw = new FileWriter(csvFile);
			writer = new CSVWriter(fw, 2, ';', '"', true);
		}

		if (printResult || writer != null) {
			int attr = res.getAttrCount();
			int cnt = 0;

			if (writer != null) {
				for (int i = 0; i < attr; i++) {
					writer.put(res.getAttr(i).getName());
				}
				writer.nl();
			}
			while (res.next()) {
				cnt++;
				System.out.print(cnt);
				for (int i = 0; i < attr; i++) {
					if (printResult)
						System.out.print(",\"" + res.getValueAt(i) + '"');
					if (writer != null)
						writer.put(res.getValueAt(i).asString());

				}
				if (printResult)
					System.out.println();
				if (writer != null)
					writer.nl();

			}

			if (writer != null) {
				fw.flush();
				fw.close();
			}
		}

		res.close();
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

	}

	public void setDql(String in) {
		dql = in;
	}

	public void setPrintResult(boolean in) {
		printResult = in;
	}

	public void setCSVFile(String in) {
		csvFile = null;
		if (in != null && in.length() != 0)
			csvFile = new File(in);
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
