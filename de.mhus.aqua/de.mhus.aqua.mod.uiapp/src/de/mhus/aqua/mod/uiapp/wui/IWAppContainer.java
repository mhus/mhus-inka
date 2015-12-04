package de.mhus.aqua.mod.uiapp.wui;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public abstract class IWAppContainer extends IWTplContainer  {

	public abstract String getTitle();
	
	public abstract boolean canChangeHeight();

	public abstract void setHeight(int height);
	
	public abstract int getHeight();
	
}
