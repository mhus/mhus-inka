package de.mhus.aqua.api;

import de.mhus.aqua.cao.AquaContentApplication;
import de.mhus.aqua.mod.Publisher;
import de.mhus.aqua.res.AquaRes;
import de.mhus.aqua.tpl.Engine;
import de.mhus.lib.config.IConfig;

public interface IAqua {

	AquaContentApplication getCaoApplication();

	Engine getTplEngine();

	String getBaseDir();

	Object getObject(String name);

	void setObject(String name, Object value);

	IConfig getConfig();

	AquaApplication getAquaApplication(String name) throws Exception;

	AquaRes createAquaRes(String name) throws Exception;

	Publisher createPublisher(String name) throws Exception;

	public IAcl getAcl(String aclId) throws Exception;

}
