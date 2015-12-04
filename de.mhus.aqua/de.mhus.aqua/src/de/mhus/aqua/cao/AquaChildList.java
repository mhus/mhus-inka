package de.mhus.aqua.cao;

import de.mhus.aqua.aaa.UserRights;
import de.mhus.aqua.api.AquaSession;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;

public abstract class AquaChildList extends CaoList<AquaSession> {

	protected AquaElement parent;
	protected AquaSession session;
	protected String right = UserRights.READ;

	public AquaChildList(AquaElement parent) throws CaoException {
		super(parent.getConnection(),parent.getId());
		this.parent = parent;
	}

	public void setSession(AquaSession session) {
		this.session = session;
	}
	
	public void setUserRight(String in) {
		right = in;
	}

}
