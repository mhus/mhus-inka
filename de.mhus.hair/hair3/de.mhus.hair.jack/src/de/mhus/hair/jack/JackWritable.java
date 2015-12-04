package de.mhus.hair.jack;

import java.io.InputStream;
import java.util.LinkedList;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Value;

import org.apache.tools.ant.filters.StringInputStream;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoInvalidException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.util.WritableElement;
import de.mhus.lib.logging.Log;

public class JackWritable extends WritableElement {

	private static Log log = Log.getLog(JackWritable.class);
	
	public JackWritable(CaoElement master) throws CaoException {
		super(master);
	}

	@Override
	public void save() throws CaoException {
		if (!isValid()) throw new CaoInvalidException();
		
		Node node = ((JackElement)master).getNode();
		
		try {
			for (String name : data.keySet()) {
				CaoMetaDefinition def = master.getMetadata().getDefinition(name);
				log.debug("Set property " + name + "[" + def.getType() + "]=" + getString(name));
				switch (def.getType()) {
				case LIST:
					CaoList list = getList(name);
					CaoMetaDefinition def2 = list.getMetadata().getDefinitionAt(0);
					LinkedList<Value> values = new LinkedList<Value>();
					for (CaoElement item : list.getElements()) {
						values.add( toValue(def2.getType(),def2.getName(),item) );
					}
					node.getProperty(name).setValue(values.toArray(new Value[values.size()]));
					break;
				default:
					Value value = toValue(def.getType(),name,this);
					if (value != null) {
						node.setProperty(name, value);
					}
				}
			}
			node.getSession().save();
		} catch (Exception e) {
			throw new CaoException(getId(),e);
		}
		
	}

	public Value toValue(TYPE type,String name, CaoElement element) throws Exception {
		Node node = ((JackElement)master).getNode();
		Value value = null;
		switch (type) {
		case BOOLEAN:
			value = node.getSession().getValueFactory().createValue(element.getBoolean(name,false));
			break;
		case DATETIME:
			value = node.getSession().getValueFactory().createValue(element.getMDate(name).toCalendar());
			break;
		case DOUBLE:
			value = node.getSession().getValueFactory().createValue(element.getDouble(name,0));
			break;
		case LONG:
			value = node.getSession().getValueFactory().createValue(element.getLong(name,0));
			break;
		case STRING:
		case TEXT:
			value = node.getSession().getValueFactory().createValue(element.getString(name));
			break;
		case BINARY: {
				Object obj = element.getObject(name);
				if (obj == null) return null;
				if (obj instanceof InputStream) {
					Binary binary = node.getSession().getValueFactory().createBinary((InputStream)obj);
					value = node.getSession().getValueFactory().createValue(binary);
				} else
				if (obj instanceof String) {
					Binary binary = node.getSession().getValueFactory().createBinary(new StringInputStream((String)obj));
					value = node.getSession().getValueFactory().createValue(binary);
				}
			}
			break;
		case ELEMENT: {
				Object obj = element.getObject(name);
				if (obj == null) return null;
				if (obj instanceof JackElement) {
					value = node.getSession().getValueFactory().createValue(((JackElement)obj).getId());
				} else {
					value = node.getSession().getValueFactory().createValue(obj.toString());
				}
			}
			break;
		}
		
		return value;
	}
	
	@Override
	public boolean isWritable(String name) {
		return true;
	}

	@Override
	public CaoElement getParent() {
		return master.getParent();
	}

	@Override
	public boolean isValid() {
		return master.isValid();
	}

}
