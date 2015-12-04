package de.mhus.aqua.mod.uiapp.wui;

import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;

public class WColumnSelector extends IWTplContainer {

	private String height = "250";
	private String width  = "100%";
	private String columnWidth = "100";
	private ISource source = null;
	private Action selectAction = null;

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("sourceUrl", source.getRequest());
		if (selectAction != null) attr.put("selectAction", selectAction.paint());
		attr.put("height", WUtil.toSize( height ) );
		attr.put("width", WUtil.toSize( width ) );
		attr.put("columnWidth", WUtil.toSize( columnWidth ) );
		
	}

	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WColumnSelector");
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(String width) {
		this.columnWidth = width;
	}
	
	public ISource getSource() {
		return source;
	}

	public void setSource(ITreeSource source) {
		this.source = source;
	}
	
	public void setSelectAction(Action selectAction) {
		this.selectAction = selectAction;
	}

	public Action getSelectAction() {
		return selectAction;
	}
	
}
