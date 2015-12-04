package de.mhu.os.fr.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.mhu.os.fr.model.Creature;
import de.mhu.os.fr.model.Surface;

public class SurfaceFrame extends JFrame {

	private SurfacePanel panel;
	protected int mouseDragY=-1;
	protected int mouseDragX=-1;
	private JScrollPane scroller;
	private JSlider slider;
	private Surface surface;
	private ControlPanel control;
	private JSplitPane split;
	protected int oldSpeed = 300;

	public SurfaceFrame( Surface s ) {
		surface = s;
		panel = new SurfacePanel( s, 5 ); 
		
		scroller = new JScrollPane( panel );
		split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		
		slider = new JSlider( 1, 300 );
		slider.setMinorTickSpacing( 10 );
		slider.setValue( 10 );
		surface.setSleepTime( 10 );
		slider.addChangeListener( new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				actionSetSpeed();
			}
			
		});
		JPanel p = new JPanel();
		p.setLayout( new BorderLayout() );
		split.setRightComponent( scroller );
		p.add( split, BorderLayout.CENTER );
		p.add( slider, BorderLayout.SOUTH );
		getContentPane().add( p );
		
		panel.addMouseMotionListener( new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				//System.out.println( "DRAGGED: " + e.getX() + "-" + e.getY() );
				if ( mouseDragX >= 0 ) {
					int diffX = mouseDragX - e.getX();
					int diffY = mouseDragY - e.getY();
					if ( diffX != 0 )
						scroller.getHorizontalScrollBar().setValue( scroller.getHorizontalScrollBar().getValue() + diffX );
					if ( diffY != 0 )
						scroller.getVerticalScrollBar().setValue( scroller.getVerticalScrollBar().getValue() + diffY );
				}
				mouseDragX = e.getX();
				mouseDragY = e.getY();
			}

			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		panel.addMouseListener( new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				if ( e.getButton() == MouseEvent.BUTTON1 ) {
					int x = e.getX() / panel.getCellWidth();
					int y = e.getY() / panel.getCellWidth();
					Creature[] creatures = surface.getCreatures( x, y );
					StringBuffer out = new StringBuffer();
					for ( Creature c : creatures )
						out.append( "----\n" ).append( c.getDump() ).append( "\n" );
					control.setInfo( out.toString() );
				} else
				if ( e.getButton() == MouseEvent.BUTTON2 ) {
					int speed = slider.getValue();
					slider.setValue( oldSpeed );
					actionSetSpeed();
					oldSpeed  = speed;
				} else
				if ( e.getButton() == MouseEvent.BUTTON3 ) {
					final int xa = e.getX() / panel.getCellWidth();
					final int ya = e.getY() / panel.getCellWidth();
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = new JMenuItem( "Mass kill");
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							int r = surface.getWidth() / 5;
							for ( int x = -r; x < r; x++ ) {
								for ( int y = -r; y < r; y++ ) {
									Creature[] creatures = surface.getCreatures( x+xa, y+ya );
									for ( Creature c : creatures )
										surface.removeCreature( c );
								}
							}
						}
						
					});
					menu.add( item );
					
					item = new JMenuItem( "Clean Surface 0");
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							int r = surface.getWidth() / 5;
							for ( int x = -r; x < r; x++ ) {
								for ( int y = -r; y < r; y++ ) {
									surface.setValue( x+xa, y+ya, (byte)0 );
								}
							}
						}
						
					});
					menu.add( item );
					
					item = new JMenuItem( "Kill Others");
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							for ( int x = 0; x < surface.getWidth(); x++ ) {
								for ( int y = 0; y < surface.getHeight(); y++ ) {
									if ( x != xa || y != ya ) {
										Creature[] creatures = surface.getCreatures( x, y );
										for ( Creature c : creatures )
											surface.removeCreature( c );
									}
								}
							}
						}
						
					});
					menu.add( item );
					
					menu.show( e.getComponent(), e.getX(), e.getY() );
				}
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
				//System.out.println( "PRESSED");
				mouseDragX=-1;
				mouseDragY=-1;
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		panel.addMouseWheelListener( new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if ( e.isControlDown() ) {
					e.consume();
					int amount = e.getWheelRotation() + panel.getCellWidth();
					System.out.println( "New AMount: " + amount );
					amount = Math.max( Math.min( amount, 30 ), 3 );
					panel.setCellWidth( amount );
					scroller.revalidate();
				}
			}
			
		});
		
		control = new ControlPanel( s );
		split.setLeftComponent( control );
		
	}

	protected void actionSetSpeed() {
		int val = slider.getValue();
		if ( val == 300 )
			surface.setSleepTime( -1 );
		else
			surface.setSleepTime( val );
		oldSpeed = 300;
	}
	
}
