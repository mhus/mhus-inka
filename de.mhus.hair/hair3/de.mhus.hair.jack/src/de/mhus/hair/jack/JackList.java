package de.mhus.hair.jack;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.JcrUtils;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetadata;

public class JackList extends CaoList {

	private Node node;
	private JackMeta meta;
	private String[] attributes;

	public JackList(CaoElement parent, String name,
			String[] attributes) throws CaoException {
		super(parent,attributes[0]);
		this.attributes = attributes;
		try {
			if (attributes != null && attributes.length > 0 && attributes[0] != null) {
				this.node = ((JackConnection)getConnection()).getSession().getNodeByIdentifier(attributes[0]);
			} else
				this.node = ((JackConnection)getConnection()).getSession().getRootNode();
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
		meta = new JackMeta(getConnection().getDriver(), node);
		
		
	}

	public JackList(JackElement jackElement, Node node, String[] attributes) throws CaoException {
		super(jackElement,null);
		this.attributes = attributes;
		this.node = node;
		meta = new JackMeta(getConnection().getDriver(), node);
	}

	@Override
	public CaoMetadata getMetadata() {
		return meta;
	}

	@Override
	public int size() {
		return -1; //TODO
	}

	@Override
	public CaoListIterator getElements() {
		try {
			return new MyListIterator();
		} catch (CaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public CaoElement getParent() {
		if (parent != null) return parent;
		try {
			Node p = node.getParent();
			if (p == null) return null;
			parent = new JackElement(getApplication(), p, attributes);
			return parent;
		} catch (Exception e) {
			e.printStackTrace(); //TODO
			return null;
		}
	}


	private class MyListIterator extends CaoListIterator {

		private Iterator<Node> childrens;

		protected MyListIterator() throws CaoException {
			super(JackList.this.getConnection());
			reset();
		}

		@Override
		protected CaoElement nextElement() throws CaoException {
			return new JackElement(getParent(), childrens.next(), attributes);
		}

		@Override
		protected boolean hasNextElement() throws CaoException {
			return childrens.hasNext();
		}

		@Override
		public void reset() throws CaoException {
			try {
				childrens = JcrUtils.getChildNodes(node).iterator();
			} catch (RepositoryException e) {
				throw new CaoException(e);
			}
		}
		
	}
	
}
