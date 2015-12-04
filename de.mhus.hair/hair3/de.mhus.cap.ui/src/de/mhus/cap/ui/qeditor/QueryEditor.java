package de.mhus.cap.ui.qeditor;


import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.ui.Activator;
import de.mhus.cap.ui.oleditor.BorderLayout;
import de.mhus.cap.ui.oleditor.ObjectListInput;
import de.mhus.cap.ui.tableeditor.TableEditor;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.cao.ICaoChangeListener;
import de.mhus.lib.io.CSVWriter;

public class QueryEditor extends EditorPart {

	public static final String ID = "de.mhus.cap.ui.qeditor";
	
	private Composite main;
	private Button bExecute;
	private Text tQuery;

	private CaoApplication app;

	private CaoObserver observer;

	private String query;

	private AutoComplete myAutoCompleation;

	private Button bExecuteFile;

	private String lastPath;

	private String lastName;

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		ObjectListInput in = (ObjectListInput)input;
		setInput(input);
		setSite(site);
		setPartName(in.getTitle());
		setTitleToolTip(in.getToolTipText());
		app = in.getElement().getApplication();
		query = in.getName();
		observer = app.getConnection().createObserver();
		observer.setListener(new ICaoChangeListener() {
			
			@Override
			public boolean isValid() {
				return false;
			}
			
			@Override
			public void elementUpdated(CaoConnection con, String id) {
			}
			
			@Override
			public void elementUnlink(CaoConnection con, String parentId, String id) {
			}
			
			@Override
			public void elementStructurChanged(CaoConnection con, String id) {
			}
			
			@Override
			public void elementLink(CaoConnection con, String parentId, String id) {
			}
			
			@Override
			public void elementDeleted(CaoConnection con, String id) {
			}
			
			@Override
			public void elementCreated(CaoConnection con, String id) {
			}
			
			@Override
			public void disconnected(CaoConnection caoConnection) {
				QueryEditor.this.dispose();
			}
		});
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		main = new Composite(parent, SWT.NONE );
		
		main.setLayout( new BorderLayout() );
		
		Composite toolBar = new Composite(main, SWT.NONE);
		toolBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		toolBar.setLayoutData(BorderLayout.NORTH);

		bExecute = new Button(toolBar, SWT.FLAT);
		try {
			bExecute.setImage(Activator.getImageDescriptor("icons/cog_go.png").createImage());
		} catch (NullPointerException ne) {}
		bExecute.setToolTipText("Execute");
		bExecute.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				execute();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		bExecuteFile = new Button(toolBar, SWT.FLAT);
		try {
			bExecuteFile.setImage(Activator.getImageDescriptor("icons/folder_go.png").createImage());
		} catch (NullPointerException ne) {}
		bExecuteFile.setToolTipText("Execute to file");
		bExecuteFile.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				executeFile();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		

		tQuery = new Text(main, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tQuery.setLayoutData(BorderLayout.CENTER);
		
		tQuery.setFont(new Font(parent.getDisplay(),"Courier",14,0) );
		
		tQuery.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
								 
				if (e.keyCode == SWT.CTRL || e.keyCode == 4194304) return;
				
//				if ((e.stateMask & 4194304) == 4194304 && e.character == SWT.CR ) {
				if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.character == SWT.CR ) {
					e.doit = false;
					execute();
				}
				
				if ((e.stateMask & SWT.CTRL) == SWT.CTRL && e.keyCode == 32 ) {
					myAutoCompleation.setActive(true);
					e.doit = false;
				}
			}
		});
		
		try {
			CaoElement res = app.queryElement(CaoDriver.AUTO_COMPLEATION, query);
			if (res != null) {
					// LinkedList<String> literals = new LinkedList<String>();
					LinkedList<String> labels = new LinkedList<String>();
					try {
						for (CaoElement ele : res.getList(CaoDriver.AUTO_COMPLETION_LITERALS).getElements()) {
							labels.add(ele.getString(CaoDriver.AUTO_COMPLETION_NAME));
						}
					}catch (Exception e) {}
					try {
						for (CaoElement ele : res.getList(CaoDriver.AUTO_COMPLETION_LABELS).getElements()) {
							labels.add(ele.getString(CaoDriver.AUTO_COMPLETION_NAME));
						}
					}catch (Exception e) {}
					myAutoCompleation = new AutoComplete(tQuery, AutoComplete.RESIZE, new AutoCompleteProvider(labels.toArray(new String[0])) );
			}
		}catch (Exception e) {
			e.printStackTrace();
			//TODO maybe a good idea to trace this ...
		}
	}

	protected void execute() {
		
		String text = tQuery.getText();
		int caret = tQuery.getCaretPosition();
		
		text = MString.getSelection(text, MString.getSelectedPart(text, caret), null );
		
		if (text == null) return;
		
		try {
			CaoElement ret = app.queryElement( query, text);
			if (ret != null) {
				CaoList list = ret.getChildren();
				if (list != null) {
					
					// open new table editor
					ObjectListInput ei = new ObjectListInput();
    				ei.setList(list);
    				ei.setTitle( text ); //TODO
    				ei.setToolTipText(text);
    				try {
    					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(ei, TableEditor.ID);
    				} catch (PartInitException e) {
    					throw new CaoException(e);
    				}
    				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CapCore.getInstance().showError(e.toString());
		}
		
	}

	protected void executeFile() {
	
		FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] { "CSV Files", "All Files" });
		dialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
		if (lastPath != null) dialog.setFilterPath(lastPath);
		if(lastName != null) dialog.setFileName(lastName);
		String res = dialog.open();
		if (res == null) return;
		
		lastPath = dialog.getFilterPath();
		lastName = dialog.getFileName();
		
		
		String text = tQuery.getText();
		int caret = tQuery.getCaretPosition();
		
		text = MString.getSelection(text, MString.getSelectedPart(text, caret), null );
		
		if (text == null) return;
		
		try {
			CaoElement ret = app.queryElement( query, text);
			if (ret != null) {
				CaoList list = ret.getChildren();
				if (list != null) {
					
					PrintWriter pw = new PrintWriter(new File(res));
					CSVWriter writer = new CSVWriter(pw);
    				
					CaoMetadata meta = null;
					for (CaoElement item : list.getElements()) {
						if (meta == null) {
							meta = item.getMetadata();
							for (CaoMetaDefinition def : meta)
								writer.put(def.getName());
							writer.nl();
						}
						
						for (CaoMetaDefinition def : meta) {
							try {
								writer.put(item.getString(def.getName()));
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						writer.nl();
					}
					
					pw.flush();
					pw.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CapCore.getInstance().showError(e.toString());
		}
		
	}

	@Override
	public void setFocus() {

	}

}
