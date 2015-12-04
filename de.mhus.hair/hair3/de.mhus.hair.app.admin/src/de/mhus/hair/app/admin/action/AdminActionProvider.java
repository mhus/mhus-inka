package de.mhus.hair.app.admin.action;

import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoList;

public class AdminActionProvider extends CaoActionProvider {

	public AdminActionProvider(MActivator activator) {
		list.add(new SelectOpenAttributesAction(activator,null));
		list.add(new SelectOpenListAction(activator,null));
	}
	
	@Override
	protected boolean canExecute(CaoList list,Object...initConfig) {
		return true;
	}

}
