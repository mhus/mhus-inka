package de.mhus.cap.core;

import de.mhus.lib.cao.CaoList;

public interface ICapEventListener {

	void hotSelected(CaoList selection);

	void propertyChanged(String property, Object value);

	void connectionAdd(ConnectionDefinition newConnection);

	void connectionRemoved(ConnectionDefinition oldConnection);

	void connectionChanged(ConnectionDefinition con);
	
}
