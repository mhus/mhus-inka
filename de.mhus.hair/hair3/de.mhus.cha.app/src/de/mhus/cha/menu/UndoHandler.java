package de.mhus.cha.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import de.mhus.cap.core.CapCore;

public class UndoHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CapCore.getInstance().firePropertyChanged(CapCore.PROPERTY_UNDO, null);
		return null;
	}


}
