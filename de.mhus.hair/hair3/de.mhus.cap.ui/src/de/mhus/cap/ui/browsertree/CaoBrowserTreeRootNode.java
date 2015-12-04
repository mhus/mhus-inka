package de.mhus.cap.ui.browsertree;

import java.util.LinkedList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.ConnectionDefinition;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.cao.util.NoneApplication;
import de.mhus.lib.swt.form.MFormSwtWizard;

public class CaoBrowserTreeRootNode extends CaoBrowserTreeNode {

	private BrowserTreeObject[] children;
	private Shell shell;
	private CaoApplication app;
	private ConnectionDefinition con;

	public CaoBrowserTreeRootNode(Shell shell, ConnectionDefinition con, BrowserTreeView view) {
		super(NoneApplication.getInstance(),con,view);
		this.shell = shell;
		this.con = con;
	}
	
	@Override
	public BrowserTreeObject[] getChildren() {
		if (!connect()) {
			CapCore.getInstance().showError("can't connect");
			return null;
		}
		return children;
	}

	public synchronized boolean connect() {
		if (children!=null) return true;
		
		try {
			// connect wizard
			MFormSwtWizard wizard = new MFormSwtWizard();
			wizard.setWindowTitle( "Open " + getLabel() );
			wizard.setActivator(CapCore.getInstance().getFactory().getActivator(con.getService()));
			CaoForm form = CapCore.getInstance().getFactory().createConfiguration(con.getService());
			form.fromUrl(con.getUrl());
			
			wizard.appendPages(form);
	
			if ( wizard.show( shell ) != MFormSwtWizard.OK ) {
				wizard.dispose();
				return false;
			}

			wizard.dispose();

			con.setUrl(form.toUrl(CapCore.getInstance().isNoSecretMode()));
			CapCore.getInstance().saveConnections();
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		
		// connect
		try {
			CaoConnection con = (CaoConnection) CapCore.getInstance().getFactory().createConnection(this.con.getService(),this.con.getUrl(),null);
			app = con.getApplication(CaoDriver.APP_CONTENT);

			LinkedList<BrowserTreeObject> out = new LinkedList<BrowserTreeObject>();
			
			CaoElement roots = app.getDefaultTree();
			
			for (CaoElement node : roots.getChildren().getElements()) {
					out.add( new CaoBrowserTreeNode(app,node,view) );
			}
			
			children = out.toArray(new BrowserTreeObject[out.size()]);
			
			observer = con.createObserver();
			observer.setListener(this);
			
		} catch (CaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				view.refresh(CaoBrowserTreeRootNode.this);
			}
			
		});
		
		return true;
	}

	public void disconnect() {
		if (children==null) return;
		app.getConnection().disconnect();
	}
	
	@Override
	public void disconnected(CaoConnection caoConnection) {
		if (children==null) return;
		app = null;
		children = null;
		observer = null;
		view.collapse(this);
		
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				view.refresh(CaoBrowserTreeRootNode.this);
			}
			
		});

	}

	@Override
	public Image getImage() {
		return con.getImage(app!=null);
	}

	@Override
	public String getLabel() {
		try {
			return con.getTitle();
		} catch (CaoException e) {
		}
		return "?";
	}

	@Override
	public boolean hasChildren() {
		return true;
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other instanceof ConnectionDefinition)
			return con.equals(other);
		return super.equals(other);
	}

}
