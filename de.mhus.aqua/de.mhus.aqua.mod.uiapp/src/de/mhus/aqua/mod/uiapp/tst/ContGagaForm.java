package de.mhus.aqua.mod.uiapp.tst;

import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='form1' title='Form Nr. 1' nls='nls_form_1'")
public class ContGagaForm implements MForm {

	private IConfig config;
	private String password;

	ContGagaForm(IConfig config) {
		this.config = config;
	}
	
	@FormSortId(1)
	@FormElement("input title='Text'")
	public void setText(String in) {
		System.out.println("set text: " + in); //XXX
		try {
			config.setString("text", in);
		} catch (MException e) {
		}
	}
	
	public String getText() {
		return config.getString("text", "");
	}

	@FormSortId(2)
	@FormElement("password title='Password' nls='password'")
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	
}
