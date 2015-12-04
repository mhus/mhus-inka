package de.mhus.inka.constgenerator;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class CGAnt extends Task {

	private Vector<FileSet> filesets = new Vector<FileSet>();
    private File dest;
    private boolean debug;

	@Override
	public void execute() {
		
		Crawler crawler = new Crawler(null);
		crawler.setDebug(debug);
		
		for (FileSet fs : filesets) {
			
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			File fromDir = fs.getDir(getProject());
			
			for (String res : ds.getIncludedFiles()) {
				try {
					File src = new File(fromDir, res);
					if (src.isFile()) {
						File dst = null;
						if (dest == null)
							dst = src.getParentFile();
						else {
							dst = new File(dest, res ).getParentFile();
						}
						crawler.parse(src, dst);
					}
				} catch (Throwable e) {
					log(res);
					e.printStackTrace();
				}
			}
		}
	}
	
	public void setDestination(String file) {
        this.dest = new File( file );
    }
	
	public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
}
