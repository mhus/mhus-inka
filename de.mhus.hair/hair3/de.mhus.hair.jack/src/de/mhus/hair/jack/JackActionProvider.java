package de.mhus.hair.jack;

import de.mhus.hair.jack.action.AddDefinitionAction;
import de.mhus.hair.jack.action.AddPropertyAction;
import de.mhus.hair.jack.action.DefaultOpenAction;
import de.mhus.hair.jack.action.DeleteAction;
import de.mhus.hair.jack.action.DumpAction;
import de.mhus.hair.jack.action.ImportFsAction;
import de.mhus.hair.jack.action.NewNodeAction;
import de.mhus.hair.jack.action.OpenAction;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.util.SetTargetAction;
import de.mhus.lib.logging.MLog;

public class JackActionProvider extends CaoActionProvider {

	JackActionProvider(MActivator activator) {
		list.add(new NewNodeAction(activator, null));
		list.add(new DumpAction(activator, null));
		list.add(new DeleteAction(activator, null));
		list.add(new AddPropertyAction(activator, null));
		list.add(new AddDefinitionAction(activator, null));
		list.add(new OpenAction(activator, null));
		list.add(new DefaultOpenAction(activator, null));
		
		list.add(new SetTargetAction(activator,null)); // TODO put in default provider

		
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
