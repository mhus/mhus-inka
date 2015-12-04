package de.mhus.aqua.cao;

import java.io.IOException;
import java.util.HashMap;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.UserRights;
import de.mhus.aqua.api.AquaSession;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.jmx.JmxMap;
import de.mhus.lib.logging.Log;

public abstract class AquaContentApplication extends CaoApplication<AquaSession> {
	
	private static Log log = Log.getLog(AquaContentApplication.class);
	
	private HashMap<String, AquaElement> cache = new HashMap<String, AquaElement>();

	protected AquaContentApplication(AquaConnection connection) throws CaoException {
		super(connection);
		try {
			Activator.getAqua().getJmxManager().register(new JmxMap(this, "nodes", cache));
		} catch (Exception e) {
			log.i(e);
		}
	}

	@Override
	public CaoList<AquaSession> queryList(String name, AquaSession access, String... args) throws CaoException {
		return null;
	}

	@Override
	public CaoElement<AquaSession> queryTree(String name, AquaSession access, String... args)
			throws CaoException {
		if (AquaConnection.TREE_NODE.equals(name)) {
			String path = args[0];
			String ext  = args[1];
						
			AquaElement element = cache.get(path);
			if (element != null) {
				if (ext != null) {
					element = (AquaElement)element.getExtendedNode(ext);
				}
				log.t("load from cache",path,ext);
				//check rights
				if (!element.getAcl().hasRight(access, UserRights.READ)) {
					log.t("access denide",path,ext);
					return null;
				}
				return element;
			}
			
			String[] parts = MString.split(path, "/");
			try {
				log.t("create",path,ext);
				element = createRootElement();
				for (int i = 1; i < parts.length; i++) {
					element = (AquaElement) element.getChild(parts[i], access);
					if (element == null) {
						log.t("not found",path,ext,i);
						return null;
					}
				}
				cache.put(path, element);
				if (ext != null) {
					element = (AquaElement)element.getExtendedNode(ext);
				}
			} catch (Exception e) {
				log.error(name,e);
				return null;
			}
			if (!element.getAcl().hasRight(access, UserRights.READ)) {
				log.t("access denide",path,ext);
				return null;
			}
			return element;
		}
		throw new CaoException("Unknown Tree");
	}

	protected abstract AquaElement createRootElement() throws IOException, Exception;
	

}
