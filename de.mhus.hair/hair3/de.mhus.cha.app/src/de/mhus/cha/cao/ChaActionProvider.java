package de.mhus.cha.cao;

import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.util.SetTargetAction;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.action.CopyToAction;
import de.mhus.cha.cao.action.CreateFolderAction;
import de.mhus.cha.cao.action.DeleteAction;
import de.mhus.cha.cao.action.MoveToAction;
import de.mhus.lib.MActivator;


public class ChaActionProvider extends CaoActionProvider {

	ChaActionProvider(MActivator activator) {
		list.add(new CreateFolderAction(activator,null));
		list.add(new CopyToAction(activator,null));
		list.add(new MoveToAction(activator,null));
		list.add(new DeleteAction(activator,null));

		list.add(new SetTargetAction(activator,null)); // TODO put in default provider
	}
	
	@Override
	protected boolean canExecute(CaoList list,Object...initConfig) {
		if (list==null || list.size() == 0) return false;
		for (Object element : list.getElements())
			if (!(element instanceof ChaElement)) return false;
		return true;
	}

}
