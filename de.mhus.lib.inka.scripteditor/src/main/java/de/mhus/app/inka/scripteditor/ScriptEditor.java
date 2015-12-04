package de.mhus.app.inka.scripteditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ScriptEditor {

	private static JTextArea script;
	private static JTextField engineName;
	private static JTextArea result;
	private static ScriptEngineManager manager = new ScriptEngineManager();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame fr = new JFrame();
		 script = new JTextArea();
		 script.setText( "new Date().toString()" );
		 engineName = new JTextField();
		 engineName.setText( "js" );
		 result = new JTextArea();
		JButton bExecute = new JButton( " Execute " );
		JPanel panel1 = new JPanel();
		panel1.setLayout( new BorderLayout() );
		JPanel panel2 = new JPanel();
		panel2.setLayout( new GridLayout( 3, 1 ) );
		panel2.add( bExecute );
		panel2.add( engineName );
		panel1.add( panel2, BorderLayout.NORTH );
		JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		split.setDividerLocation(300);
		split.setTopComponent( new JScrollPane( script ));
		split.setBottomComponent( new JScrollPane( result ) );
		panel1.add( split, BorderLayout.CENTER );
		
		fr.getContentPane().add( panel1 );
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize( 800, 800 );
		fr.show();

		bExecute.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					 ScriptEngine engine = manager.getEngineByName (engineName.getText());
					 Object ret = engine.eval(script.getText());
					 if (ret == null)
						 result.setText("[null]");
					 else
						 result.setText(ret.toString());
				} catch ( Throwable ex ) {
					result.setText( "ERROR: " + ex.getMessage() );
				}
			}
			
		});
	}

}
