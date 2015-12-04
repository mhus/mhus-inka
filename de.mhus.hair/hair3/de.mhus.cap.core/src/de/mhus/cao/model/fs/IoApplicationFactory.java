package de.mhus.cao.model.fs;

import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoApplicationFactory;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.config.IConfig;

public class IoApplicationFactory extends CaoApplicationFactory {

	@Override
	protected CaoApplication create(CaoConnection con, IConfig config) throws CaoException {
		return new IoApplication((IoConnection) con, config);
	}

	@Override
	public CaoForm createConfiguration() {
		return null;
	}

}
