package de.mhus.cha.formeditor;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.ICaoChangeListener;
import de.mhus.lib.cao.util.StringArrayList;
import de.mhus.cap.core.Access;
import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ICapEventListener;
import de.mhus.cap.core.browsertree.CaoBrowserTreeNode;
import de.mhus.cap.core.browsertree.CaoGui;
import de.mhus.cap.core.oleditor.BorderLayout;
import de.mhus.cap.core.oleditor.ObjectListInput;
import de.mhus.lib.MActivator;
import de.mhus.lib.MSingleton;
import de.mhus.lib.form.FormException;
import de.mhus.lib.form.IFormDynamic;
import de.mhus.lib.form.MFormModel;
import de.mhus.lib.form.builders.FormLayoutSimpleBuilder;
import de.mhus.lib.form.objects.FBoolean;
import de.mhus.lib.form.objects.FObject;
import de.mhus.lib.form.objects.FString;
import de.mhus.lib.form.objects.FObject.Listener;
import de.mhus.lib.form.objects.FObject.WHAT_CHANGED;
import de.mhus.lib.swt.form.MFormSwtControl;

public class FormEditor extends EditorPart implements IFormDynamic, ICapEventListener, ICaoChangeListener {

	public static final String ID = "de.mhus.cha.formeditor.FormEditor";


	private CaoElement<Access> data;
	private CaoGui gui;

	private CaoWritableElement<Access> writable;

	private MFormSwtControl control;

	private MFormModel model;
	private Action toggleLinkAction;

	private boolean dirty;

	private CaoObserver observer;

	private Composite main;

	private Button bConnected;

	private Composite formContainer;

	private Label bLabel;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
		.getLog(FormEditor.class);

	@Override
	public void doSave(IProgressMonitor arg0) {
		try {
//			model. targetSet(false);
			model.saveToTarget(false);
			writable.save();
			dirty=false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (Exception e) {
			log.error(e);
		}
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		ObjectListInput in = (ObjectListInput)input;
		data = in.getElement();
		gui  = in.getCaoGui();
		setInput(input);
		setSite(site);
		setPartName(in.getName());
		setTitleToolTip(in.getToolTipText());
		
		observer = data.getConnection().createObserver();
		observer.setListener(this);
		observer.add(data);
		
		CapCore.getInstance().getEventHandler().registerWeak(this);

	}

	@Override
	public boolean isDirty() {
		//return writable != null && ( writable.isDirty() || model.isChanged());
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		main = new Composite(parent, SWT.NONE );
		
		main.setLayout( new de.mhus.cap.core.oleditor.BorderLayout() );
		
		Composite toolBar = new Composite(main, SWT.NONE);
		toolBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		toolBar.setLayoutData(BorderLayout.NORTH);
		bConnected = new Button(toolBar, SWT.TOGGLE | SWT.FLAT);
		bConnected.setImage(Activator.getImageDescriptor("icons/icon_link_with_editor.png").createImage());
		bConnected.setToolTipText("Connect with selection");

		bLabel = new Label(toolBar,SWT.NONE);
		
		formContainer = new Composite(main, SWT.NONE);
		formContainer.setLayoutData(BorderLayout.CENTER);
		formContainer.setLayout(new FillLayout());
		createModelForm(formContainer);
		
		updateEditable();
	
//		try {
//			model.setValuesToTarget(true);
//		} catch (Throwable e1) {
//			log.info(e1);
//		}
		
		model.getChangeHandler().register(new Listener() {

			@Override
			public void formChangedEvent(FObject src, WHAT_CHANGED what) {
				if (src != null && what == WHAT_CHANGED.VALUE )
					try {
//						src.targetSet(false);
						src.saveToTarget(false);
					} catch (FormException e) {
						log.info(e);
					}
			}
			
		});
		
	}
	
	private void createModelForm(Composite parent) {

		try {
			bLabel.setText( " " + data.getName() + " (" + data.getId() + ")" );
		} catch (CaoException e1) {
			bLabel.setText("");
		}
		
		model = new MFormModel(MSingleton.instance().getActivator(),this);

		int sort = 0;
		for ( CaoMetaDefinition meta : data.getMetadata() ) {

			try {
				FObject next = null;
				switch (meta.getType()) {
				case BOOLEAN:
					next = new FBoolean();
					((FBoolean)next).setValue(false);
					break;
				case DATETIME:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case DOUBLE:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case LONG:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case STRING:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
//				case TEXT:
//					next = new FStringArea(); //TODO
//					((FString)next).setValue("");
//					break;	
//				case LIST:
//					next = new FStringArray();
//					((FStringArray)next).setValue(new String[0]);
				}
				
				if (next!=null) {
					next.initialize(model, this);
					next.setTitle(meta.getName() + " (" + meta.getType().name() + ")");
					next.setName(meta.getName());
					next.setId(meta.getName());
					next.setSortId(sort);
	
					//next.setEnabled(false);
					
					sort++;
//					next.setNls(meta.getNls());
					model.getList().add(next);
				}
			} catch ( Exception e ) {
				log.warn(e);
			}
						
		}
		
		try {
			model.setLayout(new FormLayoutSimpleBuilder(model));
		} catch (ParserConfigurationException e) {
			log.warn(e);
		}
		
		//model.setEnabled(data.isWritable());
		//model.setEnabled(false);
		
		
		control = new MFormSwtControl(model);
		
		//control.getPageLayout().getLayout().getModel().getChangeHandler().register(this);		
		control.transferToUI();
		control.getPageLayout().getLayout().getModel().validateRules(null);
		control.getPageLayout().getLayout().getModel().fireStatusEvents();

		control.createControl(parent);

		model.loadFromTarget();
		control.transferToUI();

	}

	@Override
	public void setFocus() {
		control.getMainComposite().setFocus();
	}

	@Override
	public Object getFormValue(String name) throws FormException {
		try {
			if (data.getMetadata().getDefinition(name)==null) {
				log.debug("Form Falue not found: " + name);
				return null;
			}
			switch (data.getMetadata().getDefinition(name).getType()) {
			case BOOLEAN:
				if (writable!=null)
					return writable.getBoolean(name,false);
				return data.getBoolean(name,false);
			case LIST:
				CaoList<Access> list = data.getList(name,CapCore.getInstance().getAccess());
				if (list instanceof StringArrayList) {
					return ((StringArrayList)list).getArray();
				}
				String xname = list.getMetadata().getDefinitionAt(0).getName();
				LinkedList<String> out = new LinkedList<String>();
				for (CaoElement<Access> ele : list.getElements())
					out.add(ele.getString(xname));
				return out.toArray(new String[out.size()]);
				
			default:
				if (writable!=null)
					return writable.getString(name);
				return data.getString(name);
			}
		} catch (CaoException e) {
			log.warn(e);
			throw new FormException(e, null);
		}
	}

	@Override
	public void setFormValue(String name, Object value) throws FormException {
		if (writable==null) return; 
		log.debug("SetValue of " + name + " to " + value);
		try {
			if (!dirty) {
				dirty = true;
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
			switch (data.getMetadata().getDefinition(name).getType()) {
			case LIST:
				return;
			default:
//				if (!writable.isDirty()) {
//					createUndoAction();
//				}
				writable.setString(name, value.toString());
			}
		} catch (Exception e) {
			log.warn(e);
		}
	}

	private void makeActions() {
		toggleLinkAction = new Action("",Action.AS_CHECK_BOX) {

			public void run() {
			}
		};
		
		toggleLinkAction.setText("Link with editor");
		toggleLinkAction.setToolTipText("Link with editor");
		toggleLinkAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "/icons/icon_link_with_editor.png")
		);
		
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(toggleLinkAction);
//TODO		drillDownAdapter.addNavigationActions(manager);
	}

	@Override
	public void validateFormAttribute(String name, Class<?> class1)
			throws FormException {
		
	}

	@Override
	public void hotSelected(CaoList selection,CaoGui gui) {
		if (bConnected != null && !bConnected.isDisposed() && bConnected.getSelection()) {
			
			if (data != null)
				observer.remove(data);
			data = null;
			bLabel.setText("");

			if (selection == null || selection.size() != 1) {

				if (formContainer != null) {
					formContainer.dispose();
					formContainer = null;
					main.layout();
				}
				return;
			}
			
			formContainer.dispose();
			formContainer = null;

			data = selection.getElements().next();
			this.gui  = gui;
			
			formContainer = new Composite(main, SWT.NONE);
			formContainer.setLayoutData(BorderLayout.CENTER);
			formContainer.setLayout(new FillLayout());
			
			createModelForm(formContainer);
					
			updateEditable();

			main.layout();

			observer.add(data);

		}
	}

	@Override
	public void propertyChanged(String property, Object value) {
		if (CapCore.PROPERTY_EDITABLE.equals(property)) {
			updateEditable();
		} else
		if (CapCore.PROPERTY_UNDO.equals(property)) {
			try {
				doUndo();
			} catch (Exception e) {
				log.info(e);
			}
		}
	}
	
	void doUndo() throws CaoException {
		writable.reload();
		model.loadFromTarget();
		control.transferToUI();
		writable.reload();
		dirty = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);			
	}

	private void updateEditable() {
		Boolean editable = (Boolean) CapCore.getInstance().getProperty(CapCore.PROPERTY_EDITABLE);
		boolean general = editable != null && editable.booleanValue() && data.isWritable();
		model.setEnabled( general );
		if (general) {
			try {
				model.saveToTarget(false);
				writable = data.getWritableNode();
				for ( CaoMetaDefinition meta : data.getMetadata() ) {
					FObject obj = model.getElement(meta.getName());
					if (obj != null) {
						obj.setEnabled( writable.isWritable(meta.getName()));
					}
				}
			} catch (Exception e) {
				log.info(e);
			}
			dirty = writable.isDirty();
			firePropertyChange(IEditorPart.PROP_DIRTY);
		} else {
			for ( CaoMetaDefinition meta : data.getMetadata() ) {
				FObject obj = model.getElement(meta.getName());
				if (obj != null) {
					obj.setEnabled(false);
				}
			}			
			dirty = false;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

//	private void createGlobalActionHandlers() {
//		undoAction = new UndoActionHandler(this.getSite(), undoContext);
//		redoAction = new RedoActionHandler(this.getSite(), undoContext);
//		IActionBars actionBars = getSite().getActionBars();
//		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),undoAction);
//		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),redoAction);
//	}
	
//	protected void createUndoAction() {
//		IUndoableOperation operation = new UndoAction(this);
//		IWorkbench workbench = getEditorSite().getWorkbenchWindow().getWorkbench();
//		IOperationHistory operationHistory = workbench.getOperationSupport().getOperationHistory();
//		operationHistory = OperationHistoryFactory.getOperationHistory();
//		IUndoContext undoContext = workbench.getOperationSupport().getUndoContext();
//		operation.addContext(undoContext);
//		try {
//			operationHistory.execute(operation,null,null);
//		} catch (Exception e) {
//			log.info(e);
//		}
//	}
	
	@Override
	public void elementCreated(CaoConnection con, String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementDeleted(CaoConnection con, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	getSite().getPage().closeEditor(FormEditor.this,false);
            }
		});
	}

	@Override
	public void elementUpdated(CaoConnection con,  String id) {
		if (isDirty()) return;


		Display.getDefault().asyncExec(new Runnable() {
               public void run() {
            		   try {
						data.reload();
					} catch (CaoException e) {
						log.debug(e);
					}
            		   model.loadFromTarget();
            		   control.transferToUI();
               }
		});
	}

	@Override
	public void elementLink(CaoConnection con, String parentId, String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementUnlink(CaoConnection con, String parentId, String id) {
		// TODO Auto-generated method stub
		
	}
	
}
