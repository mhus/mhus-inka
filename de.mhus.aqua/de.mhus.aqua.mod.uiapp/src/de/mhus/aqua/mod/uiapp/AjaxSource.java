package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;
import java.util.UUID;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.ISource;
import de.mhus.lib.MException;
import de.mhus.lib.MSingleton;

public abstract class AjaxSource implements ISource {

	protected UiBox box;
	private String id;

	public AjaxSource(UiBox box) {
		this.box = box;
		this.id = UUID.randomUUID().toString();
		box.registerAjaxSource(this);
	}

	public String getId() {
		return id;
	}

	public abstract void processAjax(AquaRequest request, PrintWriter writer) throws MException;

	public void close() {
		if (box == null) return;
		box.unregisterAjaxSource(this);
		box = null;
	}
	
	public boolean isClosed() {
		return box == null;
	}
	
	public String getRequest() {
		if (box==null) {
			//TODO log message !!!!
			return "";
		}
		return MSingleton.instance().getConfig().getExtracted("WEB_PATH","") + "/res/_ajax_0.0/get?nid=" + box.getPath() + "&bid=" + box.getId() + "&src=" + getId();
	}

}
