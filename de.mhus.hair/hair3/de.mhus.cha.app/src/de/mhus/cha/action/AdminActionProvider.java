package de.mhus.cha.action;

import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.MActivator;

public class AdminActionProvider extends CaoActionProvider {

	public AdminActionProvider(MActivator activator) {
		list.add(new SelectOpenListAction(activator,null));
		list.add(new SelectOpenFormAction(activator,null));
		list.add(new SelectOpenAttributesAction(activator,null));
	}
	
	@Override
	protected boolean canExecute(CaoList list,Object...initConfig) {
		return true;
	}

}
