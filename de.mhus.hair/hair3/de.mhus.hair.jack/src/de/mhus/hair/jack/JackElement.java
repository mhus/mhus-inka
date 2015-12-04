package de.mhus.hair.jack;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.query.Row;

import de.mhus.lib.MDate;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoPolicy;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.logging.Log;

public class JackElement extends CaoElement {

	private static Log log = Log.getLog(JackElement.class);
	private Node node;
	private String[] attributes;
	private JackMeta meta;

	public JackElement(CaoApplication app, Node node,
			String[] attributes) throws CaoException {
		super(app);
		this.attributes = attributes;
		this.node = node;
		meta = new JackMeta(getConnection().getDriver(), node);
	}
	
	public JackElement(CaoElement parent, Node node,
			String[] attributes) throws CaoException {
		super(parent);
		this.attributes = attributes;
		this.node = node;
		meta = new JackMeta(getConnection().getDriver(), node);
	}

	public JackElement(CaoApplication app, Row row, String[] attributes) throws CaoException {
		super(app);
		this.attributes = attributes;
		try {
			this.node = row.getNode();
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
		meta = new JackMeta(getConnection().getDriver(), node);
	}

	@Override
	public CaoList getChildren(CaoAccess access) throws CaoException {
		return new JackList(this,node,attributes);
	}

	@Override
	public boolean isNode() {
		return node.isNode();
	}

	@Override
	public String getId() throws CaoException {
		try {
			return node.getIdentifier();
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
	}

	@Override
	public String getName() throws CaoException {
		try {
			return node.getName();
		} catch (RepositoryException e) {
			throw new CaoException(getId(),e);
		}
	}

	@Override
	public CaoMetadata getMetadata() {
		return meta;
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
			log.debug(e);
			return null;
		}
	}

	@Override
	public String getString(String name) throws CaoException {
		try {
			return node.getProperty(name).getString();
		} catch (RepositoryException e) {
			throw new CaoException(getId(),e);
		}
	}

	@Override
	public boolean getBoolean(String name, boolean def) {
		try {
			return node.getProperty(name).getBoolean();
		} catch (RepositoryException e) {
			return def;
		}
	}

	@Override
	public long getLong(String name,long def) {
		try {
			return node.getProperty(name).getLong();
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public double getDouble(String name,double def) {
		try {
			return node.getProperty(name).getDouble();
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public Date getDate(String name) {
		try {
			return new MDate( node.getProperty(name).getDate()).toDate();
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public MDate getMDate(String name) {
		try {
			return new MDate( node.getProperty(name).getDate());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public CaoList getList(String name, CaoAccess access, String... attributes)
			throws CaoException {
		try {
			return new ValueList(this, name, node.getProperty(name).getValues() );
		} catch (Exception e) {
			throw new CaoException(name,e);
		}
	}

	@Override
	public Object getObject(String name, String... attributes)
			throws CaoException {
		try {
			Property property = node.getProperty(name);
			if (property.isMultiple()) {
				return getList(name, attributes);
			}
			switch (property.getType()) {
			case PropertyType.BOOLEAN:
				return getBoolean(name, false);
			case PropertyType.DATE:
				return getLong(name, 0);
			case PropertyType.BINARY:
				return property.getValue().getBinary().getStream(); //  every time a new stream
			case PropertyType.DECIMAL:
				return getDouble(name, 0);
			case PropertyType.DOUBLE:
				return getDouble(name, 0);
			case PropertyType.LONG:
				return getLong(name, 0);
			case PropertyType.NAME:
			case PropertyType.PATH:
			case PropertyType.STRING:
			case PropertyType.URI:
			case PropertyType.UNDEFINED:
				return getString(name);
			case PropertyType.REFERENCE:
			case PropertyType.WEAKREFERENCE:
				String refId = property.getString();
				Node refNode = node.getSession().getNodeByIdentifier(refId);
				return new JackElement(getApplication(), refNode, attributes);
			}
		} catch (Exception e) {
			return new CaoException(this,name,e);
		}
		return null;
	}

	@Override
	public boolean isWritable() {
		try {
			return ((JackConnection)getConnection()).getSession().hasPermission(node.getPath(),"set_property");
		} catch (RepositoryException e) {
			return false;
		}
	}

	@Override
	public CaoWritableElement getWritableNode() throws CaoException {
		return new JackWritable(this);
	}

	@Override
	public void reload() throws CaoException {
		try {
			node.refresh(true);
			meta = new JackMeta(getConnection().getDriver(), node);
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public boolean lock(int timeout) throws CaoException {
		try {
			Lock lock = null;
			LockManager manager = node.getSession().getWorkspace().getLockManager();
		    try {
		      lock = manager.getLock(node.getPath());
		      if (lock.getLockToken() != null) {
		        return true;
		      }
		    } catch (LockException e) {
		    }
		    lock = null;
		    long sleepTime = 100;
		    int tries = 0;
		    while (tries++ < 300 ) {
		      try {
		    	manager.lock(node.getPath(), false, false, timeout, "");
		        return true;
		      } catch (Exception ex) {
		    	if (timeout <= 0) return false;
		        if ( sleepTime < 500 ) {
		          sleepTime = sleepTime + 10;
		        }
		        try {
		          Thread.sleep(sleepTime);
		          timeout-=sleepTime;
		        } catch (InterruptedException e) {
		        }
		      }
		    }
		    return false;
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public boolean unlock() throws CaoException {
		try {
			LockManager manager = node.getSession().getWorkspace().getLockManager();
			if (!manager.holdsLock(node.getPath()))return false;
			manager.unlock(node.getPath());
			return true;
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
	}

	@Override
	public boolean isLocked(boolean owner) throws CaoException {
		try {
			return node.isLocked();
		} catch (RepositoryException e) {
			throw new CaoException(e);
		}
	}

	@Override
	public boolean isValid() {
		return true;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public CaoPolicy getAccessPolicy() throws CaoException {
		return new JackPolicy(this);
	}

	public boolean hasContent() {
		try {
			Node content = (Node)node.getNodes("jcr:content").next();
			return content.getProperty("jcr:data") != null;
		} catch (Throwable e) {
		}
		return false;
	}
	
	public String[] getAttributes() {
		return attributes;
	}
}
