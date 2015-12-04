package de.mhu.os.fr.nr1;

import javax.swing.JFrame;

import de.mhu.os.fr.gui.SurfaceFrame;
import de.mhu.os.fr.model.Creature;
import de.mhu.os.fr.model.SerializedSurface;
import de.mhu.os.fr.model.Surface;
import de.mhu.os.fr.model.SurfaceGenerator;
import de.mhu.os.fr.util.ValueSource;
import de.mhus.lib.MSwing;

public class Number1 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Surface s = new Surface( 100, 100 );
		SurfaceGenerator.generateSimple( s, 0.1 );
		SerializedSurface save = s.serialize();
		
		//s.addSource( 10, 10, new ValueSource( (byte)5 ) );
		SurfaceGenerator.generateSources( s, 3 );
		
		SurfaceFrame frame = new SurfaceFrame( s );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setTitle( "Number 1" );
		//ASwing.tribleFrame( frame );
		frame.pack();
		MSwing.centerFrame( frame );
		frame.setVisible( true );
		
		int round = 0;
		while ( true ) {
			round++;
			s.restore( save );
			for ( int i = 0; i < 10; i++ ) {
				Creature1 c = new Creature1( i % 2 == 0);
				s.addCreature( c, (int)(Math.random()*s.getWidth()), (int)(Math.random()*s.getHeight()) );
			}
			
			s.loop( round );
			s.clearCreatures();
			//System.out.println( "Exited" );
		}
	}

}
