package de.mhu.morse.eclipse.views;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.mhu.lib.eecm.model.IEcmConnection;
import de.mhu.lib.eecm.model.IListTableModel;
import de.mhu.lib.eecm.model.ITreeModel;
import de.mhu.lib.eecm.model.ObjectInfo;
import de.mhu.lib.log.AL;
import de.mhu.morse.eclipse.activator.Activator;
import de.mhu.morse.eclipse.views.RepositoryView.TreeParent;
import de.mhu.morse.eclipse.views.docs.DocsTableViewer;

public class DocumentsView extends ViewPart {

	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		Activator.getDefault().setDocumentsView( this );
	}

	private AL log = new AL( DocumentsView.class );
	private DocsTableViewer viewer;
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new DocsTableViewer(parent);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void show(IEcmConnection ecmConnection, String name, ObjectInfo objectInfo) {
		try {
			IListTableModel model = ecmConnection.getListTable( name );
			model.seTarget( objectInfo );
			viewer.setModel( model );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
}
