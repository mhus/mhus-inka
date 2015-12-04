package de.mhus.aqua.res;

import java.util.Map;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.tpl.Engine;
import de.mhus.lib.config.IConfig;

public class TplRes extends AquaRes {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(TplRes.class);

	private String path;

	public void setConfig(IConfig config) {
		super.setConfig(config);
		path = config.getExtracted("path");
		if (!path.endsWith("/")) path = path + "/";
	}
	
	@Override
	public void process(AquaRequest request) throws Exception {
		log.t(path,request.getPath());
		Engine engine = Activator.getAqua().getTplEngine();
		Map<String, Object> attr = engine.createAttributes(request);
		engine.execute(path + request.getExtPath(), attr, request.getWriter());
	}

}
