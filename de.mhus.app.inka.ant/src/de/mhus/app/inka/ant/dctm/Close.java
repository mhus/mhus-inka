package de.mhus.app.inka.ant.dctm;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Close extends Task {

	private String session;


	public void execute() throws BuildException {
		
		DMConnection con = Connect.getSession(session);
		con.disconnect();
		
	}
	

	public void setSession(String in) {
		session = in;
	}
	
}
