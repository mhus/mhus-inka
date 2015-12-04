package de.mhus.cap.ui.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.IConfig;

public class AddContributionItem extends CompoundContributionItem {

	@SuppressWarnings("unchecked")
	@Override
	protected IContributionItem[] getContributionItems() {

		List<IContributionItem> menuContributionList = new ArrayList<IContributionItem>();
		
		try {
			for ( IConfig item : CapCore.getInstance().getConfiguration().getConfig("definitions").getConfigBundle("connection") ) {
				
				CommandContributionItemParameter p = new CommandContributionItemParameter(
        				Activator.getDefault().getWorkbench(), 
        				null, 
        				AddConnectionHandler.ID,
        				CommandContributionItem.STYLE_PUSH);
				
				p.parameters = new HashMap<String, Object>();
				p.parameters.put(AddConnectionHandler.CONFIG, ConfigUtil.toString(item) );
				p.label = item.getExtracted("title");
				
        		menuContributionList.add(new CommandContributionItem(p));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return menuContributionList.toArray(new IContributionItem[menuContributionList.size()]);
	}

}
