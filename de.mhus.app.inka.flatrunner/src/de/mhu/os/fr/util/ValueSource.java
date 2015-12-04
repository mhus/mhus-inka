package de.mhu.os.fr.util;

import de.mhu.os.fr.model.Creature;
import de.mhu.os.fr.model.Source;
import de.mhu.os.fr.model.Surface;

public class ValueSource extends Source {

	private byte value;
	private double angle = 0;
	private double size  = 1;
	private double angelAdd;
	private int amount;
	private double sizeAdd;
	private double maxSize;
	
	public ValueSource( byte v ) {
		this( v, 0.1, 0.1, 10, 10 );
	}
	
	public ValueSource( byte v, double pAngelAdd, double pSizeAdd, int pAmount, double pMaxSize ) {
		value = v;
		angelAdd = pAngelAdd;
		sizeAdd  = pSizeAdd;
		amount   = pAmount;
		maxSize = pMaxSize;
	}
	
	@Override
	public void action(Surface s) {
		
		for ( int i = 0; i < amount; i++ ) {
			int x = (int)(Math.cos( angle ) * size) + getPositionX();
			int y = (int)(Math.sin( angle ) * size) + getPositionY();
			
			s.setValue( x, y, value );
			Creature[] cs = s.getCreatures( x, y );
			for ( Creature c : cs )
				s.removeCreature( c );
			angle+=angelAdd;
			size+=sizeAdd;
			if ( size > maxSize ) size = size-maxSize;
			if ( angle > Math.PI*2 ) angle = angle - Math.PI*2;
		}		
	}

	@Override
	public void reset() {
		angle = 0;
		size  = 1;		
	}

}
