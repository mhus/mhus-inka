package de.mhus.hair.app.admin.action;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.action.IPreferredAction;
import de.mhus.cap.ui.attreditor.AttributeEditor;
import de.mhus.cap.ui.oleditor.ObjectListInput;
import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;

public class SelectOpenAttributesAction extends CaoAction implements IPreferredAction {

	public SelectOpenAttributesAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public boolean canExecute(CaoList list,Object...initConfig) {
		return list.size() == 1;
	}

	@Override
	public MForm createConfiguration(CaoList list,Object...initConfig) {
		return null;
	}

	@Override
	public CaoOperation execute(CaoList list, Object configuration) throws CaoException {
		ObjectListInput ei = new ObjectListInput();
		CaoElement element = list.getElements().next();
		ei.setElement(  element );
		ei.setName(element.getName());
		ei.setToolTipText(element.getId());
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(ei, AttributeEditor.ID);
		} catch (PartInitException e) {
			throw new CaoException(e);
		}	
		return null;
	}

	@Override
	public String getName() {
		return CapCore.FILTER_ACTION_SELECT+".open.attributes";
	}

}
