package de.mhus.aqua.tpl;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public interface TplTask {
	
	public String getTplFileName(String section);
	
	public String getId();

	public void processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException;
	
}
