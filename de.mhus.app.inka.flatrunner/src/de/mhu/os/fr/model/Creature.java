package de.mhu.os.fr.model;

import de.mhus.lib.form.IConfigurable;

public abstract class Creature implements IConfigurable {

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

	public abstract void move(Surface s);
	public abstract int  getHealty();
	public abstract String getDump();

	
}
