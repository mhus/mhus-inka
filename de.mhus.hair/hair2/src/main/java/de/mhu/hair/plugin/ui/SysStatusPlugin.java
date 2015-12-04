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
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.mhu.hair.api.ApiToolbar;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;

public class SysStatusPlugin extends JPanel implements Plugin {

	private static final long MB = 1024 * 1024;
	private Timer timer;
	private JProgressBar progress;

	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		timer = new Timer(true);

		initUI();
		((ApiToolbar) pNode.getSingleApi(ApiToolbar.class))
				.addToolbarButton(this);

		timer.schedule(new TimerTask() {

			public void run() {
				try {
					progress
							.setMaximum((int) (Runtime.getRuntime().maxMemory() / MB));
					progress
							.setValue((int) (Runtime.getRuntime().freeMemory() / MB));
					progress
							.setString((Runtime.getRuntime().maxMemory() / MB + 1)
									+ " MB");
				} catch (Exception e) {
				}
			}

		}, 1000, 5000);

	}

	private void initUI() {

		setLayout(new BorderLayout());
		progress = new JProgressBar();
		progress.setStringPainted(true);
		add(progress, BorderLayout.EAST);
		setMinimumSize(new Dimension(100, 25));
		setPreferredSize(new Dimension(100, 25));
		progress.setMinimumSize(new Dimension(100, 25));
		progress.setPreferredSize(new Dimension(100, 25));
		progress.setMaximumSize(new Dimension(100, 25));
		setMaximumSize(new Dimension(100, 25));
	}

	public void destroyPlugin() throws Exception {
		timer.cancel();
	}

}
