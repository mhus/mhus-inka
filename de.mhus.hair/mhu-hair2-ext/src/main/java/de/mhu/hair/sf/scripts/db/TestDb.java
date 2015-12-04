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

package de.mhu.hair.sf.scripts.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.lib.APassword;

public class TestDb implements ScriptIfc {

	private String oracleUrl;
	private String oracleUser;
	private String oraclePass;

	public void initialize(PluginNode arg0, DMConnection arg1, ALogger arg2)
			throws Exception {

		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection con = DriverManager.getConnection(oracleUrl, oracleUser,
				APassword.decode(oraclePass));
		Statement statement = con.createStatement();

		// DbTableCreator.createCreateTables( "TEST", statement, arg2 );
		statement.executeQuery("select * from dual").close();
		statement.close();
		con.close();

	}

	public void execute(PluginNode arg0, DMConnection arg1,
			IDfPersistentObject[] arg2, ALogger arg3) throws Exception {

	}

	public void setOracleUrl(String in) {
		oracleUrl = in;
	}

	public void setOracleUser(String in) {
		oracleUser = in;
	}

	public void setOraclePass(String in) {
		oraclePass = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
