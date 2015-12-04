package de.mhu.morse.eclipse.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.xml.sax.SAXException;

import de.mhu.lib.eecm.model.EcmManager;
import de.mhu.lib.eecm.model.IEcmConnection;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.form.AForm;
import de.mhu.lib.form.AFormControl;
import de.mhu.lib.form.AFormModelFromXml;
import de.mhu.lib.form.IConfigurable;
import de.mhu.lib.form.TargetConfigurator;
import de.mhu.lib.form.TargetException;
import de.mhu.lib.log.AL;
import de.mhu.morse.eclipse.activator.Activator;

public class RepositoryView extends ViewPart {
	
	private AL log = new AL( RepositoryView.class );
	
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action actionRefresh;
	private Action actionConfig;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class TreeObject implements IAdaptable {
		protected String name;
		private TreeParent parent;
		protected ObjectInfo obj;
		protected TreeRepository manager;
		
		public TreeObject( ObjectInfo object ) throws Exception {
			obj = object;
			if ( obj != null ) 
				this.name = obj.getName();
			else
				this.name = "?";
		}
		
		public String getName() {
			return name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}

		public void setManager(TreeRepository pManager) {
			manager = pManager;
		}
	}
	
	class TreeRepository extends TreeParent {

		private IEcmConnection con = null;
		private String user;
		private String url;
		private String pass;
		private String driver;
		private ITreeModel model;
		
		public TreeRepository( String driver, String url, String user, String pass ) throws Exception {
			super(null);
			this.driver = driver;
			this.url = url;
			this.user = user;
			this.pass = pass;
			name = url;
		}
		
		public void findChildren() {

			if ( isImpl ) return;
			
			TreeObject dummy = getChildren()[0];
			
			if ( con == null ) {
				try {
					if ( driver != null && driver.length() != 0 ) 
						Class.forName( driver );
					con = EcmManager.connect( url, user, pass );
					
					//model = con.getDefaultTreeModel();
					model = con.getTreeModel( "mc.folders" );
					manager = this;
					
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			
			try {
				for ( Iterator<ObjectInfo> rootIter = model.getRoots().iterator(); rootIter.hasNext(); ) {
					TreeParent node = new TreeParent( rootIter.next() );
					addChild(node);
					//node.findChildren();
				}
			} catch ( Exception e ) {
				log.error( e );
			}
						
			removeChild( dummy );
			
			viewer.refresh( this );
			
			isImpl = true;
			
		}
		
	}
	
	class TreeParent extends TreeObject {
		
		private ArrayList<TreeObject> children;
		protected boolean isImpl;
		
		public TreeParent(ObjectInfo ObjectInfo) throws Exception {
			super( ObjectInfo );
			children = new ArrayList<TreeObject>();
			clean();
		}
		
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
			child.setManager( manager );
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			if ( ! isImpl )
				return true;
			return children.size()>0;
		}
		
		public void clean() {
			children.clear();
			isImpl = false;
			try {
				addChild( new TreeObject( null ) );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			viewer.collapseToLevel( this, AbstractTreeViewer.ALL_LEVELS );
			viewer.refresh( this );
		}
		
		public void findChildren() {

			if ( isImpl ) return;
			
			TreeObject dummy = getChildren()[0];
			
			try {
				
				for ( Iterator<ObjectInfo> i = manager.model.getChildFolders( obj ).iterator(); i.hasNext() ; ) {
					
					TreeParent node = new TreeParent( i.next() );
					this.addChild( node );
					
				}
				
				/*
				for ( Iterator<McDocument> i = manager.getChildDocuments( (McFolder)obj ).iterator(); i.hasNext() ; ) {
					
					TreeObject node = new TreeObject( i.next() );
					this.addChild( node );
				}
				*/			
			} catch ( Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			removeChild( dummy );
			
			isImpl = true;
			viewer.refresh( this );

		}
		
	}

	class TreeRoot extends TreeParent {

		public TreeRoot() throws Exception {
			super(null);
			removeChild( getChildren()[0] );
			isImpl = true;
		}
		
		public void findChildren() {
			
		}
		
	}
	
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;

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
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			
			// SwingAppender.showFrame( "Eclipse", 0 );
			
			try {
				invisibleRoot = new TreeRoot( );
				
				try {
					TreeRepository repo = new TreeRepository( null, "morse://localhost:6666/service", "root", "nein" );
					invisibleRoot.addChild( repo );
					
					repo = new TreeRepository( null, "morse://10.10.10.1:6666/service", "root", "nein" );
					invisibleRoot.addChild( repo );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			TreeObject to1 = new TreeObject("Leaf 1");
			TreeObject to2 = new TreeObject("Leaf 2");
			TreeObject to3 = new TreeObject("Leaf 3");
			TreeParent p1 = new TreeParent("Parent 1");
			p1.addChild(to1);
			p1.addChild(to2);
			p1.addChild(to3);
			
			TreeObject to4 = new TreeObject("Leaf 4");
			TreeParent p2 = new TreeParent("Parent 2");
			p2.addChild(to4);
			
			TreeParent root = new TreeParent("Root");
			root.addChild(p1);
			root.addChild(p2);
			
			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(root);
			*/
			
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_FILE;
			
			// if (obj instanceof TreeObject && ((TreeObject)obj).obj.isFolder() )
			//   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public RepositoryView() {
		
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
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		/*
		viewer.addOpenListener( new IOpenListener() {

			public void open(OpenEvent event) {
				showMessage( "Open " );				
			}
			
		});
		*/
		viewer.addTreeListener( new ITreeViewerListener() {

			public void treeCollapsed(TreeExpansionEvent event) {
				
			}

			public void treeExpanded(TreeExpansionEvent event) {
				
				final TreeParent finalObject = (TreeParent)event.getElement();
				Display display = viewer.getControl().getDisplay();
				display.asyncExec(new Runnable() {
					public void run() {
						finalObject.findChildren();
					}
				});

			}
			
		});
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RepositoryView.this.fillContextMenu(manager);
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
		manager.add(actionRefresh);
		manager.add(new Separator());
		manager.add(actionConfig);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionRefresh);
		manager.add(actionConfig);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionRefresh);
		manager.add(actionConfig);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		actionRefresh = new Action() {
			public void run() {
				// showMessage("Action 1 executed");
				((TreeParent)((ITreeSelection)viewer.getSelection()).getFirstElement()).clean();
			}
		};
		actionRefresh.setText("Refresh");
		actionRefresh.setToolTipText("Collapse the Node and refresh the childrens");
		actionRefresh.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_REDO ));
		
		actionConfig = new Action() {
			public void run() {
				try {
					// showMessage("Action 2 executed");
					TreeParent selected = (TreeParent)((ITreeSelection)viewer.getSelection()).getFirstElement();
					if ( selected == null ) return;
					
					IConfigurable obj = selected.manager.model.getConfigurableObject( selected.obj );
					if ( obj == null ) return;
					
					AForm form = obj.getConfigurationForm( selected.obj );
					
					if ( form == null ) return;
					TargetConfigurator configurator = new TargetConfigurator( form, obj );
					configurator.getValues();
					Shell shell = viewer.getControl().getShell();
					if ( AFormControl.showDialog( shell, form ) ) {
						try {
							configurator.setValues( false );
						} catch (TargetException e) {
							log.warn( e );
						}
					}
				} catch ( Throwable e ) {
					log.warn( e );
				}
			}
		};
		actionConfig.setText("Configure");
		actionConfig.setToolTipText("Configure this Repository Connection");
		actionConfig.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				
				TreeParent selected = (TreeParent)((ITreeSelection)viewer.getSelection()).getFirstElement();
				if ( selected == null ) return;
				
				// showMessage("Double-click detected on "+obj.toString());
				Activator.getDefault().getDocumentsView().show( selected.manager.con, "repo", selected.obj );
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Repository View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}