package de.mhus.cha.cao.action;

import java.io.File;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMonitor;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.ChaConnection;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.MFile;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='cha_copy_to_folder' title='Copy'")
public class CopyToOperation extends CaoOperation implements MForm {

	private CaoList<Access> sources;
	private ChaElement target;
	private ChaConnection connection;

	public CopyToOperation(ChaElement ChaElement) {
		target = ChaElement;
	}

	@Override
	public void dispose() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		
		connection = (ChaConnection)target.getConnection();
		//collect all affected entries
		monitor.beginTask("count", CaoMonitor.UNKNOWN);
		int cnt = 0;
		for (CaoElement<Access> element : sources.getElements()) {
			cnt = count( ((ChaElement)element).getFile(), cnt );
		}
		
		monitor.beginTask("copy", cnt);
		cnt = 0;
		for (CaoElement<Access> element : sources.getElements()) {
			cnt = copy( target.getFile(), ((ChaElement)element).getFile(), cnt );
			
		}		
		
	}

	private int copy(File target, File file, int cnt) {

		// validate action
		if (monitor.isCanceled()) return cnt;
		if ( !file.isDirectory()) return cnt; // for secure
		
		// new path
		
		File newTarget = null;
		
		cnt++;
		monitor.worked(cnt);
		newTarget = new File(target,connection.createUID());
		monitor.log().debug("Create Dir: " + newTarget.getAbsolutePath());
		monitor.subTask(file.getAbsolutePath());
		
		// validate path
		if ( newTarget.exists() ) {
			monitor.log().warn("Folder already exists: " + newTarget.getAbsolutePath());
			return cnt;
		}
		
		// create
		if ( ! newTarget.mkdir() ) {
			newTarget = null;
			monitor.log().warn("Can't create folder: " + target.getAbsolutePath() + "/" + file.getName());
			return cnt;
		}
		
		// set id
		connection.addIdPath(newTarget.getName(), newTarget.getAbsolutePath());
		
		// events
		connection.fireElementCreated(newTarget.getName());
		connection.fireElementLink(target.getName(), newTarget.getName());
		
		// copy files
		for ( File sub : file.listFiles()) {
			if (sub.isFile()) {
				monitor.log().debug("Copy File: " + file.getAbsolutePath());
				
				File targetFile = new File(target,file.getName());
				if (targetFile.exists()) {
					monitor.log().warn("Can't overwrite file: " + file.getAbsolutePath());				
				} else
				if ( !MFile.copyFile(file, targetFile) ) {
					monitor.log().warn("Can't copy file: " + file.getAbsolutePath());
				}
				
			}
		}
		
		// copy sub folders
		for ( File sub : file.listFiles(connection.getDefaultFileFilter())) {
			cnt = copy(newTarget, sub,cnt);
		}

		return cnt;
	}

	private int count(File file, int cnt) {
		
		if (monitor.isCanceled()) return cnt;

		if ( file.isDirectory() ) cnt++;
		if (!file.isDirectory()) return cnt; // for secure
		
		for ( File sub : file.listFiles(connection.getDefaultFileFilter())) {
			cnt = count(sub,cnt);
		}
		return cnt;
	}

	@Override
	public void initialize() throws CaoException {
	}

	public void setSources(CaoList<Access> list) {
		sources = list;
	}
	
}
