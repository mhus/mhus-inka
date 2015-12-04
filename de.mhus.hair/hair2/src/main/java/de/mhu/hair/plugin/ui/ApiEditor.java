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
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.documentum.fc.common.DfException;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.tools.DctmTool;
import de.mhu.hair.tools.ObjectWorkerTool;
import de.mhu.hair.tools.DctmTool.DctmExecuteApiListener;
import de.mhu.lib.AString;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.FileExtFilter;
import de.mhu.lib.swing.edi.CustomTokenMarker;
import de.mhu.lib.swing.edi.EdiArea;
import de.mhu.lib.swing.edi.textarea.Token;
import de.mhu.res.img.LUF;

public class ApiEditor extends JPanel implements DctmExecuteApiListener {

	private DMConnection con;
	private EdiArea editor;
	private JButton bExecute;
	private LoggerPanel console;
	private JButton bHistory;
	private Vector history;
	private JCheckBox cbVariables;
	private JButton bPredefined;
	private Properties predefined;
	private Properties types;

	private PluginNode node;

	public ApiEditor(PluginNode pNode, File pPredefined) {
		this(pNode, loadPredefined(pPredefined));
	}

	public ApiEditor(PluginNode pNode, Properties pPredefined) {
		node = pNode;
		con = (DMConnection) pNode.getSingleApi(DMConnection.class);
		predefined = pPredefined;
		history = new Vector();
		types = new Properties();
		try {
			types.load(this.getClass().getClassLoader().getResourceAsStream(
					this.getClass().getPackage().getName().replace('.', '/')
							+ "/api.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		initUI();

	}

	private void initUI() {

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		editor = new EdiArea();
		editor.getInputHandler().addKeyBinding("C+ENTER", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExecute();
			}
		});
		
		for ( Object v : types.keySet() )
			editor.addCcValue( v.toString() );
		
		CustomTokenMarker marker = new CustomTokenMarker(new String[] {}, null,null,null);
		
		for ( Object v : types.keySet() )
			marker.getKeywordMap().add( v.toString(), Token.KEYWORD1 );
		
		editor.setTokenMarker(marker);
		
		editor.setMinimumSize(new Dimension(100, 100));
		
//		editor.setFont(LUF.CONSOLE_FONT);
//		editor.addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));

//		JScrollPane scroller = new JScrollPane(editor);
//		scroller.setMinimumSize(new Dimension(100, 100));
		split.setTopComponent(editor);

		console = new LoggerPanel(null,null);
		console.getConsole().addMouseListener(new ObjectWorkerTool.TextIdGrapper(node, con));
//		JScrollPane scroller2 = new JScrollPane(text);

		split.setBottomComponent(console.getConsole());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		bExecute = new JButton(" Execute ");
		panel.add(bExecute);

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
									"Predefined", new String[] { "papi" }));
							if (chooser.showOpenDialog(editor) != JFileChooser.APPROVE_OPTION)
								return;
							Properties out = loadPredefined(chooser
									.getSelectedFile());
							if (out != null)
								predefined = out;
						}

					});
					menu.add(item);

					menu.addSeparator();
					item = new JMenuItem("Append Expresion to File");
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							JFileChooser chooser = new JFileChooser();
							chooser.setCurrentDirectory(new File("."));
							chooser.addChoosableFileFilter(new FileExtFilter(
									"Predefined", new String[] { "papi" }));
							if (chooser.showSaveDialog(editor) != JFileChooser.APPROVE_OPTION)
								return;
							File file = chooser.getSelectedFile();
							Properties out = null;
							if (!file.exists()) {
								if (!file.getName().endsWith(".papi"))
									file = new File(file.getParent(), file
											.getName()
											+ ".papi");
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

							out.setProperty(title, ","
									+ (cbVariables.isSelected() ? "v" : "")
									+ ":" + editor.getExecutedPart());

							try {
								FileOutputStream fis = new FileOutputStream(
										chooser.getSelectedFile());
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
		setPreferredSize(new Dimension(500, 300));

		bExecute.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionExecute();
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

	protected void importEntry(String entry) {
		String type = AString.beforeIndex(entry, ':');
		cbVariables.setSelected(false);
		if (type.endsWith(",v")) {
			cbVariables.setSelected(true);
			type = type.substring(0, type.length() - 2);
		}
		editor.setText(AString.afterIndex(entry, ':'));
	}

	protected void actionExecute() {

		boolean res = false;

		System.out.println(">>> API EDITOR:");
		System.out.println("--- " + editor.getExecutedPart());

		// add type to every line
		String[] lines = editor.getExecutedPart().split("\n");
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			if (findType(lines[i].split(",")[0]) == null) {
				addTextLine("*** Error: type not found for command: "
						+ lines[i].split(",")[0]);
				return;
			}
			out.append(findType(lines[i].split(",")[0]));
			out.append(',');
			out.append(cbVariables.isSelected() ? "v" : "");
			out.append(':');
			out.append(findCmd(lines[i]));
			out.append('\n');
		}
		try {
			DMConnection con2 = con.cloneConnection(false);
			res = DctmTool.executeApi(con2, out.toString(), editor, this);
			con2.disconnect();
		} catch (DfException e) {
			e.printStackTrace();
		}

		if (res) {
			// history
			String hstr = (cbVariables.isSelected() ? ",v" : "") + ":"
					+ editor.getExecutedPart();
			if (!history.contains(hstr)) {
				history.add(hstr);
				if (history.size() > 10)
					history.remove(0);
			}
		}
	}

	public String findType(String cmd) {
		int pos = cmd.indexOf(',');
		if (pos < 0)
			pos = cmd.length();
		pos = cmd.substring(0, pos).indexOf(':');
		if (pos > 0)
			return cmd.substring(0, pos);
		return types.getProperty(cmd);
	}

	public String findCmd(String cmd) {
		int pos = cmd.indexOf(',');
		if (pos < 0)
			pos = cmd.length();
		pos = cmd.substring(0, pos).indexOf(':');
		if (pos > 0)
			return cmd.substring(pos + 1);
		return cmd;
	}

	public void addTextLine(String string) {
		
		console.out.println(string);
		
	}

	public static Properties loadPredefined(File file) {
		if (file == null)
			return new Properties();
		try {
			Properties p = new Properties();

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
