package de.mhu.com.morse.main;

import org.tanukisoftware.wrapper.WrapperActionServer;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.AThread;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.jmx.AJmxManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.ALUtilities;

public class MorseService implements WrapperListener {

	public static AL log = new AL( MorseService.class );
	protected boolean running;
	private boolean closed;
	
	public void controlEvent(int arg0) {
	}

	public Integer start(String[] arg0) {
		System.out.println( "Starting..." );
		
		ArgsParser.initialize( arg0 );
		ConfigManager.initialize();
		ALUtilities.configure();
		AJmxManager.start();
		
		try {
			MainServer.startServer();
		} catch (Exception e) {
			log.error( e );
			return -1;
		}
		
		/*
		try
        {
            int port = 9999;
            WrapperActionServer server = new WrapperActionServer( port );
            server.enableShutdownAction( true );
            server.enableHaltExpectedAction( true );
            server.enableRestartAction( true );
            server.enableThreadDumpAction( true );
            server.enableHaltUnexpectedAction( true );
            server.enableAccessViolationAction( true );
            server.enableAppearHungAction( true );
            server.start();
            
            System.out.println( "ActionServer Enabled. " );
            System.out.println( "  Telnet localhost 9999" );
            System.out.println( "  Commands: " );
            System.out.println( "    S: Shutdown" );
            System.out.println( "    H: Expected Halt" );
            System.out.println( "    R: Restart" );
            System.out.println( "    D: Thread Dump" );
            System.out.println( "    U: Unexpected Halt (Simulate crash)" );
            System.out.println( "    V: Access Violation (Actual crash)" );
            System.out.println( "    G: Make the JVM appear to be hung." );
        }
        catch ( java.io.IOException e )
        {
            System.out.println( "Unable to open the action server socket: " + e.getMessage() );
        }
        */
		
		running = true;
		closed  = false;
		
		new AThread( new Runnable() {

			public void run() {

				while ( running )
					AThread.sleep( 1000 );
				
				try {
					new Query( MainServer.getCore().getDbProvider().getDefaultConnection(), 
							"exec close @init" ).execute().close();
				} catch (MorseException e) {
					log.error( e );
				}
			}
			
		}, "Dummy" ).start();
		
		return null;
	}

	public int stop(int arg0) {
		System.out.println( "Stopping..." );
		running = false;
		while ( closed )
			AThread.sleep( 1000 );
		return 0;
	}

	public static void main( String[] args )
    {
        System.out.println( "Initializing..." );
        
        WrapperManager.start( new MorseService(), args );
        
    }
}
