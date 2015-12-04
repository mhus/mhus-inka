package de.mhus.cao.model.fs;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetadata;

public class IoList extends CaoList {

	private File file;
	private int size;

	public IoList(File file,IoElement parent) {
		super(parent,file.getAbsolutePath());
		this.file = file;
		new ListIterator(); // set size
	}

	@Override
	public CaoMetadata getMetadata() {
		return ((IoDriver)getConnection().getDriver()).getDefaultMetadata();
	}


	@Override
	public int size() {
		return size;
	}

	private class ListIterator extends CaoListIterator {

		private Iterator<CaoElement> iterator;
		
		protected ListIterator() {
			super(IoList.this.getConnection());
			reset();
		}


		@Override
		public boolean hasNextElement() {
			return iterator.hasNext();
		}

		@Override
		public CaoElement nextElement() {
			return iterator.next();
		}

		@Override
		public void reset() {
			try {
				LinkedList<CaoElement> out = new LinkedList<CaoElement>();
				for (File n : file.listFiles() ) {
					if ( ! n.getName().startsWith(".") ) {
						out.add( new IoElement( n, (IoElement)getParent() ) );
					}
				}
				size = out.size();
				iterator = out.iterator();
			} catch (CaoException he) {
				throw new RuntimeException(he);
			}
		}

	}

	@Override
	public CaoListIterator getElements() {
		return new ListIterator();
	}

}
