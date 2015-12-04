package de.mhu.morse.eclipse.views.docs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.mhu.com.morse.eecm.MorseDocumentList;
import de.mhu.lib.eecm.model.IListTableModel;

public class DocsTableViewer {

	private Table table;
	private FormToolkit toolkit;
	private IListTableModel model;
	
	public DocsTableViewer(Composite parent) {
		
		createTable(parent);
		
	}

	private void createTable(Composite parent) {
		
		toolkit = new FormToolkit(parent.getDisplay());
		
		parent.setLayoutData( new GridData ( GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH ) );
		parent.setLayout( new FillLayout() );
		
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = toolkit.createTable( parent, style );
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
				
	}
	
	private void createColumns() {

		try {
			for ( String name : model.getDefaultColumns() ) {
				TableColumn column = new TableColumn(table, SWT.LEFT, table.getColumnCount() );		
				column.setText( name );
				column.setWidth( 100 );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public Control getControl() {
		return table.getParent();
	}

	public void setModel(IListTableModel model2) {
		model = model2;
		createColumns();
	}
	
}
