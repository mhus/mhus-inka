package de.mhus.cap.ui.attreditor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.mhus.lib.cao.CaoException;

public class UndoAction extends AbstractOperation {

	private AttributeEditor editor;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(UndoAction.class);

	public UndoAction(AttributeEditor attributeEditor) {
		super(attributeEditor.getTitle());
		editor = attributeEditor;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		try {
			editor.doUndo();
		} catch (CaoException e) {
			log.info(e);
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

}
