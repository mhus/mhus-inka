package de.mhus.cap.ui.oleditor;

import java.util.LinkedList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.mhus.cap.core.Activator;
import de.mhus.cap.core.CapCore;
import de.mhus.cap.core.dnd.CaoTransfer;
import de.mhus.cap.core.dnd.CapDragListener;
import de.mhus.cap.core.dnd.CapDropListener;
import de.mhus.cap.core.dnd.CapDropListener.LOCATION;
import de.mhus.cap.core.dnd.CapDropListener.OPERATION;
import de.mhus.cap.core.dnd.ICaoExchange;
import de.mhus.cap.core.ui.ICaoImageProvider;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoObserver;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.logging.MLog;

public class ObjectList extends Composite {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(ObjectList.class);
	
	private TableViewer tableViewer;

	private CaoList list;
	private CaoObserver observer;

	private static final Image CHECKED = Activator.getImageDescriptor("icons/checked.gif").createImage();
	private static final Image UNCHECKED = Activator.getImageDescriptor("icons/unchecked.gif").createImage();

	public ObjectList(Composite parent, CaoList list, CaoObserver observer) {
		super(parent, SWT.NONE);
		this.list = list;
		this.observer = observer;
		
		try {
			initUI();
		} catch (CaoException e) {
			e.printStackTrace();
			//TODO do something
		}
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	protected void initUI() throws CaoException {
		
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		setLayout(layout);
		
		tableViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		tableViewer.setUseHashlookup(true);
		
		createColumns(tableViewer);
		
		tableViewer.setContentProvider(new MyContentProvider());
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setInput(list);
		
		int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_TARGET_MOVE;
		Transfer[] transferTypes = new Transfer[]{CaoTransfer.getInstance(), FileTransfer.getInstance()};
		tableViewer.addDropSupport(operations, transferTypes, new CapDropListener(tableViewer));
		tableViewer.addDragSupport(operations, transferTypes, new CapDragListener(tableViewer));

	}
	
	// This will create the columns for the table
	protected void createColumns(TableViewer viewer) throws CaoException {

		IConfig[] headers = list.getApplication().getConfig().getConfig(CapCore.LIST_LIST_HEADERS).getConfigBundle("header");
		
		for ( IConfig data : headers) {
			
			MyColumnData columnData = new MyColumnData();
			
			columnData.config = data;
			columnData.imageProvider = CapCore.getInstance().getImageProvider(data.getString("imageprovider",null));
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(data.getString("title","?"));
			column.getColumn().setWidth((int)data.getLong("width",400));
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);		
			column.getColumn().setData(columnData);
		}
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	private class MyContentProvider implements IStructuredContentProvider {

		CaoList list;
		
		@Override
		public Object[] getElements(Object inputElement) {
			
			list = (CaoList)inputElement;
			
			LinkedList<MyData> out = new LinkedList<MyData>();
			try {
				for ( CaoElement data : list.getElements() ) {
					out.add(new MyData(data));
					if (observer!=null) observer.add(data);
				}
			} catch (CaoException e) {
				MLog.e(e);
			}

			return out.toArray();
		}

		@Override
		public void dispose() {
			try {
				for ( CaoElement data : list.getElements() ) {
					if (observer != null) observer.remove(data);
				}
			} catch (CaoException e) {
				MLog.e(e);
			}
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
		
	}
	
	private class MyLabelProvider  extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			try {
				CaoElement data = ((MyData)element).element;
				MyColumnData config = (MyColumnData)tableViewer.getTable().getColumn(columnIndex).getData();
				String name = config.config.getString("name","?");
				CaoMetaDefinition def = data.getMetadata().getDefinition( name );
				if (def != null) {
					if (def.getType() == TYPE.BOOLEAN) {
						return data.getBoolean(name,false) ? CHECKED : UNCHECKED; 
					}
				}		
				if (config.imageProvider != null) {
					return config.imageProvider.getImage(data, null);
				}

			} catch (Exception he) {
				log.debug(he);
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			CaoElement data = ((MyData)element).element;
			try {
			
				MyColumnData config = (MyColumnData)tableViewer.getTable().getColumn(columnIndex).getData();
				String name = config.config.getString("name","?");
				if ("**name".equals(name))
					return data.getName();
				if ("**id".equals(name))
					return data.getId();
				
				return data.getString( name );

			} catch (CaoException he) {
				//he.printStackTrace(); //TODO do something ....
				return "?";
			}
		}
		
	}

	
	public class MyData implements ICaoExchange {

		public CaoElement element;

		public MyData(CaoElement data) {
			element = data;
		}

		@Override
		public CaoElement getElement() {
			return element;
		}

		@Override
		public boolean doDrop(LOCATION loc, OPERATION oper,
				ICaoExchange[] providers) {
			if (log.isTraceEnabled()) try {log.trace("doDrop: " + loc.toString() + " " + oper.toString() + " " + element.getId() );} catch (CaoException e) {}
			return CapCore.getInstance().doDrop(loc, oper, providers, this);
		}
		
	}
	
	public class MyColumnData {
		public IConfig config;
		public ICaoImageProvider imageProvider;
	}
	
}
