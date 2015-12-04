package de.mhus.cha.cao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Element;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.ObjectNotFoundException;
import de.mhus.cap.core.Access;
import de.mhus.cap.core.CaoGenericContentAccess;
import de.mhus.lib.MCast;
import de.mhus.lib.MDate;
import de.mhus.lib.MXml;

public class ChaElement extends CaoElement<Access> implements CaoGenericContentAccess {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(ChaElement.class);

	static final String NAME_DATA = "data.xml";
	static final String NAME_METADATA = "metadata.xml";
	static final String NAME_CONTENT = "content";
	
	private File file;
	private boolean valid;
	private ChaElement parent;
	private Element data;
	private ChaMetadata metadata;

	public ChaElement(ChaConnection fsConnection, File path) throws CaoException {
		super(fsConnection);
		log.trace("Element File '" + path.getAbsolutePath() + "'");
		this.file = path;
		this.valid = file.exists();
		this.data = loadXml(NAME_DATA);
		this.metadata = new ChaMetadata(this,path);
	}
	
	public ChaElement(ChaConnection fsConnection, String id) throws CaoException {
		super(fsConnection);
		String path = fsConnection.getIdPath(id);
		log.trace("Element " + id + " '" + path + "'");
		this.file = new File(path);
		this.valid = file.exists();
		this.data = loadXml(NAME_DATA);
		this.metadata = new ChaMetadata(this,file);
	}


	public ChaElement(ChaConnection fsConnection, String id, ChaElement parent) throws CaoException {
		super(fsConnection);
		String path = fsConnection.getIdPath(id);
		log.trace("Element " + id + " '" + path + "' (" + parent.getId() + ")");
		this.file = new File(path);
		this.valid = file.exists();
		this.parent = parent;
		this.data = loadXml(NAME_DATA);
		this.metadata = new ChaMetadata(this,file);
	}

	Element loadXml(String name) throws CaoException {
		try {
			InputStream fis = (InputStream) getObject(NAME_CONTENT, name);
			Element out = MXml.loadXml(fis).getDocumentElement();
			fis.close();
			return out;
		} catch (Exception e) {
			throw new CaoException(file.getAbsolutePath(),e);
		}
	}
	
	
	
	@Override
	public CaoList<Access> getChildren(Access access) throws ObjectNotFoundException {
		if (!isNode()) throw new ObjectNotFoundException();
		return new ChaList((ChaConnection)getConnection(), file, this);
	}

	@Override
	public String getName() {
		return MXml.getValue( MXml.getElementByPath(data, "name"), false);
	}

	@Override
	public boolean isNode() {
		return true;
	}

	@Override
	public boolean getBoolean(String name, boolean def) {
		return MCast.toboolean(getString(name), def);
	}

	@Override
	public double getDouble(String name, double def) {
		return MCast.todouble(getString(name),def);
	}

	@Override
	public String getId() throws CaoException {
			return file.getName(); // name of the folder should be unique
	}

	@Override
	public long getLong(String name, long def) {
		return MCast.tolong(getString(name), def);
	}

	@Override
	public CaoMetadata getMetadata() {
		// return ((ChaDriver)getConnection().getDriver()).getDefaultMetadata();
		return metadata;
	}

	@Override
	public Object getObject(String name, String... attributes) throws CaoException {
		
		if (NAME_CONTENT.equals(name)) {
			try {
				FileInputStream fis = new FileInputStream(new File(file,attributes[0]));
				return fis;
			} catch (FileNotFoundException e) {
				throw new ObjectNotFoundException("Content " + attributes[0], e);
			}
		}
		
		return getString(name);
	}

	@Override
	public String getString(String name) {
		
		Element node = MXml.getElementByPath(data,name);
		if (node == null) {
//			CaoMetaDefinition def = getMetadata().getDefinition(name);
//			if (def == null)
//				throw new ObjectNotFoundException(name);
			return null;
		}
		return MXml.getValue(node, false);
	}

	@Override
	public boolean isWritable() {
		return file.canWrite(); //???
	}

	@Override
	public CaoList<Access> getList(String name, Access access, String... attributes) throws CaoException {
		
		throw new ObjectNotFoundException(name);
	}

	@Override
	public void reload() throws CaoException {
		file = new File(file.getPath());
		data = loadXml(NAME_DATA);
	}

	@Override
	public MDate getDate(String name) {
		return new MDate(getString(name));
	}

	@Override
	public CaoWritableElement<Access> getWritableNode() throws CaoException {
		return new ChaWritable(this,data);
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

	@Override
	public CaoElement<Access> getParent() {
		return parent;
	}
	
}
