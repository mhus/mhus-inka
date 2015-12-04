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
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.tools.ObjectTool;

public class Publish implements ScriptIfc {

	private boolean fullRefresh = false;
	private int traceLevel;
	private boolean testMode = true;
	private boolean recreatePropertySchema;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		for (int i = 0; i < pTargets.length; i++) {
			try {

				IDfSession session = pCon.getSession();
				IDfSysObject objJob = (IDfSysObject) pTargets[i];

				pLogger.out.println(">>> Publish " + objJob.getObjectName()
						+ " " + ObjectTool.getPath(objJob));
				// start the publishing job from here
				String cmd = objJob.getObjectId()
						+ ",WEBCACHE_PUBLISH,ARGUMENTS,S,-full_refresh "
						+ fullRefresh + " -recreate_property_schema "
						+ recreatePropertySchema + " -method_trace_level "
						+ traceLevel + " ";
				pLogger.out.println("--- CMD: " + cmd);
				if (testMode) {
					pLogger.out.println("+++ Testmode");
				} else {
					String strColl = session.apiGet("apply", cmd);
					if (session.apiExec("next", strColl))
						// write the result to the log file
						pLogger.out.println("--- Apply result: "
								+ session.apiGet("dump", strColl));
					else
						pLogger.out.println("*** Error with apply command: "
								+ session.apiGet("getmessage", ""));
					session.apiExec("close", strColl);
				}
			} catch (Exception e) {
				pLogger.out.println("*** ERROR: " + e);
				e.printStackTrace();
			}
		}
	}

	public void setFullRefresh(boolean in) {
		fullRefresh = in;
	}

	public void setTraceLevel(String in) {
		traceLevel = Integer.parseInt(in);
	}

	public void setRecreatePropertySchema(boolean in) {
		recreatePropertySchema = in;
	}

	public void setTest(boolean in) {
		testMode = in;
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
