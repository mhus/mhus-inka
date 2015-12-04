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
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.ui.DMList.Listener;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.PopupButton;
import de.mhu.lib.swing.PopupListener;
import de.mhu.res.img.LUF;

public class DocumentPlugin extends AbstractHotSelectMenu implements Plugin,
		ApiObjectChanged {

	private DMConnection con;
	private PluginNode node;
	private IDfFolder selected;
	private JButton bReload;
	//private JToggleButton bShowVersions;
	protected boolean showVersions;
	private DMList list;
	private Timer timer;
	private PopupButton bFilter;
	protected boolean showDeleted;
	protected boolean showFolders;

	protected void apiObjectDepricated0() {
		list.getLabel().setIcon(LUF.DOT_RED);
	}

	protected void apiObjectHotSelected0(DMConnection con,
			IDfPersistentObject[] parents2, IDfPersistentObject[] obj)
			throws Exception {
		
			showObj(con, parents2, obj);
		list.getLabel().setIcon(LUF.DOT_GREEN);
	}

	public void destroyPlugin() throws Exception {

	}

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem)pNode.getSingleApi(ApiSystem.class)).getTimer();
		node = pNode;

		showFolders = true;
		
		initUI();

		node.addApi(ApiObjectChanged.class, this);

		try {
			((ApiLayout) pNode.getSingleApi(ApiLayout.class)).setComponent(
					this, pConfig.getNode());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			initHotSelectMenu(pNode, pConfig, this);
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			if (obj instanceof IDfFolder)
				showObj(con, (IDfFolder) obj);
		}
	}

	private void showObj(DMConnection con2, IDfPersistentObject obj) {
		showObj(con2, null, new IDfPersistentObject[] { obj } );
	}
	
	private void showObj(DMConnection con2, IDfPersistentObject[] parents, IDfPersistentObject[] obj) {
		try {
			list.setConnection(con2);
			// if invalide object
			if (obj == null || obj.length < 1 || obj[0] == null ) {
				list.clear();
				return;
			}
			
			IDfFolder sel = null;
			
			// if selectino is a document or a folder
			if ( !(obj[0] instanceof IDfFolder) ) {
				if ( parents != null && parents.length > 0 && parents[0] != null && (parents[0] instanceof IDfFolder) ) {
					sel = (IDfFolder)parents[0];
				} else {
					sel = (IDfFolder)con.getExistingObject( obj[0].getRepeatingString("i_folder_id", 0) );
				}
			} else {
				sel = (IDfFolder)obj[0];
			}

			// maybe clean up list
			if ( this.selected == null || sel == null || ! this.selected.equals( sel ) ) {
				list.clear();
				this.selected = sel;
			}
			list.getLabel().setText(ObjectTool.getPath(selected));
			
			if ( list.getTable().getRowCount() == 0 ) {
			
				String dql = "select SUBSTR(r_object_id,0,2),UPPER(object_name),r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label,r_creation_date from ";
				
				if ( showFolders )
					dql+="dm_sysobject ";
				else
					dql+="dm_document ";
				
				if ( showDeleted )
					dql+="(deleted) ";
				else
				if ( showVersions )
					dql+="(all) ";
				
				dql+="where any i_folder_id='" + selected.getObjectId() + 
					 "' order by 1 desc,2,r_creation_date,language_code";
								
				IDfQuery query = con2.createQuery(dql);
				IDfCollection res = query.execute(con2.getSession(),
						IDfQuery.EXEC_QUERY);
				list.merge(res);
				list.updateHeader();
				res.close();

			}
			
			if ( !(obj[0] instanceof IDfFolder) ) {
				list.getTable().getSelectionModel().clearSelection();
				for ( int i = 0; i < list.getTable().getRowCount(); i++) {
					String idA = list.getUserObject( i );
					for ( IDfPersistentObject objB : obj ) {
						String idB = objB.getObjectId().getId();
						if ( idA.equals( idB ) )
							list.getTable().getSelectionModel().addSelectionInterval(i, i);
					}
				}
			}
			
			list.getLabel().setIcon(LUF.DOT_GREEN);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			list.clear();
		}
	}
	
//	public void show(DMConnection con2, IDfFolder object) {
//		con = con2;
//		selected = object;
//		setWorking(true);
//		timer.schedule(new ATimerTask() {
//
//			public void run0() throws Exception {
//
//				label.setText(ObjectTool.getPath(selected));
//				listModel.removeAllElements();
//				String dql = "";
//				if (showVersions)
//					dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_document (all) where any i_folder_id='"
//							+ selected.getObjectId() + "'";
//				else
//					dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_document where any i_folder_id='"
//							+ selected.getObjectId() + "'";
//
//				IDfQuery query = con.createQuery(dql);
//
//				IDfCollection res = query.execute(con.getSession(),
//						IDfQuery.READ_QUERY);
//				
////				final TreeMap v = new TreeMap();
////				while (res.next()) {
////					// System.out.println( "FOUND CHILD: " + res.getString(
////					// "object_name" ) );
////					String key = res.getString("object_name");
////
////					String[] cVersion = new String[res
////							.getValueCount("r_version_label")];
////					for (int z = 0; z < cVersion.length; z++) {
////						cVersion[z] = res.getRepeatingString("r_version_label",
////								z);
////						key = key + "_" + cVersion[z];
////					}
////
////					v.put(key, new NodeValue(new DfId(res
////							.getString("r_object_id")), selected.getObjectId(),
////							res.getString("object_name"), res
////									.getString("r_object_type"), res
////									.getString("a_content_type"), res
////									.getString("language_code"), cVersion));
////				}
////				res.close();
////
////				SwingUtilities.invokeLater(new Runnable() {
////
////					public void run() {
////						for (Iterator i = v.values().iterator(); i.hasNext();) {
////							NodeValue value = (NodeValue) i.next();
////							listModel.addElement(value);
////						}
////					}
////
////				});
//
//			}
//
//			public void onFinal(boolean isError) {
//				setWorking(false);
//			}
//
//		}, 100);
//
//	}

	private void initUI() {

		setLayout(new BorderLayout());
		list = new DMList("", new ListListener(), new String[] { "object_name","r_object_type","a_content_type","language_code","r_version_label" }, "r_object_id");
		list.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		DocumentTableCellRenderer renderer = new DocumentTableCellRenderer(list);
		renderer.setColName(0);
		renderer.setColType(1);
		renderer.setColContentType(2);
		renderer.setColLang(3);
		renderer.setColVersion(4);
		list.getTable().setDefaultRenderer(Object.class, renderer);
		add(list, BorderLayout.CENTER);
		
		JPanel accessories = new JPanel();
		accessories.setLayout(new BoxLayout(accessories,BoxLayout.X_AXIS));
		
//		list.addListSelectionListener(new ListSelectionListener() {
//
//			public void valueChanged(ListSelectionEvent arg0) {
//
//				if (arg0.getValueIsAdjusting())
//					return;
//
//				// System.out.println( "Document send HotSelect " );
//				Object[] value = list.getSelectedValues();
//				try {
//					if (value == null) {
//						ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
//								.getApi(ApiObjectHotSelect.class);
//						if (list == null)
//							return;
//
//						for (int i = 0; i < list.length; i++)
//							try {
//								list[i].apiObjectHotSelected(con, null, null);
//							} catch (Throwable t) {
//								t.printStackTrace();
//							}
//						return;
//					}
//
//					final IDfPersistentObject[] obj = new IDfPersistentObject[value.length];
//					final IDfPersistentObject[] parents = new IDfPersistentObject[value.length];
//
//					for (int i = 0; i < value.length; i++) {
//						obj[i] = con.getPersistentObject(((NodeValue) value[i])
//								.getId());
//						if (((NodeValue) value[i]).getParentId() != null)
//							parents[i] = con
//									.getPersistentObject(((NodeValue) value[i])
//											.getParentId());
//					}
//
//					final ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
//							.getApi(ApiObjectHotSelect.class);
//					if (list == null)
//						return;
//
//					for (int i = 0; i < list.length; i++)
//						try {
//							if (!list[i].equals(DocumentPlugin.this)
//									&& !(list[i] instanceof TreePlugin))
//								list[i].apiObjectDepricated();
//						} catch (Throwable t) {
//							t.printStackTrace();
//						}
//
//					timer.schedule(new TimerTask() {
//
//						public void run() {
//							for (int i = 0; i < list.length; i++)
//								try {
//									if (!list[i].equals(DocumentPlugin.this)
//											&& !(list[i] instanceof TreePlugin))
//										list[i].apiObjectHotSelected(con,
//												parents, obj);
//								} catch (Throwable t) {
//									t.printStackTrace();
//								}
//						}
//
//					}, 1);
//
//				} catch (DfException e) {
//					e.printStackTrace();
//				}
//
//			}
//
//		});
//
		bReload = new JButton();
		bReload.setBorderPainted(false);
		bReload.setRolloverEnabled(true);
		bReload.setMargin(new Insets(0, 0, 0, 0));
		bReload.setIcon(LUF.RELOAD_ICON);
		bReload.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				list.getLabel().setText("");
				list.clear();
				showObj(con, selected);
			}
		});

		bFilter = new PopupButton(LUF.FILTER_ICON,"Filter");
		
		bFilter.addCheckbox("Show Folders", new PopupListener() {

			public void beforeVisible(AbstractButton c) {
			}

			public void actionPerformed(ActionEvent e) {
				showFolders = ((JCheckBoxMenuItem)e.getSource()).isSelected();
				list.clear();
				showObj(con, selected);
			}
			
		}).setSelected(showFolders);
		
		
		bFilter.addCheckbox("Show Versions", new PopupListener() {

			public void beforeVisible(AbstractButton c) {
			}

			public void actionPerformed(ActionEvent e) {
				showVersions = ((JCheckBoxMenuItem)e.getSource()).isSelected();
				list.clear();
				showObj(con, selected);
			}
			
		}).setSelected(showVersions);
		
		bFilter.addCheckbox("Show Deleted", new PopupListener() {

			public void beforeVisible(AbstractButton c) {
			}

			public void actionPerformed(ActionEvent e) {
				showDeleted = ((JCheckBoxMenuItem)e.getSource()).isSelected();
				list.clear();
				showObj(con, selected);				
			}
			
		}).setSelected(showDeleted);
		
		
		accessories.add(bFilter);
		accessories.add(bReload);
		
//
//		JPanel panel1 = new JPanel();
//		panel1.setLayout(new BorderLayout());
//
//		JPanel panel2 = new JPanel();
//		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
//		panel2.add(bShowVersions);
//		panel2.add(bReload);
//
//		panel1.add(label, BorderLayout.NORTH);
//		panel1.add(panel2, BorderLayout.EAST);
//		add(panel1, BorderLayout.NORTH);
		
		
		list.setAccessory( accessories );

	}

	public void objectsChanged(int mode, IDfPersistentObject[] objects) {
		
	}
	
	public class ListListener extends DMListUserFieldObjListener {
		
		public ListListener() {
			super(node);
		}
	
		public boolean isCellEditable(DMList list, int row, int col) {
			return false;
		}
	
		public boolean isEditable(DMList list) {
			return false;
		}
	
		public boolean selectionValueChanged(ListSelectionEvent e,String[] ids) {
			if (ids == null) return false;
			int[] rows = list.getTable().getSelectedRows();
			DfId[] dfids = new DfId[rows.length];
			for ( int i = 0; i < ids.length; i++ )
				dfids[i] = new DfId(list.getUserObject(rows[i]));
			
			actionSelect2(dfids);
			return true;
		}

		public void mouseClickedEvent(DMList list, String[] ids, MouseEvent me) {
			try {
				if (me.getButton() == MouseEvent.BUTTON1
						&& me.getClickCount() == 1) {
					// no more
				} else {
					super.mouseClickedEvent(list, ids, me);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		protected void actionSelect2(IDfId[] id) {
			try {
				if (id == null || id.length == 0) {
					ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
							.getApi(ApiObjectHotSelect.class);
					if (list == null)
						return;
		
					for (int i = 0; i < list.length; i++)
						try {
							list[i].apiObjectHotSelected(con, null, null);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					return;
				}
		
				final IDfPersistentObject[] obj = new IDfPersistentObject[id.length];
				final IDfPersistentObject[] parents = new IDfPersistentObject[id.length];
				
				for (int i = 0; i < id.length; i++) {
					obj[i] = con.getPersistentObject( id[i] );
					parents[i] = selected;
				}
		
				final ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
						.getApi(ApiObjectHotSelect.class);
				if (list == null)
					return;
		
				for (int i = 0; i < list.length; i++)
					try {
						if (!list[i].equals(DocumentPlugin.this)
								&& !(list[i] instanceof TreePlugin))
							list[i].apiObjectDepricated();
					} catch (Throwable t) {
						t.printStackTrace();
					}
		
				timer.schedule(new TimerTask() {
		
					public void run() {
						for (int i = 0; i < list.length; i++)
							try {
								if (!list[i].equals(DocumentPlugin.this)
										&& !(list[i] instanceof TreePlugin))
									list[i].apiObjectHotSelected(con,
											parents, obj);
							} catch (Throwable t) {
								t.printStackTrace();
							}
					}
		
				}, 1);
		
			} catch (DfException e) {
				e.printStackTrace();
			}
		}
	
		public boolean valueChangedEvent(DMList list, int row, int col,
				Object oldValue, Object newValue) {
			return false;
		}
	}
}
