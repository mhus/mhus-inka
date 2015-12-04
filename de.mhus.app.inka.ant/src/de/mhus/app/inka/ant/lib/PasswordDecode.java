package de.mhus.app.inka.ant.lib;

import org.apache.tools.ant.Task;

import de.mhus.lib.MPassword;

public class PasswordDecode extends Task {
	
	
	private String text;
	private String attribute;

	public void execute() {
		String decoded = MPassword.decode(text);
		if (attribute != null)
			getProject().setProperty(attribute, decoded);
		else
			getProject().setProperty(attribute, decoded);
	}
	
	public void setText(String in) {
		text = in;
	}
	
	public void setAttribute(String in) {
		attribute = in;
	}
	
}
