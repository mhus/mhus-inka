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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.documentum.fc.client.IDfType;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.tools.ObjectTool;

public class TypesTreePanel extends JPanel {

	private DMConnection con;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree tree;
	private ApiTypes api;
	private Listener listener;

	public TypesTreePanel(DMConnection pCon, ApiTypes pApi, String start,
			String[] pIgnore, Listener pListener) {
		con = pCon;
		api = pApi;
		listener = pListener;

		initUI(start);

		addNodes(rootNode, start == null ? api.getTree() : api
				.getTreeFor(start), pIgnore);
		tree.setSelectionRow(0);
		tree.expandRow(0);

		tree.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				int mode = Listener.MODE_UNKNOWN;
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() < 2)
					mode = Listener.MODE_HOT_SELECT;
				else if (e.getButton() == MouseEvent.BUTTON1)
					mode = Listener.MODE_SELECT;
				else if (e.getButton() == MouseEvent.BUTTON3)
					mode = Listener.MODE_MENU;

				if (listener != null)
					try {
						listener
								.selectedEvent(
										api
												.getType((String) ((DefaultMutableTreeNode) tree
														.getSelectionPath()
														.getLastPathComponent())
														.getUserObject()),
										mode, tree, e.getX(), e.getY());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
			}
		});

	}

	private void addNodes(DefaultMutableTreeNode rootNode2, Hashtable tree2,
			String[] ignore) {

		if (tree2 == null)
			return;
		TreeMap sort = new TreeMap(tree2);

		for (Iterator i = sort.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			boolean ok = true;
			String key = (String) entry.getKey();
			if (ignore != null) {
				for (int j = 0; j < ignore.length; j++)
					if (key.equals(ignore[j])) {
						ok = false;
						break;
					}
			}
			if (ok) {
				DefaultMutableTreeNode n = new DefaultMutableTreeNode(key);
				treeModel.insertNodeInto(n, rootNode2, rootNode2
						.getChildCount());
				addNodes(n, (Hashtable) entry.getValue(), ignore);
			}
		}

	}

	private void initUI(String start) {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(400, 300));
		if (start == null)
			start = "Types";
		rootNode = new DefaultMutableTreeNode(start);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		JScrollPane scroller = new JScrollPane(tree);

		add(scroller, BorderLayout.CENTER);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {

				setPreferredSize(new Dimension(500, 20));
				String objType = (String) ((DefaultMutableTreeNode) value)
						.getUserObject();
				Icon icon = ObjectTool.getIcon(objType, null,false);
				if (icon != null) {
					setOpenIcon(icon);
					setClosedIcon(icon);
					setLeafIcon(icon);
				}
				return super.getTreeCellRendererComponent(tree, value, sel,
						expanded, leaf, row, hasFocus);
			}
		});

	}

	public String getSelected() {
		return (String) ((DefaultMutableTreeNode) tree.getSelectionPath()
				.getLastPathComponent()).getUserObject();
	}

	public static interface Listener {

		final int MODE_MENU = 3;
		final int MODE_SELECT = 2;
		final int MODE_HOT_SELECT = 1;
		final int MODE_UNKNOWN = 0;

		void selectedEvent(IDfType value, int mode, JComponent src, int x, int y);

	}

}
