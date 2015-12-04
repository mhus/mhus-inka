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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.AFile;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.ATabbedPane;
import de.mhu.lib.swing.LAF;
import de.mhu.lib.swing.OutputArea;
import de.mhu.res.img.LUF;

public class OutputPlugin implements Plugin {

	private static OutputPlugin instance = null;
	private Timer timer = new Timer(true);
	private PluginNode node;
	private JFrame outFrame;
	private OutputArea outPanel;
	private ApiLayout layout;
	private ATabbedPane outTab;

	public void initPlugin(PluginNode pNode, PluginConfig config) {

		if (instance != null) {

			// Change UI Frame

			instance.node = pNode;
			instance.destroyUI();
			instance.initUI(config);
			return;
		}

		// Initial Creation of UI

		instance = this;
		node = pNode;

		File logDir = new File("log");
		logDir.mkdirs();

		try {
			outPanel = new OutputArea(new FileOutputStream(new File(logDir,
					"hair_" + AFile.getFileNameDateAsString() + ".log")));
		} catch (FileNotFoundException e) {
			outPanel = new OutputArea();
		}
		outPanel.setOutputFont(LUF.CONSOLE_FONT);
		outTab = LAF.createTabbedGroup();
		outTab.addTab("I/O", outPanel);

		initUI(config);
		outPanel.setLinked(System.out);

		timer.schedule(new TimerTask() {

			public void run() {
				int cnt = outTab.getTabCount();
				for (int i = 0; i < cnt; i++)
					((OutputArea) outTab.getComponentAt(i)).refreshOutput();
			}

		}, 1000, 1000);

		System.setErr(new PrintStream(outPanel.getOutputStream()));
		System.setOut(new PrintStream(outPanel.getOutputStream()));

	}

	public OutputArea getOutput(String title) {
		int cnt = outTab.getTabCount();
		for (int i = 0; i < cnt; i++)
			if (outTab.getTitleAt(i).equals(title))
				return (OutputArea) outTab.getComponentAt(i);

		OutputArea newOut = new OutputArea();
		outTab.addTab(title, newOut);
		return newOut;
	}

	private void destroyUI() {

		if (outFrame != null)
			outFrame.dispose();
		outFrame = null;

		if (layout != null)
			layout.removeComponent(outPanel);
		layout = null;

	}

	private void initUI(PluginConfig config) {

		if (config.getNode() == null
				|| config.getNode().getAttribute("pos").equals("")) {
			outFrame = new JFrame();
			outFrame.getContentPane().add(outTab);
			outFrame.setSize(700, 300);
			outFrame.setLocation(0, 600);
			outFrame.setTitle(" I/O ");
			outFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			outFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					if (JOptionPane.showConfirmDialog(e.getComponent(),
							"Exit Program?", "Confirm",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						((ApiSystem) node.getSingleApi(ApiSystem.class))
								.exit(0);
				}
			});

			if (!config.getNode().getAttribute("icon").equals("")) {
				ImageIcon icon = ImageProvider.getInstance().getIcon(
						config.getNode().getAttribute("icon"));
				if (icon != null)
					outFrame.setIconImage(icon.getImage());
			}

			outFrame.show();

		} else {

			layout = ((ApiLayout) node.getSingleApi(ApiLayout.class));
			try {
				layout.setComponent(outTab, config.getNode());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void destroyPlugin() throws Exception {
	}

	public static synchronized OutputPlugin getInstance() {
		return instance;
	}
}
