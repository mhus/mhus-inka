package de.mhus.app.inka.ant.dctm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.documentum.fc.client.IDfPersistentObject;

public class Dump extends Task {

	private String session;
	private DMConnection con;
	private String src;
	private String language;
	private String tar;
	private boolean append;
	private String encoding = Charset.defaultCharset().name();

	public void execute() throws BuildException {
		
		con = Connect.getSession(session);
		
		try {
			IDfPersistentObject obj = Connect.findObject(con,src,language);
			
			PrintStream ps = System.out;
			if (tar != null)
				ps = new PrintStream(new FileOutputStream(new File(tar), append), true, encoding);
			ps.print( obj.dump() );
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(session + ":" + e.getMessage(),e);
		}
	}

	public void setSession(String in) {
		session = in;
	}
	
	public void setSource(String in) {
		src = in;
	}
	
	public void setLanguage(String in) {
		language = in;
	}
	
	public void setTarget(String in) {
		tar = in;
	}

	public void setAppend(boolean in) {
		append = in;
	}
	
	public void setEncoding(String in) {
		encoding  = in;
	}
	
}
