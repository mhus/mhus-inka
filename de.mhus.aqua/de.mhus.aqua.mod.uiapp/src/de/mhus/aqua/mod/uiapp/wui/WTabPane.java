package de.mhus.aqua.mod.uiapp.wui;

import java.util.LinkedList;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;

public class WTabPane extends IWTplContainer {

	protected LinkedList<String[]> tabs = new LinkedList<String[]>();
	
	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WTabPane");
	}

	public void addTab(String listName, String title) {
		tabs.add(new String[] {listName, title});
	}
	
	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("tabs", tabs);
	}

}
