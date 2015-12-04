package de.mhus.cha.menu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import de.mhus.cap.core.CapCore;

public class EditableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Boolean editable = (Boolean) CapCore.getInstance().getProperty(CapCore.PROPERTY_EDITABLE);
		CapCore.getInstance().setProperty(CapCore.PROPERTY_EDITABLE, editable == null || editable.booleanValue() == false   );
		return null;
	}


}
