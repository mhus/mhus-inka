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

package de.mhu.hair.sf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptServer.Client;
import de.mhu.hair.sf.ScriptServer.Listener;
import de.mhu.lib.AThread;

public class ScriptServerPlugin implements Plugin, Listener {

	private int port;
	private ScriptServer server;
	private DMConnection pCon;
	private String select;
	private String tmpFile;
	private int packageSize;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		pCon = (DMConnection) pNode.getSingleApi(DMConnection.class);
		port = Integer.parseInt(pConfig.getNode().getAttribute("port"));
		select = pConfig.getNode().getAttribute("dql");
		tmpFile = pConfig.getNode().getAttribute("tmp_file");
		packageSize = Integer.parseInt(pConfig.getNode().getAttribute(
				"package_size"));

		server = new ScriptServer(port, pConfig.getNode(), this);

		// write ids
		int objectSize = 0;

		server.start();

		FileOutputStream fos = new FileOutputStream(tmpFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		IDfQuery query = pCon.createQuery(select);
		IDfCollection res = query.execute(pCon.getSession(),
				IDfQuery.READ_QUERY);
		while (res.next()) {
			oos.writeUTF(res.getString("r_object_id"));
			objectSize++;
		}
		res.close();

		oos.close();
		fos.close();

		// read ids and send to tasks
		FileInputStream fis = new FileInputStream(tmpFile);
		ObjectInputStream ois = new ObjectInputStream(fis);

		int taskSize = objectSize / packageSize + 1;

		System.out.println(">>> Size : " + objectSize);
		System.out.println(">>> Tasks: " + taskSize);

		int taskId = 0;
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
				System.out
						.println("No more IDs ... wait for pending processes...");
				// wait for pending actions ....
				while (!server.allClientsReady()) {
					AThread.sleep(1000);
				}
				break;
			}

			// send to client
			System.out.println("### " + taskId + " / " + taskSize);
			taskId++;
			// logger.out.println( "--- Queue Task: " + taskId + " of " +
			// taskSize );
			server.addTask("" + taskId, (String[]) ids.toArray(new String[ids
					.size()]));

		}

		server.close();

	}

	public void scriptTaskOk(Object userObject, String[] task) {
		System.out.println("--- Task Finished: " + userObject);
	}

	public void scriptTaskError(Client client, Object userObject,
			String[] task, Throwable exception) {
		if (task == null || task.length == 0)
			return;
		System.out.println("*** Task Error: " + userObject + " at "
				+ client.getClientName());
		exception.printStackTrace(System.out);
		server.closeClient(client);
		// logger.out.println( "--- Queue Task: " + userObject + " (retry)" );
		server.addTask(userObject, task);
	}

	public void scriptReady() {

	}

	public void scriptStartTask(Client client, Object userObject, String[] task) {
		System.out.println(">>> Start Task: " + userObject + " at "
				+ client.getClientName());
	}

	public void destroyPlugin() throws Exception {

	}

}
