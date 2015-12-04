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

package de.mhu.hair.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiObjectHotSelect;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.res.img.LUF;

public class ObjectFromIdPlugin extends JPanel implements Plugin {

	private JTextField tId;
	private JButton bMenu;
	private PluginNode node;
	private DMConnection con;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		con = (DMConnection) node.getSingleApi(DMConnection.class);

		initUI();

		bMenu.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				IDfPersistentObject obj;
				try {
					obj = con.getPersistentObject(tId.getText().trim());
					ObjectWorkerTool.showMenu(node, con,
							new IDfPersistentObject[] { obj }, bMenu, 0, bMenu
									.getHeight());
				} catch (DfException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(ObjectFromIdPlugin.this, e1
							.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}

			}

		});

		tId.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						IDfPersistentObject obj = con.getPersistentObject(tId
								.getText().trim());
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
								list[i].apiObjectHotSelected(con, null,
										objArray);
							} catch (Throwable t) {
								t.printStackTrace();
							}

					} catch (DfException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error: " + ex);
					}
				}
			}
		});

		((ApiToolbar) node.getSingleApi(ApiToolbar.class))
				.addToolbarButton(this);

	}

	private void initUI() {

		tId = new JTextField();

		bMenu = new JButton(LUF.SEARCH_ICON);
		bMenu.setMargin(new Insets(0, 0, 0, 0));

		setLayout(new BorderLayout(0,0));

		add(tId, BorderLayout.CENTER);
		add(bMenu, BorderLayout.EAST);

		setPreferredSize(new Dimension(150, 22));
		setMaximumSize(new Dimension(150, 22));
	}

	public void destroyPlugin() throws Exception {

	}

}
