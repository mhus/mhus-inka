package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;
import java.util.HashMap;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.AjaxAction.ACTION;
import de.mhus.aqua.mod.uiapp.wui.IWAppContainer;
import de.mhus.lib.MException;
import de.mhus.lib.config.HashConfig;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.form.ConfigForm;
import de.mhus.lib.form.MForm;

public abstract class UiBox extends IWAppContainer {


	private HashMap<String, AjaxSource> ajaxSources = new HashMap<String, AjaxSource>();
	protected UiContainer container;
	protected boolean valid;
	private String path;
	protected AquaRequest request;
	
	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.path      = request.getPath();
		this.request = request;
		this.container = (UiContainer) request.getNode().getApplication().getUiContainer(request);
		if (config == null) config = new HashConfig();
		super.initWElement(request, config.getString("id",getId()), config);
		valid = true;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isValid() {
		return valid;
	}

	public abstract String getTitle();

	/**
	 * Process ajax requests - in the eye thrue the nose in the ear ...
	 * 
	 * @param request
	 * @param w
	 */
	public void processAjax(AquaRequest request, PrintWriter writer) throws MException {

		String src = request.getParameter("src");
		AjaxSource srcObj = ajaxSources.get(src);
		
		if (srcObj != null) {
			srcObj.processAjax(request, writer);
			return;
		}

	}

	public void registerAjaxSource(AjaxSource source) {
		ajaxSources.put(source.getId(),source);
	}
	
	public void unregisterAjaxSource(AjaxSource source) {
		ajaxSources.remove(source.getId());
	}

//	public void updateConfig(IConfig cbox) {
//		// TODO Auto-generated method stub
//		
//	}

	public MForm getConfigForm() {
		return new ConfigForm(config);
	}

	public String getType() {
		return config == null ? getClass().getCanonicalName() : config.getString("type", getClass().getCanonicalName());
	}

	public AjaxActionDefinition ajaxActionDefinitionRefresh() {
		return new AjaxActionDefinition(ACTION.CONTENT, "#in_" + getId(), this);
	}
	
	@Override
	public void setHeight(int height) {
		if (config == null || !canChangeHeight()) return;
		try {
			config.setInt("height", height);
		} catch (MException e) {
			
		}
	}

	@Override
	public int getHeight() {
		if (config == null || !canChangeHeight()) return -1;
		return config.getInt("height", -1);
	}

	public boolean isVisible() {
		return true;
	}

}
