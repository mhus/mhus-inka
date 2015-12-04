package de.mhus.cap.core.dnd;

import de.mhus.cap.core.dnd.CapDropListener.LOCATION;
import de.mhus.cap.core.dnd.CapDropListener.OPERATION;
import de.mhus.lib.cao.CaoElement;

public interface ICaoExchange {

	CaoElement getElement();

	boolean doDrop(LOCATION loc, OPERATION oper, ICaoExchange[] providers);

}
