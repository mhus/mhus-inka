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

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiObjectSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.lib.ASql;
import de.mhu.res.img.LUF;

public class RelationsPlugin extends AbstractHotSelectMenu implements Plugin,
		ApiObjectHotSelect {
	private DMConnection con;
	private DMList relChildList;
	private DMList relParentList;
	private PluginNode node;
	private IDfPersistentObject object;
	private IDfPersistentObject[] selectedObjects;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		node = pNode;
	
		initUI();

		((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(this,
				pConfig.getNode());

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			initHotSelectMenu(pNode, pConfig, this);
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			showObj(con, obj);
		}

	}

	private void initUI() {
		relChildList = new DMList(" Parents", new DMListChangeObjListener( node, 0 ) );
		relChildList.setMaxItems(100);

		JButton button = new JButton(LUF.MENU_ICON);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (selectedObjects == null)
					return;

				JPopupMenu menu = new JPopupMenu();
				JMenuItem item = new JMenuItem("Create");
				item.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {

						String id = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Parent ID:");
						if (id == null)
							return;

						String name = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Relation Name:");
						if (name == null)
							return;

						String description = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Description:");
						if (description == null)
							return;

						int myIdType = JOptionPane.showConfirmDialog(
								RelationsPlugin.this,
								"Use Chronicle Id as child?");
						if (myIdType != JOptionPane.YES_OPTION
								&& myIdType != JOptionPane.NO_OPTION)
							return;

						for (int i = 0; i < selectedObjects.length; i++) {
							try {
								String dql = "CREATE dm_relation OBJECT SET child_id='"
										+ (myIdType != JOptionPane.YES_OPTION ? ((IDfSysObject) selectedObjects[i])
												.getChronicleId().toString()
												: selectedObjects[i]
														.getObjectId()
														.toString())
										+ "', "
										+ "SET parent_id='"
										+ id
										+ "', "
										+ "SET relation_name='"
										+ name
										+ "', "
										+ "SET permanent_link=1, "
										+ "SET child_label='CURRENT', "
										+ "SET order_no=0, "
										+ "SET description='"
										+ ASql.escape(description) + "'";
								IDfQuery query = con.createQuery(dql);
								query.execute(con.getSession(),
										IDfQuery.READ_QUERY).close();

							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}

						try {
							showObj(con, object);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}

				});
				menu.add(item);

				if (relChildList.getTable().getSelectedRowCount() > 0) {
					item = new JMenuItem("Delete");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							if (JOptionPane.showConfirmDialog(
									RelationsPlugin.this, "Delete Relations?") != JOptionPane.YES_OPTION)
								return;
							int[] rows = relChildList.getTable()
									.getSelectedRows();
							for (int i = 0; i < rows.length; i++) {
								String id = (String) relChildList.getTable()
										.getValueAt(rows[i], 0);
								if (id != null && id.length() == 16) {
									String dql = "DELETE dm_relation OBJECT WHERE r_object_id='"
											+ id + "'";
									IDfQuery query = con.createQuery(dql);
									try {
										query.execute(con.getSession(),
												IDfQuery.READ_QUERY).close();
									} catch (DfException e1) {
										e1.printStackTrace();
									}
								}
							}
							try {
								showObj(con, object);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}

					});
					menu.add(item);
				}

				JButton src = (JButton) e.getSource();
				menu.show(src, 0, src.getHeight());

			}

		});
		relChildList.setAccessory(button);

		relParentList = new DMList(" Childs", new DMListChangeObjListener(node,0));
		relParentList.setMaxItems(100);
		button = new JButton(LUF.MENU_ICON);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (selectedObjects == null)
					return;

				JPopupMenu menu = new JPopupMenu();
				JMenuItem item = new JMenuItem("Create");
				item.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {

						String id = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Child ID:");
						if (id == null)
							return;

						String name = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Relation Name:");
						if (name == null)
							return;

						String description = JOptionPane.showInputDialog(
								RelationsPlugin.this, "New Description:");
						if (description == null)
							return;

						int myIdType = JOptionPane.showConfirmDialog(
								RelationsPlugin.this,
								"Use Chronicle Id as child?");
						if (myIdType != JOptionPane.YES_OPTION
								&& myIdType != JOptionPane.NO_OPTION)
							return;

						for (int i = 0; i < selectedObjects.length; i++) {
							try {
								String dql = "CREATE dm_relation OBJECT SET parent_id='"
										+ (myIdType != JOptionPane.YES_OPTION ? ((IDfSysObject) selectedObjects[i])
												.getChronicleId().toString()
												: selectedObjects[i]
														.getObjectId()
														.toString())
										+ "', "
										+ "SET child_id='"
										+ id
										+ "', "
										+ "SET relation_name='"
										+ name
										+ "', "
										+ "SET permanent_link=1, "
										+ "SET child_label='CURRENT', "
										+ "SET order_no=0,"
										+ "SET description='"
										+ ASql.escape(description) + "'";
								IDfQuery query = con.createQuery(dql);
								query.execute(con.getSession(),
										IDfQuery.READ_QUERY).close();

							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}

						try {
							showObj(con, object);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}

				});
				menu.add(item);

				if (object != null
						&& relParentList.getTable().getSelectedRowCount() > 0) {
					item = new JMenuItem("Delete");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							if (JOptionPane.showConfirmDialog(
									RelationsPlugin.this, "Delete Relations?") != JOptionPane.YES_OPTION)
								return;
							int[] rows = relParentList.getTable()
									.getSelectedRows();
							for (int i = 0; i < rows.length; i++) {
								String id = (String) relParentList.getTable()
										.getValueAt(rows[i], 0);
								if (id != null && id.length() == 16) {
									String dql = "DELETE dm_relation OBJECT WHERE r_object_id='"
											+ id + "'";
									IDfQuery query = con.createQuery(dql);
									try {
										query.execute(con.getSession(),
												IDfQuery.READ_QUERY).close();
									} catch (DfException e1) {
										e1.printStackTrace();
									}
								}
							}
							try {
								showObj(con, object);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}

					});
					menu.add(item);
				}

				JButton src = (JButton) e.getSource();
				menu.show(src, 0, src.getHeight());

			}

		});
		relParentList.setAccessory(button);

		JSplitPane split = new JSplitPane();
		split.setLeftComponent(relChildList);
		split.setRightComponent(relParentList);
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);

	}

	public void apiObjectHotSelected0(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] obj)
			throws Exception {
		if (obj == null || obj.length != 1) {
			relChildList.clear();
			relParentList.clear();
			object = null;
			selectedObjects = obj;
		} else {
			showObj(pCon, obj[0]);
			object = obj[0];
			selectedObjects = obj;
		}
		relChildList.getLabel().setIcon(LUF.DOT_GREEN);
		relParentList.getLabel().setIcon(LUF.DOT_GREEN);
	}

	private void showObj(DMConnection pCon, IDfPersistentObject obj)
			throws Exception {
		con = pCon;
		object = obj;
		
		if ( object instanceof IDfSysObject ) {
			IDfQuery query = con
					.createQuery("SELECT * FROM dm_relation WHERE child_id='"
							+ obj.getObjectId().toString() + "' OR child_id='"
							+ obj.getString("i_chronicle_id") + "'");
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.DF_READ_QUERY);
			relChildList.show(con, res);
			res.close();
	
			query = con.createQuery("SELECT * FROM dm_relation WHERE parent_id='"
					+ obj.getObjectId().toString() + "' OR parent_id='"
					+ obj.getString("i_chronicle_id") + "'");
			res = query.execute(con.getSession(), IDfQuery.DF_READ_QUERY);
			relParentList.show(con, res);
			res.close();
		} else {
			relChildList.clear();
			relParentList.clear();
			object = null;
			selectedObjects = null;
		}
	}

	public void destroyPlugin() throws Exception {
		destroyHotSelectMenu();
	}

	public void apiObjectDepricated0() {
		relChildList.getLabel().setIcon(LUF.DOT_RED);
		relParentList.getLabel().setIcon(LUF.DOT_RED);
		object = null;
		selectedObjects = null;
	}
}
