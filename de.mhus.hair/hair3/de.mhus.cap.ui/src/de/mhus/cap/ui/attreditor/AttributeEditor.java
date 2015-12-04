package de.mhus.cap.ui.attreditor;

import java.util.LinkedList;

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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.CapEventAdapter;
import de.mhus.cap.ui.oleditor.BorderLayout;
import de.mhus.cap.ui.oleditor.ObjectListInput;
import de.mhus.cap.ui.outline.OutlinePage;
import de.mhus.lib.MSingleton;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.ICaoChangeListener;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.cao.util.MutableElement;
import de.mhus.lib.form.FormException;
import de.mhus.lib.form.IFormDynamic;
import de.mhus.lib.form.MFormModel;
import de.mhus.lib.form.objects.FObject;
import de.mhus.lib.form.objects.FObject.Listener;
import de.mhus.lib.form.objects.FObject.WHAT_CHANGED;
import de.mhus.lib.form.objects.SimpleDynTableProvider;
import de.mhus.lib.logging.MLog;
import de.mhus.lib.swt.form.MFormSwtControl;

public class AttributeEditor extends EditorPart implements IFormDynamic, ICaoChangeListener {
 
	public static final String ID = "de.mhus.cap.ui.attreditor";
	
	private CaoElement data;

	private CaoWritableElement writable;

	private MFormSwtControl control;

	private MFormModel model;
	private Action toggleLinkAction;

	private boolean dirty;

	private CaoObserver observer;

	private Composite main;

	private Button bConnected;

	private Composite formContainer;

	private Label bLabel;

	private CapEventAdapter capListener;

	private OutlinePage myOutlinePage;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
		.getLog(AttributeEditor.class);

	@Override
	public void doSave(IProgressMonitor arg0) {
		try {
//			model.targetSet(false);
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
		setInput(input);
		setSite(site);
		setPartName(in.getName());
		setTitleToolTip(in.getToolTipText());
		
		observer = data.getConnection().createObserver();
		observer.setListener(this);
		observer.add(data);
		
		capListener = new CapEventAdapter() {

			@Override
			public void hotSelected(final CaoList selection) {
				Display.getDefault().asyncExec(new Runnable() {
		            public void run() {
				if (bConnected != null && !bConnected.isDisposed() && bConnected.getSelection()) {
					

					CaoElement newData = null;
					
					if (selection != null && selection.size() == 1) {
						try {
							newData = selection.getElements().next();
						} catch (CaoException e) {
							MLog.e(e);
						}
					}
					
					updateUi( newData );
					
				}
		            }});
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

		};
		CapCore.getInstance().getEventHandler().registerWeak(capListener);

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
		
		main.setLayout( new de.mhus.cap.ui.oleditor.BorderLayout() );
		
		Composite toolBar = new Composite(main, SWT.NONE);
		toolBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		toolBar.setLayoutData(BorderLayout.NORTH);
		bConnected = new Button(toolBar, SWT.TOGGLE | SWT.FLAT);
		bConnected.setImage(Activator.getImageDescriptor("icons/connect.png").createImage());
		bConnected.setToolTipText("Connect with selection");
		
		try {
			boolean found = false;
			for ( IEditorPart editor : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditors()) {
				if (editor.getClass().equals(this.getClass())) {
					found = true; break;
				}
			}
			if (!found)
				bConnected.setSelection(true);
		} catch (Exception e) {}
		
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
						src.saveToTarget(false);
					} catch (FormException e) {
						log.info(e);
					}
			}
			
		});
		
	}
	
	private void createModelForm(Composite parent) {

		model = null;
		
		if (data instanceof ICustomModel) {
			try {
				model = ((ICustomModel)data).getConfigurationForm(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		try {
			bLabel.setText( " " + data.getName() + " (" + data.getId() + ")" );
		} catch (CaoException e1) {
			bLabel.setText("");
		}
		
		if (model == null) {
			model = new CaoFormModel(MSingleton.instance().getActivator(),this, data);
		}
		
		//model.setEnabled(data.isWritable());
		//model.setEnabled(false);
		
		control = new MFormSwtControl(model);
		
		//control.getPageLayout().getLayout().getModel().getChangeHandler().register(this);		

		model.loadFromTarget();
		control.createControl(parent);

//		control.transferToUI();
//		control.getPageLayout().getLayout().getModel().fireStatusEvents();

//		model.getValuesFromTarget();
//		model.loadFromTarget();
//		control.transferToUI();

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
				SimpleDynTableProvider provider = new SimpleDynTableProvider();
				CaoList list = data.getList(name);
				provider.titles = new String[list.getMetadata().getCount()];
				for (int i = 0; i < provider.titles.length; i++)
					provider.titles[i] = list.getMetadata().getDefinitionAt(i).getName();
				for (CaoElement ele : list.getElements()) {
					String[] values = new String[provider.titles.length];
					for (int j = 0; j < provider.titles.length; j++)
						values[j] = ele.getString(provider.titles[j]);
					provider.rows.add(values);
				}
				return provider;
				
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
				if (value instanceof SimpleDynTableProvider) {
					SimpleDynTableProvider provider = (SimpleDynTableProvider)value;
					CaoList list = data.getList(name);
					LinkedList<CaoElement> dataList = new LinkedList<CaoElement>();
					for (String[] row : provider.rows) {
						MutableElement e = new MutableElement(data);
						int i = 0;
						for (String n : provider.titles) {
							 CaoMetaDefinition meta = list.getMetadata().getDefinition(n);
							if (meta != null) {
								e.getMetaDefinitions().add(meta);
								e.setString(meta.getName(), row[i]);
							}
							i++;
						}
						dataList.add(e);
					}
					CaoList caoList = new LinkedCaoList(data, dataList);
					writable.setList(name, caoList);
				}
				
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
	
	protected void updateUi(CaoElement newData) {
		if (data != null)
			observer.remove(data);
		data = null;
		bLabel.setText("");

		formContainer.dispose();
		formContainer = null;

		data = newData;
		
		if (data != null) {
			formContainer = new Composite(main, SWT.NONE);
			formContainer.setLayoutData(BorderLayout.CENTER);
			formContainer.setLayout(new FillLayout());
			
			createModelForm(formContainer);
					
			updateEditable();
		}
		
		main.layout();

		observer.add(data);

		try {
			if (myOutlinePage != null) {
				myOutlinePage.setInput(getEditorInput(),newData);
				myOutlinePage.refresh();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				model.saveToTarget(true);
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
            	getSite().getPage().closeEditor(AttributeEditor.this,false);
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

	@Override
	public boolean isValid() {
		return !main.isDisposed();
	}

	@Override
	public void elementStructurChanged(CaoConnection con, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
         		   try {
						data.reload();
					} catch (CaoException e) {
						log.debug(e);
					}
         		   updateUi(data);
            }
		});
	}

	@Override
	public void disconnected(CaoConnection con) {
		elementDeleted(con, null);
	}

	@Override
	public Object getAdapter(Class required) {
      if (IContentOutlinePage.class.equals(required)) {
         if (myOutlinePage == null) {
            myOutlinePage = new OutlinePage();
            myOutlinePage.setInput(getEditorInput(),((ObjectListInput)getEditorInput()).getElement());
         }
         return myOutlinePage;
      }
      return super.getAdapter(required);
   }
	
}
