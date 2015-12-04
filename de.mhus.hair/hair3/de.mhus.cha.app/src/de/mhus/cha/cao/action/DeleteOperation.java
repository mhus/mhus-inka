package de.mhus.cha.cao.action;

import java.io.File;
import java.io.IOException;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMonitor;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.ChaConnection;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='cha_delete' title='Delete'")
public class DeleteOperation extends CaoOperation implements MForm {

	private CaoList<Access> targets;
	private boolean confirmed;
	private CaoElement<Access> currentTarget;
	private ChaConnection connection;
	
	public DeleteOperation(CaoList<Access> list) {
		setTargets(list);
	}

	public void setTargets(CaoList<Access> list) {
		targets = list;
	}

	@Override
	public void dispose() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		if (!confirmed) return;
			
		//count
		monitor.beginTask("count", CaoMonitor.UNKNOWN);
		int cnt = 0;
		for (CaoElement<Access> element : targets.getElements()) {
			cnt = count( ((ChaElement)element).getFile(), cnt );
		}
		
		//delete
		monitor.beginTask("copy", cnt);
		cnt = 0;
		for (CaoElement<Access> element : targets.getElements()) {
			connection = (ChaConnection)currentTarget.getConnection();
			currentTarget = element;
			cnt = delete( ((ChaElement)element).getFile(), cnt );
		}
		
	}

	private int delete(File file, int cnt) {
		if (monitor.isCanceled()) return cnt;

		if (!file.isDirectory()) {
			String id = null;
			String parentId = null;
			try {
				id = file.getCanonicalPath();
				parentId = file.getParentFile().getCanonicalPath();
			} catch (IOException e) {
				monitor.log().warn(e);
			}
			if ( file.delete() ) {
				currentTarget.getConnection().fireElementUnlink(parentId, id);
				currentTarget.getConnection().fireElementDeleted(id);
			} else
				monitor.log().error("Can't delete: " + id);
			cnt++;
			return cnt;
		}
		
		monitor.subTask(file.getName());
		
		for ( File sub : file.listFiles()) {
			if (sub.isDirectory()) {
				if (sub.getName().equals(".") || sub.getName().equals("..") ) {
					
				} else {
					cnt = delete(sub,cnt);
				}
			} else
			if (sub.isFile()) {
				cnt = delete(sub,cnt);
			}
		}
		
		String id = null;
		String parentId = null;
		boolean isDir = file.isDirectory();
		try {
			id = file.getCanonicalPath();
			parentId = file.getParentFile().getName();
		} catch (IOException e) {
			monitor.log().warn(e);
		}
		if ( file.delete() ) {
			if (isDir) {
				currentTarget.getConnection().fireElementUnlink(parentId, id);
				currentTarget.getConnection().fireElementDeleted(id);
				connection.changeIdPath(id, null);
			}
		} else
			monitor.log().error("Can't delete: " + id);
		return cnt;
	}

	private int count(File file, int cnt) {
		
		if (monitor.isCanceled()) return cnt;

		if ( file.isDirectory() || file.isFile() ) cnt++;
		if (!file.isDirectory()) return cnt;
		for ( File sub : file.listFiles()) {
			if (sub.isDirectory()) {
				if (sub.getName().equals(".") || sub.getName().equals("..") ) {
					
				} else {
					cnt = count(sub,cnt);
				}
			} else
			if (sub.isFile()) {
				cnt++;
			}
		}
		return cnt;
	}

	@Override
	public void initialize() throws CaoException {
	}

	
	@FormSortId(1)
	@FormElement("checkbox title='Confirm delete operation' nls='confirm' value='0'")
	public void setConfirmed(boolean in) {
		confirmed = in;
	}
}
