package de.mhus.hair.jack;

import de.mhus.cap.core.CapCore;
import de.mhus.hair.jack.action.ImportFsAction;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoActionProvider;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.config.IConfig;

public class JackDriver extends CaoDriver {

	public JackDriver() {
		
		JackActionProvider actionProvider = new JackActionProvider(new MActivator(JackDriver.class.getClassLoader()));
		CapCore.getInstance().getFactory().registerActionProvider(actionProvider);
		
		CaoActionProvider importProvider = new CaoActionProvider() {
			
			{
				list.add(new ImportFsAction(new MActivator(JackDriver.class.getClassLoader()), null));
			}
			@Override
			protected boolean canExecute(CaoList list, Object... initConfig) {
				return true;
			}
		};
		CapCore.getInstance().getFactory().registerActionProvider(importProvider);
		
	}
	
	@Override
	public CaoConnection createConnection(String url, IConfig config) throws CaoException {
		
		JackConfiguration form = new JackConfiguration();
		form.fromUrl(url);
		form.setConfig(config);
		return initializeConnection(new JackConnection(this,form) );
	}

	@Override
	public CaoForm createConfiguration() {
		return new JackConfiguration();
	}

	@Override
	protected void initDefaultApplications() {
		registerApplication(CaoDriver.APP_CONTENT, new JackApplicationProvider());
	}

}
