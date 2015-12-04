package de.mhus.hair.app.admin.menu;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.action.ActionFilter;

public class ExtrasContributionItem extends CompoundContributionItem {

	private ActionFilter filter;

	public ExtrasContributionItem() {
		try {
			filter = new ActionFilter(CapCore.getInstance().getConfiguration().getConfig("filter").getConfigBundle("extras"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    @Override
    protected IContributionItem[] getContributionItems() {
    	return filter.getContributionItems(CapCore.getInstance().getHotSelectActionList());
    }

	
}
