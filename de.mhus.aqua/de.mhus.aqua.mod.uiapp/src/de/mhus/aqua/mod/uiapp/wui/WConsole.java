package de.mhus.aqua.mod.uiapp.wui;

import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;

public class WConsole extends IWTplContainer {

	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WConsole");
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("admin", data.getSession().isAdminActive());
	}

}
