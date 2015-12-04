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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.api.ApiLayout;
import de.mhu.hair.api.ApiLayout.Listener;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.hair.tools.actions.ActionIfc;
import de.mhu.lib.AFile;
import de.mhu.lib.AThread;

public class OxygenAdapterServer implements ActionIfc, Listener {

	private Element config;
	private boolean visiblePlugin = false;
	private JPanel main = null;
	private PluginNode node;
	private JTextField tPort;
	private JCheckBox cbUseCache;
	private JTextField tCacheDir;
	private JButton bListen;
	private JLabel lConnections;
	private JButton bClearCache;
	private JButton bClearList;
	private JPanel mainPanel;
	private DefaultTableModel actionsModel;
	private JTable actionsTable;
	private PortListener listenerThread;
	private int connectionSequence = 0;
	private int connectionCount = 0;
	private DocumentBuilder builder;
	private JTextArea tMessage;
	private JCheckBox cbLog;
	private DMConnection con;
	private JScrollPane actionsScroller;
	private JCheckBox cbLogExtended;
	private JCheckBox cbSecure;

	public void initAction(PluginNode pNode, DMConnection pCon, Element pConfig) {
		config = pConfig;
		node = pNode;
		con = pCon;
	}

	public void destroyAction() {

	}

	public boolean isEnabled(PluginNode node, DMConnection con,
			IDfPersistentObject[] target) throws Exception {
		return con != null;
	}

	public void actionPerformed(PluginNode pNode, DMConnection con,
			IDfPersistentObject[] target) throws Exception {

		setPluginVisible(true);

	}

	protected void setPluginVisible(boolean b) throws Exception {
		if (visiblePlugin == b)
			return;
		visiblePlugin = b;
		if (visiblePlugin) {
			if (main == null)
				initUI();
			((ApiLayout) node.getSingleApi(ApiLayout.class)).setComponent(main,
					config, this);
		} else
			((ApiLayout) node.getSingleApi(ApiLayout.class))
					.removeComponent(main);

	}

	private void initUI() {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			builder = dbf.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler() {

				public void error(SAXParseException exception)
						throws SAXException {
					System.out.println("--- " + exception);

				}

				public void fatalError(SAXParseException exception)
						throws SAXException {
					System.out.println("*** " + exception);
				}

				public void warning(SAXParseException exception)
						throws SAXException {
					System.out.println("+++ " + exception);
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		main = new JPanel();

		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(12, 2));

		// 1
		mainPanel.add(new JLabel("Port:"));
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
		tPort = new JTextField("22334");
		panel2.add(tPort);
		bListen = new JButton(" Listen ");
		panel2.add(bListen);
		mainPanel.add(panel2);

		// 2
		mainPanel.add(new JLabel("Secure Only"));
		cbSecure = new JCheckBox("");
		cbSecure.setSelected(true);
		mainPanel.add(cbSecure);

		// 3
		mainPanel.add(new JLabel(" "));
		mainPanel.add(new JLabel(" "));

		// 4
		mainPanel.add(new JLabel("Cache"));
		mainPanel.add(new JLabel(" "));

		// 5
		cbUseCache = new JCheckBox("");
		cbUseCache.setSelected(true);
		mainPanel.add(new JLabel("Use Cache: "));
		mainPanel.add(cbUseCache);

		// 6
		mainPanel.add(new JLabel("Directory: "));
		tCacheDir = new JTextField("oxygen_cache");
		mainPanel.add(tCacheDir);

		// 7
		mainPanel.add(new JLabel(" "));
		bClearCache = new JButton(" Clear Cache ");
		mainPanel.add(bClearCache);

		// 8
		mainPanel.add(new JLabel(" "));
		mainPanel.add(new JLabel(" "));

		// 9
		mainPanel.add(new JLabel("Connection"));
		mainPanel.add(new JLabel(" "));

		// 10
		mainPanel.add(new JLabel("Current: "));
		lConnections = new JLabel("0");
		mainPanel.add(lConnections);

		// 11
		mainPanel.add(new JLabel("Logging: "));
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

		cbLog = new JCheckBox("On ");
		cbLog.setSelected(true);
		panel1.add(cbLog);
		cbLogExtended = new JCheckBox("Extended ");
		panel1.add(cbLogExtended);
		mainPanel.add(panel1);

		// 12
		mainPanel.add(new JLabel(" "));
		bClearList = new JButton(" Clear List ");
		mainPanel.add(bClearList);

		// END
		actionsModel = new DefaultTableModel();
		actionsTable = new JTable(actionsModel);
		actionsModel.setDataVector(new Object[][] {}, new Object[] { "Time",
				"ID", "Type", "Message" });
		actionsScroller = new JScrollPane(actionsTable);

		tMessage = new JTextArea();
		tMessage.setWrapStyleWord(true);
		tMessage.setEditable(false);
		tMessage
				.addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));
		JScrollPane scroller2 = new JScrollPane(tMessage);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				actionsScroller, scroller2);

		JPanel mainPanel2 = new JPanel();
		mainPanel2.setLayout(new BorderLayout());
		mainPanel2.add(mainPanel, BorderLayout.WEST);

		main.setLayout(new BorderLayout());
		main.add(mainPanel2, BorderLayout.NORTH);
		main.add(split, BorderLayout.CENTER);
		main.setPreferredSize(new Dimension(400, 500));

		bListen.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionListen(listenerThread == null);
			}

		});

		bClearCache.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				File dir = new File(tCacheDir.getText());
				if (!dir.exists() || !dir.isDirectory())
					return;

				if (JOptionPane.showConfirmDialog(null, "Really",
						"Delete Cache", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				File[] list = dir.listFiles();
				for (int i = 0; i < list.length; i++) {
					if (list[i].isFile()
							&& (list[i].getName().endsWith(".xml") || list[i]
									.getName().endsWith(".properties")))
						list[i].delete();
				}

			}

		});

		bClearList.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionsModel.setRowCount(0);
				actionsTable.revalidate();
				actionsTable.repaint();
			}

		});

		actionsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {

						int row = actionsTable.getSelectedRow();
						if (row >= 0 && row < actionsModel.getRowCount())
							tMessage.setText((String) actionsTable.getValueAt(
									row, 3));

					}

				});

	}

	protected void actionListen(boolean modus) {

		if (modus) {

			if (listenerThread != null)
				actionListen(false);

			if (cbUseCache.isSelected()) {
				new File(tCacheDir.getText()).mkdirs();
			}

			tPort.setEnabled(false);
			cbUseCache.setEnabled(false);
			tCacheDir.setEnabled(false);

			listenerThread = new PortListener();
			listenerThread.start();
			bListen.setText(" Stop Listen ");

		} else {

			if (listenerThread != null) {
				listenerThread.setPleaseStop();

				// craete a dummy connection to free listener
				/*
				 * try { Socket s = new Socket(); s.connect( new
				 * InetSocketAddress( "localhost", Integer.parseInt(
				 * tPort.getText() ) ), 1000 ); s.close(); } catch ( Exception e
				 * ) { e.printStackTrace(); }
				 */

			}

			listenerThread = null;
			bListen.setText(" Listen ");

			tPort.setEnabled(true);
			cbUseCache.setEnabled(true);
			tCacheDir.setEnabled(true);

		}
	}

	public void windowClosed(Object source) {
		try {
			setPluginVisible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class PortListener extends AThread {

		private ServerSocket portListener;
		private boolean running = true;

		public void run() {
			try {
				addMessage(-1, "", "started");
				portListener = new ServerSocket(Integer.parseInt(tPort
						.getText()));

				while (running) {

					Socket newConnection = portListener.accept();
					if (cbLogExtended.isSelected())
						addMessage(-1, "", "new Connection from "
								+ newConnection.getLocalAddress());

					new PortSocket(newConnection).start();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				addMessage(-1, "Error", e.toString());
			}

			if (portListener != null)
				try {
					portListener.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			if (cbLogExtended.isSelected())
				addMessage(-1, "", "exit");
			actionListen(false);
		}

		public void setPleaseStop() {
			running = false;
			try {
				portListener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class PortSocket extends AThread {

		private Socket socket;
		private int myId;

		public PortSocket(Socket newConnection) {
			socket = newConnection;
			myId = connectionSequence++;

		}

		public void run() {

			connectionCount++;
			lConnections.setText(String.valueOf(connectionCount));

			try {
				if (cbLogExtended.isSelected())
					addMessage(myId, "", "started");

				InputStream is = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				OutputStream os = socket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);

				Properties prop = (Properties) ois.readObject();
				addMessage(myId, "in", prop.getProperty("dql"));

				String prefix = null;
				String domString = null;

				if (cbUseCache.isSelected()) {
					prefix = tCacheDir.getText() + "/"
							+ dqlToName(prop.getProperty("dql"));

					FileOutputStream fos = new FileOutputStream(prefix
							+ ".properties");
					prop.store(fos, "");
					fos.close();

					if (new File(prefix + ".xml").exists()) {
						if (cbLogExtended.isSelected())
							addMessage(myId, "", "Load from Cache");
						domString = AFile.readFile(new File(prefix + ".xml"));
					}
				}

				if (domString == null) {
					domString = OxygenAdapterStatic.doQuery(node, prop,
							cbSecure.isSelected());

					if (cbUseCache.isSelected()) {
						if (domString != null) {
							if (cbLogExtended.isSelected())
								addMessage(myId, "", "Store Cache");

							AFile.writeFile(new File(prefix + ".xml"),
									domString);

						} else {
							System.out.println("*** DOM is null");
						}
					}

				}

				if (domString != null) {
					addMessage(myId, "out", domString);
				} else {
					addMessage(myId, "out", "dom is null");
				}

				oos.writeObject(domString);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				addMessage(myId, "Error", e.toString());
			}
			addMessage(myId, "", "exit");

			connectionCount--;
			lConnections.setText(String.valueOf(connectionCount));

		}

	}

	private String dqlToName(String in) {

		in = in.replace('/', '_');
		in = in.replace(' ', '_');
		in = in.replace('\\', '_');
		in = in.replace('?', '_');
		in = in.replace('*', '_');
		in = in.replace('\n', '_');
		in = in.replace('\r', '_');
		in = in.replace('\t', '_');

		if (in.length() > 128)
			in = in.substring(0, 10) + "-" + in.substring(in.length() - 100)
					+ "-" + in.hashCode();

		return in;
	}

	public void addMessage(int con, String type, String string) {
		if (!cbLog.isSelected())
			return;
		actionsModel.addRow(new Object[] { new Date(), new Integer(con), type,
				string });
		if (actionsModel.getRowCount() > 200)
			actionsModel.removeRow(0);

		if (actionsScroller.getVerticalScrollBar() != null) {
			JScrollBar bar = actionsScroller.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
		}

	}

	public String getTitle() {
		return null;
	}

}
