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
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiObjectChanged;
import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.plugin.ui.ContentTree.Listener;
import de.mhu.hair.plugin.ui.ContentTree.NodeValue;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.AHeadline;
import de.mhu.lib.swing.LAF;
import de.mhu.lib.swing.PopupButton;
import de.mhu.lib.swing.PopupListener;
import de.mhu.res.img.LUF;

public class TreePlugin extends AbstractHotSelectMenu implements Plugin,
		Listener, ApiObjectChanged {

	private JPanel treepanel;
	// private JComboBox combo;
	private DMConnection con;
	private Timer timer;
	private PluginNode node;
	private ContentTree tree;
	private AHeadline label;
	private Object workMutex = new Object();
	private int workers = 0;
	private JButton bReload;
	private JButton bMenu;
	private PopupButton bFilter;
	protected boolean showDocuments;
	public static boolean TREE_HOT_SELECT = true;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
		node = pNode;

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
			show(con, (IDfSysObject) obj);
		}

		findCabinets();

	}

	public void findCabinets() throws DfException {
		// combo.setEnabled( false );
		treepanel.removeAll();
		treepanel.add(new JLabel("Loading..."));
		addWorkInProgress();
		treepanel.revalidate();
		treepanel.repaint();

		/*
		 * IDfQuery query = con.createQuery(
		 * "select object_name from dm_cabinet ORDER BY object_name" );
		 * IDfCollection col = query.execute( con.getSession(),
		 * IDfQuery.DF_READ_QUERY );
		 * 
		 * combo.removeAllItems(); combo.addItem( "[All]" ); while ( col.next()
		 * ) { IDfValue attrValue = col.getValue( "object_name" );
		 * combo.addItem( "/" + attrValue.asString() ); } col.close();
		 */

		treepanel.removeAll();
		treepanel.revalidate();
		treepanel.repaint();
		removeWorkInProgress();
		// combo.setEnabled( true );
		swingActionHotSelect();
	}

	private void initUI() {
		setLayout(new BorderLayout());
		treepanel = new JPanel();
		treepanel.setLayout(new BorderLayout());

		/*
		 * combo = new JComboBox(); combo.addActionListener( new
		 * ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent e) { swingActionHotSelect();
		 * } });
		 */

		label = LAF.createHeadline(null);
		label.setVisibleToolBar(true);
		bReload = new JButton();
		bReload.setMargin(new Insets(0, 0, 0, 0));
		bReload.setIcon(LUF.RELOAD_ICON);
		bReload.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				workers = 0;
				TreePath[] pathes = tree.getSelectedNode();
				if (pathes == null || pathes.length == 0) {

					// String selected = (String)combo.getSelectedItem();
					try {
						findCabinets();
					} catch (DfException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// combo.setSelectedItem( selected );

				} else {

					for (int i = 0; i < pathes.length; i++)
						if (!tree.refreshPath(pathes[i])) {
							// String selected =
							// (String)combo.getSelectedItem();
							try {
								findCabinets();
							} catch (DfException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							// combo.setSelectedItem( selected );
							break;
						}

				}

			}

		});

		bFilter = new PopupButton(LUF.FILTER_ICON,"Filter");
		bFilter.addCheckbox("Show Documents", new PopupListener() {

			public void beforeVisible(AbstractButton c) {
			}

			public void actionPerformed(ActionEvent e) {
				showDocuments = ((JCheckBoxMenuItem)e.getSource()).isSelected();
				if (tree != null)
					tree.setShowDocuments(showDocuments);
			}
			
		});
		
		label.getToolBar().add(bFilter);
		label.getToolBar().add(bReload);

		add(label, BorderLayout.NORTH);
		add(treepanel, BorderLayout.CENTER);

	}

	protected void swingActionHotSelect() {
		// combo.setEnabled( false );
		timer.schedule(new TimerTask() {

			public void run() {
				actionHotSelect();
			}

		}, 1);
	}

	private void actionHotSelect() {
		// combo.setEnabled( false );
		try {
			treepanel.removeAll();
			treepanel.add(new JLabel("Loading...",LUF.DOT_RED,JLabel.CENTER));
			addWorkInProgress();
			treepanel.revalidate();
			treepanel.repaint();
			// String folderName = (String)combo.getSelectedItem();
			IDfId folderId = null;
			/*
			 * if ( !"[All]".equals( folderName ) ) { IDfFolder folder =
			 * con.getSession().getFolderByPath( folderName ); if( folder ==
			 * null ) {
			 * System.out.println("Folder or cabinet does not exist in the Docbase!"
			 * ); return; } folderId = folder.getObjectId(); }
			 */

			tree = new ContentTree(con, folderId, timer, (ApiTypes) node
					.getSingleApi(ApiTypes.class), this);
			tree.setShowDocuments(showDocuments);
			treepanel.removeAll();
			removeWorkInProgress();
			treepanel.add(tree, BorderLayout.CENTER);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// combo.setEnabled( true );
		treepanel.revalidate();
		treepanel.repaint();

	}

	public void show(DMConnection pCon, IDfSysObject obj) {
		con = pCon;

		try {
			findCabinets();

			treepanel.removeAll();

			ContentTree tree = new ContentTree(con, obj.getObjectId(), timer,
					(ApiTypes) node.getSingleApi(ApiTypes.class), this);
			treepanel.add(tree, BorderLayout.CENTER);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		treepanel.revalidate();
		treepanel.repaint();
	}

	public void selectedEvent(NodeValue[] value, int mode, JComponent src,
			int x, int y) {

		try {
			if (value == null) {
				if (mode == Listener.MODE_HOT_SELECT) {
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
				}
				return;
			}

			final IDfPersistentObject[] obj = new IDfPersistentObject[value.length];
			final IDfPersistentObject[] parents = new IDfPersistentObject[value.length];

			for (int i = 0; i < value.length; i++) {
				obj[i] = con.getPersistentObject(value[i].getId());
				if (value[i].getParentId() != null)
					parents[i] = con
							.getPersistentObject(value[i].getParentId());
			}

			if (mode == Listener.MODE_HOT_SELECT) {
				final ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
						.getApi(ApiObjectHotSelect.class);
				if (list == null)
					return;

				for (int i = 0; i < list.length; i++)
					try {
						if (!list[i].equals(TreePlugin.this))
							list[i].apiObjectDepricated();
					} catch (Throwable t) {
						t.printStackTrace();
					}

				timer.schedule(new TimerTask() {

					public void run() {
						for (int i = 0; i < list.length; i++)
							try {
								if (!list[i].equals(TreePlugin.this))
									list[i].apiObjectHotSelected(con, parents,
											obj);
							} catch (Throwable t) {
								t.printStackTrace();
							}
					}

				}, 1);
			} else if (mode == Listener.MODE_SELECT) {
				/*
				 * final ApiObjectSelect[] list =
				 * (ApiObjectSelect[])node.getApi( ApiObjectSelect.class ); if (
				 * list == null ) return;
				 * 
				 * timer.schedule( new TimerTask() {
				 * 
				 * public void run() { for ( int i = 0; i < list.length; i++ )
				 * try { list[i].apiObjectSelected( con, obj ); } catch (
				 * Throwable t ) { t.printStackTrace(); } }
				 * 
				 * },1);
				 */
			} else if (mode == Listener.MODE_MENU) {
				ObjectWorkerTool.showMenu(node, con, obj, src, x, y);
			}

		} catch (DfException e) {
			e.printStackTrace();
		}

	}

	public void destroyPlugin() throws Exception {
		node.removeApi(this);
		destroyHotSelectMenu();
	}

	public void objectsChanged(int mode, IDfPersistentObject[] objects) {

		// String selected = (String)combo.getSelectedItem();

		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof IDfSysObject) {
				IDfSysObject sys = (IDfSysObject) objects[i];

				try {

					if (mode == ApiObjectChanged.DELETED) {
						tree.objectDelete(sys.getObjectId());
					} else {

						for (int j = 0; j < sys.getFolderIdCount(); j++) {
							IDfFolder folderObject = (IDfFolder) con
									.getExistingObject(((IDfSysObject) objects[i])
											.getFolderId(j));

							String folder = folderObject
									.getString("r_folder_path");

							// if ( folder.startsWith( selected ) ||
							// "[All]".equals( selected ) ) {
							if (mode == ApiObjectChanged.CREATED)
								tree.objectAdd(objects[i]);
							else if (mode == ApiObjectChanged.CHANGED)
								tree.objectUpdate(objects[i]);
							// }
						}
					}
				} catch (DfException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addWorkInProgress() {
		synchronized (workMutex) {
			workers++;
			if (workers < 2)
				setWorking(true);
		}
	}

	public void removeWorkInProgress() {
		synchronized (workMutex) {
			workers--;
			if (workers <= 0) {
				workers = 0;
				setWorking(false);
			}
		}
	}

	public void setWorking(boolean working) {
		label.setIcon(working ? LUF.DOT_RED : LUF.DOT_GREEN);
	}

	public void selectionCanceled() {
		final ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
				.getApi(ApiObjectHotSelect.class);
		if (list == null)
			return;

		for (int i = 0; i < list.length; i++)
			try {
				if (!list[i].equals(TreePlugin.this))
					list[i].apiObjectDepricated();
			} catch (Throwable t) {
				t.printStackTrace();
			}

		timer.schedule(new TimerTask() {

			public void run() {
				for (int i = 0; i < list.length; i++)
					try {
						if (!list[i].equals(TreePlugin.this))
							list[i].apiObjectHotSelected(con, null, null);
					} catch (Throwable t) {
						t.printStackTrace();
					}
			}

		}, 1);
	}

	protected void apiObjectDepricated0() {

	}

	protected void apiObjectHotSelected0(DMConnection con,
			IDfPersistentObject[] parents2, IDfPersistentObject[] obj)
			throws Exception {

		if (!TREE_HOT_SELECT)
			return;

		if (obj == null || obj.length != 1)
			return;

		String path = ObjectTool.getPath(obj[0]);

		boolean flag = true;
		DefaultMutableTreeNode docbasetreenode = tree.getRootNode();

		Vector vector = new Vector();
		vector.addElement(tree.getRootNode());
		for (StringTokenizer stringtokenizer = new StringTokenizer(path, "/"); stringtokenizer
				.hasMoreElements();) {
			String s1 = stringtokenizer.nextToken();

			DefaultMutableTreeNode docbasetreenode1 = null;
			for (Enumeration enumeration = docbasetreenode.children(); enumeration
					.hasMoreElements();) {
				DefaultMutableTreeNode docbasetreenode2 = (DefaultMutableTreeNode) enumeration
						.nextElement();
				Object uo = docbasetreenode2.getUserObject();
				if (uo instanceof ContentTree.NodeValue) {
					if (stringtokenizer.hasMoreTokens()) {
						if (((ContentTree.NodeValue) uo).getName().equals(s1)) {
							docbasetreenode1 = docbasetreenode2;
							break;
						}
					} else {
						// find last element by id
						if (((ContentTree.NodeValue) uo).getId().equals(
								obj[0].getObjectId())) {
							docbasetreenode1 = docbasetreenode2;
							break;
						}
					}
				}
			}
			if (docbasetreenode1 != null) {

				tree.loadChilds(docbasetreenode1, false);
				vector.addElement(docbasetreenode1);
				docbasetreenode = docbasetreenode1;
			} else if (!stringtokenizer.hasMoreTokens())
				flag = false;
		}

		TreePath treePath = vectorToPath(vector);
		tree.getTree().expandPath(treePath);
		tree.getTree().setSelectionPath(treePath);
		tree.getTree().scrollPathToVisible(treePath);

	}

	public TreePath vectorToPath(Vector vector) {
		Object aobj[] = new Object[vector.size()];
		for (int i = 0; i < vector.size(); i++)
			aobj[i] = vector.elementAt(i);

		return new TreePath(aobj);
	}

}
