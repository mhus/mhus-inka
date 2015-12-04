package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.List;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public class WDataInclude extends IWComponent {

	
	private String name;

	public WDataInclude(String dataName) {
		name = dataName;
	}

	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		IWComponent c = (IWComponent)data.getAttribute(name);
		if ( c == null ) return;
		c.paint(data, stream);
	}
	
	public void getJsRequirements(AquaRequest data, List<WInclude> set) {
		IWComponent c = (IWComponent)data.getAttribute(name);
		if ( c == null ) return;
		c.getJsRequirements(data, set);
	}
		
	public void getCssRequirements(AquaRequest data, List<WInclude> set) {
		IWComponent c = (IWComponent)data.getAttribute(name);
		if ( c == null ) return;
		c.getCssRequirements(data, set);
	}
	
	public void getJsResRequirements(AquaRequest data, List<Resource> set) {
		IWComponent c = (IWComponent)data.getAttribute(name);
		if ( c == null ) return;
		c.getJsResRequirements(data, set);
	}
	
	public void getCssResRequirements(AquaRequest data, List<Resource> set) {
		IWComponent c = (IWComponent)data.getAttribute(name);
		if ( c == null ) return;
		c.getCssResRequirements(data, set);
	}
	
}
