package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MString;

public class WBreadcrumb extends IWComponent {

	public void paint(AquaRequest data, PrintWriter stream) {
		stream.print("<div class='breadcrumb' ");
		stream.print(this.getClass().getCanonicalName());
		paintTagAttributes(stream);
		stream.print(">");
		String[] parts = MString.split(data.getPath(), "/");
		boolean first = true;
		StringBuffer link = new StringBuffer();
		for (String part : parts) {
			if (!MString.isEmpty(part)) {
				link.append("/").append(part);
				if (!first) stream.print("&nbsp;&gt;&nbsp;");
				stream.print("<a href='");
				stream.print(link.toString());
				stream.print("'>");
				stream.print(part);
				stream.print("</a>");
				first = false;
			}
		}
		stream.print("</div>");
		
	}
	
	
}
