package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.tpl.Engine;
import de.mhus.aqua.tpl.TplTask;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

/**
 * This is a Component (not a Container) for elements with no more additional deeper
 * elements. The name 'Box' is used to separate it more from Container. Component and Container to
 * similar. Ok, Box should contain something, but in this case it is the simple element without
 * more sections.
 * 
 * The TplBox is the pondon to TplComponent and is able to handle templates.
 * 
 * @author mikehummel
 *
 */
public abstract class IWTplBox extends IWComponent implements TplTask, WExternal {

	public static final String SECTION = "tplSection";

//	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(WTplBox.class);

	private String tplName;
	protected Engine engine;
	protected AquaRequest request;

	protected abstract void doInit() throws MException;

	protected abstract void doFillAttributes(AquaRequest data, Map<String, Object> attr);

	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.request = request;
		engine = Activator.instance().getAqua().getTplEngine();
		doInit();
	}
	
	
	@Override
	public String getTplFileName(String section) {
		return IWTplContainer.getTplFileNameInternal(tplName, section);
	}

	@Override
	public void processTplRequest(AquaRequest req, Map<String, Object> params,
			PrintWriter writer) throws MException {

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

	public void setTplName(String in) {
		tplName = in;
	}

	public String getTplName() {
		return tplName;
	}
}
