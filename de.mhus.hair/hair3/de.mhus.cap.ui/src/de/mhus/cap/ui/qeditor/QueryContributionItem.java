package de.mhus.cap.ui.qeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.action.ActionFilter;
import de.mhus.cap.ui.action.CaoCmdHandler;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.MLog;

public class QueryContributionItem extends CompoundContributionItem {


	public QueryContributionItem() {
	}

    @Override
    protected IContributionItem[] getContributionItems() {

        List<IContributionItem> menuContributionList = new ArrayList<IContributionItem>();
		
        CaoList list = CapCore.getInstance().getHotSelected();
        if (list != null && list.size() == 1) {
        	CaoApplication app;
			try {
				app = list.getElements().next().getApplication();
			} catch (CaoException e) {
				MLog.e(e);
				return null;
			}
        	IConfig[] config = app.getConfig().getConfigBundle("ql");
        	if (config.length == 0)
        		config = app.getConnection().getDriver().getConfig().getConfigBundle("ql");
        	if (config.length != 0) {
        		
        		for (IConfig c : config) {
        			
        	        CommandContributionItemParameter p = new CommandContributionItemParameter(
        					Activator.getDefault().getWorkbench(), 
        					null, 
        					OpenQueryEditorHandler.ID,
        					CommandContributionItem.STYLE_PUSH);

        			p.parameters = new HashMap<String, String>();
        			p.parameters.put(OpenQueryEditorHandler.PARAM_QUERY, c.getExtracted("name"));
        			p.label= c.getExtracted("title");
        			p.tooltip=c.getExtracted("tooltip");
        			menuContributionList.add(new CommandContributionItem(p));        			
        		}
        		
        		
        	}
        }
        
		return menuContributionList.toArray(new IContributionItem[menuContributionList.size()]);

    }

	
}
