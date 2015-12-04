package de.mhu.os.fr.model;

import de.mhu.os.fr.util.KillerSource;
import de.mhu.os.fr.util.ValueSource;

public class SurfaceGenerator {

	public static void generateSimple( Surface s, double density ) {
		int amount = (int)((double)s.getHeight() * (double)s.getWidth() * density );
		for ( int i = 0; i < amount; i++ )
			s.setValue(	(int)(Math.random() * s.getWidth()), 
						(int)(Math.random() * s.getHeight()), 
						(byte)(Math.random() * 10) );
	}
	
	public static void generateSources( Surface s, int size ) {
		for ( int i = 0; i < size; i++ )
			s.addSource( (int)(Math.random() * s.getWidth()), 
					(int)(Math.random() * s.getHeight()), 
					 new ValueSource( (byte)(Math.random() * Surface.MAX_VALUE )		 
					 ) );
	}
	
	public static void generateKillers( Surface s, int size ) {
		for ( int i = 0; i < size; i++ )
			s.addSource( (int)(Math.random() * s.getWidth()), 
					(int)(Math.random() * s.getHeight()), 
					 new KillerSource( (byte)(Math.random() * Surface.MAX_VALUE )		 
					 ) );
	}
	
}
