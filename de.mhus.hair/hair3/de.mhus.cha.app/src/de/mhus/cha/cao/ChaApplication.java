package de.mhus.cha.cao;

import java.io.File;

import de.mhus.cap.core.Access;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.ObjectNotFoundException;

public class ChaApplication extends CaoApplication<Access> {

	private String path;
	
	public ChaApplication(ChaConnection fsConnection, String path) throws CaoException {
		super(fsConnection);
		this.path = path;
	}
	
	@Override
	public CaoList<Access> queryList(String name, Access access, String... attributes) throws CaoException {
		if (name.equals(CaoDriver.LIST_DEFAULT)) {
			if (! ((ChaConnection)getConnection()).getIdPath(attributes[0]).startsWith(path))
				throw new ObjectNotFoundException(attributes[0]);
			return new ChaElement((ChaConnection)getConnection(), attributes[0]).getChildren(CapCore.getInstance().getAccess());
		}
		return null;
	}

	@Override
	public CaoElement<Access> queryTree(String name, Access access, String... attributes) throws CaoException {
		if (name.equals(CaoDriver.TREE_DEFAULT)) {
			return new ChaElement((ChaConnection)getConnection(), new File(path));
		}
		return null;
	}

}
