package de.mhus.hair.sling;

import de.mhus.hair.jack.JackDriver;
import de.mhus.lib.cao.CaoDriver;

public class SlingDriver extends JackDriver {

	@Override
	protected void initDefaultApplications() {
		registerApplication(CaoDriver.APP_CONTENT, new SlingApplicationProvider());
	}

}
