package de.mhus.cao.model.fs;

import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.config.IConfig;

public class IoDriver extends CaoDriver {

	private IoMetadata defaultMetadata;
//	private FsActionProvider actionProvider;
//	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
//		.getLog(IoDriver.class);

	public IoDriver() {
		defaultMetadata = new IoMetadata(this);
		
//		actionProvider = new FsActionProvider(new MActivator(FsDriver.class.getClassLoader()));
//		CapCore.getInstance().getFactory().registerActionProvider(actionProvider);
		
		
	}
	
	@Override
	public CaoConnection createConnection(String url,IConfig config) throws CaoException {
		IoConfiguration form = new IoConfiguration();
		form.setConfig(config);
		form.fromUrl(url);
		IoConnection con = new IoConnection(this,form);
		return initializeConnection( con );
	}

	public CaoMetadata getDefaultMetadata() {
		return defaultMetadata;
	}

	@Override
	public CaoForm createConfiguration() {
		return new IoConfiguration();
	}

	@Override
	protected void initDefaultApplications() {
		registerApplication(CaoDriver.APP_CONTENT, new IoApplicationFactory());
	}
	
}
