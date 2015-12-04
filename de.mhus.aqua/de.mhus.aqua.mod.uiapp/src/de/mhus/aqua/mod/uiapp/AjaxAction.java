package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.Action;
import de.mhus.lib.MException;

public abstract class AjaxAction extends AjaxSource implements Action {

	public enum RESULT {ERROR, OK}
	public enum ACTION {CONTENT, FIRST}
	
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(AjaxAction.class);


	protected AquaRequest request;


	private String extra = "";

	public abstract AjaxActionDefinition[] doRequest(AquaRequest request) throws Exception;
			
	public AjaxAction(UiBox box) {
		super(box);
	}

	public String getAddress() {
		return getRequest().replaceAll("\"", "\\\"");
	}
	
	public String paint() {
		return "aquaExecute(\"" + getAddress() +"\""+extra+" );";
	}

	public void setExtra(String in) {
		extra = in;
	}

	@Override
	public void processAjax(AquaRequest request, PrintWriter writer)
			throws MException {
				
		RESULT rc = RESULT.OK;
		this.request = request;
		AjaxActionDefinition[] actions = null;
		try {
			actions = doRequest(request);
		} catch (Exception e) {
			log.i(request,e);
			rc = RESULT.ERROR;
		}
		
		writer.print("{\"results\":[");
		if ( !RESULT.ERROR.equals(rc) ) {
			boolean isFirst = true;
			for (AjaxActionDefinition action : actions) {
				if (!isFirst)
					writer.print(",");
				writer.print("{\"a\":\"");
				writer.print(action.action.toString().toLowerCase());
				writer.print("\",\"n\":\"");
				writer.print(action.node);
				writer.print("\", \"v\":\"");
				PrintWriter w = new PrintWriter( new EncodeWriter(writer) );
				try {
					action.component.paint(request,w);
				} catch (Exception e) {
					log.d(request,e);
				}
				w.flush();
				writer.print("\"}");
				isFirst = false;
			}
		}
		writer.print("],");
		writer.print("\"rc\":\"");
		writer.print(rc.toString().toLowerCase());
		writer.print("\"}");		
	}

}
