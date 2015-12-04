package de.mhus.app.inka.xpatheditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mhus.lib.MXml;

public class XPathEditor {

	private static JTextArea from;
	private static JTextField rule;
//	private static JTextField repl;
	private static JTextArea result;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame fr = new JFrame();
		 from = new JTextArea();
		 from.setText( "<?xml version='1.0' encoding='iso-8859-1'?>" );
		 rule = new JTextField();
		 rule.setText( "//" );
//		 repl = new JTextField();
//		 repl.setText( "Replace" );
		 result = new JTextArea();
		JButton bExecute = new JButton( " Execute " );
		JPanel panel1 = new JPanel();
		panel1.setLayout( new BorderLayout() );
		JPanel panel2 = new JPanel();
		panel2.setLayout( new GridLayout( 2, 1 ) );
		panel2.add( bExecute );
		panel2.add( rule );
//		panel2.add( repl );
		panel1.add( panel2, BorderLayout.NORTH );
		JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		split.setDividerLocation(300);
		split.setTopComponent( new JScrollPane( from ));
		split.setBottomComponent( new JScrollPane( result ) );
		panel1.add( split, BorderLayout.CENTER );

		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.getContentPane().add( panel1 );
		fr.setSize( 800, 800 );
		fr.show();

		bExecute.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					
					XPath xpath = XPathFactory.newInstance().newXPath();
					XPathExpression expr = xpath.compile(rule.getText());

					String text = from.getText();
					// <?xml version='1.0' encoding='iso-8859-1'?>
					Document dom = MXml.loadXml(text);
					NodeList xmlres = (NodeList)expr.evaluate( dom.getDocumentElement(), XPathConstants.NODESET);
					
					StringBuffer out = new StringBuffer();
					
					for (int i = 0; i < xmlres.getLength(); i++) {
						Node res = xmlres.item(i);
						StringWriter sw = new StringWriter();
						MXml.saveXml(res, sw, true);
						out.append(sw.toString());
						out.append("\n-----------------------------------\n");
					}
					
					result.setText( out.toString() );
					
				} catch ( Exception ex ) {
					result.setText( "ERROR: " + ex.getMessage() );
					ex.printStackTrace();
				}
			}
			
		});
	}

}
