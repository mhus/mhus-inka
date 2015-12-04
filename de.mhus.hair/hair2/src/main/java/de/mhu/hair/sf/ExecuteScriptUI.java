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

package de.mhu.hair.sf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;

import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.plugin.ui.TypesTreePanel;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ACast;
import de.mhu.lib.AFile;
import de.mhu.lib.io.CSVReader;
import de.mhu.lib.swing.FileExtFilter;
import de.mhu.lib.swing.layout.TopGridLayout;
import de.mhu.lib.xml.XmlTool;
import de.mhu.res.img.LUF;

public class ExecuteScriptUI {

	private Element config;
	private JPanel panel;
	private Hashtable<String,Cnt> attributes = new Hashtable<String,Cnt>();
	private JButton bAction;
	private DMConnection con;
	private IDfPersistentObject[] targets;
	protected Thread thread;
	private LoggerPanel logger;
	private JTabbedPane tabs;
	private PluginNode node;
	private JTextArea tWhereClause;
	private JTabbedPane sourceTabs;
	private JCheckBox cbCsvHasHeader;
	private JTextField tCsvFile;
	private JCheckBox cbDirectHasHeader;
	private JTextArea tDirectList;
	private JCheckBox cbWhereCached;
	private int TAB_DESCRIPTION = -1;
	private int TAB_TARGETS = -1;
	private int TAB_CONFIG = -1;
	private int TAB_EXECUTE = -1;
	private JPanel descPanel;
	private Hashtable<String, Class<?>> elementMap;
	private PersistentManager persistents;
	private boolean targetsOk;
	private JButton bAction2;
	private boolean showTargets;
	private JCheckBox cbTransaction;

	private static JFileChooser fileChooser = new JFileChooser();
	private static JFileChooser sffileChooser = new JFileChooser();

	static {
		fileChooser.addChoosableFileFilter( new FileExtFilter( "CSV-Files", new String[] { "csv"} ) );
		sffileChooser.addChoosableFileFilter( new FileExtFilter( "Script Properties", new String[] { "sfp"} ) );
	}

	public ExecuteScriptUI(PluginNode pNode, Element pConfig, DMConnection pCon,
			IDfPersistentObject[] pTargets,boolean pTargetsOk, boolean pShowTargets ) {
		config = pConfig;
		con = pCon;
		node = pNode;
		targets = pTargets;
		targetsOk = pTargetsOk;
		if ( ! pTargetsOk ) pShowTargets = true;
		showTargets = pShowTargets;
		
		elementMap = new Hashtable<String, Class<?>>();
		elementMap.put("checkbox", CheckboxCnt.class);
		elementMap.put("input", TextCnt.class);
		elementMap.put("password", PasswordCnt.class);
		elementMap.put("textarea", TextAreaCnt.class);
		elementMap.put("select", SelectCnt.class);
		elementMap.put("file", FileCnt.class);
		elementMap.put("files", FilesCnt.class);
		elementMap.put("type", TypeCnt.class);

		if ( pConfig != null)
			persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
				.getManager(pConfig.getAttribute("persistent"));
		else
			persistents = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
			.getManager("");
			
	}

	public JComponent getConfigPanel() {
		if (panel == null) {

			panel = new JPanel();

			// description
			Element desc = (Element) XmlTool.getElementByPath(config, "description");
			if ( desc != null ) {
				descPanel = new JPanel();
				descPanel.setLayout(new BorderLayout());
				JTextPane textPane = new JTextPane();
				JScrollPane scroller3 = new JScrollPane(textPane);
				textPane.setText( XmlTool.getValue(desc, true ) );
				textPane.setEditable(false);
				textPane.addKeyListener(new KeyAdapter() {
	
					@Override
					public void keyReleased(KeyEvent e) {
						if ( e.getKeyCode() == KeyEvent.VK_ENTER )
							selectNextTab( TAB_DESCRIPTION );
					}
					
				});
			}
			
			// Config Panel
			JPanel panel2 = new JPanel();
			Element pc = (Element) XmlTool.getLocalElements(config, "panel")
					.item(0);
			NodeList list = XmlTool.getLocalElements(pc);
			panel2.setLayout(new TopGridLayout(list.getLength(), 2));
			// panel2.setLayout( new BoxLayout( panel2, BoxLayout.Y_AXIS ) );
			for (int i = 0; i < list.getLength(); i++) {
				Element item = (Element) list.item(i);
				String name = item.getNodeName();
				if (name.equals("space")) {
					panel2.add(new JLabel(" "));
					panel2.add(new JLabel(" "));
				} else if (name.equals("title")) {
					panel2.add(new JLabel(item.getAttribute("title")));
					panel2.add(new JLabel(" "));
				} else {
					
					Class<?> clazz = elementMap.get(name);
					if ( clazz != null ) {
						
						try {
							Cnt cnt = (Cnt)clazz.newInstance();
							cnt.init(item, con, node );
							cnt.createUI(panel2);
							attributes.put(item.getAttribute("name"), cnt);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
					} else {
						panel2.add(new JLabel("Unknown:"));
						panel2.add(new JLabel(name));
					}
					
				}

			}

			JScrollPane scroller2 = new JScrollPane(panel2);

			JPanel configPanel = new JPanel();
			configPanel.setLayout( new BorderLayout());
			configPanel.add(scroller2,BorderLayout.CENTER);
			
			JToolBar configTools = new JToolBar();
			
			createConfigTools(configTools);
			
			configTools.setFloatable(false);
			configPanel.add(configTools,BorderLayout.SOUTH);
			
			if ( persistents != null ) {
				String storeDir = persistents.getProperty("sf.store.dir");
				if ( storeDir == null ) {
					storeDir = "fsp";
				}
				File store = new File( storeDir, config.getAttribute("class") + ".sfp" );
				actionConfigLoad(store);
			}		
			
			// -----------------------------------------------------
			// List of affected entries

			DefaultListModel itemModel = new DefaultListModel();
			JList itemList = new JList(itemModel);
			if (targets != null) {
				for (int i = 0; i < targets.length; i++) {
					try {
						itemModel.addElement(ObjectTool.getPath(targets[i]));
					} catch (DfException e1) {
						e1.printStackTrace();
						itemModel.addElement(e1.toString());
					}
				}
			}
			JScrollPane scroller = new JScrollPane(itemList);

			// Sql Where clause

			JPanel wherePanel = new JPanel();
			wherePanel.setLayout(new BorderLayout());
			JTextField tf = new JTextField("SELECT d.r_object_id FROM ...");
			tf.setEditable(false);
			wherePanel.add(tf,
					BorderLayout.NORTH);
			tWhereClause = new JTextArea();
			wherePanel.add(new JScrollPane(tWhereClause), BorderLayout.CENTER);
			cbWhereCached = new JCheckBox("Cache result in temporary file");
			wherePanel.add(cbWhereCached, BorderLayout.SOUTH);

			// csv file

			JPanel csvPanel = new JPanel();
			csvPanel.setLayout(new BoxLayout(csvPanel, BoxLayout.Y_AXIS));
			cbCsvHasHeader = new JCheckBox("First line is header");
			cbCsvHasHeader.setEnabled(true);
			csvPanel.add(cbCsvHasHeader);

			csvPanel.add(new JLabel("CSV File:"));
			tCsvFile = new JTextField();
			csvPanel.add(tCsvFile);
			JButton bCsvFile = new JButton(" Select CSV File ");
			bCsvFile.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int res = fileChooser.showOpenDialog(sourceTabs);
					if (res != JFileChooser.APPROVE_OPTION
							|| fileChooser.getSelectedFile() == null)
						return;
					tCsvFile.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}

			});
			csvPanel.add(bCsvFile);

			JPanel csvPanel2 = new JPanel();
			csvPanel2.setLayout(new BorderLayout());
			csvPanel2.add(csvPanel, BorderLayout.NORTH);

			// direct
			JPanel directPanel = new JPanel();
			directPanel.setLayout(new BorderLayout());
			directPanel.add(new JLabel("Copy Direct CSV Data"),
					BorderLayout.NORTH);
			cbDirectHasHeader = new JCheckBox("First line is header");
			directPanel.add(cbDirectHasHeader, BorderLayout.SOUTH);
			tDirectList = new JTextArea();
			JScrollPane directScroller = new JScrollPane(tDirectList);
			directPanel.add(directScroller, BorderLayout.CENTER);

			// Tabs

			sourceTabs = new JTabbedPane(JTabbedPane.LEFT);
			sourceTabs.addTab("Selected", scroller);
			sourceTabs.addTab("DQL", wherePanel);
			sourceTabs.addTab("CSV", csvPanel2);
			sourceTabs.addTab("Direct", directPanel);

			// ----------------------------------------------------
			// Logger
			String logPrefix = config.getAttribute("log_prefix");
			if (logPrefix.equals(""))
				logPrefix = AFile.getFileSuffix(config.getAttribute("class"));
			logger = new LoggerPanel(new File("log"), logPrefix);
			logger.getConsole().setFont(LUF.CONSOLE_FONT);

			bAction = new JButton(" Start ");
			bAction.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					actionExecute();
				}

			});

			JPanel panel4 = new JPanel();
			panel4.setLayout(new BorderLayout());
			panel4.add(logger.getMainPanel(), BorderLayout.CENTER);
			panel4.add(bAction, BorderLayout.SOUTH);

			tabs = new JTabbedPane();
			if ( descPanel != null ) { 
				TAB_DESCRIPTION=tabs.getTabCount(); 
				tabs.addTab( "Description", descPanel );
			}
			if (showTargets && !"1".equals(config.getAttribute("ignore_objects"))) {
				TAB_TARGETS = tabs.getTabCount();
				tabs.addTab("Affected Objects", sourceTabs);
			}
			if (list.getLength() != 0) {
				TAB_CONFIG = tabs.getTabCount();
				tabs.addTab("Configuration", configPanel);
			}
			TAB_EXECUTE = tabs.getTabCount();
			tabs.addTab("Execute", panel4);

			panel.setLayout(new BorderLayout());
			panel.add(tabs, BorderLayout.CENTER);
			
			if ( targetsOk ) {
				if ( TAB_CONFIG != -1 )
					tabs.setSelectedIndex(TAB_CONFIG);
				else
					tabs.setSelectedIndex(TAB_EXECUTE);
					
			}
		}

		return panel;
	}
	
	protected void actionExecute() {
		if (thread == null) {
			thread = new Thread(new Runnable() {

				public void run() {
					actionStart();
				}

			});
			thread.start();
		} else {
			actionStop();
		}
	}

	private void createConfigTools(JToolBar configTools) {
		
		JButton button = new JButton(" Save Config ");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConfigSave(null);
			}
			
		});
		configTools.add(button);
		
		button = new JButton(" Load Config ");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConfigLoad(null);
			}
			
		});
		configTools.add(button);
		
		button = new JButton(" Save as default ");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConfigSaveDefault();
			}
			
		});
		button.setEnabled(persistents!=null);
		configTools.add(button);
		
		cbTransaction = new JCheckBox(" Use Transaction ");
		configTools.add(cbTransaction);
		
		bAction2 = new JButton(" Start Execution ");
		bAction2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tabs.setSelectedIndex(TAB_EXECUTE);
				actionExecute();
			}
			
		});
		configTools.add(bAction2);
		
	}

	protected void actionConfigLoad(File file) {
		
		if ( file == null ) {
			if ( sffileChooser.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION )
				return;
			file = sffileChooser.getSelectedFile();
		}
		
		if ( file == null || !file.exists() || !file.isFile() )
			return;
		
		Properties p = new Properties();
		try {
			FileInputStream fis = new FileInputStream(file);
			p.load(fis);
			fis.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
		for (String key : attributes.keySet() ) {
			Cnt value = attributes.get(key);
			value.setValue(p.getProperty(key));
		}
		
	}

	protected void actionConfigSaveDefault() {
		if ( persistents == null ) return;
		String storeDir = persistents.getProperty("sf.store.dir");
		if ( storeDir == null ) {
			storeDir = "fsp";
		}
		File fsp = new File( storeDir );
		fsp.mkdirs();
		
		File store = new File( storeDir, config.getAttribute("class") + ".sfp" );
		actionConfigSave(store);
	}
	
	protected void actionConfigSave(File file) {
		
		if ( file == null ) {
			if ( sffileChooser.showSaveDialog(panel) != JFileChooser.APPROVE_OPTION )
				return;
			file = sffileChooser.getSelectedFile();
			if ( file == null ) return;
			if ( !file.getName().endsWith(".sfp"))
				file = new File( file.getAbsolutePath() + ".sfp" );
		}
		
		
		Properties p = new Properties();
		for (String key : attributes.keySet() ) {
			Cnt value = attributes.get(key);
			p.setProperty(key, value.getValue().toString());
		}
		
		try {
			FileOutputStream fis = new FileOutputStream(file);
			p.store(fis, "Class: " + config.getAttribute("class"));
			fis.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

	private void selectNextTab(int tab) {
		if ( tab < 0 ) {
			tabs.setSelectedIndex(0);
			return;
		}
		tab++;
		if ( tab >= tabs.getTabCount() ) tab = tabs.getTabCount()-1;
		tabs.setSelectedIndex(tab);
		
	}

	protected void actionStop() {
		if (thread == null)
			return;

		if (JOptionPane.showConfirmDialog(panel,
				"Really stop the current task?", "WARNING",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		thread.stop();

		bAction.setText(" Start ");
		if ( bAction2 != null ) bAction2.setEnabled(false);
		for (Iterator i = attributes.values().iterator(); i.hasNext();) {
			((JComponent) i.next()).setEnabled(true);
		}

	}

	protected void actionStart() {

		if (!config.getAttribute("warning").equals(""))
			if (JOptionPane.showConfirmDialog(panel, config
					.getAttribute("warning"), "WARNING",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
				return;
		
		bAction.setText(" Stop ");
		if ( bAction2 != null ) bAction2.setEnabled(false);
		for ( Cnt cnt : attributes.values() ) {
			cnt.setEnabled(false);
		}

		try {
			ScriptIfc script = (ScriptIfc) this.getClass().getClassLoader()
					.loadClass(config.getAttribute("class")).newInstance();

			Hashtable<String,String> parameters = new Hashtable<String,String>();

			for (String key : attributes.keySet() ) {
				
				Cnt value = attributes.get(key);
				value.transferToScript(script);
				
				parameters.put(key, value.getValue().toString());
			}

			DMConnection con2 = null;
			if ( con != null )
				con2 = con.cloneConnection(cbTransaction.isSelected());
			
			logger.start();
			if (con2 != null)
				logger
						.printMsg("Docbase: "
								+ con2.getSession().getDocbaseName());
			if (con2 != null)
				logger.printMsg("User: " + con2.getUserName());
			for (String key : parameters.keySet()) {
				logger.printMsg(key + "=" + parameters.get(key));
			}
			logger.printMsg();
			script.initialize(node, con2, logger);

			if (sourceTabs.getSelectedIndex() == 0) {
				script.execute(node, con2, targets, logger);

			} else if (sourceTabs.getSelectedIndex() == 1) {

				String dql = "SELECT distinct d.r_object_id FROM "
						+ tWhereClause.getText();
				logger.out.println("DQL: " + dql);
				IDfQuery query = con2.createQuery(dql);
				IDfCollection res = query.execute(con2.getSession(),
						IDfQuery.EXEC_QUERY);

				if (cbWhereCached.isSelected()) {
					File tmp = null;
					do {
						tmp = new File("tmp_" + System.currentTimeMillis()
								+ ".tmp");
					} while (!tmp.createNewFile());
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(tmp));
					int allCnt = 0;
					while (res.next()) {
						oos.writeUTF(res.getString("r_object_id"));
						allCnt++;
					}
					oos.close();

					ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(tmp));

					int cnt = 0;
					int packCnt = 0;
					IDfPersistentObject[] targets = new IDfPersistentObject[100];
					try {
						while (ois.available() >= 0) {
							String id = ois.readUTF();
							try {
								targets[cnt] = con2.getPersistentObject(id);
							} catch (DfException dfe) {
								logger.err.println("--- END fetch Object: "
										+ dfe);
							}
							cnt++;
							if (cnt >= targets.length) {
								logger.printMsgLine();
								logger.printMsg("Sending Pack: "
										+ (packCnt * 100 + 1) + " to "
										+ (packCnt * 100 + 100) + " / "
										+ allCnt);
								logger.printMsgLine();
								script.execute(node, con2, targets, logger);
								cnt = 0;
								packCnt++;
							}

						}
					} catch (EOFException eofe) {
						logger.out.println("*** " + eofe);
					}

					if (cnt != 0) {
						// send the rest
						IDfPersistentObject[] old = targets;
						targets = new IDfPersistentObject[cnt];
						System.arraycopy(old, 0, targets, 0, cnt);
						old = null;
						logger.printMsgLine();
						logger.printMsg("Sending Last Pack: "
								+ (packCnt * 100 + 1) + " to "
								+ (packCnt * 100 + cnt) + " / " + allCnt);
						logger.printMsgLine();
						script.execute(node, con2, targets, logger);
					}

					ois.close();
					tmp.delete();

				} else {
					IDfPersistentObject buffer[] = new IDfPersistentObject[100];
					int pos = 0;
					int cnt = 0;
					while (res.next()) {

						buffer[pos] = con2.getPersistentObject(res
								.getString("r_object_id"));

						pos++;
						cnt++;

						if (pos >= buffer.length) {
							logger.printMsgLine();
							logger.printMsg((cnt - pos) + " - " + cnt);
							logger.printMsgLine();
							script.execute(node, con2, buffer, logger);
							pos = 0;
						}

					}

					if (pos != 0) {
						IDfPersistentObject old[] = buffer;
						buffer = new IDfPersistentObject[pos];
						System.arraycopy(old, 0, buffer, 0, pos);
						old = null;
						logger.printMsgLine();
						logger.printMsg((cnt - pos) + " - " + cnt);
						logger.printMsgLine();
						script.execute(node, con2, buffer, logger);
					}
				}

			} else if (sourceTabs.getSelectedIndex() == 2) {

				File inputFile = new File(tCsvFile.getText());
				FileReader is = new FileReader(inputFile);
				CSVReader reader = new CSVReader(is, ';', '"', true, true);
				if (cbCsvHasHeader.isSelected())
					reader.skipToNextLine();

				IDfPersistentObject buffer[] = new IDfPersistentObject[100];

				int pos = 0;
				int cnt = 0;
				while (true) {

					try {
						buffer[pos] = con2.getPersistentObject(reader.get());
					} catch (EOFException eofe) {
						break;
					} catch (DfException dfe) {
						try {
							reader.skipToNextLine();
						} catch (EOFException eofe) {
						}
						continue;
					}

					try {
						reader.skipToNextLine();
					} catch (EOFException eofe) {
					}

					pos++;
					cnt++;

					if (pos >= buffer.length) {
						logger.printMsgLine();
						logger.printMsg((cnt - pos) + " - " + cnt);
						logger.printMsgLine();
						script.execute(node, con2, buffer, logger);
						pos = 0;
					}

				}

				if (pos != 0) {
					IDfPersistentObject buffer2[] = new IDfPersistentObject[pos];
					System.arraycopy(buffer, 0, buffer2, 0, pos);
					logger.printMsgLine();
					logger.printMsg((cnt - pos) + " - " + cnt);
					logger.printMsgLine();
					script.execute(node, con2, buffer2, logger);
				}

			} else if (sourceTabs.getSelectedIndex() == 3) {

				StringReader is = new StringReader(tDirectList.getText());
				CSVReader reader = new CSVReader(is, ';', '"', true, true);
				if (cbDirectHasHeader.isSelected())
					reader.skipToNextLine();

				IDfPersistentObject buffer[] = new IDfPersistentObject[100];

				int pos = 0;
				int cnt = 0;
				while (true) {

					try {
						buffer[pos] = con2.getPersistentObject(reader.get());
					} catch (EOFException eofe) {
						break;
					} catch (DfException dfe) {
						try {
							reader.skipToNextLine();
						} catch (EOFException eofe) {
						}
						continue;
					}

					try {
						reader.skipToNextLine();
					} catch (EOFException eofe) {
					}

					pos++;
					cnt++;

					if (pos >= buffer.length) {
						logger.printMsgLine();
						logger.printMsg((cnt - pos) + " - " + cnt);
						logger.printMsgLine();
						script.execute(node, con2, buffer, logger);
						pos = 0;
					}

				}

				if (pos != 0) {
					IDfPersistentObject buffer2[] = new IDfPersistentObject[pos];
					System.arraycopy(buffer, 0, buffer2, 0, pos);
					logger.printMsgLine();
					logger.printMsg((cnt - pos) + " - " + cnt);
					logger.printMsgLine();
					script.execute(node, con2, buffer2, logger);
				}

			}

			script.destroy(node,con2,logger);

			if (con2.isTransaction() ) {
				if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(tabs, "Commit the last action?","Commit",JOptionPane.YES_NO_OPTION) )
					con2.commitTransaction();
				else
					con2.abordTransaction();
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(panel, e.toString(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
				
		thread = null;
		logger.stop();

		bAction.setText(" Start ");
		bAction2.setEnabled(true);
		for ( Cnt cnt : attributes.values() ) {
			cnt.setEnabled(true);
		}


	}

	private static class TypePanel extends JPanel {
		private Element element;
		private JTextField path;

		public TypePanel(Element def, final DMConnection con, final PluginNode node ) {
			setLayout(new BorderLayout());
			element = def;
			path = new JTextField(element.getAttribute("value"));
			add(path, BorderLayout.CENTER);
			JButton b = new JButton("?");
			add(b, BorderLayout.EAST);
			b.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					TypesTreePanel type = new TypesTreePanel(con,
							(ApiTypes) node.getSingleApi(ApiTypes.class),
							element.getAttribute("start"), element
									.getAttribute("ignore").split(","), null);
					JButton bOk = new JButton("OK");
					bOk.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							SwingUtilities.getWindowAncestor(
									(JComponent) e.getSource()).dispose();

						}

					});
					JPanel typePanel = new JPanel();
					typePanel.setLayout(new BorderLayout());
					typePanel.add(type, BorderLayout.CENTER);
					typePanel.add(bOk, BorderLayout.SOUTH);

					JOptionPane.showOptionDialog(TypePanel.this,
							"Insert name and type", "Create Folder", 0,
							JOptionPane.QUESTION_MESSAGE, null,
							new Object[] { typePanel }, null);
					path.setText(type.getSelected());

				}

			});

		}

		public Object getSelected() {
			return path.getText();
		}

		public void setSelected(String in) {
			path.setText(in);
		}

	}

	private static class FilePanel extends JPanel {
		private JTextField path;
		private Element element;

		public FilePanel(Element def) {
			element = def;
			setLayout(new BorderLayout());
			path = new JTextField(element.getAttribute("value"));
			add(path, BorderLayout.CENTER);
			JButton b = new JButton("?");
			add(b, BorderLayout.EAST);
			b.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					JFileChooser fc = new JFileChooser();
					fc.setSelectedFile(new File(path.getText()));
					String mode = element.getAttribute("mode");

					if (mode.indexOf("file") >= 0 && mode.indexOf("dir") >= 0)
						fc
								.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					else if (mode.indexOf("dir") >= 0)
						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					NodeList extList = XmlTool.getLocalElements(element, "ext");
					for (int i = 0; i < extList.getLength(); i++) {
						Element ext = (Element) extList.item(i);
						fc.addChoosableFileFilter(new FileExtFilter(ext
								.getAttribute("title"), ext.getAttribute("ext")
								.split(",")));
					}
					if (fc.showOpenDialog(FilePanel.this) == JFileChooser.APPROVE_OPTION) {
						path.setText(fc.getSelectedFile().getPath());
					}

				}

			});

		}

		public Object getSelected() {
			return path.getText();
		}

		public void setSelected(String in) {
			path.setText(in);
		}

	}
	
	static abstract class Cnt {

		protected Element item;
		protected String name;
		protected DMConnection con;
		protected PluginNode node;

		abstract public void createUI(JPanel panel2);

		abstract public void setEnabled(boolean b);

		abstract public void setValue(String in);

		public void transferToScript(ScriptIfc script) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String fname = getSetPrefix() + name.substring(0, 1).toUpperCase()
			+ name.substring(1);
			script.getClass().getMethod(fname,
					new Class[] { getValueType() }).invoke(script,
					new Object[] { getValue() });
		}

		protected String getSetPrefix() {
			return "set";
		}

		protected Class<?> getValueType() {
			return String.class;
		}

		abstract public Object getValue();
		
		public void init(Element item, DMConnection con, PluginNode node) {
			this.item=item;
			this.con = con;
			this.node = node;
			name = item.getAttribute("name");
		}
	}
	
	static class CheckboxCnt extends Cnt {

		private JCheckBox cb;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(" "));
			cb = new JCheckBox(item.getAttribute("title"));
			if (item.getAttribute("value").equals("1"))
				cb.setSelected(true);
			panel2.add(cb);
		}

		@Override
		public Object getValue() {
			return cb.isSelected();
		}
		
		protected Class<?> getValueType() {
			return boolean.class;
		}

		@Override
		public void setValue(String in) {
			cb.setSelected(ACast.toboolean(in, false));
		}

		@Override
		public void setEnabled(boolean b) {
			cb.setEnabled(b);
		}
		
	}
	
	static class TextCnt extends Cnt {

		private JTextField tf;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			tf = new JTextField(item.getAttribute("value"));
			panel2.add(tf);
		}

		@Override
		public Object getValue() {
			return tf.getText();
		}

		@Override
		public void setValue(String in) {
			tf.setText(in);
		}

		@Override
		public void setEnabled(boolean b) {
			tf.setEditable(b);
		}
		
	}
	
	static class TextAreaCnt extends Cnt {

		private JTextArea tf;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			tf = new JTextArea(item.getAttribute("value"));
			JScrollPane scroller = new JScrollPane(tf);
			scroller.setPreferredSize(new Dimension(200,200));
			panel2.add(scroller);
		}

		@Override
		public Object getValue() {
			return tf.getText();
		}

		@Override
		public void setValue(String in) {
			tf.setText(in);
		}

		@Override
		public void setEnabled(boolean b) {
			tf.setEditable(b);
		}
		
	}
	
	static class PasswordCnt extends Cnt {

		private JPasswordField tf;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			tf = new JPasswordField(item
					.getAttribute("value"));
			panel2.add(tf);
		}
		
		@Override
		public Object getValue() {
			return tf.getText();
		}

		@Override
		public void setValue(String in) {
			tf.setText(in);
		}

		@Override
		public void setEnabled(boolean b) {
			tf.setEditable(b);
		}
		
	}
	
	static class TypeCnt extends Cnt {

		private TypePanel ttp;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			ttp = new TypePanel(item, con, node);
			panel2.add(ttp);
		}

		@Override
		public Object getValue() {
			return ttp.getSelected();
		}

		@Override
		public void setValue(String in) {
			ttp.setSelected(in);
		}

		@Override
		public void setEnabled(boolean b) {
			ttp.setEnabled(b);
		}
		
	}
	
	static class SelectCnt extends Cnt {

		private JComboBox cb;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			cb = new JComboBox();
			NodeList options = XmlTool.getLocalElements(item, "option");
			for (int j = 0; j < options.getLength(); j++)
				cb.addItem(((Element) options.item(j))
						.getAttribute("value"));
			cb.setSelectedItem(item.getAttribute("selected"));
			panel2.add(cb);
		}
		
		public void transferToScript(ScriptIfc script) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String valueStr = (String) cb.getSelectedItem();
			String fname = getSetPrefix() + name.substring(0, 1).toUpperCase() + name.substring(1);
			script.getClass().getMethod(fname,
					new Class[] { int.class, String.class }).invoke(
					script,
					new Object[] {
						new Integer(cb.getSelectedIndex()), valueStr });
		}

		@Override
		public Object getValue() {
			return cb.getSelectedItem();
		}

		@Override
		public void setValue(String in) {
			cb.setSelectedItem(in);
		}

		@Override
		public void setEnabled(boolean b) {
			cb.setEnabled(b);
		}
	}
	
	static class FileCnt extends Cnt {

		private FilePanel fp;

		@Override
		public void createUI(JPanel panel2) {
			panel2.add(new JLabel(item.getAttribute("title")));
			fp = new FilePanel(item);
			panel2.add(fp);
		}

		@Override
		public Object getValue() {
			return fp.getSelected();
		}

		@Override
		public void setValue(String in) {
			fp.setSelected(in);
		}

		@Override
		public void setEnabled(boolean b) {
			fp.setEnabled(b);
		}
		
	}
	
	static class FilesCnt extends Cnt {

		private DefaultListModel model;
		private JList list;
		private JScrollPane scroller;
		private JButton bAdd;
		private JButton bRemove;
		private JButton bUp;
		private JButton bDown;

		@Override
		public void createUI(JPanel panel2) {
			
			model = new DefaultListModel();
			list = new JList(model);
			scroller = new JScrollPane(list);
			scroller.setPreferredSize(new Dimension(200,200));
			
			bAdd = new JButton("Add");
			bAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionAdd();
				}
			});
			bRemove = new JButton("RM");
			bRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionRemove();
				}
			});
			bUp = new JButton("Up");
			bUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionUp();
				}
			});
			bDown = new JButton("Dwn");
			bDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionDown();
				}
			});
			
			JPanel p1 = new JPanel();
			p1.setLayout(new BoxLayout(p1,BoxLayout.Y_AXIS));
			p1.add(bAdd);
			p1.add(bRemove);
			p1.add(bUp);
			p1.add(bDown);
			
			JPanel p2 = new JPanel();
			p2.setLayout(new BorderLayout());
			p2.add(scroller,BorderLayout.CENTER);
			p2.add(p1,BorderLayout.EAST);
			
			panel2.add(new JLabel(item.getAttribute("title")));
			
			panel2.add(p2);
		}

		protected void actionDown() {
			try {
				int[] idx = list.getSelectedIndices();
				if ( idx[ idx.length-1 ] >= (model.getSize() - 1) ) return;
				model.insertElementAt( model.remove( idx[idx.length-1] + 1 ), idx[0]);
			} catch ( Throwable t ) {
				t.printStackTrace();
			}
		}

		protected void actionUp() {
			try {
				int[] idx = list.getSelectedIndices();
				if ( idx[0] == 0 ) return;
				model.insertElementAt( model.remove(idx[0]-1), idx[idx.length-1] + 1 );
			} catch ( Throwable t ) {
				t.printStackTrace();
			}
		}

		protected void actionRemove() {
			try {
				int[] idx = list.getSelectedIndices();
				for ( int i = idx.length; i > 0; i--)
					model.remove(idx[i-1]);
			} catch ( Throwable t ) {
				t.printStackTrace();
			}
		}

		protected void actionAdd() {
			JFileChooser fc = new JFileChooser();
			if ( list.getSelectedIndex() > -1 ) 
				fc.setSelectedFile(new File((String)list.getSelectedValue()));
			String mode = item.getAttribute("mode");

			if (mode.indexOf("file") >= 0 && mode.indexOf("dir") >= 0)
				fc
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			else if (mode.indexOf("dir") >= 0)
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			fc.setMultiSelectionEnabled(true);
			
			NodeList extList = XmlTool.getLocalElements(item, "ext");
			for (int i = 0; i < extList.getLength(); i++) {
				Element ext = (Element) extList.item(i);
				fc.addChoosableFileFilter(new FileExtFilter(ext
						.getAttribute("title"), ext.getAttribute("ext")
						.split(",")));
			}
			if (fc.showOpenDialog(list) == JFileChooser.APPROVE_OPTION) {
				for ( File f : fc.getSelectedFiles() )
					model.addElement(f.getPath());
			}

		}

		@Override
		public void transferToScript(ScriptIfc script) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			String[] out = new String[model.size()];
			for ( int i = 0; i < model.getSize(); i++ )
				out[i] = (String)model.get(i);
			
			String fname = getSetPrefix() + name.substring(0, 1).toUpperCase() + name.substring(1);
			script.getClass().getMethod(fname,
					new Class[] { String[].class }).invoke(
					script,
					new Object[] {out});
		}
		
		@Override
		public Object getValue() {
			StringBuffer sb = null;
			for ( int i = 0; i < model.getSize(); i++ )
				if ( sb == null )
					sb = new StringBuffer().append(model.get(i));
				else
					sb.append("|").append(model.get(i));
			return ( sb == null ? "" : sb.toString() );
		}

		@Override
		public void setValue(String in) {
			if ( in == null || in.length() == 0) {
				model.removeAllElements();
				return;
			}
			String[] parts = in.split("\\|");
			for ( String p : parts )
				if ( p.length() != 0 )
					model.addElement(p);
		}

		@Override
		public void setEnabled(boolean b) {
			// list.setEnabled(b);
			bAdd.setEnabled(b);
			bRemove.setEnabled(b);
			bUp.setEnabled(b);
			bDown.setEnabled(b);
		}
		
	}

}
