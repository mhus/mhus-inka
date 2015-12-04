package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.tpl.Engine;
import de.mhus.aqua.tpl.TplTask;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

public abstract class IWTplContainer extends IWUiContainer implements TplTask {

	public static final String SECTION = "tplSection";
	
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(IWTplContainer.class);

	private String tplName;
	protected Engine engine;
	
	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		engine = Activator.instance().getAqua().getTplEngine();
		super.initWElement(request, id, config);
	}

	protected abstract void doFillAttributes(AquaRequest data, Map<String, Object> attr);
	
	@Override
	public String getTplFileName(String section) {
		return getTplFileNameInternal(tplName, section);
	}

	public static String getTplFileNameInternal(String tplName, String section) {
		if (section == null)
			return tplName + ".html";
		else
		if (section.startsWith("."))
			return tplName + section;
		else
		if (section.indexOf('.') > -1)
			return tplName + "_" + section;
		else
			return tplName + "_" + section + ".html";		
	}
	
	@Override
	public void processTplRequest(AquaRequest req, Map<String,Object> params, PrintWriter writer) throws MException {
		if ("paint".equals(params.get("name").toString() )) {
			try {
				String list = DEFAULT;
				Object listObj = params.get("list");
				if (listObj!=null)
					list = listObj.toString();
				paint(req, list, writer);
			} catch (Exception e) {
				log.i(getTplName(),e);
			}
			return;
		}
	}

	public void setTplName(String in) {
		tplName = in;
	}
	
	public String getTplName() {
		return tplName;
	}
	
	public void paint(AquaRequest data, PrintWriter stream) throws MException {
		Map<String, Object> attr = engine.createAttributes(data);
		doFillAttributes(data,attr);
		if (nls!=null) nls.initTpl(attr);
		paint (data,stream,attr);
	}

	public void paint(AquaRequest data, PrintWriter stream,Map<String, Object> attr) throws MException {

		engine.execute(this, (String)attr.get(SECTION), attr, stream);
		
		stream.flush();
	}

}
