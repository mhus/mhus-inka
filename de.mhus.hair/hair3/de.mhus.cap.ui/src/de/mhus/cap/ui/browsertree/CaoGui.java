package de.mhus.cap.ui.browsertree;


import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.action.ActionFilter;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoActionList;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.Log;

public class CaoGui {

	private static Log log = Log.getLog(CaoGui.class);
//	
//	public CaoGui(String service,String url) {
//		try {
//			CaoConnection con = (CaoConnection) CapCore.getInstance().getFactory().createConnection(service,url);
//			con.getContext().put(CapCore.CONTEXT_GUI_CONFIG, this);
//			content = con.getApplication(CaoDriver.APP_CONTENT);
//			filter = new ActionFilter(CapCore.getInstance().getConfiguration().getConfig("filter").getConfigBundle("select" ));
//			
//		} catch (CaoException he) {
//			he.printStackTrace(); //TODO do something ....
//		}
//	}


	public static void elementSelected(CaoList outList) {
		CaoActionList actionList = new CaoActionList();
		CapCore.getInstance().getFactory().fillWithActions(null,outList, actionList);

		CaoAction action = null;
		IConfig[] filterConfig = outList.getApplication().getConfig().getConfig("filter").getConfigBundle("open");
		if (filterConfig != null) {
			ActionFilter filter = new ActionFilter(filterConfig);
			action = filter.getPreferredAction(actionList);
		}
		
		if (action == null) {
			filterConfig = outList.getApplication().getConfig().getConfig("filter").getConfigBundle("select");
			ActionFilter filter = new ActionFilter(filterConfig);
			action = filter.getPreferredAction(actionList);
		}
		
		if (action == null) return;
		
		try {
			CapCore.getInstance().executeAction(action,outList);
		} catch (Exception e) {
			log.warn(e);
		}
	}

	public static void elementSelectedMenu(CaoList outList, IMenuManager manager) {

		ActionFilter filter = new ActionFilter(outList.getApplication().getConfig().getConfig("filter").getConfigBundle("select" ));
		
		CaoActionList actionList = new CaoActionList();
		CapCore.getInstance().getFactory().fillWithActions(null,outList, actionList);
		
		IContributionItem[] items = filter.getContributionItems(actionList);
		for ( IContributionItem item : items ) {
			manager.add(item);
		}
	}
	
}
