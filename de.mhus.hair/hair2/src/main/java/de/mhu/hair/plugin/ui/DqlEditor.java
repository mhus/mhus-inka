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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfException;
import com.documentum.fc.common.IDfId;

import de.mhu.hair.api.ApiObjectSelect;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiTypes;
import de.mhu.hair.tools.DctmTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.hair.tools.DctmTool.DctmExecuteDqlListener;
import de.mhu.lib.AFile;
import de.mhu.lib.AString;
import de.mhu.lib.ATimekeeper;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.io.CSVWriter;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.FileExtFilter;
import de.mhu.lib.swing.edi.CustomTokenMarker;
import de.mhu.lib.swing.edi.EdiArea;
import de.mhu.res.img.LUF;

public class DqlEditor extends JPanel implements DctmExecuteDqlListener {

	private DMConnection con;

	private EdiArea editor;

	private DMList list;

	private JButton bExecute;

	private LoggerPanel console;

	private JTabbedPane tab;

	private JComboBox cbType;

	private JButton bHistory;

	private Vector history;

	private JCheckBox cbVariables;

	private JButton bPredefined;

	private Properties predefined;

	private PluginNode node;

	private Timer timer;

	private Timer dqlTimer = new Timer(true);

	private DMListChangeObjListener listListener;

	private LinkedList<String> keywords;

	private List<String> types;

	private LinkedList<String> names;

	private LinkedList<String> functions;

	public DqlEditor(PluginNode pNode, File pPredefined) {
		this(pNode, loadPredefined(pPredefined));
	}

	public DqlEditor(PluginNode pNode, Properties pPredefined) {
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		timer = ((ApiSystem) pNode.getSingleApi(ApiSystem.class)).getTimer();
		predefined = pPredefined;
		history = new Vector();
		
		initFunctions();
		initKeywords();
		initNames();
		initTypes();
		
		initUI();

	}
	
	public EdiArea getEditor() {
		return editor;
	}
	
	private void initTypes() {
		ApiTypes api = (ApiTypes) node.getSingleApi(ApiTypes.class);
		if ( types == null ) {
			
			try {
				File f = new File("dql.tmp");
				if (f.exists()) {
					String content = AFile.readFile(f);
					types = new LinkedList<String>();
					for (String part : content.split(",")) {
						types.add(part);
					}
					return;
				}
			} catch (Exception e) {
				types = null;
			}
			if ( ! api.isNamesListInitialized() ) {
				if ( JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, "Load Type Labels Now?", "Loading Types", JOptionPane.YES_NO_OPTION ) )
						return;
			}
			types = api.getNamesList();
			
			try {
				File f = new File("dql.tmp");
				StringBuffer content = new StringBuffer();
				for (String part : types) {
					if (content.length() > 0)
						content.append(",");
					content.append(part);
				}
				AFile.writeFile(f, content.toString());
			} catch (Exception e) {
			}
			
			
		}
	}
	
	private void initFunctions() {
		if ( functions == null ) {
			functions = new LinkedList<String>();
			Properties p = new Properties();
			try {
				p.load(this.getClass().getClassLoader().getResourceAsStream(
						this.getClass().getPackage().getName().replace('.', '/')
								+ "/dql.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			for ( Map.Entry<Object,Object> item : p.entrySet() ) {
				if ( "function".equals( item.getValue() ) )
					functions.add(item.getKey().toString());
			}
		}
	}
	
	private void initNames() {
		if ( names == null ) {
			names = new LinkedList<String>();
			Properties p = new Properties();
			try {
				p.load(this.getClass().getClassLoader().getResourceAsStream(
						this.getClass().getPackage().getName().replace('.', '/')
								+ "/dql.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			for ( Map.Entry<Object,Object> item : p.entrySet() ) {
				if ( "name".equals( item.getValue() ) )
					names.add(item.getKey().toString());
			}
//			names.add("(all)");
//			names.add("(deleted)");
		}
	}
	
	private void initKeywords() {
		if ( keywords == null ) {
			Properties p = new Properties();
			try {
				p.load(this.getClass().getClassLoader().getResourceAsStream(
						this.getClass().getPackage().getName().replace('.', '/')
								+ "/dql.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			keywords = new LinkedList<String>();
			for ( Map.Entry<Object,Object> item : p.entrySet() ) {
				if ( "keyword".equals( item.getValue() ) )
					keywords.add(item.getKey().toString());
			}

		}
	}

	private void initUI() {

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		editor = new EdiArea();
		editor.getInputHandler().addKeyBinding("C+ENTER", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExecute();
			}
		});
		
		CustomTokenMarker marker = new CustomTokenMarker(keywords,types,functions,names);
		editor.setTokenMarker(marker);
		
		for ( String v : keywords )
			editor.addCcValue( v );
		if ( types != null )
			for ( String v : types )
				editor.addCcValue( v );
		for ( String v : functions )
			editor.addCcValue( v );
		for ( String v : names )
			editor.addCcValue( v );

		editor.setCcTokens(new char[] { ' ', '.', '\n', '\r', '\t', '(', ')', ',', '-' });
		
		editor.setMinimumSize(new Dimension(100, 100));
		
//		editor.setFont(LUF.CONSOLE_FONT);
//		editor.addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));
//
//		JScrollPane scroller = new JScrollPane(editor);
//		scroller.setMinimumSize(new Dimension(100, 100));
		split.setTopComponent(editor);

		tab = new JTabbedPane();

		listListener = new DMListChangeObjListener(node);
		list = new DMList("Result", listListener );
		tab.addTab("List", list);

		console = new LoggerPanel(null,null);
//		text.setFont(LUF.CONSOLE_FONT);
		console.getConsole().addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));
//		JScrollPane scroller2 = new JScrollPane(text);
		tab.addTab("Text", console.getConsole());

		split.setBottomComponent(tab);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		bExecute = new JButton(" Execute ");
		panel.add(bExecute);

		cbType = new JComboBox();
		cbType.addItem("[RAW]");
		cbType.addItem("READ QUERY");
		cbType.addItem("QUERY");
		cbType.addItem("CACHE QUERY");
		cbType.addItem("EXEC QUERY");
		cbType.addItem("EXEC READ QUERY");
		cbType.addItem("READ QUERY");
		cbType.addItem("APPLY");
		cbType.setSelectedIndex(1);
		panel.add(cbType);

		bHistory = new JButton(" History ");

		bHistory.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (history.size() == 0)
					return;

				ActionListener al = new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						importEntry(((JMenuItem) e.getSource()).getText());
					}

				};
				JPopupMenu menu = new JPopupMenu();
				for (int i = 0; i < history.size(); i++) {
					JMenuItem item = new JMenuItem((String) history
							.elementAt(i));
					item.addActionListener(al);
					menu.add(item);
				}

				menu.show(bHistory, 0, bHistory.getHeight());

			}

		});
		panel.add(bHistory);

		cbVariables = new JCheckBox("Variables");
		panel.add(cbVariables);

		bPredefined = new JButton("Predefined");
		bPredefined.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (predefined.size() == 0)
					return;

				ActionListener al = new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						cbVariables.setSelected(true);
						importEntry(predefined.getProperty(((JMenuItem) e
								.getSource()).getText()));
					}

				};
				JPopupMenu menu = new JPopupMenu();
				for (Iterator i = predefined.keySet().iterator(); i.hasNext();) {
					JMenuItem item = new JMenuItem((String) i.next());
					item.addActionListener(al);
					menu.add(item);
				}

				menu.show(bPredefined, 0, bPredefined.getHeight());

			}

		});
		bPredefined.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = new JMenuItem("Load Predefined");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();
							chooser.setCurrentDirectory(new File("."));
							chooser.addChoosableFileFilter(new FileExtFilter(
									"Predefined", new String[] { "pdql" }));
							if (chooser.showOpenDialog(editor) != JFileChooser.APPROVE_OPTION)
								return;
							Properties out = loadPredefined(chooser
									.getSelectedFile());
							if (out != null)
								predefined = out;
						}

					});
					menu.add(item);
					item = new JMenuItem("Append Expresion to File");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();
							chooser.setCurrentDirectory(new File("."));
							chooser.addChoosableFileFilter(new FileExtFilter(
									"Predefined", new String[] { "pdql" }));
							if (chooser.showSaveDialog(editor) != JFileChooser.APPROVE_OPTION)
								return;
							File file = chooser.getSelectedFile();
							Properties out = null;
							if (!file.exists()) {
								if (!file.getName().endsWith(".pdql"))
									file = new File(file.getParent(), file
											.getName()
											+ ".pdql");
								out = new Properties();
							} else {
								out = loadPredefined(file);
							}
							if (out == null)
								return;

							String title = JOptionPane.showInputDialog(editor,
									"Insert title of the query: ");
							if (title == null)
								return;

							if (out.containsKey(title)) {
								if (JOptionPane
										.showConfirmDialog(
												editor,
												"Expresion already exists, Overwrite?",
												"Overwrite?",
												JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
									return;
							}

							out.setProperty(title, (String) cbType
									.getSelectedItem()
									+ ","
									+ (cbVariables.isSelected() ? "v" : "")
									+ ":" + editor.getExecutedPart());

							try {
								FileOutputStream fis = new FileOutputStream(
										file);
								out.store(fis, "");
								fis.close();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

						}

					});
					menu.add(item);
					menu.show(bPredefined, e.getX(), e.getY());

				}

			}

		});
		panel.add(bPredefined);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);

		bExecute.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionExecute();
			}
		});
		bExecute.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = new JMenuItem("Export ids.dat file");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {

							try {
								JFileChooser chooser = new JFileChooser();
								chooser.setCurrentDirectory(new File("."));
								chooser
										.addChoosableFileFilter(new FileExtFilter(
												"DAT", new String[] { "dat" }));
								if (chooser.showSaveDialog(editor) != JFileChooser.APPROVE_OPTION)
									return;
								File f = chooser.getSelectedFile();
								if (f == null)
									return;
								if (!f.getName().endsWith(".dat"))
									f = new File(f.getPath() + ".dat");

								boolean append = false;
								if (f.exists() && f.exists()) {
									int res = JOptionPane.showConfirmDialog(
											editor, "Append?", "Write type",
											JOptionPane.YES_NO_CANCEL_OPTION);
									if (res == JOptionPane.CANCEL_OPTION)
										return;
									append = res == JOptionPane.YES_OPTION;
								}

								System.out.println(">>> DQL EDITOR SAVE:");
								System.out.println("--- "
										+ editor.getExecutedPart());

								FileOutputStream fos = new FileOutputStream(f,
										append);
								final ObjectOutputStream oos = new ObjectOutputStream(
										fos);
								final int[] count = new int[1];

								DctmExecuteDqlListener listener = new DctmExecuteDqlListener() {

									public void addTextLine(String string) {
										DqlEditor.this.addTextLine(string);
									}

									public void showResults(IDfCollection res) {
										try {
											while (res.next()) {
												count[0]++;
												oos
														.writeUTF(res
																.getString("r_object_id"));
											}
										} catch (Exception ex) {
											DqlEditor.this.addTextLine(ex
													.toString());
											ex.printStackTrace();
										}
									}

								};

								DqlEditor.this.addTextLine("Write "
										+ f.getAbsolutePath());

								if (cbType.getSelectedIndex() == 0)
									DctmTool.executeDql(con, editor.getExecutedPart(), editor,
											listener);
								else
									DctmTool.executeDql(con, (String) cbType
											.getSelectedItem()
											+ ","
											+ (cbVariables.isSelected() ? "v"
													: "")
											+ ":"
											+ editor.getExecutedPart(),
											editor, listener);

								oos.close();
								fos.close();
								DqlEditor.this.addTextLine("Insert: "
										+ count[0]);
								DqlEditor.this.addTextLine("FINISHED");

							} catch (Exception ex) {
								DqlEditor.this.addTextLine(ex.toString());
								ex.printStackTrace();
							}

						}

					});

					menu.add(item);

					item = new JMenuItem("Export csv file");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {

							try {
								JFileChooser chooser = new JFileChooser();
								chooser.setCurrentDirectory(new File("."));
								chooser
										.addChoosableFileFilter(new FileExtFilter(
												"CSV", new String[] { "csv" }));
								if (chooser.showSaveDialog(editor) != JFileChooser.APPROVE_OPTION)
									return;
								File f = chooser.getSelectedFile();
								if (f == null)
									return;
								if (!f.getName().endsWith(".csv"))
									f = new File(f.getPath() + ".csv");

								boolean append = false;
								if (f.exists() && f.exists()) {
									int res = JOptionPane.showConfirmDialog(
											editor, "Append?", "Write type",
											JOptionPane.YES_NO_CANCEL_OPTION);
									if (res == JOptionPane.CANCEL_OPTION)
										return;
									append = res == JOptionPane.YES_OPTION;
								}

								System.out.println(">>> DQL EDITOR SAVE:");
								System.out.println("--- "
										+ editor.getExecutedPart());

								final FileWriter fos = new FileWriter(f, append);
								final CSVWriter oos = new CSVWriter(fos, 2,
										';', '"', false);

								final int[] count = new int[1];
								final boolean finalAppend = append;

								DctmExecuteDqlListener listener = new DctmExecuteDqlListener() {

									public void addTextLine(String string) {
										DqlEditor.this.addTextLine(string);
									}

									public void showResults(IDfCollection res) {
										int attrCount = 0;
										try {
											attrCount = res.getAttrCount();
											if (!finalAppend) {
												for (int i = 0; i < attrCount; i++) {
													oos.put(res.getAttr(i)
															.getName());
												}
												oos.nl();
											}
										} catch (Exception ex) {
											DqlEditor.this.addTextLine(ex
													.toString());
											ex.printStackTrace();
											try {
												oos.close();
												fos.close();
											} catch (Exception ex2) {
												DqlEditor.this.addTextLine(ex2
														.toString());
												ex2.printStackTrace();
											}
											return;
										}

										try {
											while (res.next()) {
												count[0]++;
												for (int i = 0; i < attrCount; i++) {
													oos.put(res.getValueAt(i)
															.asString());
												}
												oos.nl();
											}
										} catch (Exception ex) {
											DqlEditor.this.addTextLine(ex
													.toString());
											ex.printStackTrace();
										}
									}

								};

								DqlEditor.this.addTextLine("Write "
										+ f.getAbsolutePath());

								if (cbType.getSelectedIndex() == 0)
									DctmTool.executeDql(con, editor.getExecutedPart(), editor,
											listener);
								else
									DctmTool.executeDql(con, (String) cbType
											.getSelectedItem()
											+ ","
											+ (cbVariables.isSelected() ? "v"
													: "")
											+ ":"
											+ editor.getExecutedPart(),
											editor, listener);

								oos.close();
								fos.close();
								DqlEditor.this.addTextLine("Insert: "
										+ count[0]);
								DqlEditor.this.addTextLine("FINISHED");

							} catch (Exception ex) {
								DqlEditor.this.addTextLine(ex.toString());
								ex.printStackTrace();
							}

						}

					});

					menu.add(item);

					item = new JMenuItem("Performance Test");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							tab.setSelectedIndex(1);
							final int interval = Integer.parseInt(JOptionPane
									.showInputDialog(DqlEditor.this,
											"Insert Count Interval", "1000"));
							bExecute.setEnabled(false);

							dqlTimer.schedule(new ATimerTask() {

								public void run0() throws Exception {
									try {

										System.out
												.println(">>> DQL EDITOR TEST:");
										System.out
												.println("--- "
														+ editor.getExecutedPart());

										final ATimekeeper tk = new ATimekeeper();
										final int[] count = new int[1];

										tk.start();
										DctmExecuteDqlListener listener = new DctmExecuteDqlListener() {

											public void addTextLine(
													String string) {
												if (tk.isRunning()) {
													tk.stop();
													DqlEditor.this
															.addTextLine("Execution Time: "
																	+ tk
																			.getCurrentTimeAsString(true));
												}
												DqlEditor.this
														.addTextLine(string);
											}

											public void showResults(
													IDfCollection res) {
												if (tk.isRunning()) {
													tk.stop();
													DqlEditor.this
															.addTextLine("Execution Time: "
																	+ tk
																			.getCurrentTime()
																	+ ";"
																	+ tk
																			.getCurrentTimeAsString(true));
												}

												ATimekeeper tk2 = new ATimekeeper();
												tk2.start();
												try {
													while (res.next()) {
														count[0]++;
														if (count[0] % interval == 0) {
															tk2.stop();
															DqlEditor.this
																	.addTextLine(count[0]
																			+ ";"
																			+ tk2
																					.getCurrentTime()
																			+ ";"
																			+ tk2
																					.getCurrentTimeAsString(true));
															tk2.reset();
															tk2.start();
														}
													}
													tk2.stop();
													DqlEditor.this
															.addTextLine(count[0]
																	+ ";"
																	+ tk2
																			.getCurrentTime()
																	+ ";"
																	+ tk2
																			.getCurrentTimeAsString(true));

												} catch (Exception ex) {
													DqlEditor.this
															.addTextLine(ex
																	.toString());
													ex.printStackTrace();
												}

											}

										};

										if (cbType.getSelectedIndex() == 0)
											DctmTool.executeDql(con, editor.getExecutedPart(),
													editor, listener);
										else
											DctmTool
													.executeDql(
															con,
															(String) cbType
																	.getSelectedItem()
																	+ ","
																	+ (cbVariables
																			.isSelected() ? "v"
																			: "")
																	+ ":"
																	+ editor.getExecutedPart(),
															editor, listener);

									} catch (Exception ex) {
										DqlEditor.this.addTextLine(ex
												.toString());
										ex.printStackTrace();
									}
									bExecute.setEnabled(true);
								}
							}, 1);
						}

					});

					menu.add(item);

					menu.show(bExecute, 0, bExecute.getHeight());
				}
			}
		});

//		editor.addKeyListener(new KeyAdapter() {
//
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown())
//					actionExecute();
//			}
//
//		});
	}

	protected void actionExecute() {
		if (!bExecute.isEnabled())
			return;
		tab.setSelectedIndex(1);
		System.out.println(">>> DQL EDITOR:");
		addTextLine(">>> Execute...");
		System.out.println("--- " + editor.getExecutedPart());
		bExecute.setEnabled(false);

		dqlTimer.schedule(new ATimerTask() {

			public void run0() throws Exception {
				boolean res = false;
				ATimekeeper timekeeper = new ATimekeeper();
				DMConnection con2 = con.cloneConnection(!editor.getExecutedPart().toLowerCase().trim().startsWith("select"));
				timekeeper.start();
				if (cbType.getSelectedIndex() == 0)
					res = DctmTool.executeDql(con2, editor.getExecutedPart(), editor, DqlEditor.this);
				else
					res = DctmTool.executeDql(con2, (String) cbType
							.getSelectedItem()
							+ ","
							+ (cbVariables.isSelected() ? "v" : "")
							+ ":"
							+ editor.getExecutedPart(), editor,
							DqlEditor.this);
				timekeeper.stop();
				addTextLine("Execution Time: "
						+ timekeeper.getCurrentTimeAsString(true));
				
				if (con2.isTransaction() ) {
					if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(editor, "Commit the last action?","Commit",JOptionPane.YES_NO_OPTION) )
						con2.commitTransaction();
					else
						con2.abordTransaction();
				}
				con2.disconnect();
				con2=null;
				
				if (res) {
					// history
					String hstr = (String) cbType.getSelectedItem()
							+ (cbVariables.isSelected() ? ",v" : "") + ":"
							+ editor.getExecutedPart();
					if (!history.contains(hstr)) {
						history.add(hstr);
						if (history.size() > 10)
							history.remove(0);
					}
				}
				bExecute.setEnabled(true);
			}

			public void onError(Throwable e) {
				e.printStackTrace();
				addTextLine(e.toString());
				bExecute.setEnabled(true);
			}

		}, 1);

	}

	protected void importEntry(String entry) {
		cbVariables.setSelected(false);
		String type = AString.beforeIndex(entry, ':');
		if (type.endsWith(",v")) {
			cbVariables.setSelected(true);
			type = type.substring(0, type.length() - 2);
		}
		editor.setText(AString.afterIndex(entry, ':'));
		cbType.setSelectedItem(type);
	}

	public void addTextLine(String string) {
		console.out.println(string);
	}

	public void showResults(IDfCollection res) {
		try {
			list.reset();
			for (int i = 0; i < res.getAttrCount(); i++) {
				if ( res.getAttr(i).getName().equals("r_object_id") ) {
					listListener.setObjectIdCellIndex(i);
					break;
				}
			}
			list.show(con, res);
						
			tab.setSelectedIndex(0);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			addTextLine(e.toString());
		}
	}

	public static Properties loadPredefined(File file) {
		try {
			Properties p = new Properties();
			if (file == null)
				return p;

			p.load(new FileInputStream(file));
			/*
			 * String line = null; int cnt = 0; Vector out = new Vector(); while
			 * ( ( line = p.getProperty( "entry." + ( ++cnt ) )) != null )
			 * out.add( line );
			 */
			return p;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;

	}
}
