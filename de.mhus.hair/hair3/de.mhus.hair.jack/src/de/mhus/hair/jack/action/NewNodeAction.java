package de.mhus.hair.jack.action;

import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;

public class NewNodeAction extends CaoAction {

	public NewNodeAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return "select.new_node";
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {
		return new NewNodeOperation(list);
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		try {
		
			CaoListIterator elements = list.getElements();
			JackElement parent = (JackElement) elements.next();
			if (elements.hasNext()) return false;
			return true;
			
		} catch (Throwable t) {
			
		}
		return false;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration)
			throws CaoException {
		((NewNodeOperation)configuration).setSources(list);
		return (CaoOperation) configuration;
	}

}
