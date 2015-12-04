package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.cao.CaoException;

public class WImageSlideshow extends IWComponent {

	private String[] imgPath;

	public WImageSlideshow(AquaRequest req) throws CaoException {
		this(req,null);
	}

	public WImageSlideshow(AquaRequest req,String id) throws CaoException {
		super(id);
//		setJsResources(new String[] {
//				req.getNode().getApplication().getRes("jquery") + "/jquery.js",
//				req.getNode().getApplication().getRes("dojo") + "/dojo/dojo.js"});
	}

	public void setImg(String[] path) {
		imgPath = path;
		
		StringBuffer sb = new StringBuffer();
//		sb.append("dojo.require(\"dojo.fx\");dojo.addOnLoad(function(){ani=dojo.fx.chain([");
//		for ( int i = 0; i < imgPath.length; i++) {
//			if ( i != 0 ) sb.append(",");
//			sb.append("dojo.fx.combine([");
//			sb.append("dojo.fadeOut({ node: \""+getId()+"_"+i+"\", duration:1500 }),");
//			sb.append("dojo.fadeIn({ node: \""+getId()+"_"+( i == imgPath.length-1 ? 0 : i+1)+"\", duration:1500 })");
//			sb.append("])");
//		}
//		
//		sb.append("]);ani.play(); });");
		
		sb.append("dojo.require(\"dojo.fx\");dojo.addOnLoad(function(){\n");
		for ( int i = 0; i < imgPath.length; i++) {
			sb.append("var a"+i+"=dojo.fx.combine([");
			sb.append("dojo.fadeOut({ node: \""+getId()+"_"+i+"\", delay: 5000, duration:1500 }),");
			sb.append("dojo.fadeIn({ node: \""+getId()+"_"+( i == imgPath.length-1 ? 0 : i+1)+"\", delay: 5000, duration:1500 })");
			sb.append("]);\n");
		}
		for ( int i = 0; i < imgPath.length; i++) {
			sb.append("dojo.connect(a"+i+", \"onEnd\", a"+( i == imgPath.length-1 ? 0 : i+1)+", \"play\");\n");
		}
		
		sb.append(" a0.play(); });");
		
	//	setJs(new String[] {sb.toString()});
	}
	
	public void paint(AquaRequest data, PrintWriter stream) {
		stream.print("<div");
		paintTagAttributes(stream);
		stream.print("><div class=\"slideshow_control\">");
		for ( int i = 0; i < imgPath.length; i++)
			stream.print("<a class=\"" + (i==0?"first":"") + (i==imgPath.length-1?"last":"")+"\" href=\"#"+getId() + "_b_" + i + "\">\uA500</a>" );
		stream.print("</div>");
		
		for ( int i = imgPath.length-1; i >= 0; i--)
			stream.print("<img id=\"" + getId() + "_"+i+"\" src=\""+imgPath[i]+"\">");
		
		stream.print("</div>");

	}

	
}
