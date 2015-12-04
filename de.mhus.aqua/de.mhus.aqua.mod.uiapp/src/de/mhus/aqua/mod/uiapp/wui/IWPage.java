package de.mhus.aqua.mod.uiapp.wui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public class IWPage extends IWTplContainer {

	@Override
	protected void doInit() throws MException {
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {

		List<Resource> jsResSet = new LinkedList<Resource>();
		getJsResRequirements(data,jsResSet);
		attr.put("list_res_js", jsResSet);
		
		List<WInclude> jsSet = new LinkedList<WInclude>();
		getJsRequirements(data, jsSet);
		attr.put("list_js", jsSet);
		
		List<Resource> cssResSet = new LinkedList<Resource>();
		getCssResRequirements(data,cssResSet);
		attr.put("list_res_css", cssResSet);
		
		List<WInclude> cssSet = new LinkedList<WInclude>();
		getCssRequirements(data,cssSet);
		attr.put("list_css", cssSet);
	}
	
}
