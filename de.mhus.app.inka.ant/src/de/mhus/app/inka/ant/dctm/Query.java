package de.mhus.app.inka.ant.dctm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.Tokenizer;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;

import de.mhus.lib.MFile;
import de.mhus.lib.MString;

public class Query extends Task {

	private String session;
	private String query;
	private Vector<FileSet> src = new Vector<FileSet>();
	private DMConnection con;
	private boolean print;
	private String attribute;
	private String delimiter = ",";

	public void execute() throws BuildException {
		
		con = Connect.getSession(session);
		
		try {
			if (src.size() != 0) {
				for (FileSet set : src) {
					DirectoryScanner ds = set.getDirectoryScanner(getProject());
					String[] includedFiles = ds.getIncludedFiles();
					for (String filePath : includedFiles) {
						executeFile(filePath);
					}
				}
			} else {
				executeQuery(query);
			}
		} catch (Exception e) {
			throw new BuildException(session + ":" + e.getMessage(),e);
		}
	}
	
	private void executeQuery(String q) throws DfException {
		log("Execute: " + q);
		IDfQuery q2 = con.createQuery(q);
		IDfCollection res = q2.execute(con.getSession(), IDfQuery.READ_QUERY);
		if (print || attribute != null) {
			boolean first = true;
			while (res.next()) {
				
				if (first) {
					if (attribute!=null) {
						for (int i = 0; i < res.getAttrCount(); i++ ) {
							IDfAttr attr = res.getAttr(i);
							if (!attr.isRepeating()) {
								getProject().setProperty(attribute + "." + attr.getName(), res.getString(attr.getName()));
								first = false;
							} else {
								for (int j = 0; j < res.getValueCount(attr.getName() ); j++) {
									if (first)
										getProject().setProperty(attribute + "." + attr.getName(), res.getRepeatingString(attr.getName(),j));
									else {
										getProject().setProperty(attribute + "." + attr.getName(), getProject().getProperty(attribute + "." + attr.getName()) + delimiter + res.getRepeatingString(attr.getName(),j));
										first = false;
									}
								}
							}
						}
					} else
						first = false;
				} else
					if (print)
						log("-------------------------------------------");
					else
					if (attribute!=null) {
						for (int i = 0; i < res.getAttrCount(); i++ ) {
							IDfAttr attr = res.getAttr(i);
							if (!attr.isRepeating()) {
								getProject().setProperty(attribute + "." + attr.getName(), getProject().getProperty(attribute + "." + attr.getName()) + delimiter + res.getString(attr.getName()));
							} else {
								for (int j = 0; j < res.getValueCount(attr.getName() ); j++) {
									getProject().setProperty(attribute + "." + attr.getName(), getProject().getProperty(attribute + "." + attr.getName()) + delimiter + res.getRepeatingString(attr.getName(),j));
								}
							}
						}
					}
				if (print)
					for (int i = 0; i < res.getAttrCount(); i++ ) {
						IDfAttr attr = res.getAttr(i);
						if (attr.isRepeating()) {
							log(attr.getName() + "[" + res.getValueCount(attr.getName()) + "]");
							for (int j = 0; j < res.getValueCount(attr.getName() ); j++) {
								log("  [" + j + "]: " + res.getRepeatingString(attr.getName(), j));
							}
						} else {
							log(attr.getName() + ": " + res.getString(attr.getName()));
						}
					}
			}
		}
		
		res.close();
	}

	private void executeFile(String filePath) throws FileNotFoundException, DfException {
		File f = new File(filePath);
		String content = MFile.readFile(f);
		executeQuery(content);
	}

	public void setSession(String in) {
		session = in;
	}
	
	public void setQuery(String in) {
		query = in;
	}
	
	public final void addSrcfiles(FileSet fileset) {
        src.add(fileset);
    }
	
	public void setPrint(boolean in) {
		print = in;
	}
	
	public void setAttribute(String in) {
		attribute = in;
	}
	
}
