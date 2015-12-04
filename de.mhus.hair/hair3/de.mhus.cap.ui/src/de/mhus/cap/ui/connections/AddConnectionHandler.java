package de.mhus.cap.ui.connections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.MConfigFactory;

public class AddConnectionHandler extends AbstractHandler {

	public static final String ID = "de.mhus.cap.ui.add_connection";
	public static final String CONFIG = "config";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			if (event.getParameters() == null) return null;
			IConfig config =  MConfigFactory.getInstance().toConfig( (String)event.getParameters().get(CONFIG) );
			if (config == null) return null;
			
			ConnectionDefinition con = new ConnectionDefinition(config);
			InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), "Name", "Insert name of the new Connection", con.getTitle(), null);
			if (dialog.open() == InputDialog.CANCEL)
				return null;
			
			con.setTitle(dialog.getValue());
			
			CapCore.getInstance().addConnection(con);
			CapCore.getInstance().saveConnections();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
