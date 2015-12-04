package de.mhus.hair.app.admin.intro;

import org.eclipse.jface.action.AbstractAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction introAction;
	private IWorkbenchAction quitAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction preferencesAction;
	private IWorkbenchAction showHelpAction;
	private IWorkbenchAction searchHelpAction;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction aboutAction;
	private ShowViewsMenu views;
	private AbstractAction editAction;
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;    
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(IWorkbenchWindow window) {
		super.makeActions(window);
		
		introAction = ActionFactory.INTRO.create(window);
		register(introAction);
		
		this.quitAction = ActionFactory.QUIT.create(window);
		register(this.quitAction);

		this.saveAction = ActionFactory.SAVE.create(window);
		register(this.saveAction);
		
		this.preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(this.preferencesAction);

		this.showHelpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(this.showHelpAction);

		this.searchHelpAction = ActionFactory.HELP_SEARCH.create(window);
		register(this.searchHelpAction);

		this.dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(this.dynamicHelpAction);

		this.aboutAction = ActionFactory.ABOUT.create(window);
		register(this.aboutAction);

		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);
		
		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);
		
		this.views = new ShowViewsMenu(window, "viewsShortlist");
		
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		
		MenuManager mm_file = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		mm_file.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		mm_file.add(this.quitAction);
		menuBar.add(mm_file);

		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

//		MenuManager menuEdit = new MenuManager("&Edit",IWorkbenchActionConstants.M_EDIT);
//
//		menuEdit.add(undoAction);
//		menuEdit.add(redoAction);
//		menuBar.add(menuEdit);

		
		// window menu
		MenuManager mm_window = new MenuManager("&Window",
				IWorkbenchActionConstants.M_WINDOW);
		mm_window.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		mm_window.add(this.preferencesAction);
		menuBar.add(mm_window);

		// Show View submenu
		MenuManager mm_showview = new MenuManager("&Show View");
		mm_showview.add(this.views);
		mm_window.add(mm_showview);

		// help menu
		MenuManager mm_help = new MenuManager("&Help");
		mm_help.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		mm_help.add(new Separator());
		mm_help.add(this.showHelpAction);
		mm_help.add(this.searchHelpAction);
		mm_help.add(this.dynamicHelpAction);
		mm_help.add(new Separator());
		mm_help.add(this.aboutAction);
		menuBar.add(mm_help);
		
		
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		
		ToolBarManager cbm = new ToolBarManager(coolBar.getStyle());
		coolBar.add(cbm);
		cbm.add(this.saveAction);
		
	
	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		/*
		super.fillStatusLine(statusLine);
		StatusLineContributionItem appstatus = new StatusLineContributionItem(
				"APPSTATUS", 70);
		// appstatus.setText("Repository not open");
		statusLine.add(appstatus);
		statusLine.update(true);
		HairCore.getInstance().setAppStatusItem(appstatus);
		*/
	}
}
