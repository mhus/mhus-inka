package de.mhus.cha.cao;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetadata;

public class ChaList extends CaoList {

	private File file;
	private int size;
	private ChaElement parent;

	public ChaList(ChaConnection connection, File file,ChaElement parent) {
		super(connection,file.getAbsolutePath());
		this.file = file;
		this.parent = parent;
		new ListIterator(); // set size
	}

	@Override
	public CaoMetadata getMetadata() {
		return ((ChaDriver)getConnection().getDriver()).getDefaultMetadata();
	}


	@Override
	public int size() {
		return size;
	}

	private class ListIterator extends CaoListIterator {

		private Iterator<CaoElement> iterator;
		
		protected ListIterator() {
			super(ChaList.this.getConnection());
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
				for (File n : file.listFiles( ((ChaConnection)getConnection()).getDefaultFileFilter()) ) {
					out.add( new ChaElement((ChaConnection)getConnection(), n.getName(), parent ) );
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

	@Override
	public CaoElement getParent() {
		return parent;
	}
}
