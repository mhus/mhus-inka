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

package de.mhu.hair.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Timer;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiMenuBar;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiObjectWorker;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.xml.XmlTool;

public class ClipboardPlugin implements Plugin, ActionListener,
		ApiObjectHotSelect, ApiObjectWorker {

	private DMConnection con;
	private PluginNode node;
	private IDfPersistentObject[] targets;
	private IDfPersistentObject[] copyTargets;
	private ApiTypes typesApi;
	private IDfPersistentObject[] parents;
	private IDfPersistentObject[] copyParents;
	private Vector menuItems = new Vector();
	private Timer timer;
	private SimpleTextWindow window;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		node = pNode;
		ApiMenuBar menuBar = (ApiMenuBar) node.getSingleApi(ApiMenuBar.class);
		typesApi = (ApiTypes) node.getSingleApi(ApiTypes.class);
		timer = new Timer(true);

		NodeList list = XmlTool.getLocalElements(pConfig.getNode(), "item");
		for (int i = 0; i < list.getLength(); i++) {
			Element conf = (Element) list.item(i);
			String action = conf.getAttribute("action");
			JMenuItem item = new JMenuItem(conf.getAttribute("title"));
			item.setActionCommand(action);
			item.addActionListener(this);
			if (conf.getAttribute("key").length() != 0)
				item.setAccelerator(KeyStroke.getKeyStroke(conf
						.getAttribute("key")));
			menuBar.addMenuItem(conf.getAttribute("location"), item);

			if (!action.equals("select")) {
				menuItems.add(item);
			}
		}

		refreshMenuItems();
		node.addApi(ApiObjectHotSelect.class, this);

		window = new SimpleTextWindow(node,"Clipboard");
		
	}

	private void refreshMenuItems() {

		for (Iterator i = menuItems.iterator(); i.hasNext();)
			((JMenuItem) i.next()).setEnabled(copyTargets != null);
	}

	public void destroyPlugin() throws Exception {

	}

	public void actionPerformed(final ActionEvent e) {
		timer.schedule(new ATimerTask() {

			public void run0() throws Exception {
				swingActionPerformed(e);
			}

		}, 100);
	}

	public void swingActionPerformed(ActionEvent e) {

		try {
			String action = e.getActionCommand();

			if ("select".equals(action)) {

				if (targets == null)
					return;
				copyTargets = targets;
				copyParents = parents;

			} else if ("copy".equals(action)) {

				if (targets == null
						|| targets.length != 1
						|| !typesApi
								.isTypeOf("dm_folder", targets[0].getType())
						|| copyTargets == null || copyTargets.length == 0)
					return;

				for (int i = 0; i < copyTargets.length; i++) {
					copyTargets[i].fetch(null);
					copyTo((IDfSysObject) copyTargets[i],
							(IDfSysObject) targets[0]);
				}

				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.CREATED, copyTargets);

				copyTargets = null;
				copyParents = null;

			} else if ("move".equals(action)) {

				if (targets == null
						|| targets.length != 1
						|| !typesApi
								.isTypeOf("dm_folder", targets[0].getType())
						|| copyTargets == null || copyTargets.length == 0)
					return;

				for (int i = 0; i < copyTargets.length; i++) {
					copyTargets[i].fetch(null);
					// unlinkAll( (IDfSysObject)copyTargets[i] );
					if (copyParents != null && copyParents[i] != null)
						((IDfSysObject) copyTargets[i]).unlink(copyParents[i]
								.getObjectId().toString());

					((IDfSysObject) copyTargets[i]).link(targets[0]
							.getObjectId().toString());
					copyTargets[i].save();
				}

				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.DELETED, copyTargets);
				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.CREATED, copyTargets);
				// ObjectChangedTool.objectsChanged( node,
				// ApiObjectChanged.CHANGED, targets );

				copyTargets = null;
				copyParents = null;

			} else if ("link".equals(action)) {

				if (targets == null
						|| targets.length != 1
						|| !typesApi
								.isTypeOf("dm_folder", targets[0].getType())
						|| copyTargets == null || copyTargets.length == 0)
					return;

				for (int i = 0; i < copyTargets.length; i++) {
					copyTargets[i].fetch(null);
					((IDfSysObject) copyTargets[i]).link(targets[0]
							.getObjectId().toString());
					copyTargets[i].save();
				}

				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.CREATED, copyTargets);

				copyTargets = null;
				copyParents = null;

			} else if ("clear".equals(action)) {

				copyTargets = null;
				copyParents = null;

			} else if ("print".equals(action)) {

				if (copyTargets == null)
					return;
				for (int i = 0; i < copyTargets.length; i++) {
					if (copyParents != null && copyParents[i] != null)
						window.getLogger().out.println("Item:  "
								+ ObjectTool.getPath(copyParents[i]) + '/'
								+ ObjectTool.getName(copyTargets[i]));
					else
						window.getLogger().out.println("Item:? "
								+ ObjectTool.getPath(copyTargets[i]));
				}

			} else if ("unlink".equals(action)) {

				if (copyTargets == null || copyParents == null)
					return;

				for (int i = 0; i < copyTargets.length; i++) {
					copyTargets[i].fetch(null);
					if (copyParents[i] != null) {
						IDfSysObject sys = (IDfSysObject) copyTargets[i];

						sys.unlink(copyParents[i].getObjectId().toString());
						if (sys.getValueCount("i_folder_id") == 0) {
							window.getLogger().out.println("*** can't remove link from "
									+ ObjectTool.getPath(sys)
									+ " it's the last link! ");
						} else {
							sys.save();
						}
					} else {
						window.getLogger().out.println("--- ignore "
								+ ObjectTool.getPath(copyTargets[i])
								+ " there is no parent info");
					}
				}

				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.DELETED, copyTargets);

				copyTargets = null;
				copyParents = null;

			} else if ("unicate".equals(action)) {

				if (copyTargets == null || copyParents == null)
					return;

				for (int i = 0; i < copyTargets.length; i++) {
					copyTargets[i].fetch(null);
					if (copyParents[i] != null) {
						IDfSysObject sys = (IDfSysObject) copyTargets[i];

						unlinkAll(sys);
						sys.link(copyParents[i].getObjectId().toString());
						sys.save();
					} else
						window.getLogger().out.println("--- ignore "
								+ ObjectTool.getPath(copyTargets[i])
								+ " there is no parent info");
				}

				ObjectChangedTool.objectsChanged(node,
						ApiObjectChanged.DELETED, copyTargets);

				copyTargets = null;
				copyParents = null;

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			JComponent panel = ((ApiToolbar) node
					.getSingleApi(ApiToolbar.class)).getMainComponent();
			JOptionPane.showMessageDialog(panel, ex.toString(), "Error", 0);
		}

		window.getLogger().out.println("FINISH");
		refreshMenuItems();

	}

	private void copyTo(IDfSysObject src, IDfSysObject dest) throws DfException {

		Vector childs = null;

		if (typesApi.isTypeOf("dm_folder", src.getType())) {
			IDfCollection col = ((IDfFolder) src).getContents("r_object_id");
			childs = new Vector();
			while (col.next())
				childs.add(col.getString("r_object_id"));
			col.close();
		}

		unlinkAll(src);

		src.link(dest.getObjectId().toString());
		IDfId newId = src.saveAsNew(true);
		src.revert();

		if (childs != null) {
			for (Iterator i = childs.iterator(); i.hasNext();) {
				String id = (String) i.next();
				copyTo(con.getExistingObject(id), con.getExistingObject(newId));
			}
		}

	}

	private void unlinkAll(IDfSysObject src) throws DfException {

		int cnt = src.getValueCount("i_folder_id");
		String[] values = new String[cnt];
		for (int i = 0; i < cnt; i++)
			values[i] = src.getRepeatingString("i_folder_id", i);
		for (int i = 0; i < cnt; i++)
			src.unlink(values[i]);

	}

	public void apiObjectDepricated() {
		targets = null;
	}

	public void apiObjectHotSelected(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
			throws Exception {
		targets = pObj;
		parents = pParents;
	}

	public String getTitleFor(DMConnection pCon, IDfPersistentObject[] pObj) {
		return "Select";
	}

	public boolean canWorkOn(DMConnection pCon, IDfPersistentObject[] pObj) {
		return pObj != null;
	}

	public void workWith(DMConnection pCon, IDfPersistentObject[] pObj) {

	}

}
