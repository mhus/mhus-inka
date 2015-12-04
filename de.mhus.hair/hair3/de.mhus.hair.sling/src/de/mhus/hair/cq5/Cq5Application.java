package de.mhus.hair.cq5;

import java.util.LinkedList;

import javax.jcr.Node;

import de.mhus.hair.jack.JackConfiguration;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackRootElement;
import de.mhus.hair.sling.SlingApplication;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.util.LinkedCaoList;
import de.mhus.lib.cao.util.MutableElement;
import de.mhus.lib.config.IConfig;

public class Cq5Application extends SlingApplication {

	public Cq5Application(JackConnection connection, IConfig config)
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

	public String getUri() {
		JackConfiguration c = ((JackConnection)getConnection()).getConfiguration();
		String uri = c.getUri();
		uri = MString.beforeLastIndex(uri, '/'); // remove .../server
		uri = MString.beforeLastIndex(uri, '/'); // remove .../crx
		return uri;
	}
	
}
