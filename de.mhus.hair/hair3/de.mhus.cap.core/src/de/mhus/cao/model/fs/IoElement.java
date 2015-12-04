package de.mhus.cao.model.fs;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import de.mhus.lib.MCast;
import de.mhus.lib.MDate;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoGenericContentAccess;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.ObjectNotFoundException;

public class IoElement extends CaoElement implements CaoGenericContentAccess {

	private File file;
	private boolean valid;

	public IoElement(IoApplication app, String path) throws CaoException {
		super(app);
		this.file = new File(path);
		this.valid = file.exists();
	}
	
	public IoElement(IoElement parent, String path) throws CaoException {
		super(parent);
		this.file = new File(path);
		this.valid = file.exists();
	}

	public IoElement(File file, IoElement parent) throws CaoException {
		super(parent);
		this.file = file;
		this.valid = file.exists();
	}
	
	@Override
	public CaoList getChildren(CaoAccess access) throws ObjectNotFoundException {
		if (!isNode()) throw new ObjectNotFoundException();
		return new IoList(file, this);
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public boolean isNode() {
		return file.isDirectory();
	}

	@Override
	public boolean getBoolean(String name, boolean def) {
		if ("directory".equals(name))
			return file.isDirectory();
		else
		if ("hidden".equals(name))
			return file.isHidden();
		else
		if ("writable".equals(name))
			return file.canWrite();
		else
		if ("readable".equals(name))
			return file.canRead();
		else
		if ("executable".equals(name))
			return file.canExecute();
		else
			return def;
	}

	@Override
	public double getDouble(String name, double def) {
		return def;
	}

	@Override
	public String getId() throws CaoException {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			throw new CaoException(file.getAbsolutePath(),e);
		}
	}

	@Override
	public long getLong(String name,long def) {
		if ("modified".equals(name))
			return file.lastModified();
		else
			return def;
	}

	@Override
	public CaoMetadata getMetadata() {
		return ((IoDriver)getConnection().getDriver()).getDefaultMetadata();
	}

	@Override
	public Object getObject(String name, String... attributes) throws CaoException {
		return getString(name);
	}

	@Override
	public String getString(String name) throws CaoException {
		if ("directory".equals(name))
			return ""+file.isDirectory();
		else
		if ("hidden".equals(name))
			return ""+file.isHidden();
		else
		if ("writable".equals(name))
			return ""+file.canWrite();
		else
		if ("readable".equals(name))
			return ""+file.canRead();
		else
		if ("executable".equals(name))
			return ""+file.canExecute();
		else
		if ("name".equals(name))
			return file.getName();
		else
		if ("path".equals(name))
			return file.getParent();
		else
		if ("modified".equals(name))
			return MCast.toString(getDate(name));
		else
			throw new ObjectNotFoundException(name);
	}

	@Override
	public boolean isWritable() {
		return file.canWrite();
	}

	@Override
	public CaoList getList(String name, CaoAccess access, String... attributes) throws CaoException {
		throw new ObjectNotFoundException(name);
	}

	@Override
	public void reload() {
		file = new File(file.getPath());
	}

	@Override
	public MDate getMDate(String name) {
		if ("modified".equals(name)) {
			return new MDate(file.lastModified(),TimeZone.getDefault());
		} else
			return null;
	}

	@Override
	public Date getDate(String name) {
		if ("modified".equals(name)) {
			return new MDate(file.lastModified(),TimeZone.getDefault()).toDate();
		} else
			return null;
	}
	
	@Override
	public CaoWritableElement getWritableNode() throws CaoException {
		return new IoWritable(this);
	}

	public File getFile() {
		return file;
	}

	@Override
	public boolean isLocked(boolean owner) throws CaoException {
		return owner;
	}

	@Override
	public boolean lock(int timeout) throws CaoException {
		return false;
	}

	@Override
	public boolean unlock() throws CaoException {
		return false;
	}

	public boolean isValid() {
		return valid;
	}

	public void setInvalid() {
		this.valid = false;
	}

	
}
