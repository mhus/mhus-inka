package de.mhus.hair.jack;

import javax.jcr.Value;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoListIterator;
import de.mhus.lib.cao.CaoMetadata;

public class ValueList extends CaoList {

	private JackElement element;
	private ValueElement[] values;
	private String name;
	private CaoMetadata meta;

	public ValueList(JackElement element, String name, Value[] values) throws CaoException {
		super(element, null);
		this.element = element;
		try {
			this.meta = new ValueMeta(name, element.getMetadata(), element.getNode().getProperty(name));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.values = new ValueElement[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = new ValueElement(meta,element,values[i]);
		}
		
		this.name = name;
	}

	@Override
	public CaoMetadata getMetadata() {
		return meta;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public CaoListIterator getElements() {
		return new MyIterator();
	}

	private class MyIterator extends CaoListIterator {

		int pos;
		
		protected MyIterator() {
			super(ValueList.this.getConnection());
			reset();
		}

		@Override
		protected CaoElement nextElement() throws CaoException {
			if (pos >=values.length) return null;
			pos++;
			return values[pos-1];
		}

		@Override
		protected boolean hasNextElement() throws CaoException {
			return pos < values.length;
		}

		@Override
		public void reset() {
			pos = 0;
		}
		
	}
}
