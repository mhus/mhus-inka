package de.mhus.cha.menu;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

import de.mhus.lib.cao.CaoException;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.action.ActionFilter;

public class FileContributionItem extends CompoundContributionItem {

	private ActionFilter filter;

	public FileContributionItem() {
		try {
			filter = new ActionFilter(CapCore.getInstance().getConfiguration().queryList("filter", CapCore.getInstance().getAccess(), "menu", "new"));
		} catch (CaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    @Override
    protected IContributionItem[] getContributionItems() {
    	return filter.getContributionItems(CapCore.getInstance().getHotSelectActionList());
    }

	
}
