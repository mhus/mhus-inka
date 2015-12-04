package de.mhus.aqua.caosql;

import java.io.IOException;
import java.util.HashMap;

import de.mhus.aqua.Activator;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaContentApplication;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.jmx.JmxMap;
import de.mhus.lib.logging.Log;

public class ASContentApplication extends AquaContentApplication {
	
	protected ASContentApplication(AquaConnection connection) throws CaoException {
		super(connection);
	}

	@Override
	protected AquaElement createRootElement() throws IOException, Exception {
		return new ASNode((AquaConnection) getConnection());
	}

}
