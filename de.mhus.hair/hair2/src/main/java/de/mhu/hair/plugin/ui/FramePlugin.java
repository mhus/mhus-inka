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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class FramePlugin extends AbstractFrame implements ApiToolbar, ApiLayout {

	private JFrame frame;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {
		super.initPlugin(pNode, pConfig);

		frame = new JFrame();
		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.pack();
		configFrame(node, persistents, frame, pConfig.getNode(), null);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(e.getComponent(),
						"Exit Program?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(0);
			}
		});

		frame.show();
	}

	protected void setMenuBarInternal(JMenuBar menuBar2) {
		frame.getContentPane().add(menuBar2, BorderLayout.NORTH);
		// frame.setJMenuBar( menuBar2 );
		frame.validate();
		frame.repaint();
	}

}
