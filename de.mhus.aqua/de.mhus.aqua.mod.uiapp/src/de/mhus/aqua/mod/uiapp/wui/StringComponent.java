package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public class StringComponent extends IWComponent {

	private String content = "";
	
	public void setContent(String in) {
		content = in;
	}
	
	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		stream.print(content);
	}
}
