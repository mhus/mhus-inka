package de.mhus.hair.app.admin;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ICapApplication;
import de.mhus.cap.core.InitializeCaoFromOsgi;
import de.mhus.hair.app.admin.action.AdminActionProvider;
import de.mhus.lib.MActivator;

public class HairApplication implements ICapApplication {

	private static final String PROPERTY_CAP_CONFIG_DIR = "hair.config.dir";
	private static final String CAP_CONFIG_DEFAULT_PATH = "./config";

	public HairApplication() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doStart() {
		InitializeCaoFromOsgi.init();
		CapCore.getInstance().getFactory().registerActionProvider(new AdminActionProvider(new MActivator(this.getClass().getClassLoader())));
	}

//	@Override
//	public File getConfigurationBaseDir() {
//		return new File(System.getProperty(PROPERTY_CAP_CONFIG_DIR,CAP_CONFIG_DEFAULT_PATH));
//	}

}
