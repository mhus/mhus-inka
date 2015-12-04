package de.mhus.aqua.mod.uiapp;

import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;
import de.mhus.lib.MException;

public class UiAquaDelegate {

	public IWComponent createWComponent(String name,AquaSession session) throws MException {
		return Activator.instance().createWComponent(name, session);
	}
	
	public ComponentInfo[] findWComponents(String group,AquaSession session) {
		return Activator.instance().findWComponents(group, session);
	}

}
