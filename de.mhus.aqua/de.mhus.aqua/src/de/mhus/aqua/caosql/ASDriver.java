package de.mhus.aqua.caosql;

import de.mhus.aqua.cao.AquaConfig;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaDriver;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;

public class ASDriver extends AquaDriver {

	@Override
	public CaoConnection createConnection(String url) throws CaoException {
		return new ASConnection(this,url);
	}

}
