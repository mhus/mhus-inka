package de.mhus.aqua.mod.uiapp.wui;

import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.mod.uiapp.wui.WUtil.DIRECTION;
import de.mhus.lib.MException;

public class WTree extends IWTplBox {

	private boolean showInfoText = false;
	private String infoText = null;
	private String title = null;
	private String defaultInfoText = "";
	private String height = "250";
	private String width  = "250";
	private String infoTitle = "";
	private ISource source = null;
	private String infoHeight = "150";
	private String infoWidth  = "150";
	private WUtil.DIRECTION infoDirection = DIRECTION.EAST;
	private Action selectAction = null;
	
	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WTree");		
	}
	
	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("showInfoText", showInfoText);
		attr.put("infoText", WUtil.toText(nls,infoText));
		attr.put("title", WUtil.toText(nls,title));
		attr.put("defaultInfoText", WUtil.toText(nls,defaultInfoText));
		attr.put("height", WUtil.toSize( height ) );
		attr.put("width", WUtil.toSize( width ) );
		attr.put("infoTitle", WUtil.toText(nls,infoTitle));
		attr.put("infoWidth", WUtil.toSize(infoWidth) );
		attr.put("infoHeight", WUtil.toSize(infoHeight) );
		attr.put("infoDirection", infoDirection.toString().toLowerCase());
		attr.put("sourceUrl", source.getRequest());
		if (selectAction != null) attr.put("selectAction", selectAction.paint());
	}
	
	public boolean isShowInfoText() {
		return showInfoText;
	}

	public void setShowInfoText(boolean showInfoText) {
		this.showInfoText = showInfoText;
	}

	public String getInfoText() {
		return infoText;
	}

	public void setInfoText(String infoText) {
		this.infoText = infoText;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDefaultInfoText() {
		return defaultInfoText;
	}

	public void setDefaultInfoText(String defaultInfoText) {
		this.defaultInfoText = defaultInfoText;
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

	public String getInfoTitle() {
		return infoTitle;
	}

	public void setInfoTitle(String infoTitle) {
		this.infoTitle = infoTitle;
	}

	public ISource getSource() {
		return source;
	}

	public void setSource(ITreeSource source) {
		this.source = source;
	}

	public String getInfoHeight() {
		return infoHeight;
	}

	public void setInfoHeight(String infoHeight) {
		this.infoHeight = infoHeight;
	}

	public String getInfoWidth() {
		return infoWidth;
	}

	public void setInfoWidth(String infoWidth) {
		this.infoWidth = infoWidth;
	}

	public WUtil.DIRECTION getInfoDirection() {
		return infoDirection;
	}

	public void setInfoDirection(WUtil.DIRECTION infoDirection) {
		this.infoDirection = infoDirection;
	}

	public void setSelectAction(Action selectAction) {
		this.selectAction = selectAction;
	}

	public Action getSelectAction() {
		return selectAction;
	}

	
	
}
