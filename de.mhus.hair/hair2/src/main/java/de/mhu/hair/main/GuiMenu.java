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

package de.mhu.hair.main;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GuiMenu implements MainIfc {

	public void startMain() {

		final JFrame frame = new JFrame();

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 1));

		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if ("".equals(cmd))
					System.exit(0);

				frame.dispose();
				try {
					Main.main(new String[] { "-st", cmd });
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		};

		JButton button = new JButton(" DCTM GUI ");
		button.setActionCommand("gui");
		button.addActionListener(listener);
		panel.add(button);

		button = new JButton(" WDK Browser ");
		button.setActionCommand("wdk_browser");
		button.addActionListener(listener);
		panel.add(button);

		button = new JButton(" PCD Browser ");
		button.setActionCommand("pcd");
		button.addActionListener(listener);
		panel.add(button);

		button = new JButton(" Cancel ");
		button.setActionCommand("");
		button.addActionListener(listener);
		panel.add(button);

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setLocation(200, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.show();

	}

	public static void show(Map startMap, String[] args) {
		// TODO Auto-generated method stub

	}

}
