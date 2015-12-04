package de.mhus.cha.cao.action;

import de.mhus.cap.core.Access;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.MActivator;
import de.mhus.lib.form.MForm;

public class CreateFolderAction extends CaoAction<Access> {

	public CreateFolderAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public boolean canExecute(CaoList<Access> list,Object...initConfig) {
		return list.size() == 1 && list.getElements().next().isNode();
	}

	@Override
	public MForm createConfiguration(CaoList<Access> list,Object...initConfig) {
		return new CreateFolderOperation(list);
	}

	@Override
	public CaoOperation execute(CaoList<Access> list, Object configuration) throws CaoException {
		((CreateFolderOperation)configuration).setTarget(list.getElements().next());
		return (CreateFolderOperation)configuration;
	}

	@Override
	public String getName() {
		return CaoDriver.ACTION_CREATE_FOLDER;
	}

}
