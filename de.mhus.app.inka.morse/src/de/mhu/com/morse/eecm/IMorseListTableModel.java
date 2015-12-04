package de.mhu.com.morse.eecm;

import de.mhu.lib.eecm.model.IListTableModel;
import de.mhu.lib.log.AL;

public interface IMorseListTableModel extends IListTableModel {

	public void setConnection(MorseConnection morseConnection);
	
}
