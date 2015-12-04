package de.mhus.hair.app.admin.intro;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import de.mhus.cap.ui.browsertree.BrowserTreeView;


public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		layout.addStandaloneView(BrowserTreeView.ID,  true, IPageLayout.LEFT, 0.20f, editorArea);
		
		layout.getViewLayout(BrowserTreeView.ID).setCloseable(false);
		
		  layout.setEditorAreaVisible(false);  
		  
		  
		  IFolderLayout consoleFolder = layout.createFolder("console",  
		    IPageLayout.BOTTOM, 0.65f, "messages");  
		  consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);  
		  
	}
}
