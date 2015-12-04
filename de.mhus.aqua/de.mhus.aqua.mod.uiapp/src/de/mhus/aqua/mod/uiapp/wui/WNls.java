package de.mhus.aqua.mod.uiapp.wui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.AquaSession;
import de.mhus.aqua.mod.AquaModule;
import de.mhus.lib.MActivator;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.util.MNls;

public class WNls extends MNls {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(WNls.class);

	private IConfig cnls;
	private String path;
	private AquaSession session;
	private String name;
	private String locale;

	private MActivator activator;

	public WNls(AquaRequest request,AquaModule module, String name) {
		cnls = module.getConfiguration().getConfig("localisation");
		path = cnls.getExtracted("path");
		this.name = name;
		this.session = request.getSession();
		activator = new MActivator() {

			public InputStream getResource(String name) throws Exception {
				File f = new File(path,name);
				if (!f.exists()) return null;
				log.t("load nls", name);
				return new FileInputStream(f);
			}
			
		};
		find("");
	}
		
	@Override
	public String getDefaultLocale() {
		return session.getDefaultLocale();
	}
	
	@Override
	public String find(String in, Map<String, Object> attributes) {
		
		if (locale==null || !locale.equals(session.getLocale())) {
			locale = session.getLocale();
			try {
				properties.clear();
				load(activator,null,name,locale);
			} catch (Exception e) {
				log.d(locale,e);
			}
		}
		
		return super.find(in,attributes);
		
	}
	
	public void initTpl(Map<String, Object> attr) {
		attr.put("nls", properties);
	}
	
}
