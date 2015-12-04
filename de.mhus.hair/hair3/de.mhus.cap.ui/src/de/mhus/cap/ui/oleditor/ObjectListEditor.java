package de.mhus.cap.ui.oleditor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.CapEventAdapter;
import de.mhus.cap.core.dnd.ICaoExchange;
import de.mhus.cap.ui.browsertree.CaoGui;
import de.mhus.cap.ui.outline.OutlinePage;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.cao.ICaoChangeListener;
import de.mhus.lib.cao.util.LinkedCaoList;

public class ObjectListEditor extends EditorPart implements ICaoChangeListener {

	public static final String ID = "de.mhus.cap.ui.oleditor.OLEditor";
	private CaoList list;
	private ObjectList listView;
	private CaoObserver observer;
	private Button bConnected;
	private Composite main;
	protected CaoElement lastParent;
	private CapEventAdapter capListener;
	private OutlinePage myOutlinePage;
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(ObjectListEditor.class);
	
	public ObjectListEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		ObjectListInput in = (ObjectListInput)input;
		list = in.getList();
		setInput(input);
		setSite(site);
		setPartName(in.getName());
		setTitleToolTip(in.getToolTipText());
		
		observer = list.getConnection().createObserver();
		observer.setListener(this);
		observer.add(list.getParent());
		
		capListener = new CapEventAdapter() {
			@Override
			public void hotSelected(final CaoList selection) {
				Display.getDefault().asyncExec(new Runnable() {
		            public void run() {
				if (bConnected != null && !bConnected.isDisposed() && bConnected.getSelection()) {

					if (selection.size() <= 0)
						return;
					
					try {
						
						CaoElement newParent = selection.getElements().next();
						list = newParent.getChildren();
						if (list != null) {
							if ( (lastParent == null && newParent == null) || lastParent.equals(newParent))
								return;
							lastParent = newParent;
						} else {
							return;
						}
						
		            	listView.dispose();
		
		            		
//				            	CaoListIterator elements = list.getElements();
//				            	if (elements.hasNext()) {
//				            		CaoElement first = elements.next();
//				            		if (first.isNode()) {
//				            			CaoList firstList = first.getChildren();
//				            			setPartName(first.getName());
//					            		listView = new ObjectList(main, firstList, gui,observer);
//					            		listView.setLayoutData(BorderLayout.CENTER);
//				            		} else {
//				            			setPartName(list.getId());
//					            		listView = new ObjectList(main, list, gui,observer);
//					            		listView.setLayoutData(BorderLayout.CENTER);		            			
//				            		}
			            		listView = new ObjectList(main, list,observer);
			            		listView.setLayoutData(BorderLayout.CENTER);		 
			            		firePropertyChange(IEditorPart.PROP_TITLE);
			            		
			            		if (myOutlinePage != null) {
			            			myOutlinePage.setInput(getEditorInput(),lastParent);
			            			myOutlinePage.refresh();
			            		}
//				            	}
		            	} catch (Exception e) {
		            		log.error(e);
		            	}
		        		main.layout(true);
			 
				}
		            }});
			}

		};
		CapCore.getInstance().getEventHandler().registerWeak(capListener);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		main = new Composite(parent, SWT.NONE );
		
		main.setLayout( new BorderLayout() );
		
		Composite toolBar = new Composite(main, SWT.NONE);
		toolBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		toolBar.setLayoutData(BorderLayout.NORTH);
		bConnected = new Button(toolBar, SWT.TOGGLE | SWT.FLAT);
		bConnected.setImage(Activator.getImageDescriptor("icons/connect.png").createImage());
		bConnected.setToolTipText("Connect with selection");
		
		listView = new ObjectList(main, list,observer);
		lastParent = list.getParent();
		listView.setLayoutData(BorderLayout.CENTER);
		
		hookDoubleClickAction();
		hookSelectionChangedAction();
		hookContextMenu();
	}

	@Override
	public void setFocus() {
		listView.getTableViewer().getTable().setFocus();
	}

	private void hookDoubleClickAction() {
		listView.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// doubleClickAction.run();
	
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				List<?> list = sel.toList();
				List<CaoElement> outList = null;
				// CaoElement first = null;
				try {
					for (Object item : list) {
						if (item instanceof ICaoExchange) {
							//first = (CaoElement)item;
							if (outList==null) outList = new LinkedList<CaoElement>();
							outList.add( ((ICaoExchange)item).getElement() );
						}
					}
					
					if (outList==null) {
						return;
					}
					
					CaoGui.elementSelected(new LinkedCaoList(outList.get(0),outList));
					
				} catch (Exception e) {
					log.warn(e);
				}
			}
		});
	}
	
	private void hookSelectionChangedAction() {
		listView.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection sel = (StructuredSelection)event.getSelection();
				List<?> list = sel.toList();
				List<CaoElement> outList = null;
				try {
					for (Object item : list) {
						if (item instanceof ICaoExchange) {
							//first = (CaoElement)item;
							if (outList==null) outList = new LinkedList<CaoElement>();
							outList.add( ((ICaoExchange)item).getElement() );
						}
					}
					
					if (outList==null) {
						CapCore.getInstance().setHotSelectedItems(null);
						return;
					}
					
					CapCore.getInstance().setHotSelectedItems(new LinkedCaoList(outList.get(0),outList) );
					
				} catch (CaoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ObjectListEditor.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(listView.getTableViewer().getControl());
		listView.getTableViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, listView.getTableViewer());
		getSite().setSelectionProvider(listView.getTableViewer());
	}

	private void fillContextMenu(IMenuManager manager) {
		
		StructuredSelection sel = (StructuredSelection)listView.getTableViewer().getSelection();
		List<?> list = sel.toList();
		List<CaoElement> outList = null;
		try {
			for (Object item : list) {
				if (item instanceof ICaoExchange) {
					if (outList==null) outList = new LinkedList<CaoElement>();
					outList.add( ((ICaoExchange)item).getElement() );
				}
			}
			
			if (outList==null) {
				return;
			}
			
			CaoGui.elementSelectedMenu(new LinkedCaoList(outList.get(0),outList),manager);
			
		} catch (Exception e) {
			log.warn(e);
		}

		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public void elementCreated(CaoConnection con, String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void elementDeleted(CaoConnection con, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	getSite().getPage().closeEditor(ObjectListEditor.this,false);
            }
		});
	}

	@Override
	public void elementLink(CaoConnection con, String parentId, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	listView.getTableViewer().refresh();
            }
		});
	}

	@Override
	public void elementUnlink(CaoConnection con, String parentId, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	listView.getTableViewer().refresh();
            }
		});
	}

	@Override
	public void elementUpdated(CaoConnection con, String id) {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	listView.getTableViewer().refresh(true);
            }
		});
	}

	@Override
	public void elementStructurChanged(CaoConnection con, String id) {
		elementUpdated(con,id);
	}

	@Override
	public boolean isValid() {
		return !main.isDisposed();
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
