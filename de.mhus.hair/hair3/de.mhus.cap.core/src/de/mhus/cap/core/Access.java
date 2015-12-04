package de.mhus.cap.core;

import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoPrincipal;

/**
 * It's a dummy - maybe delete ....
 * @author mikehummel
 *
 */
public class Access implements CaoAccess {

	@Override
	public CaoPrincipal getCurrentUser() {
		return null;
	}

}
