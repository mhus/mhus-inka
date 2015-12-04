package de.mhu.hair.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.documentum.fc.common.DfException;

import de.mhu.lib.AFile;
import de.mhu.lib.AString;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.ASwing;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class DfcConfigEditor {

	
	public static final int CANCEL = 1;
	public static final int APPROVED = 100;
	public static final int UNKNOWN = 0;
	public static final String DEFAULT_ITEM = "<Standard>";
	private static final int NOT_CHANGED = 2;
	private static final int ERROR = 3;

	private File shared;
	private JSeparator jSeparator1;
	private JButton bSelect;
	private JButton bCancel;
	private JTextArea tConfigValue;
	private JScrollPane jScrollPane1;
	private JLabel lDocbroker;
	private JComboBox cbConfig;
	private JLabel lConfigname;
	private JButton bDeleteConfig;
	private JButton bEditConfig;
	private JButton bNewConfig;

	private File config;
	private File dfc;
	private TreeMap<String, String> dfcTemplates;
	private JPanel mainPanel;
	private Component parentComponent;
	private int result = UNKNOWN;
	private JButton bEncrypt;
	private JButton bSave;
	private File currentFile;

	public DfcConfigEditor() throws Exception {
		this(null);
	}
	
	//$hide>>$
	public DfcConfigEditor(Component parentComponent) throws Exception {
		this.parentComponent = parentComponent;
		if ( !init() ) throw new Exception("Can't init editor");
		mainPanel = initUI();
		reloadConfigs();
	}
	//$hide<<$
	
	private void reloadConfigs() {
		cbConfig.removeAllItems();
		cbConfig.addItem( DEFAULT_ITEM );
		for ( String cname : dfcTemplates.keySet() )
			cbConfig.addItem(cname);
		cbConfig.setSelectedIndex(0);
	}

	protected boolean init() {
		
		if ( shared == null ) {
			String sharedString = null;
			try {
				InputStream is = this.getClass().getResourceAsStream("/dfc.properties");
				Properties p =  new Properties();
				p.load(is);
				is.close();
				sharedString = p.getProperty("dfc.data.dir");
			} catch ( Exception e ) {
				e.printStackTrace();
			}		
			if ( sharedString == null ) {
				sharedString = System.getenv("DOCUMENTUM_SHARED");			
			}
			if ( sharedString == null ) {
				//JOptionPane.showMessageDialog(null, "$DOCUMENTUM_SHARED not set" );
				JFileChooser fs = new JFileChooser();
				fs.setDialogTitle("Choose Documentum Shared");
				fs.setMultiSelectionEnabled(false);
				fs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if ( fs.showOpenDialog(null) != JFileChooser.APPROVE_OPTION )
					return false;
				
				shared = fs.getSelectedFile();
				
			} else {
				shared = new File( sharedString );
			}
		}
		
		if ( ! shared.exists() ) {
			JOptionPane.showMessageDialog(null, "$DOCUMENTUM_SHARED not extists" );
			return false;
		}
		
		if ( ! shared.isDirectory() ) {
			JOptionPane.showMessageDialog(null, "$DOCUMENTUM_SHARED is not a directory" );
			return false;
		}
		
		config = new File( shared, "config" );
		if ( !config.exists() ) {
			JOptionPane.showMessageDialog(null, "$DOCUMENTUM_SHARED/config not extists" );
			return false;
		}
		
		dfc = new File( config, "dfc.properties" );
		
		if ( !config.exists() ) {
			JOptionPane.showMessageDialog(null, "$DOCUMENTUM_SHARED/config/dfc.properties not extists" );
			return false;
		}
		
		// list of dfc templates
		
		String[] list = config.list();
		dfcTemplates = new TreeMap<String,String>();
		for ( String item : list )
			if ( item.startsWith( "dfc-") && item.endsWith( ".properties" ) )
				dfcTemplates.put(item.substring(4,item.lastIndexOf('.')), item );
		
		return true;
	}
	
	protected JPanel initUI() {
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		GroupLayout mainPanelLayout = new GroupLayout((JComponent)panel);
		panel.setLayout(mainPanelLayout);
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    .addComponent(getCbConfig(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			    .addComponent(getLConfigname(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			.addComponent(getLDocbroker(), GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			.addComponent(getJScrollPane1(), 0, 137, Short.MAX_VALUE)
			.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
			.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    .addComponent(getBCancel(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			    .addComponent(getBSelect(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			.addContainerGap());
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createSequentialGroup()
			.addContainerGap()
			.addGroup(mainPanelLayout.createParallelGroup()
			    .addGroup(mainPanelLayout.createSequentialGroup()
			        .addGroup(mainPanelLayout.createParallelGroup()
			            .addComponent(getBCancel(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)
			            .addGroup(GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
			                .addComponent(getLConfigname(), GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)
			                .addGap(13)))
			        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
			        .addGroup(mainPanelLayout.createParallelGroup()
			            .addComponent(getBSelect(), GroupLayout.Alignment.LEADING, 0, 238, Short.MAX_VALUE)
			            .addComponent(getCbConfig(), GroupLayout.Alignment.LEADING, 0, 238, Short.MAX_VALUE)))
			    .addComponent(getLDocbroker(), GroupLayout.Alignment.LEADING, 0, 381, Short.MAX_VALUE)
			    .addComponent(getJScrollPane1(), GroupLayout.Alignment.LEADING, 0, 382, Short.MAX_VALUE))
			.addContainerGap());
		panel.setPreferredSize(new java.awt.Dimension(405, 249));

		toolBar.add(getBNewConfig());
		toolBar.add(getBEditConfig());
		toolBar.add(getBDeleteConfig());
		toolBar.add(getJSeparator1());
		toolBar.add(getBSave());
		toolBar.add(getBEncrypt());

		mainPanel.add(panel,BorderLayout.CENTER);
		mainPanel.add(toolBar,BorderLayout.NORTH);
		toolBar.setPreferredSize(new java.awt.Dimension(405, 27));
		return mainPanel;
	}

	private JButton getBNewConfig() {
		if(bNewConfig == null) {
			bNewConfig = new JButton();
			// bNewConfig.setText("New");
			bNewConfig.setToolTipText("Create new configuration file");
			bNewConfig.setIcon(ImageProvider.getInstance().getIcon("hair:/new.gif"));
			bNewConfig.setMargin(new Insets(20,20,20,20));
			bNewConfig.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bNewConfigActionPerformed(evt);
				}
			});
		}
		return bNewConfig;
	}
	
	private JButton getBEditConfig() {
		if(bEditConfig == null) {
			bEditConfig = new JButton();
			// bEditConfig.setText("Edit");
			bEditConfig.setToolTipText("Edit current configuration");
			bEditConfig.setIcon(ImageProvider.getInstance().getIcon("hair:/file_locked.png"));
			bEditConfig.setMargin(new Insets(20,20,20,20));
			bEditConfig.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bEditConfigActionPerformed(evt);
				}
			});
		}
		return bEditConfig;
	}
	
	private JButton getBDeleteConfig() {
		if(bDeleteConfig == null) {
			bDeleteConfig = new JButton();
			//bDeleteConfig.setText("Delete");
			bDeleteConfig.setToolTipText("Delete current configuration");
			bDeleteConfig.setIcon(ImageProvider.getInstance().getIcon("hair:/delete.gif"));
			bDeleteConfig.setMargin(new Insets(20,20,20,20));
			bDeleteConfig.setPreferredSize(new java.awt.Dimension(45, 23));
			bDeleteConfig.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bDeleteConfigActionPerformed(evt);
				}
			});
		}
		return bDeleteConfig;
	}

	//$hide>>$
	public static int show(Component parent) throws Exception {
		JDialog dialog = new JDialog(parent == null ? null : (Frame)SwingUtilities.getWindowAncestor(parent));
		DfcConfigEditor editor = new DfcConfigEditor(dialog);
		dialog.getContentPane().add( editor.getComponent() );
		ASwing.halfFrame(dialog);
		if  (parent == null )
			ASwing.centerFrame(dialog);
		else
			ASwing.centerDialog(SwingUtilities.getWindowAncestor(parent), dialog);
		dialog.setTitle("Dfc Config Editor");
		// dialog.setIconImage(ImageProvider.getInstance().getIcon("hair:/hair.png").getImage());
		dialog.setModal(true);
		dialog.setVisible(true);
		dialog.dispose();
		return editor.getResult();
	}
	
	private int getResult() {
		return result;
	}

	public static void main(String[] args) {
		try {
			show(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//$hide<<$
	
	private Component getComponent() {
		return mainPanel;
	}
	
	private void bCancelActionPerformed(ActionEvent evt) {
		result = CANCEL;
		if ( parentComponent != null ) parentComponent.setVisible(false);
	}
	
	private JLabel getLConfigname() {
		if(lConfigname == null) {
			lConfigname = new JLabel();
			lConfigname.setText(" Config Name: ");
		}
		return lConfigname;
	}
	
	private JComboBox getCbConfig() {
		if(cbConfig == null) {
			ComboBoxModel jComboBox1Model = 
				new DefaultComboBoxModel();
			cbConfig = new JComboBox();
			cbConfig.setModel(jComboBox1Model);
			cbConfig.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					cbConfigActionPerformed(evt);
				}
			});
		}
		return cbConfig;
	}
	
	private JLabel getLDocbroker() {
		if(lDocbroker == null) {
			lDocbroker = new JLabel();
			lDocbroker.setText("-");
		}
		return lDocbroker;
	}
	
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTConfigValue());
		}
		return jScrollPane1;
	}
	
	private JTextArea getTConfigValue() {
		if(tConfigValue == null) {
			tConfigValue = new JTextArea();
			tConfigValue.setEditable(false);
			tConfigValue.setPreferredSize(new java.awt.Dimension(379, 134));

		}
		return tConfigValue;
	}
	
	private JButton getBCancel() {
		if(bCancel == null) {
			bCancel = new JButton();
			bCancel.setText(" Cancel ");
			bCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bCancelActionPerformed(evt);
				}
			});
		}
		return bCancel;
	}
	
	private JButton getBSelect() {
		if(bSelect == null) {
			bSelect = new JButton();
			bSelect.setText(" Select ");
			bSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bSelectActionPerformed(evt);
				}
			});
		}
		return bSelect;
	}
	
	private void bSelectActionPerformed(ActionEvent evt) {
		
		if ( currentFile == null || !currentFile.exists() ) return;
		if ( currentFile.equals( dfc ) ) {
			result = NOT_CHANGED;
		} else {
			if ( ! AFile.copyFile(currentFile, dfc) )
				result = ERROR;
			result = APPROVED;	
		}
		if ( parentComponent != null ) parentComponent.setVisible(false);
	}
	
	private void cbConfigActionPerformed(ActionEvent evt) {
		String item = (String)cbConfig.getSelectedItem();
		tConfigValue.setText("");
		tConfigValue.setEditable(false);
		bDeleteConfig.setEnabled(true);
//		if ( bSave.isEnabled() ) {
//		}
		bSave.setEnabled(false);
		bEncrypt.setEnabled(false);
		tConfigValue.setBackground(Color.LIGHT_GRAY);
		if ( item == null ) {
			return;
		}
		if ( DEFAULT_ITEM.equals(item) ) {
			currentFile = dfc;
			bDeleteConfig.setEnabled(false);
		} else
			currentFile = new File(config,dfcTemplates.get(item));
		
		if ( currentFile != null && currentFile.exists() ) {
			bEditConfig.setEnabled(false);
			bDeleteConfig.setEnabled(false);
			tConfigValue.setText( AFile.readFile(currentFile) );
			if ( currentFile.canWrite() ) {
				bEditConfig.setEnabled(true);
				bDeleteConfig.setEnabled(true);
			}
			try {
				Properties p = new Properties();
				InputStream is = new FileInputStream(currentFile);
				p.load(is);
				is.close();
				lDocbroker.setText( p.getProperty("dfc.docbroker.host[0]") + ":" + p.getProperty("dfc.docbroker.port[0]"));
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			lDocbroker.setToolTipText( currentFile.getAbsolutePath() );
		}
		
	}
	
	private void bNewConfigActionPerformed(ActionEvent evt) {
		String title = JOptionPane.showInputDialog( mainPanel, "Insert new config title", "Create DFC Config", JOptionPane.INFORMATION_MESSAGE);
		if ( ! AFile.copyFile(currentFile, new File(config, "dfc-" + title+".properties") ) ) {
			JOptionPane.showMessageDialog(mainPanel, "Can't create configuration.", "Create DFC Config", JOptionPane.ERROR_MESSAGE);
		} else {
			init();
			reloadConfigs();
			cbConfig.setSelectedItem(title);
		}
	}
	
	private void bEditConfigActionPerformed(ActionEvent evt) {
		tConfigValue.setEditable(true);
		tConfigValue.setBackground(Color.WHITE);
		bSave.setEnabled(true);
		bEncrypt.setEnabled(true);
	}
	
	private void bDeleteConfigActionPerformed(ActionEvent evt) {
		String item = (String)cbConfig.getSelectedItem();
		if ( item == null ) return;
		if ( JOptionPane.showConfirmDialog(mainPanel, "Really delete config " + item, "Delete Config", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION ) {
			return;
		}
		new File( config, dfcTemplates.get(item)).delete();
		init();
		reloadConfigs();
	}
	
	private JSeparator getJSeparator1() {
		if(jSeparator1 == null) {
			jSeparator1 = new JSeparator();
			jSeparator1.setOrientation(SwingConstants.VERTICAL);
			jSeparator1.setMaximumSize(new java.awt.Dimension(5, 32767));
			jSeparator1.setPreferredSize(new java.awt.Dimension(5, 23));
		}
		return jSeparator1;
	}
	
	private JButton getBSave() {
		if(bSave == null) {
			bSave = new JButton();
			// bSave.setText("Save");
			bSave.setToolTipText("Save changes in current configuration");
			bSave.setIcon(ImageProvider.getInstance().getIcon("hair:/save.gif"));
			bSave.setMargin(new Insets(20,20,20,20));
			bSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bSaveActionPerformed(evt);
				}
			});
		}
		return bSave;
	}
	
	private void bSaveActionPerformed(ActionEvent evt) {
		AFile.writeFile(currentFile, tConfigValue.getText());
		cbConfigActionPerformed(null);
	}
	
	private JButton getBEncrypt() {
		if(bEncrypt == null) {
			bEncrypt = new JButton();
			// bEncrypt.setText("Encrypt");
			bEncrypt.setToolTipText("Encrypt a password and write it to the cursor");
			bEncrypt.setIcon(ImageProvider.getInstance().getIcon("hair:/lock.gif"));
			bEncrypt.setMargin(new Insets(20,20,20,20));
			bEncrypt.setPreferredSize(new java.awt.Dimension(46, 23));
			bEncrypt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					bEncryptActionPerformed(evt);
				}
			});
		}
		return bEncrypt;
	}
	
	private void bEncryptActionPerformed(ActionEvent evt) {
		String real = JOptionPane.showInputDialog(mainPanel,"Insert the realtext password", "Encrypt password");
		if ( real == null || real.length() == 0 ) return;
		
		try {
			String enc = com.documentum.fc.tools.RegistryPasswordUtils.encrypt(real);
			tConfigValue.getDocument().insertString( tConfigValue.getSelectionStart(), enc, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}
