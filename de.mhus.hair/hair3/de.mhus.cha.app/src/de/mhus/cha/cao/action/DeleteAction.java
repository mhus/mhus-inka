package de.mhus.cha.cao.action;

import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.form.MForm;

public class DeleteAction extends CaoAction<Access> {

	public DeleteAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public boolean canExecute(CaoList<Access> list, Object... initConfig) {
		for (CaoElement<Access> element : list.getElements()) {
			if (! (element instanceof ChaElement)) return false;
		}
		return true;
	}

	@Override
	public MForm createConfiguration(CaoList<Access> list, Object... initConfig)
			throws CaoException {
		return new DeleteOperation(list);
	}

	@Override
	public CaoOperation execute(CaoList<Access> list, Object configuration)
			throws CaoException {
		((DeleteOperation)configuration).setTargets(list);
		return (DeleteOperation)configuration;
	}

	@Override
	public String getName() {
		return CaoDriver.ACTION_DELETE;
	}

}
