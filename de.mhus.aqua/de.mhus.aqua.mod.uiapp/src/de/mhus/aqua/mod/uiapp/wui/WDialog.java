package de.mhus.aqua.mod.uiapp.wui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;

public class WDialog extends IWTplContainer {

	private LinkedList<Map<String, Object>> buttons = new LinkedList<Map<String,Object>>();
	private String title = "";
	private Action closeAction;
	private String height = "300";
	private String width  = "500";
	private Action completeAction = null;
	
	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WDialog");
	}

	public void addButton(String title, Action action) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("title", WUtil.toText(nls, title));
		map.put("action", action.paint());
		map.put("raw_action", action);
		buttons.add(map);
	}
	
	public void setCloseAction(Action action) {
		closeAction = action;
	}
	
	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("close_title", "Close");
		attr.put("buttons", buttons);
		attr.put("title", WUtil.toText(nls, title));
		attr.put("close_action", closeAction.paint());
		attr.put("height", WUtil.toSize( height ) );
		attr.put("width", WUtil.toSize( width ) );
		if (completeAction!=null) attr.put("actioncomplete", completeAction.paint());
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void addContent(IWComponent content) {
		addChild("content",content);
	}
	
	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		return height;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getWidth() {
		return width;
	}
	
	public Action getCloseDialogAction() {
		return new StringAction("if (win_$uid$) {win_$uid$.hide();win_$uid$=null;}", getId());
	}
	
	public void onCompleteAction(Action completeAction) {
		this.completeAction = completeAction;
	}

	public Action getCompleteAction() {
		return completeAction;
	}

}
