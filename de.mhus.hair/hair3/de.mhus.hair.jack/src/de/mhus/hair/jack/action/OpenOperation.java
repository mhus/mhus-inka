package de.mhus.hair.jack.action;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.filters.StringInputStream;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.mhus.cap.core.ICapRcpAction;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;

public class OpenOperation extends CaoOperation implements ICapRcpAction {

	private CaoList sources;
	private IWorkbenchWindow window;

	public OpenOperation(CaoList list) {
		sources = list;
	}

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		
		final IWorkbenchPage page = window.getActivePage();
		
		for (CaoElement item : sources.getElements()) {
			
			JackElement jack = (JackElement) item;
			if (jack.hasContent()) {
				
				File tmpFile;
				try {
					tmpFile = File.createTempFile("hair", ".txt");
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new CaoException(e1);
				}
				final JackEditorInput editorInput = new JackEditorInput(jack.getNode(),new Path(tmpFile.getAbsolutePath()));
				
//				IEditorReference[] editorRefs = page.findEditors(editorInput,null,IWorkbenchPage.MATCH_INPUT);
				IEditorDescriptor desc = window.getWorkbench().getEditorRegistry().getDefaultEditor(editorInput.getName());
				if (desc == null) {
					System.out.println("User default txt editor");
					desc = window.getWorkbench().getEditorRegistry().getDefaultEditor("text.txt");
				}
				System.out.println(desc);
				if (desc != null) {
					final IEditorDescriptor descFinal = desc;
					window.getShell().getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							try {
								final IEditorPart editor = page.openEditor(
										editorInput, descFinal.getId());
								editor.addPropertyListener(new IPropertyListener() {
									public void propertyChanged(Object source,
											int propId) {
										if (IEditorPart.PROP_DIRTY == propId) {
											if (editor instanceof AbstractDecoratedTextEditor && (!editor.isDirty())) {
												System.out.println("Save");
												IDocumentProvider documentProvider = ((AbstractDecoratedTextEditor)editor).getDocumentProvider();
												String content = documentProvider.getDocument(editorInput).get();
												try {
													editorInput.setContents(new StringInputStream(content));
													editorInput.getNode().getSession().save();
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}
									}
								});
							} catch (PartInitException e) {
								e.printStackTrace();
							}
						}
					});
				}
				
			}
		}
	}

	@Override
	public void dispose() throws CaoException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRcpHandles(IWorkbenchWindow window) {
		this.window = window;
	}

}
