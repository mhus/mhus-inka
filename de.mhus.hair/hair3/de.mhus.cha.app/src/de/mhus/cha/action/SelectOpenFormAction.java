package de.mhus.cha.action;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.mhus.lib.cao.CaoAction;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cap.core.Access;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.action.IPreferredAction;
import de.mhus.cap.core.attreditor.AttributeEditor;
import de.mhus.cap.core.browsertree.CaoGui;
import de.mhus.cap.core.oleditor.ObjectListInput;
import de.mhus.cha.formeditor.FormEditor;
import de.mhus.lib.MActivator;
import de.mhus.lib.form.MForm;

public class SelectOpenFormAction extends CaoAction<Access> implements IPreferredAction {

	public SelectOpenFormAction(MActivator activator, String resourceName) {
		super(activator, resourceName);
	}

	@Override
	public boolean canExecute(CaoList<Access> list,Object...initConfig) {
		return list.size() == 1;
	}

	@Override
	public MForm createConfiguration(CaoList<Access> list,Object...initConfig) {
		return null;
	}

	@Override
	public CaoOperation execute(CaoList<Access> list, Object configuration) throws CaoException {
		ObjectListInput ei = new ObjectListInput();
		CaoElement<Access> element = list.getElements().next();
		ei.setElement(  element );
		ei.setName(element.getName());
		ei.setToolTipText(element.getId());
		ei.setCaoGui( (CaoGui) element.getConnection().getContext().get(CapCore.CONTEXT_GUI_CONFIG) );
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(ei, FormEditor.ID);
		} catch (PartInitException e) {
			throw new CaoException(e);
		}	
		return null;
	}

	@Override
	public String getName() {
		return CapCore.FILTER_ACTION_SELECT+".open.form";
	}

}
