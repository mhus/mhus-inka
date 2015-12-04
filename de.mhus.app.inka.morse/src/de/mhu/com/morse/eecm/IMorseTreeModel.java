package de.mhu.com.morse.eecm;

import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.log.AL;

public interface IMorseTreeModel extends ITreeModel {

	public void setConnection(MorseConnection morseConnection);
	
}
