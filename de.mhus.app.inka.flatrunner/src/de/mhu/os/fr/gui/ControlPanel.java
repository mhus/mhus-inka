package de.mhu.os.fr.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.mhu.os.fr.model.Surface;

public class ControlPanel extends JPanel {

	private JTextArea info;
	private JLabel tSize;
	private JLabel tCreated;
	private JLabel tRemoved;
	private JLabel tHealty;
	private JLabel tHealtyMin;
	private JLabel tHealtyMax;
	private JLabel tRound;
	private JLabel tAge;
	private Surface surface;
	private JButton bNextRound;

	public ControlPanel(Surface s) {
		
		surface = s;
		
		bNextRound = new JButton( "End Round" );
		bNextRound.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				surface.finishRound();
			}
			
		});
		
		tRound = new JLabel();
		tAge = new JLabel();
		tSize = new JLabel();
		tCreated = new JLabel();
		tRemoved = new JLabel();
		tHealty = new JLabel();
		tHealtyMin = new JLabel();
		tHealtyMax = new JLabel();
		
		JPanel p = new JPanel();
		p.setLayout( new GridLayout( 6, 3 ) );
		
		p.add( new JLabel( "Round" ) );
		p.add( new JLabel( "Age") );
		p.add( new JLabel( " " ) );
		p.add( tRound );
		p.add( tAge );
		p.add( new JLabel( " " ) );
		p.add( new JLabel( "Size" ) );
		p.add( new JLabel( "Add") );
		p.add( new JLabel( "Rem" ) );
		p.add( tSize );
		p.add( tCreated );
		p.add( tRemoved );
		p.add( new JLabel( "Min" ) );
		p.add( new JLabel( "Sum") );
		p.add( new JLabel( "Max" ) );
		p.add( tHealtyMin );
		p.add( tHealty );
		p.add( tHealtyMax );
		
		info = new JTextArea();
		info.setEditable( false );
		JScrollPane scroller = new JScrollPane( info );
		setLayout( new BorderLayout() );
		
		add( scroller, BorderLayout.CENTER );
		add( p, BorderLayout.NORTH );
		add( bNextRound, BorderLayout.SOUTH );
		
		Dimension d = new Dimension( 200, 300 );
		setMinimumSize( d );
		setPreferredSize( d );
		
		s.eventHandler().register( new Surface.Listener() {

			public void eventChanged(int x, int y) {
			
			}

			public void eventChangedAll() {
			
			}

			public void eventRoundFinished(int round, long age, int childSize, int addedChildCnt,
					int removedChildCnt, long healty, int healtyMin,
					int healtyMax) {
				tRound.setText( String.valueOf( round ) );
				tAge.setText( String.valueOf( age ) );
				tSize.setText( String.valueOf( childSize ) );
				tCreated.setText( String.valueOf( addedChildCnt ) );
				tRemoved.setText( String.valueOf( removedChildCnt ) );
				tHealty.setText( String.valueOf( healty ) );
				tHealtyMin.setText( String.valueOf( healtyMin ) );
				tHealtyMax.setText( String.valueOf( healtyMax ) );
			}
			
		});
	}

	public void setInfo(String in) {
		info.setText( in );
	}
	
}
