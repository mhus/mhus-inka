package de.mhu.hair.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class JavaInfoDialog extends JDialog {

	public JavaInfoDialog() {
		
		JTextArea text = new JTextArea();
		text.setEditable(false);
		JScrollPane scroller=new JScrollPane(text);
		getContentPane().add(scroller,BorderLayout.CENTER);
		JButton bClose = new JButton(" Close ");
		getContentPane().add(bClose,BorderLayout.SOUTH);
		bClose.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			
		});
		
		StringBuffer info = new StringBuffer();
		
		for ( Map.Entry<Object, Object> entry : System.getProperties().entrySet() )
			info.append("Property: ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
		
		for ( Map.Entry<String, String> entry : System.getenv().entrySet() )
			info.append("Env: ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
		
		info.append("Memory: ").append(Runtime.getRuntime().maxMemory() + "/" + Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().totalMemory() + "\n");
		info.append("Time: " + System.currentTimeMillis() + "\n");
		
		text.setText(info.toString());
		
		setSize(500,500);
		setLocation(10,10);
		setTitle("System Info");
		
		setVisible(true);
		
		
	}
}
