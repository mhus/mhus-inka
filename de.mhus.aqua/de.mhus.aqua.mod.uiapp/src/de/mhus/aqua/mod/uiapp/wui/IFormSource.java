package de.mhus.aqua.mod.uiapp.wui;

import de.mhus.lib.form.MForm;
import de.mhus.lib.form.MFormModel;

public interface IFormSource extends ISource {

	public MForm getForm();
	public MFormModel getModel();
	
}
