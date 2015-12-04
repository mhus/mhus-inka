package de.mhu.hair.sf.scripts;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;

import javax.swing.JPanel;
import javax.swing.JTextField;

import bsh.ConsoleInterface;

import de.mhu.hair.gui.ALogger;
import de.mhu.hair.gui.LoggerPanel;
import de.mhu.lib.swing.edi.EdiArea;
import de.mhu.lib.swing.edi.textarea.DefaultInputHandler;

public class BshInputArea extends EdiArea implements ConsoleInterface {

	private PipedOutputStream outPipe;
	private PipedInputStream inPipe;
	private InputStreamReader in;
	private OutputStreamWriter outWriter;

	private OutputStream os = new OutputStream() {

		public void write(int b) throws IOException {
			try {
				getDocument().insertString(getDocument().getLength(),
							new String(new char[] { (char) b }), null);
			} catch ( Exception e ) {
				new IOException(e.toString());
			}
		}

	};
	private de.mhu.hair.sf.scripts.BshInputArea.OutPrintStream out;
	private de.mhu.hair.sf.scripts.BshInputArea.OutPrintStream err;

	public BshInputArea() throws IOException {
		super();
		outPipe	= new PipedOutputStream();
		inPipe  = new PipedInputStream(outPipe);
		in = new InputStreamReader(inPipe);
		outWriter = new OutputStreamWriter(outPipe);
		
		inputHandler = new MyInputHandler();
		inputHandler.addDefaultKeyBindings();
		inputHandler.addKeyBinding("ENTER", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExecute();
			}
		});
		inputHandler.addKeyBinding("UP", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		inputHandler.addKeyBinding("DOWN", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		out = new OutPrintStream(os);
		err = new OutPrintStream(os);
		
	}

	protected void actionExecute() {
		try {
			
			String line = getLineText( getCaretLine() );
			if ( line.startsWith("bsh %") ) line = line.substring(5);
			line = line.trim();
			if ( line.length() == 0 ) return;
			
			getDocument().insertString(getDocument().getLength(),"\n", null);
			
			outWriter.write( line );
			outWriter.write('\n');
			outWriter.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void error(Object o) {
		err.println(o);
	}

	public PrintStream getErr() {
		return err;
	}

	public Reader getIn() {
		return in;
	}

	public PrintStream getOut() {
		return out;
	}

	public void print(Object o) {
		out.print(o);
	}

	public void println(Object o) {
		out.println(o);		
	}
	
	private class OutPrintStream extends PrintStream {

		public OutPrintStream(OutputStream os) {
			super(os, true);
		}

	}

	class MyInputHandler extends DefaultInputHandler {
		
	}
}
