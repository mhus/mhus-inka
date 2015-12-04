package de.mhu.hair.plugin.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

import de.mhu.hair.Build;
import de.mhu.lib.resources.ImageProvider;
import de.mhu.lib.swing.ASwing;

public class HelpMenu extends JMenu {

	public HelpMenu() {
		setText("Help");
		
		JMenuItem item = new JMenuItem("About");
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JTextArea ta = new JTextArea();
				ta.setText( Build.getInstance().getName() + 
						"\n\nVersion: " +Build.getInstance().getVersion() +
						"\n(c) Copyright Mike Hummel 2009. All rights reserved." +
						"\nVisit http://www.mhus.de");
				ta.setEditable(false);
				ta.setFont( Font.decode("Courier-BOLD-20") );
				frame.getContentPane().add(ta);
				frame.pack();
				frame.setTitle("About " + Build.getInstance().getName() );
				frame.setIconImage( ImageProvider.getInstance().getIcon("hair:/hair.gif").getImage());
				ASwing.centerFrame(frame);
				frame.setVisible(true);
			}
			
		});
		add(item);
		
	}
	
}
