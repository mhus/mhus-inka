package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

public interface WExternal {

	public void initWElement(AquaRequest request, String id, IConfig config) throws Exception;
	
	public void paint(AquaRequest request, PrintWriter stream) throws MException;
	
}
