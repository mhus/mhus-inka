package de.mhu.os.fr.model;

import java.util.HashSet;
import java.util.LinkedList;

import de.mhus.lib.MEventHandler;
import de.mhus.lib.MThread;


public class Surface {

	public static final byte MAX_VALUE = 10;
	private int sleepTime = 100;
	
	private int h;
	private int w;
	private byte[] values;
	private HashSet<Creature>[] creaturesOnSurface;
	private HashSet<Creature> creatures;
	private LinkedList<Source> sources;
	private MEventHandler<Listener> eventHandler = new MEventHandler<Listener>();
	private int removedChildCnt;
	private int addedChildCnt;
	private boolean finishRound;
	
	public Surface( int pWidth, int pHeight ) {
		w = pWidth;
		h = pHeight;
		values = new byte[ w * h ];
		creatures = new HashSet<Creature>();
		creaturesOnSurface = new HashSet[ w * h ];
		for ( int i = 0; i < creaturesOnSurface.length; i++ )
			creaturesOnSurface[i] = new HashSet<Creature>();
		sources = new LinkedList<Source>();
	}

	public int transformX( int x ) {
		while ( x < 0 ) x = w+x;
		return x % w;
	}
	
	public int transformY( int y ) {
		while ( y < 0 ) y = h+y;
		return y % h;
	}
	
	public byte getValue( int x, int y ) {
		x = transformX( x );
		y = transformY( y );
		return values[ x * h + y ];
	}
	
	public void setValue( int x, int y, byte value ) {
		x = transformX( x );
		y = transformY( y );
		if ( values[ x * h + y ] != value ) {
			values[ x * h + y ] = value;
			fireChanged( x, y );
		}
	}
	
	public void addSource( int x, int y, Source s ) {
		x = transformX( x );
		y = transformY( y );
		s.setPosition(x, y);
		sources.add( s );
	}
	
	public boolean addCreature( Creature c, int x, int y ) {
		x = transformX( x );
		y = transformY( y );
		synchronized ( this ) {
			if ( creatures.contains( c ) ) return false;
			addedChildCnt++;
			creatures.add( c );
			toPos( c, x, y );
		}
		return true;
	}
	
	public boolean moveCreature( Creature c, int x, int y ) {
		x = transformX( x );
		y = transformY( y );
		synchronized ( this ) {
			if ( ! creatures.contains( c ) ) return false;
			removePos( c );
			toPos( c, x, y );
		}
		return true;
	}
	
	private void removePos(Creature c) {
		int x = c.getPositionX();
		int y = c.getPositionY();
		if ( x < 0 || y < 0 ) return;
		creaturesOnSurface[ x * h + y ].remove( c );
		c.setPosition( -1, -1 );
		fireChanged( x, y );
	}

	private void toPos( Creature c, int x, int y ) {
		creaturesOnSurface[ x * h + y ].add( c );
		c.setPosition( x, y );
		fireChanged( x, y );
	}

	public int getHeight() {
		return h;
	}

	public int getWidth() {
		return w;
	}

	public int getCreatureSize(int x, int y) {
		x = transformX( x );
		y = transformY( y );
		synchronized ( this ) {
			return creaturesOnSurface[ x * h + y ].size();
		}
	}
	
	public HashSet<Creature> getCreatures() {
		synchronized ( this ) {
			return (HashSet<Creature>)creatures.clone();
		}
	}

	public byte take( int x, int y ) {
		x = transformX( x );
		y = transformY( y );
		byte v = values[ x * h + y ];
		values[ x * h + y ] = 0;
		fireChanged( x, y );
		return v;
	}
	
	public boolean drop( int x, int y, byte v ) {
		if ( v < 0 || v >= MAX_VALUE ) return false;
		x = transformX( x );
		y = transformY( y );
		if ( values[ x * h + y ] != 0 ) return false;
		if ( values[ x * h + y ] == 0 ) return true;
		values[ x * h + y ] = v;
		fireChanged( x, y );
		return true;
	}
	
	public void removeCreature( Creature c ) {
		synchronized ( this ) {
			if ( creatures.remove( c ) ) {
				removePos( c );
				removedChildCnt++;
			}
		}
	}

	public int getCreatureSize() {
		return creatures.size();
	}
	
	public MEventHandler<Listener> eventHandler() {
		return eventHandler;
	}
	
	private void fireChanged( int x, int y) {
		Object[] list = eventHandler.getListenersArray();
		for ( Object o : list )
			((Listener)o).eventChanged( x, y );
	}
	
	private void fireChangedAll() {
		Object[] list = eventHandler.getListenersArray();
		for ( Object o : list )
			((Listener)o).eventChangedAll();
	}
	
	private void fireRoundFinished(int round, long age, int childSize, int addedChildCnt, int removedChildCnt, long healty, int healtyMin, int healtyMax) {
		Object[] list = eventHandler.getListenersArray();
		for ( Object o : list )
			((Listener)o).eventRoundFinished( round, age, childSize, addedChildCnt, removedChildCnt, healty, healtyMin, healtyMax );
	}
	
	public static interface Listener {

		void eventChanged(int x, int y);

		void eventRoundFinished(int round, long age, int childSize, int addedChildCnt,
				int removedChildCnt, long healty, int healtyMin, int healtyMax);

		void eventChangedAll();
		
	}

	public SerializedSurface serialize() {
		SerializedSurface out = new SerializedSurface();
		out.w = w;
		out.h = h;
		out.values = new byte[ values.length ];
		System.arraycopy( values, 0, out.values, 0, values.length );
		return out;
	}

	public void restore(SerializedSurface save) throws Exception {
		if ( w != save.w || h != save.h ) throw new Exception( "Can't restore surface" );
		System.arraycopy( save.values, 0, values, 0, values.length );
		fireChangedAll();
	}

	public Creature[] getCreatures(int x, int y) {
		x = transformX( x );
		y = transformY( y );
		synchronized ( this ) {
			return creaturesOnSurface[ x * h + y ].toArray( new Creature[ creaturesOnSurface[ x * h + y ].size() ] );
		}
	}
	
	public void loop( int round ) {
		long age = 0;
		finishRound = false;
		for ( Source s : sources )
			s.reset();
		
		while ( !finishRound && getCreatureSize() > 0 ) {
			if ( sleepTime >= 0 ) {
				
				for ( Source s : sources )
					s.action( this );
				
				long healty = 0;
				int healtyMin = Integer.MAX_VALUE;
				int healtyMax = 0;
				removedChildCnt = 0;
				addedChildCnt   = 0;
				for ( Creature c : getCreatures() ) {
					c.move( this );
					int h = c.getHealty();
					healtyMin = Math.min( healtyMin, h );
					healtyMax = Math.max( healtyMax, h );
					healty+=h;
				}
				age++;
				//System.out.println( "Size: " + s.getCreatureSize() + " (" + healty + ")" );
				fireRoundFinished( round, age, getCreatureSize(), addedChildCnt, removedChildCnt, healty, healtyMin, healtyMax );
				//System.out.println( "Age: " + age );
				MThread.sleep( sleepTime+1 );
			} else {
				MThread.sleep( 200 );
			}
		}
		
	}

	public void setSleepTime(int val) {
		sleepTime  = val;
	}

	public void finishRound() {
		finishRound = true;
	}

	public void clearCreatures() {
		synchronized ( this ) {
			for ( int i = 0; i < creaturesOnSurface.length; i++ )
				creaturesOnSurface[i].clear();
			creatures.clear();
		}
	}
	
}
