package de.mhus.cha.app;

import java.io.File;

import de.mhus.lib.cao.CaoFactory;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ICapApplication;
import de.mhus.cap.core.InitializeCaoFromOsgi;
import de.mhus.cha.action.AdminActionProvider;
import de.mhus.cha.cao.ChaDriver;
import de.mhus.lib.MActivator;

public class ChaApplication implements ICapApplication {

	private static final String PROPERTY_CAP_CONFIG_DIR = "cha.config.dir";
	private static final String CAP_CONFIG_DEFAULT_PATH = "./config";

	public ChaApplication() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doStart() {
		InitializeCaoFromOsgi.init();
		CapCore.getInstance().getFactory().registerActionProvider(new AdminActionProvider(new MActivator(this.getClass().getClassLoader())));
	}

	@Override
	public File getConfigurationBaseDir() {
		return new File(System.getProperty(PROPERTY_CAP_CONFIG_DIR,CAP_CONFIG_DEFAULT_PATH));
	}

}
