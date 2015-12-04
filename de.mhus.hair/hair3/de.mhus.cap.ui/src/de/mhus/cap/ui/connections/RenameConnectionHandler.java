package de.mhus.cap.ui.connections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.logging.MLog;

public class RenameConnectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		CaoList list = CapCore.getInstance().getHotSelected();
		if (list == null) return null;
		
		
		
		try {
			for (CaoElement item : list.getElements()) {
				if (item instanceof ConnectionDefinition) {
					try {
						ConnectionDefinition con = (ConnectionDefinition) item;

						InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), "Rename Connection", "Insert name of the Connection", con.getTitle(), null);
						if (dialog.open() == InputDialog.CANCEL)
							return null;

						con.setTitle(dialog.getValue());
						CapCore.getInstance().saveConnections();
						CapCore.getInstance().fireConnectionChanged(con);
						
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
