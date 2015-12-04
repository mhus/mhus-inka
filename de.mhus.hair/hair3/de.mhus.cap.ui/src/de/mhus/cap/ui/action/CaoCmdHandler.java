package de.mhus.cap.ui.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.mhus.cap.core.CapCore;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoList;

public class CaoCmdHandler extends AbstractHandler {

	public static final String ID = "de.mhus.cap.ui.action.caocmdhandler";
	public static final String PARAM_ACTION = "de.mhus.cap.ui.action.caocmdhandler.action";
	
	/**
	 * Get the view this event occurred on.
	 * 
	 * @param event
	 * @return {@link MarkerSupportView} or <code>null</code>
	 */
	public IWorkbenchPart getView(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		return part;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			String actionName = event.getParameter(PARAM_ACTION);
			
			CaoList list = CapCore.getInstance().getHotSelected();
			CaoAction action = CapCore.getInstance().getHotSelectActionList().getAction(actionName);
			if (action==null) return null;
			
			CapCore.getInstance().executeAction(action, list);
						
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

}
