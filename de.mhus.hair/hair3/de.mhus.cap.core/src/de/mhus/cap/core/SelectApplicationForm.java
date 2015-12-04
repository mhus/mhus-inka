package de.mhus.cap.core;

import org.eclipse.core.runtime.IConfigurationElement;

import de.mhus.lib.MCast;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.objects.IDynOptionsProvider;

@FormElement("name='select_application' title='Select Application'")
public class SelectApplicationForm implements MForm {

	private String application = "0";
	private String[] appTitles;
	private String[] appIds;
	private IConfigurationElement[] configs;

	public SelectApplicationForm(IConfigurationElement[] config) {
		configs   = config;
		appTitles = new String[config.length];
		appIds    = new String[config.length];
		for (int i = 0; i < config.length; i++) {
			appTitles[i] = config[i].getAttribute("name");
			appIds[i] = String.valueOf(i);
		}
	}
	
	@FormElement("select dynamic='1' nls='docbase' title='Docbase'")
	public void setApplication(String in) {
		application = in;
	}
	
	public String getApplication() {
		return application;
	}
	
	public IDynOptionsProvider getApplicationDataProvider() {
		return new IDynOptionsProvider() {

			@Override
			public String[] getOptionTitles() {
				return appTitles;
			}

			@Override
			public String[] getOptionValues() {
				return appIds;
			}
			
		};
	}

	public IConfigurationElement getConfig() {
		return configs[MCast.toint(application, 0)];
	}

	
	
}
