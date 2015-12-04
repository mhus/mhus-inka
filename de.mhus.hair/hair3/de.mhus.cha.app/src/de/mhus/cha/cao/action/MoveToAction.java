package de.mhus.cha.cao.action;

import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.cao.util.CaoClipboard;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.MActivator;
import de.mhus.lib.form.MForm;

public class MoveToAction extends CaoAction<Access> {

	public MoveToAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public boolean canExecute(CaoList<Access> list, Object... initConfig) {
		for (CaoElement<Access> item : list.getElements())
			if (! (item instanceof ChaElement))
				return false;
		
		CaoElement<?> target = null;
		if (	initConfig == null || 
				initConfig.length == 0 )
			target = CaoClipboard.getClipboard().getTarget();
		else
			target = (CaoElement<?>) initConfig[0];
		
		if ( 	target == null || 
				!(target instanceof ChaElement) || 
				!((ChaElement)target).isNode() )
			return false;
		return true;
	}

	@Override
	public MForm createConfiguration(CaoList<Access> list, Object... initConfig)
			throws CaoException {
		
		CaoElement<?> target = null;
		if (	initConfig == null || 
				initConfig.length == 0 )
			target = CaoClipboard.getClipboard().getTarget();
		else
			target = (CaoElement<?>) initConfig[0];
		
		return new MoveToOperation(list,(CaoElement<Access>) target);
	}

	@Override
	public CaoOperation execute(CaoList<Access> list, Object configuration)
			throws CaoException {
		((MoveToOperation)configuration).setSources(list);
		return (MoveToOperation)configuration;
	}

	@Override
	public String getName() {
		return CaoDriver.ACTION_CAP_MOVE_TO;
	}

}
