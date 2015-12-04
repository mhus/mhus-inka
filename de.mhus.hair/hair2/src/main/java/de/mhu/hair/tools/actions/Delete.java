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

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectChangedTool;
import de.mhu.hair.tools.ObjectTool;

public class Delete implements ActionIfc {

	private Timer timer;

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		return (target != null && target.length != 0);
	}

	public void actionPerformed(final PluginNode node, final DMConnection con,
			final IDfPersistentObject[] target) throws Exception {

		JComponent component = ((ApiLayout) node.getSingleApi(ApiLayout.class))
				.getMainComponent();

		StringBuffer names = new StringBuffer();
		int len = 0;
		int lines = 1;
		boolean folders = false;
		boolean append = true;
		for (int i = 0; i < target.length; i++) {

			if (append) {
				names.append('\'');
				String name = ObjectTool.getName(target[i]);
				len = len + name.length();
				names.append(name);
				names.append("' ");

				if (len > 100) {
					names.append('\n');
					len = 0;
					lines++;
				}

				if (lines > 30) {
					names.append("...");
					append = false;
				}
			}
			if (target[i] instanceof IDfFolder)
				folders = true;

		}
		final int resVersions = JOptionPane.showConfirmDialog(component, names
				.toString(), "Delete Versions ?",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if (resVersions == JOptionPane.CANCEL_OPTION) {
			return;
		}

		final int resStructure = folders ? JOptionPane.showConfirmDialog(
				component, names.toString(), "Delete Childs ?",
				JOptionPane.YES_NO_CANCEL_OPTION) : JOptionPane.NO_OPTION;

		if (resStructure == JOptionPane.CANCEL_OPTION) {
			return;
		}

		final int resUnlink = JOptionPane.showConfirmDialog(component, names
				.toString(), "Unlink Childs ?",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if (resUnlink == JOptionPane.CANCEL_OPTION) {
			return;
		}

		timer.schedule(new TimerTask() {

			public void run() {
				try {
					for (int i = 0; i < target.length; i++) {
						System.out.println(">>> Delete " + (i + 1) + "/"
								+ target.length + ": "
								+ target[i].getObjectId());

						delete(target[i],
								resStructure == JOptionPane.YES_OPTION,
								resUnlink == JOptionPane.YES_OPTION);

					}

					System.out.println("FINISHED DELETE");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void delete(IDfPersistentObject obj, boolean children,
					boolean unlink) throws DfException {

				try {
					if (obj instanceof IDfFolder) {
						IDfFolder folder = (IDfFolder) obj;
						String dql = "SELECT r_object_id FROM dm_sysobject (all) WHERE ANY i_folder_id='"
								+ folder.getObjectId() + "'";

						IDfCollection list = con.createQuery(dql).execute(
								con.getSession(), IDfQuery.READ_QUERY);
						// folder.getContents( "r_object_id" );
						Vector ids = new Vector();
						while (list.next())
							ids.add(con.getExistingObject(list
									.getString("r_object_id")));
						list.close();
						for (Iterator i = ids.iterator(); i.hasNext();) {
							try {
								IDfSysObject child = (IDfSysObject) i.next();
								System.out.println("--- "
										+ ObjectTool.getPath(child));
								if (unlink
										&& child.getValueCount("i_folder_id") > 1) {
									child.unlink(obj.getObjectId().toString());
									child.save();
									ObjectChangedTool.objectsChanged(node,
											ApiObjectChanged.CHANGED,
											new IDfPersistentObject[] { obj });
								} else {
									delete(child, children, unlink);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					System.out.println("*** ERROR: " + e);
					e.printStackTrace();
				}

				try {
					if (resVersions == JOptionPane.YES_OPTION)
						if (obj instanceof IDfSysObject) {
							((IDfSysObject) obj).destroyAllVersions();
							// target[i].destroy();
						} else
							obj.destroy();
					else
						obj.destroy();

					ObjectChangedTool.objectsChanged(node,
							ApiObjectChanged.DELETED,
							new IDfPersistentObject[] { obj });
				} catch (Exception e) {
					System.out.println("*** ERROR: " + e);
					e.printStackTrace();
				}
			}

		}, 100);

	}

	public void initAction(PluginNode node, DMConnection con, Element config) {
		// TODO Auto-generated method stub
		timer = ((ApiSystem) node.getSingleApi(ApiSystem.class)).getTimer();
	}

	public void destroyAction() {
		// TODO Auto-generated method stub

	}

	public String getTitle() {
		return null;
	}
}
