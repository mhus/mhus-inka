package de.mhus.app.inka.ant.lib;

import org.apache.tools.ant.Task;

import de.mhus.lib.MPassword;

public class PasswordEncode extends Task {
	
	
	private String text;
	private String attribute;

	public void execute() {
		String encoded = MPassword.encode(text);
		if (attribute != null)
			getProject().setProperty(attribute, encoded);
		else
			log(encoded);
	}
	
	public void setText(String in) {
		text = in;
	}
	
	public void setAttribute(String in) {
		attribute = in;
	}
	
}
