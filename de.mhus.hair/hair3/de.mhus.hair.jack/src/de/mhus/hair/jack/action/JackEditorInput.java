package de.mhus.hair.jack.action;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import de.mhus.lib.MFile;

public class JackEditorInput implements IStorageEditorInput {

	private Node node;
	private IPath storage;

	public JackEditorInput(Node node, IPath storagePath) {
		System.out.println(storagePath);
		this.node = node;
		this.storage = storagePath;
	}
	

	protected void log() {
		
	}

	public boolean equals(Object other) {
		return other instanceof JackEditorInput && node.equals(((JackEditorInput)other).node);
	}
	
	@Override
	public boolean exists() {
		log();
		try {
			return node.getSession().isLive();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getFileExtension() {
		log();
		return MFile.getFileSuffix(getName());
	}

	public long getLocalTimeStamp() {
		log();
		try {
			return node.getProperty("jcr:created").getLong();
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}
	}

	public long getModificationStamp() {
		try {
			return node.getProperty("jcr:created").getLong();
		} catch (Throwable e) {
			e.printStackTrace();
			return getLocalTimeStamp();
		}
	}

	public boolean isAccessible() {
		log();
		try {
			return exists() && !node.isLocked();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return false;
	}

	public InputStream getContents() throws CoreException {
		log();
		try {
			return ((Node)node.getNodes("jcr:content").next()).getProperty("jcr:data").getBinary().getStream();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		log();
		try {
			return node.getName();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return "??.???";
	}

	public void setContents(InputStream source) throws CoreException {
		log();
		try {
			node.getNodes("jcr:content").nextNode().getProperty("jcr:data").setValue(source);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		log();
		return null;
	}


	@Override
	public IPersistableElement getPersistable() {
		log();
		return null;
	}


	@Override
	public String getToolTipText() {
		log();
		return getName();
	}


	@Override
	public Object getAdapter(Class adapter) {
		log();
		return null;
	}


	@Override
	public IStorage getStorage() throws CoreException {
		log();
		return new IStorage() {
			public Object getAdapter(Class adapter) {
				return null;
			}
			public boolean isReadOnly() {
				return false;
			}
			public String getName() {
				return JackEditorInput.this.getName();
			}
			public IPath getFullPath() {
				return storage;
			}
			public InputStream getContents() throws CoreException {
				return JackEditorInput.this.getContents();
			}
			};
	}


	public Node getNode() {
		return node;
	}

}
