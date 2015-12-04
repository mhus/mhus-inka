package de.mhus.cap.core.dnd;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class CapDragListener implements DragSourceListener {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(CapDragListener.class);
	private Viewer viewer;
	
	public CapDragListener(Viewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void dragFinished(DragSourceEvent event) {
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		ISelection selection = viewer.getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection)selection;
			List<?> list = sel.toList();
			List<ICaoExchange> outList = null;
			try {
				for (Object item : list) {
					if (item instanceof ICaoExchange) {
						if (outList==null) outList = new LinkedList<ICaoExchange>();
						outList.add( (ICaoExchange)item );
					}
				}
				
				if (outList==null) {
					return;
				}
				
				event.data = outList.toArray(new ICaoExchange[outList.size()]);
				
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}

	@Override
	public void dragStart(DragSourceEvent event) {
	}


}
