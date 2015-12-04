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

package de.mhu.hair.sf.scripts.sql;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.lib.io.CSVWriter;

public class WriterCsv implements ScriptIfc {

	private String driver;
	private String jdbcUrl;
	private String user;
	private String pass;
	private File csvFile;
	private String query;
	private boolean append;
	private boolean header;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		Class.forName(driver);

		Connection con = DriverManager.getConnection(jdbcUrl, user, pass);

		Statement statement = con.createStatement();

		if (query.toUpperCase().trim().startsWith("SELECT")) {
			ResultSet res = statement.executeQuery(query);

			if (append && header && csvFile.exists())
				header = false;

			FileWriter fw = new FileWriter(csvFile, append);
			CSVWriter writer = new CSVWriter(fw, 2, ';', '"', true);

			int colNum = res.getMetaData().getColumnCount();

			if (header) {
				for (int i = 0; i < colNum; i++)
					writer.put(res.getMetaData().getColumnName(i + 1));
				writer.nl();
			}

			int cnt = 0;

			while (res.next()) {
				cnt++;

				for (int i = 0; i < colNum; i++)
					writer.put(res.getString(i + 1));

				writer.nl();

				if (cnt % 1000 == 0)
					pLogger.out.println("--- Rows: " + cnt);
			}

			pLogger.out.println(">>> Rows: " + cnt);

			writer.close();
			fw.close();
			res.close();

		} else {

			int ret = statement.executeUpdate(query);
			pLogger.out.print(">>> RET: " + ret);

		}

		statement.close();
		con.close();
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

	}

	public void setDriver(String in) {
		driver = in;
	}

	public void setJdbcUrl(String in) {
		jdbcUrl = in;
	}

	public void setUser(String in) {
		user = in;
	}

	public void setPass(String in) {
		pass = in;
	}

	public void setCsvFile(String in) {
		csvFile = new File(in);
	}

	public void setQuery(String in) {
		query = in;
	}

	public void setAppend(boolean in) {
		append = in;
	}

	public void setHeader(boolean in) {
		header = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
