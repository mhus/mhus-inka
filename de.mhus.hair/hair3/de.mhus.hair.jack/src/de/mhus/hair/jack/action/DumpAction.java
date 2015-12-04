package de.mhus.hair.jack.action;

import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;

public class DumpAction extends CaoAction {

	public DumpAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return "select.dump";
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {
		return new DumpOperation();
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		try {
			CaoListIterator elements = list.getElements();
			elements.next();
			if (elements.hasNext()) return false;
			return true;
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration)
			throws CaoException {
		((DumpOperation)configuration).setSources(list);
		return (CaoOperation) configuration;
	}

}
