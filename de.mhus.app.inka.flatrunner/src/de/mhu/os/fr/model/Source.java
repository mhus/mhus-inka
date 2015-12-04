package de.mhu.os.fr.model;

public abstract class Source {

	private int posX;
	private int posY;
	
	public final void setPosition( int x, int y ) {
		posX = x;
		posY = y;
	}
	
	public final int getPositionX() {
		return posX;
	}
	
	public final int getPositionY() {
		return posY;
	}

	public abstract void reset();
	public abstract void action( Surface s );
	
}
