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

package de.mhu.hair.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Timer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDbor;
import com.documentum.fc.client.IDfDborEntry;
import com.documentum.fc.client.IDfEnumeration;

import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.plugin.PluginLoader;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.ALUtilities;

public class DctmGui implements MainIfc {

	/**
	 * @param args
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws Exception {

		if ( ! ArgsParser.initialize(args) )
			System.out.println( "*** ArgsParser already initialized");

		System.setProperty("apple.laf.useScreenMenuBar", "true");

		PluginLoader.initialize(ArgsParser.getInstance(), null);

		
		new DctmGui().startMain();
	}

	public void startMain() {

		ArgsParser ap = ArgsParser.getInstance();
		AL.setLogToStdOut(false);
		ConfigManager.initialize();
		ALUtilities.configure();
		// TEST
		IDfClient m_client = null;
		try {
			m_client = DfClient.getLocalClient();
		} catch ( Error t ) {
			if ( t.toString().indexOf( "dfc.data.dir") > 0 ) {
				File f = new File( "dfc/dfc.properties" );
				if ( f.exists() && f.isFile() ) {
					JFileChooser fc = new JFileChooser();
					JOptionPane.showMessageDialog(null, "Your dfc.properties is not linked to the correct \nDocumentum shared directory, please choose the directory now...");
					fc.setDialogTitle("Select Documentum Shared Directory");
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if ( System.getenv("DOCUMENTUM_SHARED") != null )
						fc.setSelectedFile(new File(System.getenv("DOCUMENTUM_SHARED")));
					boolean approved = true;
					do {
						approved = true;
						if ( fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION )
							throw t;
						File x = fc.getSelectedFile();
						if ( x == null || !x.isDirectory() || !x.exists() || ! ( new File(x,"dfc.jar").exists() && new File(x,"dfc/dfc.jar").exists() ) ) {
							if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,"The selected file seams not to be the documentum shared directory\nYou wan't to use this directory?", "Warning", JOptionPane.YES_NO_OPTION ) )
								approved = false;
						}
					} while (!approved);
					try {
						Properties p = new Properties();
						InputStream is = new FileInputStream( f );
						p.load(is);
						is.close();
						p.setProperty("dfc.data.dir", fc.getSelectedFile().getAbsolutePath() );
						OutputStream os = new FileOutputStream( f );
						p.store(os, "Modified by hair");
						os.close();
					} catch ( Exception e ) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error: " + e.toString() );	
						throw t;
					}
					// JOptionPane.showMessageDialog(null, "Please restart hair");
					System.exit(100);
				} else {
					JOptionPane.showMessageDialog(null, "Check your dfc.properties and set the dfc.data.dir to the documentum directory");
					throw t;
				}
			} else {
				t.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error: " + t.toString() );	
				throw t;
			}
			
		} catch ( Exception e ) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e.toString() );	
		}
		
		try {
			IDfDbor dbor = m_client.getDbor();

			for (IDfEnumeration enu = dbor.getAll(); enu.hasMoreElements();) {
				IDfDborEntry entry = (IDfDborEntry) enu.nextElement();
				System.out.println("DBOR ENTRY: " + entry.getName() + " ("
						+ entry.getVersion() + "): " + entry.getJavaClass());
			}

			// unregister TBO
			/*
			 * try { dbor.unregister( "accelera_content" ); } catch (
			 * DfServiceException se ) { se.printStackTrace(); }
			 */
			/*
			 * // if not already registered... IDfDborEntry entry = new
			 * DfDborEntry(); entry.setName( "accelera_content" );
			 * entry.setServiceBased( false ); // true for SBO, false for TBO
			 * String strJavaClass = AcceleraContentTbo.class.getName();
			 * entry.setJavaClass( strJavaClass ); entry.setVersion(
			 * AcceleraContentTbo.version ); // i.e. "1.0"
			 * 
			 * dbor.register( entry );
			 */

			// END TEST
			String pluginPath = "plugins";
			if (ap.isSet("hair_plugins"))
				pluginPath = ap.getValues("hair_plugins")[0];
			// if ( ! new File( "plugins" ).exists() && new File( "../plugins"
			// ).exists() )
			// pluginPath = "../plugins";

			PluginNode root = new PluginNode(pluginPath + "/hair.dctm_gui");
			// PluginNode root = new PluginNode( "tree" );
			root.addApi(ApiSystem.class, new ApiSystem() {

				private Timer timer = new Timer();

				public void exit(int ret) {
					System.exit(ret);
				}

				public Timer getTimer() {
					return timer;
				}

			});
			root.start(new Hashtable(), true);
		} catch (Error e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e.toString() );			
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error: " + e.toString() );						
		}

	}

}
