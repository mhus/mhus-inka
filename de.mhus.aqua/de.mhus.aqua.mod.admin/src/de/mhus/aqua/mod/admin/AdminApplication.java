package de.mhus.aqua.mod.admin;

import de.mhus.aqua.api.AquaContainer;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.UiApplication;
import de.mhus.aqua.mod.uiapp.UiEditableContainer;
import de.mhus.lib.MException;

public class AdminApplication extends UiApplication {

	public AquaContainer createUiContainer(AquaRequest request) throws MException {
		return new UiEditableContainer(request,this);
	}

}
