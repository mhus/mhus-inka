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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.resources.ImageProvider;

public class StatusPlugin extends JPanel implements Plugin, ApiObjectHotSelect {

	private JTextField path;
	private JLabel lLocked;
	private DMConnection con;
	private PluginNode node;
	private PluginConfig config;

	public static final Icon LOCKED = ImageProvider.getInstance().getIcon(
			"hair:/lock.gif");

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		config = pConfig;
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);

		initUI();

		((ApiToolbar) pNode.getSingleApi(ApiToolbar.class))
				.addToolbarButton(this);

		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.hotselect_") >= 0) {
			pNode.addApi(ApiObjectHotSelect.class, this);
		}
		if (pConfig.getNode().getAttribute("listen").indexOf("_obj.last_") >= 0) {
			IDfPersistentObject obj = con.getPersistentObject(pConfig
					.getProperty("objid"));
			showObj(con, obj);
		}

	}

	public void showObj(DMConnection con2, IDfPersistentObject obj) {
		try {
			path.setText(ObjectTool.getPath(obj));

			if ((obj instanceof IDfSysObject)
					&& ((IDfSysObject) obj).isCheckedOut()) {
				lLocked.setEnabled(true);
			} else {
				lLocked.setEnabled(false);
			}
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			path.setText(e.toString());
		}

	}

	public void showObj(DMConnection con2, IDfPersistentObject[] obj) {
		// try {
		path.setText(obj.length + " Items");

		lLocked.setEnabled(false);
		/*
		 * } catch (DfException e) { e.printStackTrace(); path.setText(
		 * e.toString() ); }
		 */
	}

	private void initUI() {

		path = new JTextField();
		path.setEditable(true);
		path.setBackground(getBackground());
		
		ApiPersistent.PersistentManager persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
		.getManager(config.getNode().getAttribute("persistent"));
		String bgColorTxt = persistents.getProperty("frame.bgcolor");
		if (bgColorTxt != null) {
			path.setBackground(Color.decode(bgColorTxt));
		}
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		lLocked = new JLabel();
		lLocked.setIcon(LOCKED);
		panel.add(new JLabel(" "));
		panel.add(lLocked);
		panel.add(new JLabel(" "));

		setLayout(new BorderLayout());
		add(panel, BorderLayout.WEST);
		add(path, BorderLayout.CENTER);
		add(new JLabel(" "), BorderLayout.EAST);

		clearObj();

		path.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					actionInsertPath();
				}
			}

		});

		path.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu menu = new JPopupMenu();
					String[] languages = new String[] { "de_DE", "en_US" };
					for (int i = 0; i < languages.length; i++) {
						JMenuItem item = new JMenuItem(languages[i]);
						final String lang = languages[i];
						item.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent e) {
								actionInsertPath(lang);
							}

						});
						menu.add(item);
					}

					menu.show(path, e.getX(), path.getHeight());
				}

			}

		});

	}

	protected void actionInsertPath(String lang) {

		try {
			IDfPersistentObject obj = con.getExistingObject(path.getText(),
					lang);
			if (obj == null) {
				JOptionPane.showMessageDialog(null, "Not found");
				return;
			}

			IDfPersistentObject[] objArray = new IDfPersistentObject[] { obj };
			ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
					.getApi(ApiObjectHotSelect.class);
			if (list == null)
				return;

			for (int i = 0; i < list.length; i++)
				try {
					list[i].apiObjectDepricated();
				} catch (Throwable t) {
					t.printStackTrace();
				}

			for (int i = 0; i < list.length; i++)
				try {
					list[i].apiObjectHotSelected(con, null, objArray);
				} catch (Throwable t) {
					t.printStackTrace();
				}

		} catch (DfException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e);
		}

	}

	protected void actionInsertPath() {
		try {
			String pathStr = path.getText().trim();
			if (pathStr.length() == 0)
				return;
			if (pathStr.startsWith("\"") && pathStr.endsWith("\""))
				pathStr = pathStr.substring(1, pathStr.length() - 1);
			else {
				if (!pathStr.startsWith("/")) {
					pathStr = '/' + pathStr;
					path.setText(pathStr);
				}

				if (pathStr.indexOf("->") > 0) {
					pathStr = pathStr.replaceAll("->", "/");
					path.setText(pathStr);
				}
				if (pathStr.indexOf("/ ") > 0) {
					pathStr = pathStr.replaceAll("/ ", "/");
					path.setText(pathStr);
				}
				if (pathStr.indexOf(" /") > 0) {
					pathStr = pathStr.replaceAll(" /", "/");
					path.setText(pathStr);
				}
				if (pathStr.endsWith("/")) {
					pathStr = pathStr.substring(0, pathStr.length() - 1);
					path.setText(pathStr);
				}
			}

			IDfPersistentObject obj = con.getSession().getObjectByPath(pathStr);
			if (obj == null && pathStr.endsWith(".html")) {
				pathStr = pathStr.substring(0, pathStr.length() - 4) + "xml";
				obj = con.getSession().getObjectByPath(pathStr);
				if (obj != null)
					path.setText(pathStr);
			}

			if (obj == null) {
				JOptionPane.showMessageDialog(null, "Not found");
				return;
			}

			IDfPersistentObject[] objArray = new IDfPersistentObject[] { obj };
			ApiObjectHotSelect[] list = (ApiObjectHotSelect[]) node
					.getApi(ApiObjectHotSelect.class);
			if (list == null)
				return;

			for (int i = 0; i < list.length; i++)
				try {
					list[i].apiObjectDepricated();
				} catch (Throwable t) {
					t.printStackTrace();
				}

			for (int i = 0; i < list.length; i++)
				try {
					list[i].apiObjectHotSelected(con, null, objArray);
				} catch (Throwable t) {
					t.printStackTrace();
				}

		} catch (DfException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e);
		}

	}

	public void destroyPlugin() throws Exception {

	}

	public void apiObjectDepricated() {
		clearObj();
	}

	private void clearObj() {
		lLocked.setEnabled(false);
		path.setText("");
	}

	public void apiObjectHotSelected(DMConnection pCon,
			IDfPersistentObject[] pParents, IDfPersistentObject[] pObj)
			throws Exception {
		if (pObj == null || pObj.length == 0) {
			clearObj();
			return;
		}

		if (pObj.length == 1)
			showObj(pCon, pObj[0]);
		else {
			showObj(pCon, pObj);
		}
	}

}
