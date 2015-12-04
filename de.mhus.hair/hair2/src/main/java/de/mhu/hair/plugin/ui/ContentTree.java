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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.tools.DctmTool;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.res.img.LUF;

public class ContentTree extends JPanel {

	private DefaultTreeModel treeModel;
	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private DMConnection con;
	private Listener listener;
	private Timer timer;
	private Object workMutex;
	private int workers;
	private ApiTypes typesApi;
	private boolean showDocuments;

	public ContentTree(DMConnection pConnection, IDfId pRootId, Timer pTimer,
			ApiTypes pTypesApi, Listener pListener) throws Exception {

		con = pConnection;
		listener = pListener;
		timer = pTimer;
		typesApi = pTypesApi;

		if (pRootId == null) {

			initUI("Cabinets");

			IDfQuery query = con
					.createQuery("select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_cabinet ORDER BY object_name");
			IDfCollection col = query.execute(con.getSession(),
					IDfQuery.DF_READ_QUERY);
			while (col.next()) {
				IDfId id = col.getId("r_object_id");
				String name = col.getString("object_name");
				String type = col.getString("r_object_type");
				String language = col.getString("language_code");
				String cType = col.getString("a_content_type");
				String[] cVersion = new String[col
						.getValueCount("r_version_label")];
				for (int z = 0; z < cVersion.length; z++)
					cVersion[z] = col.getRepeatingString("r_version_label", z);

				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
						new NodeValue(id, null, name, type, cType, language,
								cVersion));
				treeModel.insertNodeInto(childNode, rootNode, rootNode
						.getChildCount());
				lookForChilds(childNode);
			}
			col.close();

		} else {

			// IDfSysObject obj = con.getExistingObject( pRootId );
			String dql = "";
			if (showDocuments)
				dql = "select object_name,r_object_type,a_content_type,language_code,r_version_label from dm_sysobject where r_object_id='"
						+ pRootId.getId() + "'";
			else
				dql = "select object_name,r_object_type,a_content_type,language_code,r_version_label from dm_folder where r_object_id='"
						+ pRootId.getId() + "'";
			IDfQuery query = con.createQuery(dql);
			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			res.next();
			String name = res.getString("object_name");
			String type = res.getString("r_object_type");
			String language = res.getString("language_code");
			String cType = res.getString("a_content_type");
			String[] cVersion = new String[res.getValueCount("r_version_label")];
			for (int z = 0; z < cVersion.length; z++)
				cVersion[z] = res.getRepeatingString("r_version_label", z);

			res.close();

			initUI(new NodeValue(pRootId, pRootId, name, type, cType, language,
					cVersion));
			lookForChilds(rootNode);
		}

		tree.expandRow(0);
	}

	private void lookForChilds(DefaultMutableTreeNode node) throws Exception {
		try {
			NodeValue value = (NodeValue) node.getUserObject();
			if (value.isExpanded() || value.isFolderChecked()
					|| node.getChildCount() != 0)
				return;

			value.setIsFolderChecked(true);
			if (typesApi.isTypeOf("dm_folder", typesApi
					.getType(value.getType()))) {
				treeModel.insertNodeInto(new DefaultMutableTreeNode(
						"Please Wait"), node, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadChilds(DefaultMutableTreeNode node, boolean reload)
			throws Exception {
		try {
			final boolean removeFirst = node.getChildCount() != 0;
			if (reload) {
				while (node.getChildCount() != 0)
					treeModel.removeNodeFromParent((MutableTreeNode) node
							.getChildAt(0));
				if (removeFirst)
					treeModel.insertNodeInto(new DefaultMutableTreeNode(
							"Please Wait"), node, 0);
			}

			NodeValue nodeValue = (NodeValue) node.getUserObject();

			// if ( !nodeValue.isFolderChecked() && node.getChildCount() != 0 )
			// return;

			if (!reload && nodeValue.isExpanded())
				return;

			// System.out.println( "--- Load childs for " + nodeValue );
			if (listener != null)
				listener.addWorkInProgress();
			final IDfId id = nodeValue.getId();
			nodeValue.setExpanded();
			/*
			 * IDfCollection dir = null; try { dir = con.getChilds( id ); if (
			 * dir == null ) return; } catch ( Exception e ) {
			 * System.out.println( e ); return; } while ( dir.next() ) {
			 * 
			 * NodeValue value = new NodeValue( new DfId( dir.getString(
			 * "r_object_id" ) ), dir.getString( "object_name" ), dir.getString(
			 * "r_object_type" ) ); DefaultMutableTreeNode child = new
			 * DefaultMutableTreeNode( value ); treeModel.insertNodeInto( child,
			 * node, 0 ); //loadChilds( child ); }
			 */

			String dql = "";
			if (showDocuments)
				dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_sysobject where any i_folder_id='"
						+ id.getId() + "'";
			else
				dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_folder where any i_folder_id='"
						+ id.getId() + "'";
			IDfQuery query = con.createQuery(dql);

			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			final TreeMap v = new TreeMap();
			while (res.next()) {
				// System.out.println( "FOUND CHILD: " + res.getString(
				// "object_name" ) );
				String key = res.getString("object_name") + "_"
						+ res.getString("r_object_id");
				if (typesApi.isFolder(res.getString("r_object_type")))
					key = " " + key;

				String[] cVersion = new String[res
						.getValueCount("r_version_label")];
				for (int z = 0; z < cVersion.length; z++)
					cVersion[z] = res.getRepeatingString("r_version_label", z);

				v.put(key, new NodeValue(
						new DfId(res.getString("r_object_id")), id, res
								.getString("object_name"), res
								.getString("r_object_type"), res
								.getString("a_content_type"), res
								.getString("language_code"), cVersion));
			}
			res.close();
			if (listener != null)
				listener.removeWorkInProgress();

			for (Iterator i = v.values().iterator(); i.hasNext();) {
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(i
						.next());
				treeModel.insertNodeInto(newNode, node, node.getChildCount());
				try {
					lookForChilds(newNode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (removeFirst)
				treeModel.removeNodeFromParent((MutableTreeNode) node
						.getChildAt(0));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadChildsAsync(final DefaultMutableTreeNode node,
			boolean reload) throws Exception {

		try {
			final boolean removeFirst = node.getChildCount() != 0;
			if (reload) {
				while (node.getChildCount() != 0)
					treeModel.removeNodeFromParent((MutableTreeNode) node
							.getChildAt(0));
				if (removeFirst)
					treeModel.insertNodeInto(new DefaultMutableTreeNode(
							"Please Wait"), node, 0);
			}

			NodeValue nodeValue = (NodeValue) node.getUserObject();

			// if ( !nodeValue.isFolderChecked() && node.getChildCount() != 0 )
			// return;

			if (!reload && nodeValue.isExpanded())
				return;

			// System.out.println( "--- Load childs for " + nodeValue );
			if (listener != null)
				listener.addWorkInProgress();
			final IDfId id = nodeValue.getId();
			nodeValue.setExpanded();
			/*
			 * IDfCollection dir = null; try { dir = con.getChilds( id ); if (
			 * dir == null ) return; } catch ( Exception e ) {
			 * System.out.println( e ); return; } while ( dir.next() ) {
			 * 
			 * NodeValue value = new NodeValue( new DfId( dir.getString(
			 * "r_object_id" ) ), dir.getString( "object_name" ), dir.getString(
			 * "r_object_type" ) ); DefaultMutableTreeNode child = new
			 * DefaultMutableTreeNode( value ); treeModel.insertNodeInto( child,
			 * node, 0 ); //loadChilds( child ); }
			 */

			String dql = "";
			if (showDocuments) {
				dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_sysobject where any i_folder_id='"
						+ id.getId() + "'";
			} else {
				dql = "select r_object_id,object_name,r_object_type,a_content_type,language_code,r_version_label from dm_folder where any i_folder_id='"
						+ id.getId() + "'";
			}

			IDfQuery query = con.createQuery(dql);

			IDfCollection res = query.execute(con.getSession(),
					IDfQuery.READ_QUERY);
			final TreeMap v = new TreeMap();
			while (res.next()) {
				// System.out.println( "FOUND CHILD: " + res.getString(
				// "object_name" ) );
				String key = res.getString("object_name") + "_"
						+ res.getString("r_object_id");
				if (typesApi.isTypeOf("dm_folder", typesApi.getType(res
						.getString("r_object_type"))))
					key = " " + key;

				String[] cVersion = new String[res
						.getValueCount("r_version_label")];
				for (int z = 0; z < cVersion.length; z++)
					cVersion[z] = res.getRepeatingString("r_version_label", z);

				v.put(key, new NodeValue(
						new DfId(res.getString("r_object_id")), id, res
								.getString("object_name"), res
								.getString("r_object_type"), res
								.getString("a_content_type"), res
								.getString("language_code"), cVersion));
			}
			res.close();
			if (listener != null)
				listener.removeWorkInProgress();

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					for (Iterator i = v.values().iterator(); i.hasNext();) {
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
								i.next());
						treeModel.insertNodeInto(newNode, node, node
								.getChildCount());
						try {
							lookForChilds(newNode);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (removeFirst)
						treeModel.removeNodeFromParent((MutableTreeNode) node
								.getChildAt(0));
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initUI(Object rootUserObj) {

		rootNode = new DefaultMutableTreeNode(rootUserObj);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {

				setPreferredSize(new Dimension(500, 20));
				Object uoObj = ((DefaultMutableTreeNode) value).getUserObject();
				if (uoObj instanceof NodeValue) {
					NodeValue uo = (NodeValue) uoObj;
					Icon icon = ObjectTool.getIcon(uo.getType(), uo
							.getContentType(),DctmTool.isFolder(uo.getId().getId()) );
					if (icon != null) {
						setOpenIcon(icon);
						setClosedIcon(icon);
						setLeafIcon(icon);
					}
				} else if (uoObj instanceof String) {
					String uo = (String) uoObj;
					if ("Cabinets".equals(uo)) {
						setOpenIcon(LUF.HAIR_ICON);
						setClosedIcon(LUF.HAIR_ICON);
						setLeafIcon(LUF.HAIR_ICON);
					} else {
						setOpenIcon(LUF.DOT_RED);
						setClosedIcon(LUF.DOT_RED);
						setLeafIcon(LUF.DOT_RED);
					}
				} else {
				}
				return super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);

			}
		});

		tree.addTreeExpansionListener(new TreeExpansionListener() {

			public void treeCollapsed(TreeExpansionEvent event) {

			}

			public void treeExpanded(TreeExpansionEvent event) {

				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event
						.getPath().getLastPathComponent();
				Object valueObj = node.getUserObject();
				if (!(valueObj instanceof NodeValue))
					return;

				final NodeValue value = (NodeValue) valueObj;

				if (value.isExpanded())
					return;

				timer.schedule(new TimerTask() {

					public void run() {
						try {

							// while ( node.getChildCount() != 0 )
							// treeModel.removeNodeFromParent(
							// (MutableTreeNode)node.getChildAt( 0 ) );

							loadChildsAsync(node, false);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}, 100);

			}

		});

		JScrollPane scrollPane = new JScrollPane(tree);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		tree.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener() {

					public void valueChanged(TreeSelectionEvent e) {

						if (listener == null)
							return;

						TreePath[] pathes = tree.getSelectionPaths();
						if (pathes == null || pathes.length == 0) {
							listener.selectionCanceled();
							return;
						}

						NodeValue[] selection = new NodeValue[pathes.length];
						for (int i = 0; i < pathes.length; i++) {
							Object object = ((DefaultMutableTreeNode) pathes[i]
									.getLastPathComponent()).getUserObject();
							if (!(object instanceof NodeValue)) {
								listener.selectedEvent(null,
										Listener.MODE_HOT_SELECT, tree, 0, 0);
								return;
							}
							selection[i] = (NodeValue) object;
						}

						listener.selectedEvent(selection,
								Listener.MODE_HOT_SELECT, tree, 0, 0);

					}

				});

		tree.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {

				if (listener == null)
					return;

				TreePath[] pathes = tree.getSelectionPaths();
				if (pathes == null || pathes.length == 0) {
					listener.selectionCanceled();
					return;
				}

				int mode = Listener.MODE_UNKNOWN;
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() < 2)
					// mode = Listener.MODE_HOT_SELECT;
					return; // already done by selection listener
				else if (e.getButton() == MouseEvent.BUTTON1)
					mode = Listener.MODE_SELECT;
				else if (e.getButton() == MouseEvent.BUTTON3)
					mode = Listener.MODE_MENU;

				NodeValue[] selection = new NodeValue[pathes.length];
				for (int i = 0; i < pathes.length; i++) {
					Object object = ((DefaultMutableTreeNode) pathes[i]
							.getLastPathComponent()).getUserObject();
					if (!(object instanceof NodeValue)) {
						listener.selectedEvent(null, mode, tree, e.getX(), e
								.getY());
						return;
					}
					selection[i] = (NodeValue) object;
				}

				listener.selectedEvent(selection, mode, tree, e.getX(), e
						.getY());
			}
		});

	}

	public static class NodeValue {
		private IDfId id;
		private String name;
		private String type;
		private boolean expanded = false;
		private String cType;
		private boolean isFolderChecked;
		private String language;
		private IDfId parent;
		private String[] version;
		private String versionText;

		public NodeValue(IDfId pId, IDfId pParent, String pName, String pType,
				String pContentType, String pLanguage, String[] pVersion) {
			id = pId;
			name = pName;
			type = pType;
			cType = pContentType;
			language = pLanguage;
			parent = pParent;
			version = pVersion;
			versionText = "";
			for (int i = 0; i < version.length; i++)
				versionText = versionText + version[i] + ',';
		}

		public void setIsFolderChecked(boolean in) {
			isFolderChecked = in;
		}

		public boolean isFolderChecked() {
			return isFolderChecked;
		}

		public IDfId getId() {
			return id;
		}

		public IDfId getParentId() {
			return parent;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public String getContentType() {
			return cType;
		}

		public String toString() {
			return getName() + " (" + getLanguage() + "," + getType() + ") "
					+ versionText;
		}

		public String getLanguage() {
			return language;
		}

		public void setExpanded() {
			expanded = true;
		}

		public boolean isExpanded() {
			return expanded;
		}

		public void setName(String objectName) {
			name = objectName;
		}

	}

	public static interface Listener {

		final int MODE_MENU = 3;
		final int MODE_SELECT = 2;
		final int MODE_HOT_SELECT = 1;
		final int MODE_UNKNOWN = 0;

		void selectedEvent(NodeValue[] selection, int mode, JComponent src,
				int x, int y);

		void selectionCanceled();

		void addWorkInProgress();

		void removeWorkInProgress();

	}

	public void objectDelete(IDfId objectId) {
		DefaultMutableTreeNode node = findNode(objectId, rootNode);
		if (node == null)
			return;
		treeModel.removeNodeFromParent(node);
	}

	private DefaultMutableTreeNode findNode(IDfId id,
			DefaultMutableTreeNode root) {
		if (root.getUserObject() instanceof String) {
			for (int i = 0; i < root.getChildCount(); i++) {
				DefaultMutableTreeNode node = findNode(id,
						(DefaultMutableTreeNode) root.getChildAt(i));
				if (node != null)
					return node;
			}
			return null;
		}
		if (((NodeValue) root.getUserObject()).getId().equals(id))
			return root;
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode node = findNode(id,
					(DefaultMutableTreeNode) root.getChildAt(i));
			if (node != null)
				return node;
		}
		return null;
	}

	public void objectAdd(IDfPersistentObject object) {
		int cnt;
		try {
			cnt = object.getValueCount("i_folder_id");

			for (int i = 0; i < cnt; i++) {
				IDfId id = object.getRepeatingId("i_folder_id", i);
				DefaultMutableTreeNode node = findNode(id, rootNode);
				if (node != null)
					try {
						treeModel.insertNodeInto(new DefaultMutableTreeNode(
								"Please Wait"), node, 0);
						loadChildsAsync(node, true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void objectUpdate(IDfPersistentObject object) {

		if (!(object instanceof IDfSysObject))
			return;

		try {
			IDfSysObject sys = (IDfSysObject) object;
			DefaultMutableTreeNode node = findNode(object.getObjectId(),
					rootNode);
			if ( node == null ) return;
			NodeValue value = (NodeValue) node.getUserObject();
			value.setName(sys.getObjectName());
			treeModel.nodeChanged(node);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public TreePath[] getSelectedNode() {
		return tree.getSelectionPaths();
	}

	public boolean refreshPath(TreePath path) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
				.getParent();

		if (parent != null) {
			int index = treeModel.getIndexOfChild(parent, node);
			treeModel.removeNodeFromParent(node);
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node
					.getUserObject());
			treeModel.insertNodeInto(newNode, parent, index);
			try {
				NodeValue val = (NodeValue) node.getUserObject();
				val.setIsFolderChecked(false);
				val.expanded = false;
				lookForChilds(newNode);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else
			return false;
	}

	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	public JTree getTree() {
		return tree;
	}

	public void setShowDocuments(boolean in) {
		showDocuments = in;
	}

}
