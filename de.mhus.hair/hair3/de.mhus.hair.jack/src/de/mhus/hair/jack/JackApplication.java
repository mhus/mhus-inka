package de.mhus.hair.jack;

import javax.jcr.Node;

import de.mhus.lib.cao.CaoAccess;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.util.EmptyElement;
import de.mhus.lib.config.IConfig;

public class JackApplication extends CaoApplication {

	public JackApplication(JackConnection connection, IConfig config) throws CaoException {
		super(connection,config);
	}

	@Override
	public CaoElement queryElement(String name, CaoAccess access, String... attributes)
			throws CaoException {
		
		try {
			if (CaoDriver.TREE_DEFAULT.equals(name)) {
				Node node = ((JackConnection)getConnection()).getSession().getRootNode();
				return new JackRootElement(this,node,node.getSession().getWorkspace().getName(),attributes);
			}
			if (CaoDriver.LIST_DEFAULT.equals(name)) {
				return new EmptyElement(this,name, new JackList(new EmptyElement(this, name),name,attributes) );
			}
			
			if ("xpath".equals(name)) {
				return new JackQueryResult(this,"xpath",attributes[0]);
			}
			
			if ("sql".equals(name)) {
				return new JackQueryResult(this,"JCR-SQL2",attributes[0]);
			}
			
		} catch (Exception e) {
			throw new CaoException(e);
		}
		throw new CaoException("Unknown: " + name);
	}

//	@Override
//	public CaoList queryList(String name, CaoAccess access, String... attributes)
//			throws CaoException {
//		return new JackList(new EmptyElement(this, name),name,attributes);
//	}

}
