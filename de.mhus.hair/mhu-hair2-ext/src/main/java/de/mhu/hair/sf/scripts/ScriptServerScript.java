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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;
import de.mhu.hair.sf.ScriptServer;
import de.mhu.hair.sf.ScriptServer.Client;
import de.mhu.hair.sf.ScriptServer.Listener;
import de.mhu.lib.AThread;

public class ScriptServerScript implements ScriptIfc, Listener {

	private ScriptServer server;
	private int port;
	private ALogger logger;
	private String tmpFile;
	private String select;
	private int packageSize = 100;

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		server = new ScriptServer(port, null, this);
		logger = pLogger;
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {

		// write ids
		int objectSize = 0;

		server.start();

		FileOutputStream fos = new FileOutputStream(tmpFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		if (pTargets == null || select.length() != 0) {

			IDfQuery query = pCon.createQuery(select);
			IDfCollection res = query.execute(pCon.getSession(),
					IDfQuery.READ_QUERY);
			while (res.next()) {
				oos.writeUTF(res.getString("r_object_id"));
				objectSize++;
			}
			res.close();
		} else {
			for (int i = 0; i < pTargets.length; i++) {
				oos.writeUTF(pTargets[i].getObjectId().toString());
				objectSize++;
			}
		}
		oos.close();
		fos.close();

		// read ids and send to tasks
		FileInputStream fis = new FileInputStream(tmpFile);
		ObjectInputStream ois = new ObjectInputStream(fis);

		int taskSize = objectSize / packageSize + 1;

		logger.out.println(">>> Size : " + objectSize);
		logger.out.println(">>> Tasks: " + taskSize);

		int taskId = 0;
		logger.setMaximum(taskSize);
		while (true) {

			// is Busy ?
			while (server.isQueued()) {
				// logger.out.println( "Wait for Clients ... sleep 2min" );
				AThread.sleep(1000);
			}

			// find pages
			Vector ids = new Vector();
			try {
				for (int i = 0; i < packageSize; i++)
					ids.add(ois.readUTF());
			} catch (Exception ex) {

			}

			if (ids.size() == 0) {
				logger.out
						.println("No more IDs ... wait for pending processes...");
				// wait for pending actions ....
				while (!server.allClientsReady()) {
					AThread.sleep(1000);
				}
				break;
			}

			// send to client
			logger.setValue(taskId);
			taskId++;
			// logger.out.println( "--- Queue Task: " + taskId + " of " +
			// taskSize );
			server.addTask("" + taskId, (String[]) ids.toArray(new String[ids
					.size()]));

		}

		server.close();

	}

	public void setPort(String in) {
		port = Integer.parseInt(in);
	}

	public void setTmpFile(String in) {
		tmpFile = in;
	}

	public void setSelect(String in) {
		select = in;
	}

	public void setPackageSize(String in) {
		packageSize = Integer.parseInt(in);
	}

	public void scriptTaskOk(Object userObject, String[] task) {
		logger.out.println("--- Task Finished: " + userObject);
	}

	public void scriptTaskError(ScriptServer.Client client, Object userObject,
			String[] task, Throwable exception) {
		if (task == null || task.length == 0)
			return;
		logger.out.println("*** Task Error: " + userObject + " at "
				+ client.getClientName());
		exception.printStackTrace(logger.out);
		server.closeClient(client);
		// logger.out.println( "--- Queue Task: " + userObject + " (retry)" );
		server.addTask(userObject, task);
	}

	public void scriptReady() {

	}

	public void scriptStartTask(Client client, Object userObject, String[] task) {
		logger.out.println(">>> Start Task: " + userObject + " at "
				+ client.getClientName());
	}

	public void destroy(PluginNode node, DMConnection con, ALogger logger) {
		// TODO Auto-generated method stub

	}

}
