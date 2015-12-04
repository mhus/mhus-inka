package de.mhus.aqua.cao;

import de.mhus.aqua.api.AquaSession;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;

public abstract class AquaDriver extends CaoDriver<AquaSession> {

	public static final String APP_AAA = "app_aaa";

	@Override
	public CaoForm createConfiguration() {
		return new AquaConfig();
	}

}
