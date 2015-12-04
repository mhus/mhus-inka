package de.mhus.hair.sling;

import de.mhus.hair.jack.JackConnection;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoApplicationFactory;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.config.IConfig;

public class SlingApplicationProvider extends CaoApplicationFactory {

	@Override
	protected CaoApplication create(CaoConnection con, IConfig config2)
			throws CaoException {
		return new SlingApplication((JackConnection) con, config2);
	}

	@Override
	public CaoForm createConfiguration() {
		return null;
	}
	
}