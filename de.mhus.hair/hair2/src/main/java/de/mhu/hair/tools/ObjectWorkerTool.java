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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiObjectWorker;
import de.mhu.hair.api.ApiObjectWorkerFactory;
import de.mhu.hair.api.ApiObjectWorkerGui;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;

public class ObjectWorkerTool {

	public static void showMenu(PluginNode node, final DMConnection con,
			final IDfPersistentObject[] obj, JComponent src, int x, int y) {

		JPopupMenu menu = new JPopupMenu();
		ApiObjectWorker[] list1 = (ApiObjectWorker[]) node
				.getApi(ApiObjectWorker.class);
		if (list1 != null) {
			for (int i = 0; i < list1.length; i++) {
				if (list1[i].canWorkOn(con, obj)) {
					if (list1[i] instanceof ApiObjectWorkerGui) {
						((ApiObjectWorkerGui) list1[i]).appendPopupMenu(menu,
								node, con, obj);
					} else {
						final ApiObjectWorker worker = list1[i];
						JMenuItem item = new JMenuItem(list1[i].getTitleFor(
								con, obj));
						item.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent e) {
								worker.workWith(con, obj);
							}

						});
						menu.add(item);
					}
				}

			}

		}

		ApiObjectWorkerFactory[] list2 = (ApiObjectWorkerFactory[]) node
				.getApi(ApiObjectWorkerFactory.class);

		if (menu.getComponentCount() != 0
				&& (list2 != null || list2.length != 0)) {
			menu.addSeparator();
		}

		if (list2 != null) {
			for (int i = 0; i < list2.length; i++) {
				if (list2[i].canCreateFor(con, obj)) {
					final ApiObjectWorkerFactory worker = list2[i];
					JMenuItem item = new JMenuItem(list2[i].getTitleFor(con,
							obj));
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							try {
								worker.createWorker(con, obj);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}

					});
					menu.add(item);
				}

			}
		}

		if (menu.getComponentCount() == 0)
			return;

		menu.show(src, x, y);

	}

	public static class TextIdGrapper implements MouseListener {

		private PluginNode xnode;
		private DMConnection xcon;

		public TextIdGrapper(PluginNode pNode, DMConnection pCon) {
			xnode = pNode;
			xcon = pCon;
		}

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent e) {

			if (e.getButton() == MouseEvent.BUTTON3) {
				JTextComponent text = (JTextComponent) e.getSource();
				String idStr = text.getSelectedText();
				if (idStr == null || idStr.length() != 16)
					return;
				try {
					IDfPersistentObject obj = xcon.getPersistentObject(idStr);
					ObjectWorkerTool.showMenu(xnode, xcon,
							new IDfPersistentObject[] { obj }, text, e.getX(),
							e.getY());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}

}
