package de.mhus.aqua.core;

import de.mhus.aqua.api.AquaSession;

public class AquaRootSession extends AquaSession {

	public AquaRootSession() throws Exception {
		super();
		setAdminActive(true);
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
	}

	@Override
	protected void cleanSession() {
	}

}
