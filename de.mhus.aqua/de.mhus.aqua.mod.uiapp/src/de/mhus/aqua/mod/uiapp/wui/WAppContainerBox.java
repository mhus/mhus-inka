package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;

public class WAppContainerBox extends IWAppContainer {

	private IWAppContainer c;

	public WAppContainerBox(IWAppContainer c) throws MException {
		super();
		this.c = c;
	}

	@Override
	public String getTitle() {
		return c.getTitle();
	}

	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		
		stream.print("<li class=\"item yui3-dd-drop yui3-dd-draggable\" id=\"box_");
	    stream.print(c.getId());
	    stream.print("\"><div class=\"mod\"><h2 id=\"h2_");
	    stream.print(c.getId());
	    stream.print("\"><strong>");
	    stream.print(c.getTitle());
	    stream.print("</strong><a title=\"configure module\" class=\"setup\" href=\"#\"></a><a title=\"close module\" class=\"close\" href=\"#\"></a></h2><div id=\"in_");
	    stream.print(c.getId());
	    stream.print("\" class=\"inner\"");
	    int height = canChangeHeight() ? getHeight() : -1;
	    if (height >= 0 )
	    	stream.print(" style=\"height:" + height + "px\"");
	    stream.print(">");
		c.paint(data,stream);
		stream.print("</div>");
		if (canChangeHeight()) {
			stream.print("<div class='resize' id='resize_");
			stream.print(c.getId());
			stream.print("'></div>");
		}
		stream.print("</div></li>");
	}

	@Override
	protected void doInit() throws MException {
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
	}

	@Override
	public boolean canChangeHeight() {
		return c.canChangeHeight();
	}

	@Override
	public void setHeight(int height) {
		c.setHeight(height);
	}

	@Override
	public int getHeight() {
		return c.getHeight();
	}

}
