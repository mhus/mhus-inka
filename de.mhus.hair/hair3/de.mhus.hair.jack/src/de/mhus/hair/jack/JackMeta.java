package de.mhus.hair.jack;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.commons.JcrUtils;

import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;

public class JackMeta extends CaoMetadata {

	public JackMeta(CaoDriver driver, Node node) throws CaoException {
		super(driver);
		try {
			for (Property p : JcrUtils.getProperties(node) ) {
				TYPE type = TYPE.STRING;
				long len = 0;
				if (p.isMultiple()) {
					type = TYPE.LIST;
				} else {
					type = getTypeForJcr(p);
					len = getLengthForJcr(p);
				}
				definition.add(new CaoMetaDefinition(this,p.getName(),type,p.getName(),len));
			}
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	public static TYPE getTypeForJcr(Property p) throws RepositoryException {
		switch (p.getType()) {
		case PropertyType.BINARY: return TYPE.BINARY;
		case PropertyType.BOOLEAN: return TYPE.BOOLEAN;
		case PropertyType.DATE: return TYPE.DATETIME;
		case PropertyType.DECIMAL: return TYPE.DOUBLE;
		case PropertyType.DOUBLE: return TYPE.DOUBLE;
		case PropertyType.LONG: return TYPE.LONG;
		case PropertyType.NAME: return TYPE.STRING;
		case PropertyType.PATH: return TYPE.STRING;
		case PropertyType.REFERENCE: return TYPE.ELEMENT;
		case PropertyType.STRING: return TYPE.STRING;
		case PropertyType.UNDEFINED: return TYPE.STRING;
		case PropertyType.URI: return TYPE.STRING;
		case PropertyType.WEAKREFERENCE: return TYPE.ELEMENT;
		}		
		return TYPE.STRING;
	}
	
	public static long getLengthForJcr(Property p) throws ValueFormatException, RepositoryException {
		if (p.isMultiple()) {
			switch (p.getType()) {
			case PropertyType.BINARY:
			case PropertyType.STRING:
				long[] l = p.getLengths();
				if (l==null) return 0;
				long max = 0;
				for (long ll : l)
					max = Math.max(max, ll);
				return max;
			}
			return 0;	
		}
		switch (p.getType()) {
		case PropertyType.BINARY:
		case PropertyType.STRING:
			return p.getLength();
		}
		return 0;
	}
	
}
