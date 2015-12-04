package de.mhus.aqua.mod.uiapp.tst;


import java.util.Date;

import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='form1' title='Form Nr. 1' nls='nls_form_1'")
public class TestForm implements MForm {

	private String input = "Input";
	private String password = "Password";
	private Date   date = new Date();
	
	@FormSortId(1)
	@FormElement("input title='Input' nls='input'")
	public void setInput(String input) {
		this.input = input;
	}
	
	public String getInput() {
		return input;
	}

	@FormSortId(2)
	@FormElement("input title='Input2' nls='input2'")
	public void setInput2(String input) {
		this.input = input;
	}
	
	public String getInput2() {
		return input;
	}
	
	@FormSortId(3)
	@FormElement("password title='Password' nls='password'")
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	
	@FormSortId(4)
	@FormElement("date title='Date' nls='date'")
	public void setDate(Date in) {
		date = in;
	}
	
	public Date getDate() {
		return date;
	}
}
