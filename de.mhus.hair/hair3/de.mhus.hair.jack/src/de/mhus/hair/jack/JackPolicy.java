package de.mhus.hair.jack;

import java.util.LinkedList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Value;

import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoPolicy;
import de.mhus.lib.cao.CaoWritableElement;
import de.mhus.lib.cao.util.MutableCaoList;
import de.mhus.lib.cao.util.MutableElement;

public class JackPolicy extends CaoPolicy {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log.getLog(JackPolicy.class);
	
	private static final String DEFINED = "defined";

	private static final String RULE = "rule";

	public JackPolicy(JackElement element)
			throws CaoException {
		super(element, false, false);

		
		
		
	}
	
	public CaoList getList(String name, CaoAccess access, String... attributes)
	throws CaoException {
	
		if (DEFINED.equals(name)) {
			return getDefinedList(attributes);
		}
		
		return super.getList(name, access, attributes);
	}

	protected CaoList getDefinedList(String[] attributes) throws CaoException {

		LinkedList<CaoElement> list = new LinkedList<CaoElement>();
		try {
			Node nPolicy = ((JackElement)element).getNode().getNode("rep:policy");
			if (nPolicy != null) {
				for (NodeIterator ni = nPolicy.getNodes();ni.hasNext();) {
					Node child = (Node) ni.next();
					if ("rep:GrantACE".equals(child.getPrimaryNodeType().getName())) {
						MutableElement p = new MutableElement(this);
						p.getMetaDefinitions().add(new CaoMetaDefinition(p.getMetadata(), CaoPolicy.PRINCIPAL, TYPE.ELEMENT, null, 0));
						p.getMetaDefinitions().add(new CaoMetaDefinition(p.getMetadata(), RULE, TYPE.STRING, null, 256,CaoPolicy.CATEGORY_RIGHT));
						
						CaoWritableElement pw = p.getWritableNode();
						String principal = child.getProperty("rep:principalName").getString();
						if (child.hasProperty("rep:glob"))
							principal+= " at " + child.getProperty("rep:glob").getString();
						pw.setString(CaoPolicy.PRINCIPAL,principal);
						
						Property priv = child.getProperty("rep:privileges");
						if (priv.isMultiple()) {
							StringBuffer sb = new StringBuffer();
							for (Value val : priv.getValues()) {
								sb.append("[").append(val.getString()).append("]");
							}
							pw.setString(RULE,sb.toString());								
						} else {
							pw.setString(RULE,priv.getString());
						}
						pw.save();

						list.add(p);
					}
				}
			}
		} catch (Exception e) {
			// log.d(e);
			log.d(e.toString());
		}
		return new MutableCaoList(this, list);		
		
	}
	
	protected void fillMetaData(LinkedList<CaoMetaDefinition> definition) {
		super.fillMetaData(definition);
		definition.add(new CaoMetaDefinition(meta,DEFINED,TYPE.LIST,null,0,CATEGORY_POLICY) );
	}
	
}
