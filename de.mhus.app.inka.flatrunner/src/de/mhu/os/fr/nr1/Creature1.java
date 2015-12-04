package de.mhu.os.fr.nr1;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhus.lib.MSingleton;
import de.mhus.lib.form.FormException;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.MFormModel;
import de.mhu.os.fr.model.Creature;
import de.mhu.os.fr.model.Surface;

public class Creature1 extends Creature {

	private static final int HEALTY_CREATE = 250;

	private static final int HEALTY_MINUS = 50;

	private static final int HEALTY_CHILD = 100;

	int healty = 100;
	
	int[] healtyReaction = new int[ Surface.MAX_VALUE ];
	int[] urges = new int[ Surface.MAX_VALUE ];
	boolean d[] = new boolean[ Surface.MAX_VALUE ];
	int ages = 0;
	double agesMulti = 0.01;
	int urgeCreate = 5;
	int urgeCreature = 0;
	boolean isKiller = false;
	int killerHealty = 0;
	int strange = 0;
	
	public Creature1(Creature1 parent ) {
		//System.out.println( "New" );
		healty = HEALTY_CHILD;
		parent.healty-=HEALTY_CHILD;
		for ( int i = 0; i < Surface.MAX_VALUE; i++ ) {
			healtyReaction[i] = parent.healtyReaction[i];
			urges[i] = parent.urges[i];
			d[i] = parent.d[i];
		}
		
		double limit = Math.random();
		for ( int i = 0; i < Surface.MAX_VALUE; i++ ) {
			if ( Math.random() < limit ) {
				healtyReaction[i] = (int)(Math.random() * 40)-20;
				urges[i] = (int)(Math.random() * 40)-20;
				d[i] = Math.random() > 0.5;
			}
		}
		
		agesMulti = agesMulti + (Math.random()*0.1-0.05 );
		if ( agesMulti < 0.01 ) agesMulti = 0.01;
		
		if ( Math.random() > 0.9d ) {
			agesMulti = (Math.random()+0.01d);
			urgeCreate = (int)(Math.random()*30d)+1;
			urgeCreature = (int)(Math.random()*40d)-20;
			strange = (int)(Math.random()*30d);
			isKiller = Math.random() > 0.7d;
			killerHealty = (int)(Math.random()*30d);
		}
		
	}
	
	public Creature1( boolean type ) {
		if ( type ) {
			for ( int i = 0; i < Surface.MAX_VALUE; i++ ) {
				healtyReaction[i] = (int)(Math.random() * i * 4)-20;
				urges[i] = (int)(Math.random() * i * 4)-20;
				//healtyReaction[i] = i*2-10;
				//urges[i] = i*2-10;
				d[i] = Math.random() > 0.5;
			}
		} else {
			for ( int i = 0; i < Surface.MAX_VALUE; i++ ) {
				healtyReaction[i] = (int)(Math.random() * (Surface.MAX_VALUE-i-1) * 4)-20;
				urges[i] = (int)(Math.random() * (Surface.MAX_VALUE-i-1) * 4)-20;
				//healtyReaction[i] = (Surface.MAX_VALUE-i-1)*2-10;
				//urges[i] = (Surface.MAX_VALUE-i-1)*2-10;		
				d[i] = Math.random() > 0.5;
			}
		}
		agesMulti = (Math.random()+0.01d);
		urgeCreate = (int)(Math.random()*30d)+1;
		urgeCreature = (int)(Math.random()*40d) - 20;
		
		strange = (int)(Math.random()*30d);
		isKiller = Math.random() > 0.8;
		killerHealty = (int)(Math.random()*30d);
		
	}
	
	@Override
	public void move(Surface s) {
		
		int newX = getPositionX();
		int newY = getPositionY();
		int urge = 0;
		for ( int x = getPositionX()-1; x <= getPositionX()+1; x++ ) {
			for ( int y = getPositionY()-1; y <= getPositionY()+1; y++ ) {
				int u = urges[ s.getValue( x, y) ] + urgeCreature * s.getCreatureSize( x, y );
				if ( urge < u ) {
					newX = x;
					newY = y;
					urge = u;
				}
			}
		}
		if ( healty > HEALTY_CREATE ) {
			// urge to create
			for ( int x = getPositionX()-1; x <= getPositionX()+1; x++ ) {
				for ( int y = getPositionY()-1; y <= getPositionY()+1; y++ ) {
					int u = s.getCreatureSize( x, y);
					if ( u < 2 && urgeCreate > urge ) {
						newX = x;
						newY = y;
						urge = u;
					}
				}
			}
		}
		if ( s.getCreatureSize( newX, newY ) < 5 ) 
			s.moveCreature( this, newX, newY );
		else {
			newX = getPositionX();
			newY = getPositionY();
		}
		int h = s.getValue( newX, newY );
		healty+= healtyReaction[ h ];
		ages++;
		healty = healty - (int)((double)ages*agesMulti) - (s.getCreatureSize( newX, newY)+1);
		
		if ( isKiller ) {
			healty = healty - killerHealty;
		}
		
		boolean isCreated = false;
		if ( healty > HEALTY_CREATE ) {
			if ( s.getCreatureSize( newX, newY ) < 2 ) {
				s.addCreature( new Creature1( this ), newX, newY );
				healty-=HEALTY_MINUS;
				isCreated = true;
			}
		}

		if ( isKiller && !isCreated ) {
			Creature[] creatures = s.getCreatures( newX, newY );
			for ( Creature other : creatures )
				if ( ((Creature1)other).strange*other.getHealty() < strange*healty ) {
					healty = healty + other.getHealty();
					s.removeCreature( other );
				}
		}
		
		switchValue( s, newX, newY, d[ h ] );
		
		if ( healty < 0 ) {
			//System.out.println( "Died");
			s.removeCreature( this );
		}
	}

	public void switchValue( Surface s, int x, int y, boolean up ) {
		byte b = (byte)(s.getValue( x, y ) + ( up ? 1 : -1 ));		
		if ( b < 0 ) b = 0;
		if ( b >= Surface.MAX_VALUE ) b = Surface.MAX_VALUE-1;
		s.setValue(x, y, b );
	}

	@Override
	public int getHealty() {
		return healty;
	}

	@Override
	public String getDump() {
		
		return "ages=" + ages + "\nhealty=" + healty + "\nhealtyReaction=" + deepToString( healtyReaction )
		+ "\nurges=" + deepToString( urges )
		+ "\nd=" + deepToString( d )
		+ "\nagesMulti=" + agesMulti + "\nurgeCreate=" + urgeCreate
		+ "\nurgeCreature=" + urgeCreature
		+ "\nisKiller=" + isKiller
		+ "\nkillerHealty="+killerHealty
		+ "\nstrange=" + strange;
	}
	
	private String deepToString(int[] a ) {
		StringBuffer out = new StringBuffer();
		out.append( '[' );
		for ( int i = 0; i < a.length; i++ ) {
			if ( i != 0 ) out.append( ',' );
			out.append( a[i] );
		}
		out.append( ']' );
			
		return out.toString();
	}

	private String deepToString(boolean[] a ) {
		StringBuffer out = new StringBuffer();
		out.append( '[' );
		for ( int i = 0; i < a.length; i++ ) {
			if ( i != 0 ) out.append( ',' );
			out.append( a[i] );
		}
		out.append( ']' );
			
		return out.toString();
	}

	@Override
	public MFormModel getConfigurationForm() throws FormException {
		return new MFormModel(MSingleton.instance().getActivator(), this);
	}

}
