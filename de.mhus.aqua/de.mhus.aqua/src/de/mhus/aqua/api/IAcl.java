package de.mhus.aqua.api;

import de.mhus.aqua.aaa.User;

public interface IAcl {

	boolean hasRight(AquaSession session, String right);

	boolean hasRight(User user, String right);

	String getId();

}
