package de.mhu.hair.main;

import java.util.Hashtable;
import java.util.Timer;

import javax.swing.JOptionPane;

import de.mhu.hair.api.ApiSystem;
import de.mhu.hair.plugin.PluginLoader;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.lib.ArgsParser;

public class OfflineGui implements MainIfc {

	public static void main(String[] args) throws Exception {
		ArgsParser.initialize(args);
		PluginLoader.initialize(ArgsParser.getInstance(), null);
		new OfflineGui().startMain();
	}

	public void startMain() {

		ArgsParser ap = ArgsParser.getInstance();

		try {
			String pluginPath = "plugins";
			if (ap.isSet("hair_plugins"))
				pluginPath = ap.getValues("hair_plugins")[0];
			// if ( ! new File( "plugins" ).exists() && new File( "../plugins"
			// ).exists() )
			// pluginPath = "../plugins";

			PluginNode root = new PluginNode(pluginPath + "/hair.offline_gui");
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
