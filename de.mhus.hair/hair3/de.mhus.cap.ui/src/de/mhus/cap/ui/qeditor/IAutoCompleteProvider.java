package de.mhus.cap.ui.qeditor;

import org.eclipse.swt.widgets.Text;

public interface IAutoCompleteProvider {

	String[] getValues(Text owner);

	void doAutoCompleate(Text owner, String selection);
	
}
