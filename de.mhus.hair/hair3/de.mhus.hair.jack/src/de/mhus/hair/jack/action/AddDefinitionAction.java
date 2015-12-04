package de.mhus.hair.jack.action;

import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.logging.MLog;

public class AddDefinitionAction  extends CaoAction {

	public AddDefinitionAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return "select.add_definition";
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {
		return new AddDefinitionOperation();
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		try {
			for (CaoElement source : list.getElements()) {
				if ( ! (source instanceof JackElement))
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
		((AddDefinitionOperation)configuration).setSource(list);
		return (AddDefinitionOperation)configuration;
	}

}
