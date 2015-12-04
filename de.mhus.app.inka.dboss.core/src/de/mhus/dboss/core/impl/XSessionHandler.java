package de.mhus.dboss.core.impl;

import de.mhus.lib.MEventHandler;

public class XSessionHandler extends MEventHandler<Session> {

	public void close() {
		for (Session session : getListeners())
			session.close();
	}
	
}
