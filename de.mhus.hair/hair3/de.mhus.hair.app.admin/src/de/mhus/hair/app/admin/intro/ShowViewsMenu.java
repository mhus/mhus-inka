/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.mhus.hair.app.admin.intro;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

import de.mhus.cap.ui.browsertree.BrowserTreeView;

public class ShowViewsMenu extends ContributionItem {
	private static final String SHOW_VIEW_ID = "org.eclipse.ui.views.showView"; //$NON-NLS-1$
	private static final String VIEW_ID_PARM = "org.eclipse.ui.views.showView.viewId"; //$NON-NLS-1$
	private static final String PARAMETER_MAKE_FAST = "org.eclipse.ui.views.showView.makeFast"; //$NON-NLS-1$
	private IWorkbenchWindow window;

	private static final String NO_TARGETS_MSG = WorkbenchMessages.Workbench_showInNoTargets;

	private Comparator actionComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			if (collator == null) {
				collator = Collator.getInstance();
			}
			CommandContributionItemParameter a1 = (CommandContributionItemParameter) o1;
			CommandContributionItemParameter a2 = (CommandContributionItemParameter) o2;
			return collator.compare(a1.label, a2.label);
		}
	};

	private Action showDlgAction;

	/** Added special show search entity action. */
	private Action showSearchEntityAction;

	private Map actions = new HashMap(21);

	// Maps pages to a list of opened views
	private Map openedViews = new HashMap();

	protected boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			ShowViewsMenu.this.dirty = true;
		}
	};
	private boolean makeFast;

	private static Collator collator;

	/**
	 * Creates a Show View menu.
	 * 
	 * @param window the window containing the menu
	 * @param id the id
	 */
	public ShowViewsMenu(IWorkbenchWindow window, String id) {
		this(window, id, false);
	}

	/**
	 * Creates a Show View menu.
	 * 
	 * @param window the window containing the menu
	 * @param id the id
	 * @param makeFast use the fact view variant of the command
	 */
	public ShowViewsMenu(IWorkbenchWindow window, String id,
			final boolean makeFast) {
		super(id);
		this.window = window;
		this.makeFast = makeFast;
		final IHandlerService handlerService = (IHandlerService) window
				.getService(IHandlerService.class);
		final ICommandService commandService = (ICommandService) window
				.getService(ICommandService.class);
		final ParameterizedCommand cmd = getCommand(commandService, makeFast);

		this.showDlgAction = new Action(WorkbenchMessages.ShowView_title) {
			@Override
			public void run() {
				try {
					handlerService.executeCommand(cmd, null);
				} catch (final ExecutionException e) {
					// Do nothing.
				} catch (NotDefinedException e) {
					// Do nothing.
				} catch (NotEnabledException e) {
					// Do nothing.
				} catch (NotHandledException e) {
					// Do nothing.
				}
			}
		};

		this.showSearchEntityAction = new Action("Search Entity View") {
			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(BrowserTreeView.ID,
									String.valueOf(System.currentTimeMillis()),
									IWorkbenchPage.VIEW_ACTIVATE);
				} catch (Exception e) {
					// do nothing
				}
				return;
			}
		};
		this.showSearchEntityAction.setImageDescriptor(null);

		window.getWorkbench().getHelpSystem().setHelp(this.showDlgAction,
				IWorkbenchHelpContextIds.SHOW_VIEW_OTHER_ACTION);
		// indicate that a show views submenu has been created
		((WorkbenchWindow) window)
				.addSubmenu(WorkbenchWindow.SHOW_VIEW_SUBMENU);

		this.showDlgAction.setActionDefinitionId(SHOW_VIEW_ID);

	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Fills the menu with Show View actions.
	 */
	private void fillMenu(IMenuManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		// If no page disable all.
		IWorkbenchPage page = this.window.getActivePage();
		if (page == null) {
			return;
		}

		// If no active perspective disable all
		if (page.getPerspective() == null) {
			return;
		}

		// Get visible actions.
		List viewIds = Arrays.asList(page.getShowViewShortcuts());

		// add all open views
		viewIds = addOpenedViews(page, viewIds);

		List actList = new ArrayList(viewIds.size());
		for (Iterator i = viewIds.iterator(); i.hasNext();) {
			String id = (String) i.next();
			if (id.equals(IIntroConstants.INTRO_VIEW_ID)) {
				continue;
			}

			// call special open command for search entity view
			if ("net.metasafe.tools.ui.views.searchentity".equals(id)) {
				CommandContributionItemParameter item = getItem(id);
				if (item.icon != null)
					this.showSearchEntityAction.setImageDescriptor(item.icon);
				innerMgr.add(this.showSearchEntityAction);
				continue;
			}

			CommandContributionItemParameter item = getItem(id);
			if (item != null) {
				actList.add(item);
			}
		}
		Collections.sort(actList, this.actionComparator);
		for (Iterator i = actList.iterator(); i.hasNext();) {
			CommandContributionItem item = new CommandContributionItem(
					(CommandContributionItemParameter) i.next());
			if (WorkbenchActivityHelper.filterItem(item)) {
				item.dispose();
				continue;
			}
			innerMgr.add(item);
		}

		// Add Other ..
		innerMgr.add(new Separator());
		innerMgr.add(this.showDlgAction);
	}

	private CommandContributionItemParameter getItem(String viewId) {
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(viewId);
		if (desc == null) {
			return null;
		}
		String label = desc.getLabel();

		CommandContributionItemParameter parms = new CommandContributionItemParameter(
				this.window, viewId, SHOW_VIEW_ID,
				CommandContributionItem.STYLE_PUSH);
		parms.label = label;
		parms.icon = desc.getImageDescriptor();
		parms.parameters = new HashMap();

		parms.parameters.put(VIEW_ID_PARM, viewId);
		if (this.makeFast) {
			parms.parameters.put(PARAMETER_MAKE_FAST, "true"); //$NON-NLS-1$
		}
		return parms;
	}

	private List addOpenedViews(IWorkbenchPage page, List actList) {
		ArrayList views = getParts(page);
		ArrayList result = new ArrayList(views.size() + actList.size());

		for (int i = 0; i < actList.size(); i++) {
			Object element = actList.get(i);
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		for (int i = 0; i < views.size(); i++) {
			Object element = views.get(i);
			if (result.indexOf(element) < 0) {
				result.add(element);
			}
		}
		return result;
	}

	private ArrayList getParts(IWorkbenchPage page) {
		ArrayList parts = (ArrayList) this.openedViews.get(page);
		if (parts == null) {
			parts = new ArrayList();
			this.openedViews.put(page, parts);
		}
		return parts;
	}

	@Override
	public void fill(Menu menu, int pindex) {
		int index = pindex;
		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(this.menuListener);
		}

		if (!this.dirty) {
			return;
		}

		MenuManager manager = new MenuManager();
		fillMenu(manager);
		IContributionItem items[] = manager.getItems();
		if (items.length == 0) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(NO_TARGETS_MSG);
			item.setEnabled(false);
		} else {
			for (int i = 0; i < items.length; i++) {
				items[i].fill(menu, index++);
			}
		}
		this.dirty = false;
	}

	// for dynamic UI
	protected void removeAction(String viewId) {
		this.actions.remove(viewId);
	}

	/**
	 * @param commandService
	 * @param makeItFast
	 */
	private ParameterizedCommand getCommand(ICommandService commandService,
			final boolean makeItFast) {
		Command c = commandService.getCommand(SHOW_VIEW_ID);
		Parameterization[] parms = null;
		if (makeItFast) {
			try {
				IParameter parmDef = c.getParameter(PARAMETER_MAKE_FAST);
				parms = new Parameterization[] { new Parameterization(parmDef,
						"true") //$NON-NLS-1$
				};
			} catch (NotDefinedException e) {
				// this should never happen
			}
		}
		return new ParameterizedCommand(c, parms);
	}
}
