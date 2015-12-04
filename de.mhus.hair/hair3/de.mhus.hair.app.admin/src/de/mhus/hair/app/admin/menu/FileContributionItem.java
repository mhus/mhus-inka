package de.mhus.hair.app.admin.menu;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.action.ActionFilter;

public class FileContributionItem extends CompoundContributionItem {

	private ActionFilter filter;

	public FileContributionItem() {
		try {
			filter = new ActionFilter(CapCore.getInstance().getConfiguration().getConfig("filter").getConfigBundle("menu_new"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    protected IContributionItem[] getContributionItems() {
    	return filter.getContributionItems(CapCore.getInstance().getHotSelectActionList());
    }

	
}
