package de.mhus.app.inka.ant.dctm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.documentum.fc.client.IDfSysObject;

import de.mhus.lib.MFile;

public class Overwrite extends Task {

	private String session;
	private DMConnection con;
	private String tar;
	private String format;
	private String language;
	private String content;

	public void execute() throws BuildException {
		
		con = Connect.getSession(session);
		
		try {
			IDfSysObject obj = (IDfSysObject) Connect.findObject(con,tar,language);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(content);
			MFile.copyFile(fis, os);
			fis.close();
			
			if (format != null)
				obj.setContentEx(os,format,0);
			else
				obj.setContent(os);
			
			obj.save();
			
		} catch (Exception e) {
			throw new BuildException(session + ":" + e.getMessage(),e);
		}
	}

	public void setSession(String in) {
		session = in;
	}
	
	public void setTarget(String in) {
		tar = in;
	}
	
	public void setLanguage(String in) {
		language = in;
	}
	
	public void setFormat(String in) {
		format = in;
	}
	
	public void setContent(String in) {
		content = in;
	}

	
}
