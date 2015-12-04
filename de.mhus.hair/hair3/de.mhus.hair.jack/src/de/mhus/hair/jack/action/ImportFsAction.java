package de.mhus.hair.jack.action;

import de.mhus.cao.model.fs.IoElement;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.cao.util.CaoClipboard;
import de.mhus.lib.form.MForm;
import de.mhus.lib.logging.MLog;

public class ImportFsAction extends CaoAction {

	public ImportFsAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public String getName() {
		return CaoDriver.ACTION_CAP_MOVE_TO;
	}

	@Override
	public MForm createConfiguration(CaoList list, Object... initConfig)
			throws CaoException {
		
		CaoElement target = null;
		if (	initConfig == null || 
				initConfig.length == 0 )
			target = CaoClipboard.getClipboard().getTarget();
		else
			target = (CaoElement) initConfig[0];

		return new ImportFsOperation((JackElement)target);
	}

	@Override
	public boolean canExecute(CaoList list, Object... initConfig) {
		
		try {
			for (CaoElement item : list.getElements())
				if (! (item instanceof IoElement))
					return false;
		} catch (CaoException e) {
			MLog.e(e);
			return false;
		}
		
		CaoElement target = null;
		if (	initConfig == null || 
				initConfig.length == 0 )
			target = CaoClipboard.getClipboard().getTarget();
		else
			target = (CaoElement) initConfig[0];
		
		if ( 	target == null || 
				!(target instanceof JackElement) )
			return false;
		
		return true;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration)
			throws CaoException {
		((ImportFsOperation)configuration).setSources(list);
		return (CaoOperation) configuration;	}

}
