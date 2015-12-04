package de.mhus.cap.ui.qeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.cap.ui.attreditor.AttributeEditor;
import de.mhus.cap.ui.oleditor.ObjectListInput;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.IConfig;

public class OpenQueryEditorHandler extends AbstractHandler {

	public static final String ID = "de.mhus.cap.ui.qeditor.open";
	public static final String PARAM_QUERY = "query";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			if (event.getParameters() == null) return null;
			String queryName = (String)event.getParameters().get(PARAM_QUERY);
			if (queryName == null) return null;
			CaoList list = CapCore.getInstance().getHotSelected();
	        if (list != null && list.size() == 1) {
	        	
				CaoElement element = list.getElements().next();
	        	CaoApplication app = element.getApplication();
	        	IConfig[] config = app.getConfig().getConfigBundle("ql");
	        	if (config.length == 0)
	        		config = app.getConnection().getDriver().getConfig().getConfigBundle("ql");
	        	if (config.length != 0) {
	        		for (IConfig c : config) {
	        			if (queryName.equals(c.getExtracted("name"))) {
	        				
	        				ObjectListInput ei = new ObjectListInput();
	        				ei.setElement(  element );
	        				ei.setName( queryName );
	        				ei.setTitle( c.getExtracted("title") );
	        				ei.setToolTipText(c.getExtracted("tooltip"));
	        				try {
	        					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(ei, QueryEditor.ID);
	        				} catch (PartInitException e) {
	        					throw new CaoException(e);
	        				}
	        				
	        				return null;
	        			}
	        		}
	        	}
	        	
	        	
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
