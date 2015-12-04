package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;

public class WTable  extends IWTplContainer {

	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		stream.print("<div class='table' ");
		stream.print(this.getClass().getCanonicalName());
		paintTagAttributes(stream);
		engine.execute(this, null, null, stream);
		stream.print("</div>");
		
	}

	@Override
	protected void doInit() throws MException {

		setTplName(Activator.instance().getId() + "/wtable");

		addJsResources(  new WAquaResource(Activator.instance().getRes("yui"),"/yui/yui-min.js") );
		addCssResources( new WAquaResource(Activator.instance().getRes("yui"), "/cssfonts/fonts-min.css") ) ;
		addJs( new IWTplInclude( engine, this, ".js", null) );
		addCss( new IWTplInclude(engine,this,".css", null) );
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		// TODO Auto-generated method stub
		
	}
	

}
