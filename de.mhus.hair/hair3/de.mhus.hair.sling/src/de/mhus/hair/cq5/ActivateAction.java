package de.mhus.hair.cq5;

import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.logging.MLog;

public class ActivateAction extends CaoAction {

	public ActivateAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return "extras.activate";
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {
		return new ActivateOperation();
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		try {
			for (CaoElement source : list.getElements()) {
				if ( ! (source.getApplication() instanceof Cq5Application))
					return false;
			}
		} catch (CaoException e) {
			MLog.e(e);
			return false;
		}
		return true;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration)
			throws CaoException {
		((ActivateOperation)configuration).setSource(list);
		return (ActivateOperation)configuration;
	}

}
