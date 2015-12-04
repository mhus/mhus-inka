package de.mhus.cap.ui.connections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.logging.MLog;

public class RemoveConnectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		CaoList list = CapCore.getInstance().getHotSelected();
		if (list == null) return null;
		
		MessageDialog dialog = new MessageDialog(
				Display.getCurrent().getActiveShell(),
				"Remove Connection",
				null,
				"Really remove connections?",
				MessageDialog.CONFIRM,
				new String[] {"Remove","Cancel"},
				0
				);
		
		if (dialog.open() != 0) return null; 
		
		try {
			for (CaoElement item : list.getElements()) {
				if (item instanceof ConnectionDefinition) {
					try {
						CapCore.getInstance().removeConnection((ConnectionDefinition) item);
						CapCore.getInstance().saveConnections();
					} catch (MException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (CaoException e) {
			MLog.e(e);
		}
		
		return null;
	}

}
