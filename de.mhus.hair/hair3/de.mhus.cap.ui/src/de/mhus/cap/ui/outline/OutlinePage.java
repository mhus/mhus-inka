package de.mhus.cap.ui.outline;

import java.util.LinkedList;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import de.mhus.cap.ui.Activator;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.logging.MLog;

public class OutlinePage extends ContentOutlinePage {

	private IEditorInput myInput;
	private CaoElement root;

	public OutlinePage() {
	}

	public void setInput(IEditorInput editorInput,CaoElement caoElement) {
		myInput = editorInput;
		this.root = caoElement;
	}

	public void createControl(Composite parent) {
	      super.createControl(parent);
	      TreeViewer viewer= getTreeViewer();
	      viewer.setContentProvider(new MyContentProvider());
	      viewer.setLabelProvider(new MyLabelProvider());
	      viewer.addSelectionChangedListener(this);
	      viewer.setInput(myInput);
	      viewer.expandAll();
	   }
	
	private class MyLabelProvider  extends LabelProvider {

		private Image elementImage;
		private Image multiImage;
		private Image attrImage;
		
		MyLabelProvider() {
			elementImage = Activator.getImageDescriptor("icons/pill.png").createImage();
			multiImage = Activator.getImageDescriptor("icons/tag_orange.png").createImage();
			attrImage = Activator.getImageDescriptor("icons/tag_blue.png").createImage();
		}
		
		
		public String getText(Object obj) {
			try {
				if (obj instanceof CaoElement)
					return ((CaoElement)obj).getName();
				if (obj instanceof CaoList)
					return ((CaoList)obj).getId();
			} catch (CaoException e) {
			}
			
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if (obj instanceof CaoElement)
				return elementImage;
			if (obj instanceof CaoList)
				return multiImage;
			return attrImage;
		}
	}
	
	private class MyContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (root == element) return null;
			if (element instanceof CaoList) return ((CaoList)element).getParent();
			return root;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element == null) return true;
			if (element instanceof CaoElement) return true;
			if (element instanceof CaoList) return true;
			return false;
		}

		@Override
		public Object[] getElements(Object element) {
			
			if (element == null || element == myInput) return new Object[] {root};
			
//			if (root != null && root == element) {
//				LinkedList<CaoElement> out = new LinkedList<CaoElement>();
//				for (CaoElement item : cao.getElements()) {
//					out.add(item);
//				}
//				return out.toArray();
//			}
			if (element instanceof CaoElement ) {
				CaoMetadata meta = ((CaoElement)element).getMetadata();
				Object[] out = new Object[meta.getCount()];
				for (int i = 0; i < meta.getCount(); i++) {
					CaoMetaDefinition def = meta.getDefinitionAt(i);
					try {
						if (def.getType() == TYPE.LIST) {
							out[i] = ((CaoElement)element).getList(def.getName());
						} else {
							out[i] = def.getName() + " = " + ((CaoElement)element).getString(def.getName());
						}
					} catch (Exception e) {
						out[i] = def.getName() + ": " + e.toString();
					}
				}
				return out;
			}
			
			if (element instanceof CaoList) {
				LinkedList<CaoElement> out = new LinkedList<CaoElement>();
				try {
					for (CaoElement item : ((CaoList)element).getElements()) {
						out.add(item);
					}
				} catch (CaoException e) {
					MLog.e(e);
				}
				return out.toArray();
			}
			
			return new Object[0];
		}
		
	}

	public void refresh() {
		getTreeViewer().refresh();
		getTreeViewer().expandAll();
	}
}
