package de.mhus.aqua.mod.uiapp.wui;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.UiBox;
import de.mhus.lib.MException;

public abstract class IWLayout extends IWTplContainer {

	
	public abstract void recreate( ContainerContributor contributor );
	
	public abstract int getDefaultContainer();
	
	public abstract int getContainerSize();

	public abstract IWComponent appendBox(int list, int posContainer, UiBox box) throws MException;

	public abstract IWComponent insertBox(int posContainer, int pos, UiBox box) throws MException;
	
}
