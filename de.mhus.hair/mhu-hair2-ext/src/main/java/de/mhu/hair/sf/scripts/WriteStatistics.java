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
import java.util.Date;
import java.util.Hashtable;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.lib.io.CSVWriter;

public class WriteStatistics implements ScriptIfc {

	private String dql;
	private int interval;
	private String titles;
	private boolean append;
	private File file;
	private String[] titlesSplit;
	private int max;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		FileWriter fw = null;
		CSVWriter csv = null;

		titlesSplit = titles.split(",");

		if (!file.exists() || !append) {
			fw = new FileWriter(file);
			csv = new CSVWriter(fw, 2, ';', '"', true);
			if (titles.length() != 0) {
				csv.put("timestamp");
				csv.put("time");

				for (int i = 0; i < titlesSplit.length; i++)
					csv.put(titlesSplit[i]);
				csv.nl();
			}
		} else {
			fw = new FileWriter(file, true);
			csv = new CSVWriter(fw, 2, ';', '"', true);
		}

		IDfQuery query = pCon.createQuery(dql);

		long startTime = 0;
		int cnt = 0;
		while (true) {

			long time = System.currentTimeMillis();
			if (startTime == 0)
				startTime = time;

			IDfCollection res = query.execute(pCon.getSession(),
					IDfQuery.READ_QUERY);

			long t = (time - startTime) / 1000;
			csv.put(String.valueOf(time));
			csv.put(String.valueOf(t));

			pLogger.out.print(">>> " + new Date() + "    " + t + ",");
			Hashtable ht = new Hashtable();
			while (res.next()) {
				String k = res.getValueAt(0).asString();
				String v = res.getValueAt(1).asString();
				ht.put(k, v);
			}

			for (int i = 0; i < titlesSplit.length; i++) {
				String v = (String) ht.get(titlesSplit[i]);
				csv.put(v);
				pLogger.out.print(v + ",");
			}

			csv.nl();
			fw.flush();
			res.close();
			pLogger.out.println();

			cnt++;
			if (max > 0 && max <= cnt) {
				pLogger.out.println("--- Maximum reached");
				break;
			}

			time = interval * 1000 - (System.currentTimeMillis() - time);
			if (time > 0)
				try {
					pLogger.out.println("--- Sleep ...");
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		fw.close();

	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

	}

	public void setDql(String in) {
		dql = in;
	}

	public void setInterval(String in) {
		interval = Integer.parseInt(in);
	}

	public void setTitles(String in) {
		titles = in;
	}

	public void setAppend(boolean in) {
		append = in;
	}

	public void setFile(String in) {
		file = new File(in);
	}

	public void setMax(String in) {
		max = Integer.parseInt(in);
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
