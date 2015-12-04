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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfPreferences;

import de.mhu.hair.Build;
import de.mhu.hair.api.ApiPersistent;
import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.api.ApiPersistent.PersistentManager;
import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.plugin.Plugin;
import de.mhu.hair.plugin.PluginConfig;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.plugin.dctm.ApiDocbases;
import de.mhu.hair.tools.ObjectTool;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.Rot13;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.AToolBarButton;
import de.mhu.lib.swing.layout.TopGridLayout;
import de.mhu.res.img.LUF;

public class ConnectPlugin implements Plugin {

	private static final String DEFAULT_CONFIG = "dock";
	private static final String ARG_KEY_HAIR_UI = "hair_ui";
	
	private PersistentManager config;
	private JComboBox cbDocbases;
	private JTextField tUser;
	private JPasswordField tPass;
	// private JComboBox cbConfigs;
	private JFrame frame;

	private Timer timer;
	private JButton bConnect;
	private PluginNode node;
	//private JButton bUserList;
	private boolean savePasswords;
	// private JComboBox cbBofRepository;
	// private JTextField tBofUser;
	// private JPasswordField tBofPass;
	private JLabel lDocbroker;
	private String uiConfig;
	private JButton bCancel;
	private AToolBarButton bEditConfig;
	//private JButton bRefresh;

	/**
	 * @param args
	 * @throws DfException
	 */
	public void initPlugin(PluginNode pNode, PluginConfig pConfig)
			throws Exception {

		node = pNode;
		timer = ((ApiSystem) node.getSingleApi(ApiSystem.class)).getTimer();

		cbDocbases = new JComboBox();
		//cbBofRepository = new JComboBox();
		tUser = new JTextField();
		//tBofUser = new JTextField();
		tPass = new JPasswordField();
		//tBofPass = new JPasswordField();

		bConnect = new JButton(" Connect ");
		//bRefresh = new JButton(" Refresh ");
		bCancel = new JButton( " Cancel " ); 
		bEditConfig = new AToolBarButton(LUF.EDITABLE_ICON, "Edit" );
		config = ((ApiPersistent) node.getSingleApi(ApiPersistent.class))
				.getManager("hair_connect");
		savePasswords = "1".equals(config.getProperty("unsecure", "0"));

		uiConfig = DEFAULT_CONFIG;
		if ( ArgsParser.getInstance() != null && ArgsParser.getInstance().isSet( ARG_KEY_HAIR_UI ) )
			uiConfig = ArgsParser.getInstance().getValue(ARG_KEY_HAIR_UI, 0);
		
		JPanel panel = new JPanel();
		panel.setLayout(new TopGridLayout( 5, 2 ));
		
		panel.add(new JLabel(" Docbroker: "));
		lDocbroker = new JLabel();
		panel.add(lDocbroker);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(cbDocbases,BorderLayout.CENTER);
		p2.add(bEditConfig,BorderLayout.EAST);
		
		panel.add( new JLabel( " Docbase: ") );
		panel.add(p2);

		panel.add(new JLabel(" User: "));
		panel.add(tUser);

		panel.add(new JLabel(" Password: "));
		panel.add(tPass);

		panel.add(bCancel);
		panel.add(bConnect);

		frame = new JFrame();
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setSize(600, frame.getHeight());
		frame.setResizable(false);
		frame.setTitle(Build.getInstance().getName() + " " + Build.getInstance().getVersion() );
		ASwing.centerFrame(frame);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(0);
			}
		});

		bConnect.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				swingConnectAction();
			}

		});

		bCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(0);
			}

		});
		
		bEditConfig.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				IDfClientX clientx = new DfClientX(); 
				String v = clientx.getDFCVersion();
				try {
					if ( v.startsWith( "5.") ) {
						if ( DmclConfigEditor.show(frame) == DmclConfigEditor.APPROVED ) {
							// JOptionPane.showMessageDialog(frame,"Please restart " + Build.getInstance().getName(), "Exit "+ Build.getInstance().getName(), JOptionPane.INFORMATION_MESSAGE );
							((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(100);
						}					
					} else {
						if ( DfcConfigEditor.show(frame) == DfcConfigEditor.APPROVED ) {
							// JOptionPane.showMessageDialog(frame,"Please restart " + Build.getInstance().getName(), "Exit "+ Build.getInstance().getName(), JOptionPane.INFORMATION_MESSAGE );
							((ApiSystem) node.getSingleApi(ApiSystem.class)).exit(100);
						}
					}
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
			}

		});
		
//		bRefresh.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				try {
//					loadValues();
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//
//		});
		
		tPass.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					swingConnectAction();
			}
		});

		cbDocbases.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionLoadConfig();
			}

		});

		if (!pConfig.getNode().getAttribute("icon").equals("")) {
			ImageIcon icon = ImageProvider.getInstance().getIcon(
					pConfig.getNode().getAttribute("icon"));
			if (icon != null)
				frame.setIconImage(icon.getImage());
		}
		frame.show();

		loadValues();
		actionLoadConfig();
		
		if (tUser.getText().equals(""))
			cbDocbases.grabFocus();
		else {
			tPass.setSelectionStart(0);
			tPass.setSelectionEnd(tPass.getText().length());
			tPass.grabFocus();
		}

		if ( ArgsParser.getInstance().isSet( "login_auto" ) && lDocbroker.getText().equals( ArgsParser.getInstance().getValue("login_auto", 0) ) ) {
			connectAction();
		}
		
	}
	
	protected void loadValues() throws Exception {
		
		IDfDocbaseMap myMap = ((ApiDocbases) node
				.getSingleApi(ApiDocbases.class)).getMap();
		
		cbDocbases.removeAllItems();
		//cbBofRepository.removeAllItems();
		
		for (int i = 0; i < myMap.getDocbaseCount(); i++) {
			System.out.println("Docbase " + (i + 1) + ": "
					+ myMap.getDocbaseId(i) + " " + myMap.getDocbaseName(i)
					+ " (" + myMap.getDocbaseDescription(i) + ")");
			cbDocbases.addItem(myMap.getDocbaseName(i));
			//cbBofRepository.addItem(myMap.getDocbaseName(i));
		}
		
		cbDocbases.setSelectedItem(config.getProperty("con.docbase", ""));
		
		String docbase = ((ApiDocbases) node.getSingleApi(ApiDocbases.class))
		.getDocbase();
		lDocbroker.setText(docbase);
	}

	protected void actionLoadConfig() {
		String docbroker = lDocbroker.getText();
		String docbase   = (String) cbDocbases.getSelectedItem();
		tUser
				.setText(config.getProperty("con.user." + docbroker
						+ "." + docbase, tUser
						.getText()));
		if (savePasswords)
			tPass.setText(Rot13.decode(config.getProperty("con.pass."
					+ docbroker + "."
					+ docbase, tUser.getText())));

	}

	protected void swingConnectAction() {
		timer.schedule(new TimerTask() {

			public void run() {
				connectAction();
			}

		}, 200);
	}

	protected void connectAction() {
		try {
			cbDocbases.setEnabled(false);
			tUser.setEnabled(false);
			tPass.setEnabled(false);
			bConnect.setEnabled(false);
			bEditConfig.setEnabled(false);

			String docbroker = lDocbroker.getText();
			String docbase   = (String) cbDocbases.getSelectedItem();
			
			DMConnection con = new DMConnection(tUser.getText(), tPass
					.getText(), docbase );

			node.addApi(DMConnection.class, con);
			PluginNode child = node.createChild(node.getConfigName() + "/"
					//+ (String) cbConfigs.getSelectedItem());
					+ uiConfig );
			Hashtable params = new Hashtable();
			params.put("docbase", con.getDocbaseName());
			params.put("docbase_user", con.getUserName());

			child.start(params, true);

			config.setProperty("con.docbase", docbase);
			config.setProperty("con.user." + docbroker + "." + docbase, tUser.getText());
			if (savePasswords)
				config.setProperty("con.pass." + docbroker + "." + docbase, Rot13.encode(tPass.getText()));
			
			config.save();
			frame.dispose();

		} catch (Exception e1) {

			e1.printStackTrace();
			cbDocbases.setEnabled(true);
			tUser.setEnabled(true);
			tPass.setEnabled(true);
			bConnect.setEnabled(true);
			bEditConfig.setEnabled(true);
//			cbBofRepository.setEnabled(true);
//			tBofUser.setEnabled(true);
//			tBofPass.setEnabled(true);

		}
	}

	public void destroyPlugin() throws Exception {
		
	}

}
