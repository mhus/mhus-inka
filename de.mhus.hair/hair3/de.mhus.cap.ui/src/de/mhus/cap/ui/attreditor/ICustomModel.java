package de.mhus.cap.ui.attreditor;

import de.mhus.lib.form.IFormDynamic;
import de.mhus.lib.form.MFormModel;

public interface ICustomModel {

	public MFormModel getConfigurationForm(IFormDynamic target);
	
}
