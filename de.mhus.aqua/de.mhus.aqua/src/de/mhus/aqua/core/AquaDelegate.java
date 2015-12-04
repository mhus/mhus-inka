package de.mhus.aqua.core;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.IAcl;
import de.mhus.aqua.api.IAqua;
import de.mhus.aqua.api.IRequestProcessor;
import de.mhus.aqua.cao.AquaContentApplication;
import de.mhus.aqua.mod.Publisher;
import de.mhus.aqua.res.AquaRes;
import de.mhus.aqua.tpl.Engine;
import de.mhus.lib.config.IConfig;

public class AquaDelegate implements IRequestProcessor,IAqua {

	@Override
	public void processRequest(AquaRequest request) throws Exception {
		Activator.getAqua().processRequest(request);
	}

	@Override
	public void processHeadRequest(AquaRequest request) throws Exception {
		Activator.getAqua().processHeadRequest(request);
	}

	@Override
	public AquaContentApplication getCaoApplication() {
		return (AquaContentApplication) Activator.getAqua().getCaoApplication();
	}

	@Override
	public Engine getTplEngine() {
		return Activator.getAqua().getTplEngine();
	}

	@Override
	public String getBaseDir() {
		return Activator.getAqua().getBaseDir();
	}

	@Override
	public Object getObject(String name) {
		return Activator.getAqua().getObject(name);
	}

	@Override
	public void setObject(String name, Object value) {
		Activator.getAqua().setObject(name, value);
	}

	@Override
	public IConfig getConfig() {
		return Activator.getAqua().getConfig();
	}

	@Override
	public AquaApplication getAquaApplication(String name) throws Exception {
		return Activator.getAqua().getAquaApplication(name);
	}

	@Override
	public AquaRes createAquaRes(String name) throws Exception {
		return Activator.getAqua().createAquaRes(name);
	}

	@Override
	public Publisher createPublisher(String name) throws Exception {
		return Activator.getAqua().createPublisher(name);
	}

	@Override
	public IAcl getAcl(String aclId) throws Exception {
	return Activator.getAqua().getAcl(aclId);
	}

}
