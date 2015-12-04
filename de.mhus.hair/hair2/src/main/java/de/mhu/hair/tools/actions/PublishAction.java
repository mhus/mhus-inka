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

package de.mhu.hair.tools.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ATimerTask;

public class PublishAction implements ActionIfc {

	private Timer timer;
	protected boolean inAction = false;
	private HashSet scs;
	private HashSet scsNames;
	private String filter = "eBase_WCM_.*Active_DE_DE";

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		// timer = ((ApiSystem)pNode.getSingleApi( ApiSystem.class
		// )).getTimer();
		timer = new Timer(true);
	}

	public void destroyAction() {
	}

	public boolean isEnabled(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		return pCon != null && pTarget != null && pTarget.length != 0;
	}

	public void actionPerformed(PluginNode pNode, final DMConnection pCon,
			final IDfPersistentObject[] pTarget) throws Exception {

		scs = new HashSet();
		scsNames = new HashSet();

		inAction = true;

		timer.schedule(new ATimerTask() {

			public void run0() throws Exception {

				filter = JOptionPane.showInputDialog(null, "Filer", filter);
				for (int i = 0; i < pTarget.length; i++) {
					try {
						findPublish(pCon, pTarget[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (scs.size() == 0) {
					JOptionPane.showMessageDialog(null, "No Config found");
				} else {

					StringBuffer sb = new StringBuffer();
					int cnt = 0;
					for (Iterator i = scsNames.iterator(); i.hasNext();) {
						sb.append(i.next());
						sb.append(' ');
						if (cnt % 5 == 0)
							sb.append('\n');
						cnt++;
					}

					int res = JOptionPane.showConfirmDialog(null, sb.toString()
							+ "\nFull Refresh \"Yes\", incrimental \"No\"",
							"Refresh Mode", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (res == JOptionPane.YES_OPTION
							|| res == JOptionPane.NO_OPTION) {

						if (res == JOptionPane.YES_OPTION) {
							int res2 = JOptionPane.showConfirmDialog(null,
									"Really Full Refresh?", "Confirm",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (res2 != JOptionPane.YES_OPTION)
								return;
						}

						DMConnection clone = pCon.cloneConnection(false);

						for (Iterator i = scs.iterator(); i.hasNext();) {
							String id = (String) i.next();
							if (res == JOptionPane.YES_OPTION)
								publishingFull(clone.getSession(), id);
							else
								publishingIncremental(clone.getSession(), id);
						}

						clone.disconnect();
					}
				}

				inAction = false;
				System.out.println(">>> Publish FINISHED");
			}

		}, 100);

	}

	private void findPublish(DMConnection pCon, IDfPersistentObject pTarget)
			throws DfException {

		String path = ObjectTool.getPath(pTarget);
		System.out.println(">>> Publish " + path);
		for (int i = 0; i < pTarget.getValueCount("i_folder_id"); i++) {
			findPublishForFolder(pCon, pCon.getPersistentObject(pTarget
					.getRepeatingId("i_folder_id", i)));
		}

	}

	private void findPublishForFolder(DMConnection pCon,
			IDfPersistentObject pTarget) throws DfException {
		IDfSession session = pCon.getSession();

		String path = ObjectTool.getPath(pTarget);
		if (path.indexOf('/') < 0) {
			System.out.println("*** no path for " + path);
			return;
		}

		System.out.println("--- Folder " + path);
		int pos = path.indexOf('/', 1);

		if (pos > 0)
			path = path.substring(0, pos);

		String strCabId = pCon.getSession().getFolderByPath(path).getObjectId()
				.getId();

		// we also need to queue the publishing jobs to be started
		// after the update
		String q = "select r_object_id,object_name from dm_webc_config "
				+ "WHERE source_folder_id = '" + strCabId + "' ";

		// System.out.println( "--- Q: " + q );

		IDfQuery query = pCon.createQuery(q);
		IDfCollection res = query.execute(session, IDfQuery.EXEC_QUERY);

		while (res.next()) {
			String name = res.getString("object_name");
			String id = res.getString("r_object_id");
			if (filter == null || name.matches(filter)) {
				System.out.println("Add Web Cache Publishing Job " + name
						+ " (" + id + ")");
				scs.add(res.getString("r_object_id"));
				scsNames.add(name);
			}
		}
		res.close();

	}

	private void publishingIncremental(IDfSession session, String strJobId)
			throws DfException {
		// now start publishing jobs if required

		IDfSysObject objJob = (IDfSysObject) session.getObject(new DfId(
				strJobId));

		System.out.println("--- Publishing Job " + objJob.getObjectName());
		// start the publishing job from here
		String cmd = strJobId
				+ ",WEBCACHE_PUBLISH,ARGUMENTS,S,-full_refresh false -method_trace_level 0";
		System.out.println("--- CMD: " + cmd);
		String strColl = session.apiGet("apply", cmd);
		if (session.apiExec("next", strColl))
			// write the result to the log file
			System.out.println("Apply result: "
					+ session.apiGet("dump", strColl));
		else
			System.out.println("Error with apply command: "
					+ session.apiGet("getmessage", ""));
		session.apiExec("close", strColl);

	}

	private void publishingFull(IDfSession session, String strJobId)
			throws DfException {
		// now start publishing jobs if required

		IDfSysObject objJob = (IDfSysObject) session.getObject(new DfId(
				strJobId));

		System.out.println("--- Publishing Job " + objJob.getObjectName());
		// start the publishing job from here
		String cmd = strJobId
				+ ",WEBCACHE_PUBLISH,ARGUMENTS,S,-full_refresh true -method_trace_level 0";
		System.out.println("--- CMD: " + cmd);
		String strColl = session.apiGet("apply", cmd);
		if (session.apiExec("next", strColl))
			// write the result to the log file
			System.out.println("Apply result: "
					+ session.apiGet("dump", strColl));
		else
			System.out.println("Error with apply command: "
					+ session.apiGet("getmessage", ""));
		session.apiExec("close", strColl);

	}

	public String getTitle() {
		return null;
	}

}
