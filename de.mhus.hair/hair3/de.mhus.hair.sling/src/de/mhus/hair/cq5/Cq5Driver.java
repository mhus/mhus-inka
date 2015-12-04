package de.mhus.hair.cq5;

import de.mhus.cap.core.CapCore;
import de.mhus.hair.sling.SlingDriver;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoDriver;

public class Cq5Driver extends SlingDriver {

	public Cq5Driver() {
		Cq5ActionProvider actionProvider = new Cq5ActionProvider(new MActivator(Cq5Driver.class.getClassLoader()));
		CapCore.getInstance().getFactory().registerActionProvider(actionProvider);
	}
	
	@Override
	protected void initDefaultApplications() {
		registerApplication(CaoDriver.APP_CONTENT, new Cq5ApplicationProvider());
	}

}
