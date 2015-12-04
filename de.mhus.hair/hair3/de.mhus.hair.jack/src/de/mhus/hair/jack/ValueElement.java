package de.mhus.hair.jack;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import javax.jcr.Value;

import de.mhus.lib.MDate;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.util.LinkedCaoList;

public class ValueElement extends CaoElement {

	private CaoMetadata meta;
	private Value value;

	protected ValueElement(CaoMetadata meta,JackElement element, Value value) throws CaoException {
		super(element);
		this.meta = meta;
		this.value = value;
	}

	@Override
	public CaoList getChildren(CaoAccess access) throws CaoException {
		return new LinkedCaoList(getParent(), new LinkedList<CaoElement>());
	}

	@Override
	public boolean isNode() {
		return false;
	}

	@Override
	public String getId() throws CaoException {
		return null;
	}

	@Override
	public String getName() throws CaoException {
		if (meta.getCount() < 1) return "?";
		return meta.getDefinitionAt(0).getName();
	}

	@Override
	public CaoMetadata getMetadata() {
		return meta;
	}

	@Override
	public String getString(String name) throws CaoException {
		try {
			return value.getString();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public CaoList getList(String name, CaoAccess access, String... attributes)
			throws CaoException {
		return null;
	}

	@Override
	public Object getObject(String name, String... attributes)
			throws CaoException {
		return getString(name);
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public CaoWritableElement getWritableNode() throws CaoException {
		return null;
	}

	@Override
	public void reload() throws CaoException {
	}

	@Override
	public boolean lock(int timeout) throws CaoException {
		return false;
	}

	@Override
	public boolean unlock() throws CaoException {
		return false;
	}

	@Override
	public boolean isLocked(boolean owner) throws CaoException {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	public boolean getBoolean(String name, boolean def) {
		try {
			return value.getBoolean();
		} catch (Exception e) {
			return def;
		}
	}
	
	public int getInt(String name, int def) {
		try {
			return (int) value.getLong();
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name, long def) {
		try {
			return value.getLong();
		} catch (Exception e) {
			return def;
		}
	}
	
	public float getFloat(String name, float def) {
		try {
			return (float) value.getDouble();
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(String name, double def) {
		try {
			return value.getDouble();
		} catch (Exception e) {
			return def;
		}
	}
	
	public Calendar getCalendar(String name) {
		try {
			return value.getDate();
		} catch (Exception e) {
			return null;
		}
	}

	public MDate getMDate(String name) {
		try {
			return new MDate(value.getDate());
		} catch (Exception e) {
			return null;
		}
	}

	public Date getDate(String name) {
		try {
			return new MDate(value.getDate()).toDate();
		} catch (Exception e) {
			return null;
		}
	}

}
