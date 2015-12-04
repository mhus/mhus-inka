package de.mhus.app.inka.ant.lib;

import org.apache.tools.ant.Task;

import de.mhus.lib.MString;
import de.mhus.lib.io.TextReader;

public class ConsoleInput extends Task {

	private String prompt;
	private String attribute;
	private String def;
	private boolean force = false;
	private boolean lower = false;

	public void execute() {
		if (!force && getProject().getProperties().containsKey(attribute)) {
			getProject().log("Attribute is already set, ignore");
			return;
		}
		String line = "";
		System.out.print(prompt);
		TextReader reader = new TextReader(System.in);
		line = reader.readLine();

		if (MString.isEmpty(line) && !MString.isEmpty(def))
			line = def;
		if (lower && line != null)
			line = line.toLowerCase();
		getProject().setProperty(attribute, line);
	}
	
	public void setPrompt(String in) {
		prompt = in;
	}
	
	public void setAttribute(String in) {
		attribute = in;
	}
	
	public void setDefault(String in) {
		def = in;
	}

	public void setForce(boolean in) {
		force  = in;
	}
	
	public void setLowerCase(boolean in) {
		lower = in;
	}
	
}
