package de.mhus.cap.ui.tableeditor;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.mhus.cap.core.dnd.CaoTransfer;
import de.mhus.cap.core.dnd.CapDragListener;
import de.mhus.cap.core.dnd.CapDropListener;
import de.mhus.cap.ui.oleditor.ObjectListInput;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.io.CSVWriter;
import de.mhus.lib.logging.MLog;

public class TableEditor extends EditorPart {

	public static final String ID = "de.mhus.cap.ui.tableeditor";
	
	private TableViewer viewer;
	private CaoList list;

	private MyLabelProvider lp;

	private LinkedList<CaoElement> out;

	private Clipboard cb;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		ObjectListInput in = (ObjectListInput)input;
		setInput(input);
		setSite(site);
		setPartName(in.getTitle());
		setTitleToolTip(in.getToolTipText());
		
		list = in.getList();
		
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		viewer = new TableViewer(parent);
		
		lp = new MyLabelProvider();
		viewer.setContentProvider(new MyContentProvider());
		viewer.setLabelProvider(lp);
		viewer.setSorter(new MyViewerSorter());
		
		if (list != null) {
			
			Table table = viewer.getTable();
			
			out = new LinkedList<CaoElement>();
			
			try {
				for (CaoElement item : list.getElements()) {
					if (item != null) out.add(item);
				}
			} catch (CaoException e) {
				MLog.e(e);
			}
			
			CaoMetadata meta = list.getMetadata();

			int i = 0;
			for (CaoMetaDefinition m : meta) {
				TableColumn tc = new TableColumn(table, SWT.LEFT);
				tc.setText(m.getName());
				final int fi = i;
				tc.addSelectionListener(new SelectionAdapter() {
				      public void widgetSelected(SelectionEvent event) {
				        ((MyViewerSorter) viewer.getSorter())
				            .doSort(fi);
				        viewer.refresh();
				      }
				    });
				i++;
			}
		
		    // Turn on the header and the lines
		    table.setHeaderVisible(true);
		    table.setLinesVisible(true);

		    viewer.setInput(list);
		    viewer.refresh();
		    
			// Pack the columns
		    for (int j = 0, n = table.getColumnCount(); j < n; j++) {
		      table.getColumn(j).pack();
		    }
		    
		    cb = new Clipboard(Display.getDefault());
		    
		    viewer.getTable().addKeyListener(new KeyListener() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.character == 'c') {
						
						StringWriter out = new StringWriter();
						CSVWriter writer = new CSVWriter(out);
						
						StructuredSelection sel = (StructuredSelection)viewer.getSelection();
						List<?> list = sel.toList();
						CaoMetadata meta = TableEditor.this.list.getMetadata();
						// header
						for (CaoMetaDefinition m : meta) {
							writer.put(m.getName());
						}
						writer.nl();
						// data
						for (Object o : list) {
							if (o instanceof CaoElement) {
								CaoElement cao = (CaoElement)o;
								for (CaoMetaDefinition m : meta) {
									try {
										writer.put( cao.getString(m.getName()) );
									} catch (Exception ex) {
										MLog.t(ex);
									}
								}
								writer.nl();
							}
						}
						
						
				        TextTransfer textTransfer = TextTransfer.getInstance();
				        cb.setContents(new Object[] { out.toString() },
				            new Transfer[] { textTransfer });
					}
				}
			});
		    
		    
		}
		
	}

	@Override
	public void setFocus() {

	}

	
	private class MyContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return out.toArray();
		}
		
	}
	
	private class MyLabelProvider implements ITableLabelProvider {

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
			if (element == null) return "[null]";
			try {
				CaoElement item = (CaoElement) element;
				String colName = viewer.getTable().getColumn(columnIndex).getText();
				return item.getString(colName);
			} catch (CaoException e) {
				//e.printStackTrace();
				return "[?]";
			}
		}
		
	}
	
	private class MyViewerSorter extends ViewerSorter {
		
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		  
		private int column;
		private int direction;

		public void doSort(int column) {
		    if (column == this.column) {
		      // Same column as last sort; toggle the direction
		      direction = 1 - direction;
		    } else {
		      // New column; do an ascending sort
		      this.column = column;
		      direction = ASCENDING;
		    }
		  }
		
		public int compare(Viewer viewer, Object e1, Object e2) {
			String s1 = lp.getColumnText(e1, column);
			String s2 = lp.getColumnText(e2, column);
			if (s1 == null && s2 == null) {
				return 0;
			}
			if (s1 == null && s2 != null) {
				return direction == ASCENDING ? 1 : -1;
			}
			if (s1 != null && s2 == null) {
				return direction == ASCENDING ? -1 : 1;
			}
			
			int rc = collator.compare(s1,s2);
			
			if (direction == DESCENDING)
			      rc = -rc;

			return rc;
		}
		
	}

}
