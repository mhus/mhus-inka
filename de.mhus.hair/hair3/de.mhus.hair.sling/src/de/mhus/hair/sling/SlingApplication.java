package de.mhus.hair.sling;

import java.util.LinkedList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import de.mhus.hair.jack.JackApplication;
import de.mhus.hair.jack.JackConfiguration;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.hair.jack.JackRootElement;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.cao.util.MutableElement;
import de.mhus.lib.config.IConfig;

public class SlingApplication extends JackApplication {

	public SlingApplication(JackConnection connection, IConfig config)
			throws CaoException {
		super(connection, config);
	}

	public CaoElement queryElement(String name, CaoAccess access,
			String... attributes) throws CaoException {

		try {
			if (CaoDriver.TREE_DEFAULT.equals(name)) {
				Node node = ((JackConnection) getConnection()).getSession()
						.getRootNode();
				MutableElement ele = new MutableElement(this);
				ele.setName("Content");
				LinkedList<CaoElement> list = new LinkedList<CaoElement>();
				list.add(new JackRootElement(this, node, node.getSession()
						.getWorkspace().getName(), attributes));
				ele.setChildren(new LinkedCaoList(ele, list));
				
				return  ele;
			}
		} catch (Exception e) {
			throw new CaoException(e);
		}
		return super.queryElement(name, access, attributes);
	}

	public SlingConversation createConversation() {
		SlingConversation sc = new SlingConversation(this);
		return sc;
	}

	/**
	 * Return the uri of the element to the sling repository without the pending slash or extension at the end.
	 * If not possible return null.
	 * 
	 * @param element 
	 * @return
	 */
	public String getUri(CaoElement element) {
		
		if (element == null) return getUri();
		
		if (! (element instanceof JackElement) ) return null;
		String uri = getUri();
		if (uri == null) return null;
		
		JackElement jack = (JackElement)element;
		String path;
		try {
			path = jack.getNode().getPath();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return null;
		}
		
		return uri + path;
		
	}
	
	/**
	 * Return the base uri to the sling repository without the pending slash at the end.
	 * If not possible return null.
	 * 
	 * @return
	 */
	public String getUri() {
		JackConfiguration c = ((JackConnection)getConnection()).getConfiguration();
		String uri = c.getUri();
		uri = MString.beforeLastIndex(uri, '/'); // remove .../server
		return uri;
	}
	
}
