package de.mhu.lib.apps.regexeditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RegEditor {

	private static JTextArea from;
	private static JTextField rule;
	private static JTextField repl;
	private static JTextArea result;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame fr = new JFrame();
		 from = new JTextArea();
		 from.setText( "Source text" );
		 rule = new JTextField();
		 rule.setText( ".*" );
		 repl = new JTextField();
		 repl.setText( "Replace" );
		 result = new JTextArea();
		JButton bExecute = new JButton( " Execute " );
		JPanel panel1 = new JPanel();
		panel1.setLayout( new BorderLayout() );
		JPanel panel2 = new JPanel();
		panel2.setLayout( new GridLayout( 3, 1 ) );
		panel2.add( bExecute );
		panel2.add( rule );
		panel2.add( repl );
		panel1.add( panel2, BorderLayout.NORTH );
		JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		split.setDividerLocation(300);
		split.setTopComponent( new JScrollPane( from ));
		split.setBottomComponent( new JScrollPane( result ) );
		panel1.add( split, BorderLayout.CENTER );
		
		fr.getContentPane().add( panel1 );
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setSize( 800, 800 );
		fr.show();

		bExecute.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					result.setText( 
							from.getText().replaceAll( rule.getText(), repl.getText() )
							);
				} catch ( Exception ex ) {
					result.setText( "ERROR: " + ex.getMessage() );
				}
			}
			
		});
	}

}
