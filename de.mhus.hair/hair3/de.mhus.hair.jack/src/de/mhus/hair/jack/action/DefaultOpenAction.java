package de.mhus.hair.jack.action;

import de.mhus.cap.core.CapCore;
import de.mhus.hair.jack.JackApplication;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoActionList;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;

public class DefaultOpenAction extends CaoAction {

	public DefaultOpenAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return CapCore.ACTION_OPEN_WITH_SELECT;
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {		
		
		return null;
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		if (list==null || list.size() != 1 || ! (list.getApplication() instanceof JackApplication) ) return false;
		
		return true;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration)
			throws CaoException {
		
		CaoActionList actionList = new CaoActionList();
		CapCore.getInstance().getFactory().fillWithActions(null,list, actionList);
		
		JackElement element = (JackElement)list.getElements().next();
		CaoAction action = null;
		try {
			if (element.getNode().getPrimaryNodeType().getPrimaryItemName().equals("jcr:content")) {
				action = actionList.getAction("select.open");
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		if (action == null) {
			try {
				if (element.getNode().getNodes().hasNext()) {
					action = actionList.getAction("select.open.list");
				} else {
					action = actionList.getAction("select.open.attributes");					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (action != null && action.canExecute(list)) {
			MForm config = action.createConfiguration(list);
			return action.execute(list, config);
		}
		return null;
	}

}
