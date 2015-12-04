package de.mhu.hair.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileFilter;

import de.mhu.hair.gui.JavaInfoDialog;

public class ClasspathConfigEditor extends JPanel {

	private LinkedList<File> listDir;
	private LinkedList<File> listFile;
	private DefaultListModel model;
	private JList list;
	private JFileChooser chooser;
	private String cpPath;

	public ClasspathConfigEditor() {

		listDir  = new LinkedList<File>();
		listFile = new LinkedList<File>();
		
		cpPath = LocalArgsParser.getInstance().isSet("hair_classpath_properties") ? LocalArgsParser.getInstance().getValue("hair_classpath_properties", 0) : "classpath.properties";

		try {
			File cpConfig = new File( cpPath );
			if ( cpConfig.exists() ) {
				Properties p = new Properties();
				p.load(new FileInputStream(cpConfig));
				
				for ( Object okey : p.keySet() ) {
					String key = (String)okey;
					if ( key.endsWith(".dir") )
						listDir.add( new File(p.getProperty(key)) );
					if ( key.endsWith(".file") )
						listFile.add( new File(p.getProperty(key)) );
				}
							
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".jar");
			}
			@Override
			public String getDescription() {
				return "JAR Files";
			}
		});
		
		JFrame frame =  new JFrame();
		halfFrame(frame);
		centerFrame(frame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Collect classpath for mhu-hair; Append documentum/shared/dfc directory");
		initUI();
		updateList();
		frame.getContentPane().add(this);
		frame.setVisible(true);
	}

	private void updateList() {
		
		model.removeAllElements();
		
		for ( File file : listDir ) {
			model.addElement( "D " + ( file.exists() ? "  " : "! " ) + file.getAbsolutePath() );
		}
		
		for ( File file : listFile ) {
			model.addElement( "F " + ( file.exists() ? "  " : "! " ) + file.getAbsolutePath() );
		}
		
	}

	private void initUI() {
		
		JButton bSave    = new JButton(" Save and exit ");
		JButton bAddDir  = new JButton(" Append directory ");
		JButton bAddFile = new JButton(" Append file ");
		JButton bRemove  = new JButton(" Remove ");
		JButton bInfo    = new JButton(" System Info " );
		
		bSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSave();
			}});
		bAddDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddDir();
			}});
		bAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionAddFile();
			}});
		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionRemove();
			}});
		bInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JavaInfoDialog();
			}});
		
		JToolBar bar = new JToolBar();
		bar.add( bSave );
		bar.addSeparator();
		bar.add( bAddDir);
		bar.add(bAddFile);
		bar.addSeparator();
		bar.add(bRemove);
		bar.addSeparator();
		bar.add(bInfo);
		
		model = new DefaultListModel();
		list = new JList(model);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroller = new JScrollPane(list);
		
		setLayout(new BorderLayout());
		
		add( bar, BorderLayout.NORTH);
		add( scroller, BorderLayout.CENTER);
		
	}

	protected void actionRemove() {
		Object[] sel = list.getSelectedValues();
		for ( Object item : sel) {
			String str = (String)item;
			if (str.startsWith("D")) {
				listDir.remove( new File(str.substring(4)));
			} else {
				listFile.remove( new File(str.substring(4)));
			}
		}
		updateList();
	}

	protected void actionAddFile() {
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION )
			return;
		for ( File file : chooser.getSelectedFiles() )
			listFile.add(file);
		updateList();
	}

	protected void actionAddDir() {
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION )
			return;
		for ( File file : chooser.getSelectedFiles() )
			listDir.add(file);
		updateList();
	}

	protected void actionSave() {
		
		// validate
		
		boolean ok = false;
		
		for ( File f : listFile ) {
			if ( f.exists() && f.getName().equals("dfc.jar")) {
				ok = true;
				break;
			}
		}
		
		for ( File f : listDir ) {
			if ( f.exists() ) {
				String[] list = f.list();
				for ( String item : list )
					if (item.equals("dfc.jar")) {
						ok = true;
						break;
					}
			}
		}
		
		if (!ok) {
			if ( JOptionPane.showConfirmDialog(this, "dfc.jar not found in the classpath.\nDo you realy want to save it?", "Realy save?", JOptionPane.OK_CANCEL_OPTION)
					!= JOptionPane.OK_OPTION )
				return;
		}
		// save
		
		try {
			File cpConfig = new File( cpPath );
			Properties p = new Properties();
			int cnt = 0;
			for ( File f : listFile ) {
				cnt++;
				p.setProperty("" + cnt + ".file", f.getAbsolutePath());
			}
			cnt = 0;
			for ( File f : listDir ) {
				cnt++;
				p.setProperty("" + cnt + ".dir", f.getAbsolutePath());
			}
			FileOutputStream fos = new FileOutputStream(cpConfig);
			p.store(fos, "");
			fos.close();
			
			JOptionPane.showMessageDialog(this, "File saved\n" + cpPath );

			System.exit(100);
		} catch (Exception e ) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Can't save file\n" + cpPath);
		}
	}
	
	public static void main(String[] args) {
		LocalArgsParser.initialize(args);
		new ClasspathConfigEditor();
	}
	
	static public void centerFrame(Window _frame) {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();

		int width = (int) _frame.getSize().getWidth();
		int height = (int) _frame.getSize().getHeight();

		int x = (screenWidth - width) / 2;
		int y = (screenHeight - height) / 2;

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		_frame.setLocation(x, y);

	}
	
	static public void halfFrame(Window _frame) {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();

		_frame.setSize(screenWidth / 2, screenHeight / 2);

	}
	
}
