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

package de.mhu.hair.tools;

import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.lib.AString;

public class DctmTool {

	private static Hashtable variableHistory = new Hashtable();
	private static String historyValue = "";

	public static boolean executeDql(DMConnection con, String in,
			JComponent main, DctmExecuteDqlListener listener) {

		Hashtable variables = new Hashtable();

		String p = AString.beforeIndex(in, ':');
		String[] params = p.split(",");
		if (params.length < 1)
			return false;
		String dql = AString.afterIndex(in, ':');

		if (params.length > 1 && params[1].equals("v")) {

			String[] split = dql.split("\\$");
			for (int i = 0; i < split.length; i += 2) {
				if (i + 1 < split.length) {
					if (!split[i + 1].equals(""))
						if (variables.get(split[i + 1]) == null) {
							String def = (String) variableHistory
									.get(split[i + 1]);
							String out = JOptionPane.showInputDialog(main,
									"Variable: " + split[i + 1], def);
							if (out == null)
								return false;
							variableHistory.put(split[i + 1], out);
							variables.put(split[i + 1], out);
						}
				}
			}
		}

		return executeDql(con, in, variables, listener);
	}

	public static boolean executeDql(DMConnection con, String in,
			Hashtable variables, DctmExecuteDqlListener listener) {

		IDfCollection res = null;

		try {
			String p = AString.beforeIndex(in, ':');
			String[] params = p.split(",");
			if (params.length < 1) {
				listener.addTextLine("Wrong number of parameters: "
						+ params.length);
				return false;
			}
			String dql = AString.afterIndex(in, ':');

			if (params.length > 1 && params[1].equals("v")) {

				String[] split = dql.split("\\$");
				dql = "";
				for (int i = 0; i < split.length; i += 2) {
					dql = dql + split[i];
					if (i + 1 < split.length) {
						if (split[i + 1].equals(""))
							dql = dql + "$";
						else {
							Object out = variables.get(split[i + 1]);
							if (out == null) {
								listener.addTextLine("Variable not defined: '"
										+ split[i + 1]);
								return false;
							}
							variableHistory.put(split[i + 1], (String) out
									.toString());
							dql = dql + ((String) out).toString();
						}
					}
				}
			}

			IDfQuery query = con.createQuery(dql);

			int type = IDfQuery.DF_APPLY;
			if (params[0].equals("APPLY"))
				type = IDfQuery.DF_APPLY;
			else if (params[0].equals("QUERY"))
				type = IDfQuery.DF_QUERY;
			else if (params[0].equals("CACHE QUERY"))
				type = IDfQuery.DF_CACHE_QUERY;
			else if (params[0].equals("EXEC QUERY"))
				type = IDfQuery.DF_EXEC_QUERY;
			else if (params[0].equals("EXEC READ QUERY"))
				type = IDfQuery.DF_EXECREAD_QUERY;
			else if (params[0].equals("READ QUERY"))
				type = IDfQuery.DF_READ_QUERY;
			else {
				listener.addTextLine("Type Unknown: " + params[0]);
				return false;
			}

			res = query.execute(con.getSession(), type);

			listener.showResults(res);

			res.close();

			listener.addTextLine("OK");

		} catch (DfException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			listener.addTextLine(e1.toString());
			if (res != null)
				try {
					res.close();
				} catch (DfException e2) {
					e2.printStackTrace();
				}
			return false;
		}

		return true;
	}

	/**
	 * Ask for parameters and calls the non GUI function.
	 * 
	 * @param con
	 * @param in
	 * @param main
	 * @param listener
	 * @return
	 */
	public static boolean executeApi(DMConnection con, String in,
			JComponent main, DctmExecuteApiListener listener) {

		Hashtable variables = new Hashtable();

		String[] lines = in.split("\n");
		for (int l = 0; l < lines.length; l++) {

			// split parameters
			String p = AString.beforeIndex(lines[l], ':');
			String[] params = p.split(",");
			if (params.length < 1)
				return false;
			String api = AString.afterIndex(lines[l], ':');

			// ask for variables and store it into a map
			if (params.length > 1 && params[1].equals("v")) {

				String[] split = api.split("\\$");
				for (int i = 0; i < split.length; i += 2) {
					if (i + 1 < split.length) {
						if (!split[i + 1].equals(""))
							if (variables.get(split[i + 1]) == null) {
								String def = (String) variableHistory
										.get(split[i + 1]);
								String out = JOptionPane.showInputDialog(main,
										"Variable: " + split[i + 1], def);
								if (out == null)
									return false;
								variableHistory.put(split[i + 1], out);
								variables.put(split[i + 1], out);
							}
					}
				}
			}

			params[0] = params[0].toUpperCase();
			if (params[0].equals("SET")) {
				String val = JOptionPane.showInputDialog(main, "Value for: "
						+ lines[l], historyValue);
				if (val == null)
					return false;
				historyValue = val;
			}

			boolean ret = executeApiCommand(con, lines[l], variables,
					historyValue, listener);
			if (!ret)
				return false;
		}

		return true;

	}

	public static boolean executeApi(DMConnection con, String in,
			Hashtable variables, String[] dmSetValue,
			DctmExecuteApiListener listener) {
		String[] lines = in.split("\n");
		for (int l = 0; l < lines.length; l++) {
			boolean ret = executeApiCommand(con, lines[l], variables,
					dmSetValue[l], listener);
			if (!ret)
				return false;
		}

		return true;

	}

	/**
	 * executes an api string. The Format of the string is: 'typedefinition:api
	 * command' <p/> typedefinition is a comma seperated list. First is the
	 * type, second is a marker for validating variables, e.g. 'GET,v:' , means
	 * to make a apiGet() call and check for variables. <p/> The following types
	 * are provided: GET,SET,EXEC <p/> If you call the SET, dmSetValue should be
	 * set. <p/> The api command can contain markers for variables. The markers
	 * are included in dollar signs, e.g. describe,c,$tyte$ will replace $type$
	 * with the value from the variables Hashtable. If you need a single dollar
	 * in your command, use $$ (without a name between the dollars). <p/> If you
	 * dont want to parse for the dollars, use in the typedefinition no 'v',
	 * e.g. 'GET,:'. <p/> The session identifier has to be 'c'. <p/> Some
	 * Examples:<br/> GET,v:describe,c,$type$<br/>
	 * GET,:describe,c,dm_document<br/>
	 * 
	 * @param con
	 * @param in
	 * @param variables
	 * @param dmSetValue
	 * @param listener
	 * @return
	 */
	public static boolean executeApiCommand(DMConnection con, String in,
			Hashtable variables, String dmSetValue,
			DctmExecuteApiListener listener) {

		try {
			String p = AString.beforeIndex(in, ':');
			String[] params = p.split(",");
			if (params.length < 1) {
				listener.addTextLine("Wrong number of parameters: "
						+ params.length);
				return false;
			}
			String api = AString.afterIndex(in, ':');

			if (params.length > 1 && params[1].equals("v")) {

				String[] split = api.split("\\$");
				api = "";
				for (int i = 0; i < split.length; i += 2) {
					api = api + split[i];
					if (i + 1 < split.length) {
						if (split[i + 1].equals(""))
							api = api + "$";
						else {
							Object out = variables.get(split[i + 1]);
							if (out == null) {
								listener.addTextLine("Variable not defined: '"
										+ split[i + 1]);
								return false;
							}
							variableHistory.put(split[i + 1], (String) out
									.toString());
							api = api + ((String) out).toString();
						}
					}
				}
			}

			String cmd = api;
			String ses = "";
			String args = "";

			if (AString.isIndex(cmd, ',')) {
				ses = AString.afterIndex(cmd, ',');
				cmd = AString.beforeIndex(cmd, ',');
			}

			if (AString.isIndex(ses, ',')) {
				args = AString.afterIndex(ses, ',');
				ses = AString.beforeIndex(ses, ',');
			}

			if (!ses.equals("c")) {
				listener.addTextLine("Wrong Session: '" + ses
						+ "' should be 'c'");
				return false;
			}

			// check type
			params[0] = params[0].toUpperCase();
			boolean error = false;

			if (params[0].equals("EXEC")) {
				if (con.getSession().apiExec(cmd, args))
					listener.addTextLine("OK");
				else {
					listener.addTextLine("ERROR");
					error = true;
				}
			} else if (params[0].equals("GET")) {
				String line = con.getSession().apiGet(cmd, args);
				if (line == null) {
					listener.addTextLine("ERROR");
					error = true;
				} else {
					listener.addTextLine(line);
					listener.addTextLine("OK");
				}
			} else if (params[0].equals("SET")) {
				if (con.getSession().apiSet(cmd, args, dmSetValue))
					listener.addTextLine("OK");
				else {
					listener.addTextLine("ERROR");
					error = true;
				}
			} /*
			 * else if ( params[0].equals( "GETFILE" ) ) { con.getSession().api
			 * }
			 */

			if (error) {
				try {
					listener.addTextLine("Message: "
							+ con.getSession().apiGet("getmessage", ""));
				} catch (Exception exx) {
					exx.printStackTrace();
					listener.addTextLine("No Message: " + exx.toString());
				}
			}

			return !error;

		} catch (DfException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			listener.addTextLine("ERROR: " + e1.toString());
		}

		return false;
	}

	public static interface DctmExecuteApiListener {

		void addTextLine(String string);

	}

	public static interface DctmExecuteDqlListener {

		void addTextLine(String string);

		void showResults(IDfCollection res);

	}

	public static boolean isFolder(String id) {
		return (id.startsWith("0b") || id.startsWith("0c"));
	}

	public static boolean isFolder(IDfPersistentObject obj) throws DfException {
		return isFolder(obj.getObjectId().toString());
	}

}
