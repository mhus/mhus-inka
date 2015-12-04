package de.mhus.cap.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.mhus.lib.swt.form.MFormSwtWizard;

public class CapStarter {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(CapStarter.class);
	
	public static void start() throws Exception {
		IConfigurationElement[] config = null;
		log.i("start");
		
		// get possible applications
		IConfigurationElement[] apps = Platform.getExtensionRegistry().getConfigurationElementsFor(CapCore.EXTENSION_APPLICATION);
		if (apps.length == 0) {
			log.fatal("No application found");
			MessageDialog.openError(Display.getDefault().getActiveShell(),"ERROR","No application found");
			throw new Exception("No application found");
		} else
		if (apps.length == 1) {
			log.info("Start application: " + apps[0].getAttribute("name"));
			// use the first one
			ICapApplication app = (ICapApplication) apps[0].createExecutableExtension("class");
			CapCore.initialize(app);
		}
		else {
			
			
			// ask the user ? Conflicts with persistence
			Shell shell = Display.getDefault().getActiveShell();
			SelectApplicationForm form = new SelectApplicationForm(apps);
			MFormSwtWizard wizard = new MFormSwtWizard();
			
			wizard.setWindowTitle( "Select Application" ); //TODO use title
			
			wizard.appendPages(form);
	
			if ( wizard.show( shell ) != MFormSwtWizard.OK ) {
				wizard.dispose();
				throw new Exception("No application selected");
			}

			wizard.dispose();

			ICapApplication app = (ICapApplication) form.getConfig().createExecutableExtension("class");
			CapCore.initialize(app);
			
		}
		
		// initialize dependencies
		config = Platform.getExtensionRegistry().getConfigurationElementsFor(CapCore.EXTENSION_IMAGE_PROVIDER);
		for (IConfigurationElement e : config) {
			try {
				e.createExecutableExtension("class");
			} catch (CoreException e1) {
				log.warn(e1);
			}
		}

	}
}
