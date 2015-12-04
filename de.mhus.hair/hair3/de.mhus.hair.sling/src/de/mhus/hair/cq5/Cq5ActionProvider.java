package de.mhus.hair.cq5;

import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.logging.MLog;

public class Cq5ActionProvider extends CaoActionProvider {

	Cq5ActionProvider(MActivator activator) {
		list.add(new ActivateAction(activator, null));
	}
	
	@Override
	protected boolean canExecute(CaoList list, Object... initConfig) {
		if (list==null || list.size() == 0) return false;
		try {
			for (Object element : list.getElements())
				if (!(element instanceof JackElement)) return false;
		} catch (CaoException e) {
			MLog.e(e);
			return false;
		}
		return true;
	}

}
