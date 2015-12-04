package de.mhus.hair.app.admin.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.mhus.cap.core.CapCore;

public class UndoHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CapCore.getInstance().firePropertyChanged(CapCore.PROPERTY_UNDO, null);
		return null;
	}


}
