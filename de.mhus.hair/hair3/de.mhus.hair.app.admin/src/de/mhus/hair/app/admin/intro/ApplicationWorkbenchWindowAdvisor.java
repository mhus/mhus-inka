package de.mhus.hair.app.admin.intro;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
    	  
    	  IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
          configurer.setInitialSize(new Point(700, 550));
          configurer.setShowCoolBar(true);
          configurer.setShowStatusLine(true);
          configurer.setShowProgressIndicator(true);
          configurer.setShowMenuBar(true);
          configurer.setTitle("Content Access Platform");
    }
}
