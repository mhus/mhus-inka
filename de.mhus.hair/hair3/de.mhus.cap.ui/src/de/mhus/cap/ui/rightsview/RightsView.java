package de.mhus.cap.ui.rightsview;

import java.util.LinkedList;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.CapEventAdapter;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoPolicy;

public class RightsView extends ViewPart {

	private TableViewer viewer;
	private CaoList selection;
	private CapEventAdapter capListener;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent,SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		
		// Set up the table
	    Table table = viewer.getTable();
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));
	    table.setHeaderVisible(true);
	    table.setLinesVisible(true);
	    
	    // Add the first name column
	    TableColumn tc1 = new TableColumn(table, SWT.LEFT);
	    tc1.setWidth(100);
	    tc1.setText("Type");
	    
	    TableColumn tc2 = new TableColumn(table, SWT.LEFT);
	    tc2.setWidth(100);
	    tc2.setText("Principal");
	    
	    TableColumn tc3 = new TableColumn(table, SWT.LEFT);
	    tc3.setWidth(400);
	    tc3.setText("Access");
	    
	}

	@Override
	public void setFocus() {
		
	}

	class ContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (selection == null || selection.size() != 1) {
				return new Data[0];
			}
			
			try {
				CaoElement caoElement = selection.getElements().next();
				
				LinkedList<Data> out = new LinkedList<Data>();
				
				CaoPolicy policy = caoElement.getAccessPolicy();
				
				if (policy == null ) return new Data[0];
				
				CaoMetadata metas = policy.getMetadata();

				if (metas == null ) 
					return new Data[0];

//				// Collect current rights
//				for (CaoMetaDefinition meta :  metas.getDefinitionsWithCategory(CaoPolicy.CATEGORY_RIGHT)) {
//					out.add(new Data("current",meta.getName(),policy.getString(meta.getName())));
//				}
				
				// collect policy lists
				for (CaoMetaDefinition meta : policy.getMetadata().getDefinitionsWithCategory(CaoPolicy.CATEGORY_POLICY) ) {
					
					CaoList policyList = policy.getList(meta.getName());
					for ( CaoElement nextPolicy : policyList.getElements()) {
						StringBuffer access = new StringBuffer();
						for (CaoMetaDefinition nextMeta :  nextPolicy.getMetadata().getDefinitionsWithCategory(CaoPolicy.CATEGORY_RIGHT)) {
							if (nextMeta.getType() == TYPE.BOOLEAN) {
								if (nextPolicy.getBoolean(nextMeta.getName(), false))
									access.append(nextMeta.getName()).append(" ");
							} else
								access.append(nextPolicy.getString(nextMeta.getName(),"")).append(" ");
						}
						out.add(new Data(meta.getName(),nextPolicy.getString(CaoPolicy.PRINCIPAL),access.toString()));
					}
				}
				
				return out.toArray(new Data[out.size()]);
				
			} catch (Exception e) {
				e.printStackTrace();
				return new Data[0];				
			}
		}

		public void refresh() {
			firePropertyChange(PROP_TITLE);
		}
		
	}

	class LabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			Data data = (Data) element;
			if (columnIndex == 0)
				return data.type;
			if (columnIndex == 1)
				return data.key;
			if (columnIndex == 2)
				return data.value;
			return null;
		}
		
	}
	
	class Data {

		private String type;
		private String key;
		private String value;

		public Data(String type, String key, String value) {
			this.key = key;
			this.value = value;
			this.type = type;
		}
		
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		capListener = new CapEventAdapter() {
			
			@Override
			public void hotSelected(CaoList selection) {
				if (viewer.getTable().isDisposed()) return;
				RightsView.this.selection = selection;
				final Object[] data = new ContentProvider().getElements(null);
				
				viewer.getTable().getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						//viewer.setInput(new Object());
						//viewer.getTable().removeAll();
						viewer.setItemCount(0);
						viewer.add(data);
						viewer.getTable().update();
						((ContentProvider)viewer.getContentProvider()).refresh();
						//		viewer.refresh(true);
						//		Display.getDefault().asyncExec(new Runnable() {
						//            public void run() {
						//            }
						//		});
					}
				});
				
			}
			
		};
		CapCore.getInstance().getEventHandler().registerWeak(capListener);
	}
	
}
