package de.mhu.os.fr.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import de.mhu.os.fr.model.Surface;

public class SurfacePanel extends JPanel {

	private Surface s;
	private int w;
	private Color colors[] = { Color.blue, Color.cyan, Color.lightGray, Color.yellow, Color.green, 
							   Color.gray, Color.magenta, Color.white, Color.black, Color.orange };
	private int maxCreatureSize;

	public SurfacePanel( Surface pSurface, int cellWidth ) {
		super();
		enableEvents(AWTEvent.KEY_EVENT_MASK );
		setFocusable( true );
		s = pSurface;
		setCellWidth( cellWidth );
		s.eventHandler().register( new Surface.Listener() {

			public void eventChanged(int x, int y) {
				repaint( x*w, y*w, w, w );
			}

			public void eventChangedAll() {
				repaint();
			}

			public void eventRoundFinished(int round, long age, int childSize, int addedChildCnt,
					int removedChildCnt, long healty, int healtyMin,
					int healtyMax) {
				
			}
			
		});
	}

	public void setCellWidth( int cellWidth ) {
		w = cellWidth;
		maxCreatureSize = Math.min( w, w ) / 2 - 1;
		Dimension d = new Dimension( w * s.getWidth(), w * s.getHeight() );
		setPreferredSize( d );
		setMinimumSize( d );
		setMaximumSize( d );
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		
		Rectangle clip = g.getClipBounds();
		int startX = 0;
		int startY = 0;
		int stopX  = 0;
		int stopY  = 0;
		int realX  = 0;
		int realY  = 0;
		
		if ( clip != null ) {
			startX = clip.x / w - 2;
			startY = clip.y / w - 2;
			stopX  = clip.width  / w + startX + 4;
			stopY  = clip.height / w + startY + 4;
			realX  = clip.x + clip.width;
			realY  = clip.y + clip.height;
			if ( stopX > s.getWidth() ) stopX = s.getWidth();
			if ( stopY > s.getHeight() ) stopY = s.getHeight();
			if ( startX < 0 ) startX = 0;
			if ( startY < 0 ) startY = 0;
		} else {
			stopX  = s.getWidth();
			stopY  = s.getHeight();
			realX  = getWidth();
			realY  = getHeight();
		}
		
		int pixelX = startX * w;
		int pixelY = realY;
		for ( int x = startX; x < stopX; x++ ) {
			pixelY = startY * w;
			for ( int y = startY; y < stopY; y++ ) {
				
				Color color = colors[ s.getValue( x, y ) % 10 ];
				g.setColor( color );
				g.fillRect( pixelX, pixelY, w, w );
				
				int size = s.getCreatureSize( x, y );
				if ( size > maxCreatureSize ) size = maxCreatureSize;
				g.setColor( Color.red );
				for ( int i = 1; i <= size; i++ )
					g.drawRect( pixelX+i, pixelY+i, w-i*2-1, w-i*2-1 );
					
				pixelY+= w;
			}
			pixelX+= w;
		}
		
		if ( realX > pixelX ) {
			g.setColor( Color.gray );
			g.fillRect( pixelX, 0, realX - pixelX, realY );
		}
		if ( realY > pixelY ) {
			g.setColor( Color.gray );
			g.fillRect( 0, pixelY, realX, realY - pixelY );
		}
		
	}

	public int getCellWidth() {
		return w;
	}

}
