package de.mhus.aqua.mod.uiapp;

import de.mhus.aqua.mod.uiapp.AjaxAction.ACTION;
import de.mhus.aqua.mod.uiapp.wui.IWComponent;

public class AjaxActionDefinition {
	ACTION action;
	String node;
	IWComponent component;
	
	public AjaxActionDefinition(ACTION action, String node, IWComponent component) {
		this.action = action;
		this.node = node;
		this.component = component;
	}

}
