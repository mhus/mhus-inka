package de.mhus.cap.ui.browsertree;

import java.util.LinkedList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.dnd.CapDropListener.LOCATION;
import de.mhus.cap.core.dnd.CapDropListener.OPERATION;
import de.mhus.cap.core.dnd.ICaoExchange;
import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.cao.ICaoChangeListener;
import de.mhus.lib.logging.Log;

public class CaoBrowserTreeNode extends BrowserTreeObject implements ICaoChangeListener, ICaoExchange {

	private CaoElement node;
	private CaoApplication app;
	protected CaoObserver observer;
	protected BrowserTreeView view;
	private CaoBrowserTreeNode base;
	private ICaoImageProvider imageProvider;
	private static Log log = Log.getLog(CaoBrowserTreeNode.class);
	
	public CaoBrowserTreeNode(CaoApplication app, CaoElement node, BrowserTreeView view,CaoBrowserTreeNode base) {
		this(app,node,view);
		this.base = base;
	}
	
	public CaoBrowserTreeNode(CaoApplication app, CaoElement node, BrowserTreeView view) {
		this.app = app;
		this.node = node;
		this.view = view;
		observer = node.getConnection().createObserver();
		observer.setListener(this);
		observer.add(node);
		try {
			imageProvider = CapCore.getInstance().getImageProvider(node.getApplication().getConfig().getConfig("tree").getString("imageprovider",CapCore.DEFAULT_IMAGE_PROVIDER));
		} catch (Throwable t) {
			imageProvider = CapCore.getInstance().getImageProvider(CapCore.DEFAULT_IMAGE_PROVIDER);
		}
	}

	public CaoElement getCaoElement() {
		return node;
	}
	

	@Override
	public BrowserTreeObject[] getChildren() {
		CaoList list;
		try {
			if (log.isTraceEnabled()) log.trace("Load Children: " + node.getId() );
			list = node.getChildren();
		} catch (CaoException e) {
			log.warn(e);
			return null;
		}
		LinkedList<CaoBrowserTreeNode> out = new LinkedList<CaoBrowserTreeNode>();
		try {
			for ( CaoElement data : list.getElements() ) {
				//observer.add(data);
				out.add(new CaoBrowserTreeNode(app,(CaoElement)data,view,base==null?this:base));
			}
		} catch (CaoException e) {
			log.e(e);
			return null;
		}
		return out.toArray(new BrowserTreeObject[ out.size() ] );
	}

	@Override
	public String getLabel() {
		try {
			return node.getName();
		} catch (CaoException he) {
			log.warn(he);
			return "?";
		}
	}

	@Override
	public boolean hasChildren() {
		return node.isNode();
	}

	@Override
	public Image getImage() {
		Image image = null;
		ICaoImageProvider imageProvider = getImageProvider();
		if (imageProvider != null )
			image = imageProvider.getImage(node,null);
		
		if (image==null) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (node.isNode())
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
		return image;
	}

	public CaoApplication getApplication() {
		return app;
	}

	@Override
	public void elementCreated(CaoConnection con, String id) {
			
	}

	@Override
	public void elementDeleted(CaoConnection con, String id) {
		
	}

	@Override
	public void elementUpdated(CaoConnection con, String id) {
		try {
			if (id.equals(node.getId())) {
				Display.getDefault().asyncExec(new Runnable() {
		               public void run() {
		            		   view.refreshLabel(CaoBrowserTreeNode.this);
		               }
				});
			}
		} catch (CaoException e) {
			log.debug(e);
		}
	}

	@Override
	public void elementLink(CaoConnection con, String parentId, String id) {
		try {
			if (parentId.equals(node.getId())) {
				Display.getDefault().asyncExec(new Runnable() {
		               public void run() {
		            		   view.refresh(CaoBrowserTreeNode.this);
		               }
				});
			}
		} catch (CaoException e) {
			log.debug(e);
		}
	}

	@Override
	public void elementUnlink(CaoConnection con, String parentId, String id) {
		try {
			if (parentId.equals(node.getId())) {
				Display.getDefault().asyncExec(new Runnable() {
		               public void run() {
		            	   
		            	   view.refresh(CaoBrowserTreeNode.this);
		               }
				});
			}
		} catch (CaoException e) {
			log.debug(e);
		}
	}


	@Override
	public void elementStructurChanged(CaoConnection con, String id) {
		try {
			node.reload();
		} catch (CaoException e) {
			log.debug(e);
		}
		elementUpdated(con,id);
	}
	
	@Override
	public CaoElement getElement() {
		return node;
	}

	@Override
	public boolean doDrop(LOCATION loc, OPERATION oper, ICaoExchange[] providers) {
		if (log.isTraceEnabled()) try {log.trace("doDrop: " + loc.toString() + " " + oper.toString() + " " + node.getId() );} catch (CaoException e) {}
		
		return CapCore.getInstance().doDrop(loc, oper, providers, this);
		
	}

	public ICaoImageProvider getImageProvider() {
		return base==null?imageProvider:base.getImageProvider();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void disconnected(CaoConnection caoConnection) {
	}

}
