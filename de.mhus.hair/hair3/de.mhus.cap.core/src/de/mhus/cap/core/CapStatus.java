package de.mhus.cap.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.mhus.lib.cao.CaoApplication;

public class CapStatus extends Status {

	public CapStatus(CaoApplication app, String msg) {
		super(IStatus.ERROR,Activator.PLUGIN_ID,msg);
	}

}
