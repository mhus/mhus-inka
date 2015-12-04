package de.mhus.cap.ui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import de.mhus.cap.core.Activator;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoActionList;
import de.mhus.lib.config.IConfig;

public class ActionFilter {

	private IConfig[] filters;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(ActionFilter.class);

	public ActionFilter(IConfig[] iConfigs) {
		this.filters = iConfigs;
	}
	
    public IContributionItem[] getContributionItems(CaoActionList list) {

        List<IContributionItem> menuContributionList = new ArrayList<IContributionItem>();

        for(CaoAction action : list ) {
        	if (accepts(action)) {
        		CommandContributionItemParameter p = new CommandContributionItemParameter(
        				Activator.getDefault().getWorkbench(), 
        				null, 
        				CaoCmdHandler.ID,
        				CommandContributionItem.STYLE_PUSH);
        		
        		p.parameters = new HashMap<String, String>();
        		p.parameters.put(CaoCmdHandler.PARAM_ACTION, action.getName());
        		p.label=action.getResourceBundle().getString(action.getName() + ".title", action.getName());
        		p.tooltip=action.getResourceBundle().getString(action.getName() + ".tooltip", action.getName());
        		menuContributionList.add(new CommandContributionItem(p));        		
        	}
        }

		return menuContributionList.toArray(new IContributionItem[menuContributionList.size()]);
	}
	
	public synchronized boolean accepts(CaoAction action) {
		return accepts(action, filters, false);
	}
	
	protected synchronized boolean accepts(CaoAction action, IConfig[] filters2, boolean def) {
		if (filters2==null || filters2.length == 0) return def;
		for ( IConfig element : filters2 ) {
			try {
				for (IConfig f : element.getConfigBundle() ) {
					if (f.getName().equals("include")) {
						String pattern = f.getString("pattern","");
						if ( MString.compareFsLikePattern(action.getName(), pattern) ) {
							return accepts(action, f.getConfigBundle(), true);
						}
					} else
					if (f.getName().equals("exclude")) {
						String pattern = f.getString("pattern","");
						if ( MString.compareFsLikePattern(action.getName(), pattern) ) {
							return accepts(action, f.getConfigBundle(), false);
						}
					}
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
		
		return def;
	}

	public CaoAction getPreferredAction(CaoActionList list) {
        for(CaoAction action : list ) {
        	if (accepts(action)) {
        		if (action instanceof IPreferredAction)
        			return action;
        	}
        }
        
        for(CaoAction action : list ) {
        	if (accepts(action)) {
        		return action;
        	}
        }
        
        return null;
	}
	
}
