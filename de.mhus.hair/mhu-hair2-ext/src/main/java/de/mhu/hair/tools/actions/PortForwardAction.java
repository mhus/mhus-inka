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

package de.mhu.hair.tools.actions;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.net.PortForwarder;

public class PortForwardAction implements ActionIfc {

	private PluginNode node;
	private Element config;
	private ApiLayout layout;
	private PersistentManager persistents;

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		node = pNode;
		config = pConfig;
		layout = (ApiLayout) node.getSingleApi(ApiLayout.class);
		persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
				.getManager(pConfig.getAttribute("persistent"));
	}

	public void destroyAction() {

	}

	public boolean isEnabled(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		return true;
	}

	public void actionPerformed(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTarget) throws Exception {
		new PfwGui();
	}

	public class PfwGui extends JPanel implements Listener,
			de.mhu.lib.net.PortForwarder.Listener {

		private JTextField srcPort;
		private JTextField dstHost;
		private JTextField dstPort;
		private JButton bAction;
		private PortForwarder pf = null;
		private JCheckBox cbTrace;
		private LoggerPanel logger;
		private JTextField tBufferSize;

		PfwGui() throws Exception {
			initUI();
			refresh();

			layout.setComponent(this, config, this);

		}

		private void refresh() {
			boolean da = (pf == null);
			srcPort.setEnabled(da);
			dstHost.setEnabled(da);
			dstPort.setEnabled(da);
			cbTrace.setEnabled(da);
			tBufferSize.setEnabled(da);
			if (da)
				bAction.setText(" Start ");
			else
				bAction.setText(" Stop ");
		}

		private void initUI() {
			setLayout(new BorderLayout());
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(7, 2));

			// 1
			panel.add(new JLabel("Source Port: "));
			srcPort = new JTextField("8086");
			panel.add(srcPort);

			// 2
			panel.add(new JLabel("Dest Host: "));
			dstHost = new JTextField(persistents.getProperty(
					"portforward.host", ".portal.fra.dlh.de"));
			panel.add(dstHost);

			// 3
			panel.add(new JLabel("Dest Port: "));
			dstPort = new JTextField(persistents.getProperty(
					"portforward.port", "8086"));
			panel.add(dstPort);

			// 4
			panel.add(new JLabel("Trace: "));
			cbTrace = new JCheckBox();
			panel.add(cbTrace);

			// 5
			panel.add(new JLabel("Buffer Size: "));
			tBufferSize = new JTextField("102400");
			panel.add(tBufferSize);

			// 6
			panel.add(new JLabel(" "));
			panel.add(new JLabel(" "));

			// 7
			panel.add(new JLabel(" "));
			bAction = new JButton(" Start ");
			panel.add(bAction);

			bAction.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionAction();
				}

			});

			add(panel, BorderLayout.NORTH);

			logger = new LoggerPanel(null, null) {

				public void setMinimum(int arg0) {
					// TODO Auto-generated method stub

				}

				public void setMaximum(int arg0) {
					// TODO Auto-generated method stub

				}

				public void setValue(int arg0) {
					// TODO Auto-generated method stub

				}

			};

			add(logger.getMainPanel(), BorderLayout.CENTER);

		}

		protected void actionAction() {

			if (pf == null) {
				pf = new PortForwarder(Integer.parseInt(srcPort.getText()),
						dstHost.getText(), Integer.parseInt(dstPort.getText()),
						this, cbTrace.isSelected(), 60 * 1000, Integer
								.parseInt(tBufferSize.getText()));
				pf.start();
			} else {
				pf.stop();
				pf = null;
			}
			refresh();
		}

		public void windowClosed(Object source) {
			if (pf != null)
				actionAction();
		}

		public void started(int arg0) {
			logger.out.println(">>> Started " + arg0);
		}

		public void trace(int arg0, int arg1, byte[] arg2, int arg3) {
			if (arg1 == PortForwarder.TRACE_IN)
				logger.out.println(arg0
						+ " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			else
				logger.out.println(arg0
						+ " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			logger.out.println(new String(arg2, 0, arg3));
			logger.out.println(arg0
					+ " ------------------------------------------");
		}

		public void exit(int arg0) {
			logger.out.println(">>> Exit " + arg0);
		}

		public void error(int arg0, Exception arg1) {
			logger.out.println("*** Error " + arg0 + ": " + arg1);
			arg1.printStackTrace(logger.out);

		}

		public void connected(int arg0, InetAddress arg1, InetAddress arg2) {
			logger.out.println("--- Connected " + arg0 + ": " + arg1 + " --> "
					+ arg2);
		}

	}

	public String getTitle() {
		return null;
	}

}
