package de.mhu.com.morse.main;

import java.io.File;

import javax.swing.JFrame;

import de.mhu.lib.AThread;
import de.mhu.lib.ATimekeeper;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.config.FileConfigLoader;
import de.mhu.lib.jmx.AJmxManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.ALUtilities;
import de.mhu.lib.log.FileAppender;
import de.mhu.lib.log.SwingAppender;
import de.mhu.lib.log.TcpServerAppender;
import de.mhu.com.morse.server.CmdsModule;
import de.mhu.com.morse.server.CoreModule;
import de.mhu.com.morse.server.ServerModul;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;
import de.mhu.lib.plugin.AfPluginRoot;
import de.mhu.lib.plugin.utils.ALLogger;
import de.mhu.lib.plugin.utils.XmlConfig;

public class MainServer {

	private static AL log = new AL( MainServer.class );
	private static CoreModule core;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArgsParser.initialize( args );
		ConfigManager.initialize();
		ALUtilities.configure();
		AJmxManager.start();
		
		try {
						
			startServer();
			
			// root.disbale();
			// root.destroy();
			
			while ( true ) {
				AThread.sleep( 10000 );
			}
			
		} catch (Exception e) {
			log.error( e );
		}
	}

	public static void startServer() throws Exception {
		
		ATimekeeper tk = new ATimekeeper();
		tk.start();
		
		AfPluginRoot root = new AfPluginRoot();
		root.enable();
		
		ALLogger logger = new ALLogger();
		XmlConfig config = new XmlConfig( "config.xml" );
		root.addPlugin( logger, "log" );
		root.refreshTools();
		root.addPlugin( config, "config" );
		root.refreshTools();
		
		root.enablePlugin( "config" );
		root.enablePlugin( "log" );
		
		AfPluginNode nodeNio = new AfPluginNode();
		
		String name = root.addPlugin( nodeNio, "nio" );
		root.enablePlugin( name );

		AfPluginNode nodeCore = new AfPluginNode();
		
		name = root.addPlugin( nodeCore, "core" );
		root.enablePlugin( name );

		core = new CoreModule();
		name = nodeCore.addPlugin( core, "core" );
		nodeCore.enablePlugin( name );
		
		// -----------------------------------------------------
		
		AfPluginNode nodeIntIo = new AfPluginNode();
		root.addPlugin( nodeIntIo, "intio" );
		root.enablePlugin( "intio" );
		
		CmdsModule intCmds = new CmdsModule();
		nodeIntIo.addPlugin( intCmds, "intcmds" );
		nodeIntIo.enablePlugin("intcmds");
		
		// -----------------------------------------------------
		
		ServerModul server = new ServerModul( intCmds.getDelegator() );
		name = nodeNio.addPlugin( server, "server" );
		nodeNio.enablePlugin( name );
		
		tk.stop();
		log.info( "Startup: " + tk.getCurrentTimeAsString( true ) );
		
	}

	public static CoreModule getCore() {
		return core;
	}
	
}
