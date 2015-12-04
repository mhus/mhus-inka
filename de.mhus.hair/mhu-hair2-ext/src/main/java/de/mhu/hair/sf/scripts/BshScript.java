package de.mhu.hair.sf.scripts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import bsh.Interpreter;

import com.documentum.fc.client.IDfPersistentObject;

import de.mhu.hair.dctm.DMConnection;
import de.mhu.hair.gui.ALogger;
import de.mhu.hair.plugin.PluginNode;
import de.mhu.hair.sf.ScriptIfc;

public class BshScript implements ScriptIfc {

	private PipedOutputStream outPipe;
	private PipedInputStream inPipe;
	private InputStreamReader in;
	private Interpreter interpreter;
	private Thread thread;
	private String initial;
	private String targets;
	private String destroy;

	public void destroy(PluginNode pNode, DMConnection pCon, ALogger pLogger) {

		try {
			interpreter.source( destroy );
		} catch ( Exception ioe ) {
			System.out.println("+++ Not found: " + destroy + " " + ioe);
		}

		thread.stop();
	}

	public void execute(PluginNode pNode, DMConnection pCon,
			IDfPersistentObject[] pTargets, ALogger pLogger) throws Exception {
		
		interpreter.set("targets", pTargets);
		try {
			interpreter.source( targets );
		} catch ( IOException ioe ) {
			System.out.println("+++ Not found: " + targets + " " + ioe);
		}
		
	}

	public void initialize(PluginNode pNode, DMConnection pCon, ALogger pLogger)
			throws Exception {

		outPipe	= new PipedOutputStream();
		inPipe  = new PipedInputStream(outPipe);
		in = new InputStreamReader(inPipe);
		interpreter = new Interpreter(in,pLogger.out,pLogger.out,true);
		
		if ( pCon != null ) {
			interpreter.set("con", pCon);
			interpreter.set("session", pCon.getSession());
			interpreter.set("clientx", pCon.clientx);
		}
		
		thread = new Thread(interpreter);
		thread.start();
		
		try {
			interpreter.source( initial );
		} catch ( IOException ioe ) {
			System.out.println("+++ Not found: " + initial + " " + ioe);
		}
	}

	public void setInitial(String in) {
		initial=in;
	}
	public void setTargets(String in) {
		targets=in;
	}
	public void setDestroy(String in) {
		destroy=in;
	}
}
