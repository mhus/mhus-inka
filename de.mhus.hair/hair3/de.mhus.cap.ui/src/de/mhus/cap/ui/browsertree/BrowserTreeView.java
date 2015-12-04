package de.mhus.cap.ui.browsertree;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.CapEventAdapter;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.cap.core.dnd.CaoTransfer;
import de.mhus.cap.core.dnd.CapDragListener;
import de.mhus.cap.core.dnd.CapDropListener;
import de.mhus.cap.ui.Activator;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.logging.Log;


public class BrowserTreeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.mhus.cap.ui.browsertree_View";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private BrowserTreeRoot invisibleRoot;
	
	private Action refreshAction;
	private Action connectAction;
	private Action disconnectAction;
	private Action renameAction;

	//private Action toggleLinkAction;
	private boolean linked;
	
	private static Log log = Log.getLog(BrowserTreeView.class);
	
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		private CapEventAdapter ea;
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof BrowserTreeObject) {
				return ((BrowserTreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof BrowserTreeObject) {
				return ((BrowserTreeObject)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof BrowserTreeObject)
				return ((BrowserTreeObject)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			
			invisibleRoot = new BrowserTreeRoot();
			invisibleRoot.setName("");
			
			try {
				
				for (ConnectionDefinition con : CapCore.getInstance().getConnections()) {
					addConnection(con);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ea = new CapEventAdapter() {
				
				@Override
				public void connectionAdd(ConnectionDefinition newConnection) {
					addConnection(newConnection);
				}

				@Override
				public void connectionRemoved(ConnectionDefinition oldConnection) {
					removeConnection(oldConnection);
				}

				@Override
				public void connectionChanged(ConnectionDefinition con) {
					for (BrowserTreeObject child : invisibleRoot.getChildren() ) {
						if (child instanceof CaoBrowserTreeRootNode && ((CaoBrowserTreeRootNode)child).equals(con)) {
							((CaoBrowserTreeRootNode)child).disconnect();
							viewer.refresh();
							return;
						}
					}
				}

			};
			CapCore.getInstance().getEventHandler().registerWeak(ea);
			
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if (obj instanceof BrowserTreeObject)
				return ((BrowserTreeObject)obj).getImage();
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class IndexSorter extends ViewerSorter {

		@Override
		public void sort(Viewer viewer, Object[] elements) {
		//	super.sort(viewer, elements);
		}
		
	}
		
	/**
	 * The constructor.
	 */
	public BrowserTreeView() {
	}

	protected void addConnection(final ConnectionDefinition con) {
		viewer.getTree().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				invisibleRoot.addChild( new CaoBrowserTreeRootNode(viewer.getTree().getShell(),con, BrowserTreeView.this ) );
				viewer.refresh();
			}
			
		});
	}

	protected void removeConnection(final ConnectionDefinition oldConnection) {
		viewer.getTree().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				for (BrowserTreeObject child : invisibleRoot.getChildren() ) {
					if (child instanceof CaoBrowserTreeRootNode && ((CaoBrowserTreeRootNode)child).equals(oldConnection)) {
						((CaoBrowserTreeRootNode)child).disconnect();
						invisibleRoot.removeChild( child );
						viewer.refresh();
						return;
					}
				}
			}
			
		});
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new IndexSorter());
		viewer.setInput(getViewSite());

		int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_TARGET_MOVE;
		Transfer[] transferTypes = new Transfer[]{CaoTransfer.getInstance(),FileTransfer.getInstance()};
		viewer.addDropSupport(operations, transferTypes, new CapDropListener(viewer));
		viewer.addDragSupport(operations, transferTypes, new CapDragListener(viewer));

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "example_treeview.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		hookSelectionChangedAction();
		hookKeyboardActions();
		
	}

	private void hookKeyboardActions() {
		viewer.getTree().addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.F2) {
					try {
						IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
						handlerService.executeCommand("de.mhus.cap.ui.rename_connection", null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BrowserTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(connectAction);
		manager.add(disconnectAction);
		manager.add(renameAction);
//		manager.add(toggleLinkAction);
//		manager.add(new Separator());
//		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		
		ITreeSelection sel = (ITreeSelection)viewer.getSelection();
		List<?> list = sel.toList();
		List<CaoElement> outList = null;
		CaoBrowserTreeNode first = null;
		try {
			for (Object item : list) {
				if (item instanceof CaoBrowserTreeNode) {
					first = (CaoBrowserTreeNode)item;
					if (outList==null) outList = new LinkedList<CaoElement>();
					outList.add( ((CaoBrowserTreeNode)item).getCaoElement() );
				}
			}
			
			if (outList!=null) {
				CaoGui.elementSelectedMenu(new LinkedCaoList(outList.get(0),outList),manager);
			}
			
		} catch (Exception e) {
			log.warn(e);
		}

		 drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		 manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(connectAction);
		manager.add(disconnectAction);
		manager.add(renameAction);
//		manager.add(toggleLinkAction);
//		manager.add(action2);
//		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				
				ITreeSelection sel = (ITreeSelection)viewer.getSelection();
				List<?> list = sel.toList();
				try {
					for (Object item : list) {
						if (item instanceof CaoBrowserTreeNode) {
							((CaoBrowserTreeNode)item).getCaoElement().reload();
						}
						viewer.refresh(item);
					}
					
				} catch (Exception e) {
					log.warn(e);
				}
				
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh");
		refreshAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "/icons/arrow_refresh.png")
		);

		connectAction = new Action() {
			public void run() {
				
				ITreeSelection sel = (ITreeSelection)viewer.getSelection();
				List<?> list = sel.toList();
				try {
					for (Object item : list) {
						if (item instanceof CaoBrowserTreeRootNode) {
							((CaoBrowserTreeRootNode)item).connect();
							viewer.expandToLevel(item, 1);
						}
						// viewer.refresh(item);
					}
					
				} catch (Exception e) {
					log.warn(e);
				}
				
			}
		};
		connectAction.setText("Connect");
		connectAction.setToolTipText("Connect");
		connectAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "/icons/database_connect.png")
		);

		disconnectAction = new Action() {
			public void run() {
				
				ITreeSelection sel = (ITreeSelection)viewer.getSelection();
				List<?> list = sel.toList();
				try {
					for (Object item : list) {
						if (item instanceof CaoBrowserTreeRootNode) {
							((CaoBrowserTreeRootNode)item).disconnect();
						}
						// viewer.refresh(item);
					}
					
				} catch (Exception e) {
					log.warn(e);
				}
				
			}
		};
		disconnectAction.setText("Disconnect");
		disconnectAction.setToolTipText("Disconnect");
		disconnectAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "/icons/database_disconnect.png")
		);
		
		renameAction = new Action() {
			public void run() {
				try {
					IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
					handlerService.executeCommand("de.mhus.cap.ui.rename_connection", null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		renameAction.setText("Rename");
		renameAction.setToolTipText("Rename");
		renameAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						Activator.PLUGIN_ID, "/icons/database_edit.png")
		);
		
//		toggleLinkAction = new Action("",Action.AS_CHECK_BOX) {
//
//			public void run() {
//				linked = toggleLinkAction.isChecked();
//			}
//		};
//		
//		toggleLinkAction.setText("Link with editor");
//		toggleLinkAction.setToolTipText("Link with editor");
//		toggleLinkAction.setImageDescriptor(
//				AbstractUIPlugin.imageDescriptorFromPlugin(
//						Activator.PLUGIN_ID, "/icons/icon_link_with_editor.png")
//		);
		
	}
		
//		action1.setText("Action 1");
//		action1.setToolTipText("Action 1 tooltip");
//		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
//		
//		action2 = new Action() {
//			public void run() {
//				showMessage("Action 2 executed");
//			}
//		};
//		action2.setText("Action 2");
//		action2.setToolTipText("Action 2 tooltip");
//		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
//		doubleClickAction = new Action() {
//			public void run() {
//				ISelection selection = viewer.getSelection();
//				Object obj = ((IStructuredSelection)selection).getFirstElement();
//				showMessage("Double-click detected on "+obj.toString());
//			}
//		};
//	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// doubleClickAction.run();
	
				ITreeSelection sel = (ITreeSelection)event.getSelection();
				List<?> list = sel.toList();
				List<CaoElement> outList = null;
				CaoBrowserTreeNode first = null;
				try {
					for (Object item : list) {
						if (item instanceof CaoBrowserTreeNode) {
							first = (CaoBrowserTreeNode)item;
							if (outList==null) outList = new LinkedList<CaoElement>();
							outList.add( ((CaoBrowserTreeNode)item).getCaoElement() );
						}
					}
					
					if (outList==null || outList.size() < 1) {
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
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ITreeSelection sel = (ITreeSelection)event.getSelection();
				List<?> list = sel.toList();
				List<CaoElement> outList = null;
				try {
					for (Object item : list) {
						if (item instanceof CaoBrowserTreeNode) {
							if (outList==null) {
								outList = new LinkedList<CaoElement>();
							}
							outList.add( ((CaoBrowserTreeNode)item).getCaoElement() );
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

//	private void showMessage(String message) {
//		MessageDialog.openInformation(
//			viewer.getControl().getShell(),
//			"Sample View",
//			message);
//	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void refresh(BrowserTreeObject node) {
		viewer.refresh(node,true);
		//viewer.reveal(node);
	}

	public void refreshLabel(BrowserTreeObject node) {
		viewer.refresh(node, true);
	}

	public void collapse(CaoBrowserTreeRootNode item) {
		viewer.collapseToLevel(item, TreeViewer.ALL_LEVELS);
//		viewer.refresh(item);
	}

	public void refresh(Object element) {
		if (element == null)
			viewer.refresh(true);
		else
			viewer.refresh(element,true);
	}
}