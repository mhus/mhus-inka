package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;

public class WImage extends IWComponent {

	private String imgPath;

	public WImage() {
		super();
	}

	public WImage(String id) {
		super(id);
	}

	public void setImg(String path) {
		imgPath = path;
	}
	
	public void paint(AquaRequest data, PrintWriter stream) {
		stream.print("<img");
		paintTagAttributes(stream);
		stream.print(" src=\""+imgPath+"\">");

	}

	
}
