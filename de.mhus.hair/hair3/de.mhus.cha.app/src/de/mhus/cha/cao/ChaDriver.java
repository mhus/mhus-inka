package de.mhus.cha.cao;

import de.mhus.cap.core.Access;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoFactory;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.MActivator;

public class ChaDriver extends CaoDriver<Access> {

	private ChaMetadata defaultMetadata;
	private ChaActionProvider actionProvider;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
		.getLog(ChaDriver.class);

	public ChaDriver() {
		defaultMetadata = new ChaMetadata(this);
		
		actionProvider = new ChaActionProvider(new MActivator(ChaDriver.class.getClassLoader()));
		CapCore.getInstance().getFactory().registerActionProvider(actionProvider);
	}
	
	@Override
	public CaoConnection<Access> createConnection(String url) throws CaoException {
		return new ChaConnection(this,url);
	}

	public CaoMetadata getDefaultMetadata() {
		return defaultMetadata;
	}

	@Override
	public CaoForm createConfiguration() {
		return new ChaConfiguration();
	}
	
}
