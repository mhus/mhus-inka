package de.mhus.aqua.res;

import java.util.HashMap;

import de.mhus.aqua.api.AquaApplication;
import de.mhus.aqua.api.AquaContainer;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;
import de.mhus.lib.config.XmlConfig;
import de.mhus.lib.logging.Log;

public class ResApplication extends AquaApplication {

	private static Log log = Log.getLog(ResApplication.class);
	private HashMap<String, AquaRes> resources = new HashMap<String, AquaRes>();
	

	@Override
	public void process(AquaRequest request)
			throws Exception {
		
		if (request.getExtPath()==null) {
			request.sendErrorNotFound();
			log.t("no ext path",request.getPath());
			return;
		}
		
		int pos = request.getExtPath().indexOf("/");
		if (pos < 0) {
			request.sendErrorNotFound();
			log.t("no / in ext path",request.getPath(),request.getExtPath());
			return;			
		}
		String resId   = request.getExtPath().substring(0,pos);
		String extPath = request.getExtPath().substring(pos);

		AquaRes res = resources.get(resId);
		if (res == null) {
			log.d("resource not found",resId,extPath);
			request.sendErrorNotFound();
			return;						
		}
		
		request.setExtPath(extPath);
		res.process(request);
				
	}

	@Override
	public void initialize() throws Exception {
	}

	public void register(AquaRes res) {
		String id = res.getName() + "_" + res.getVersion();
		log.d("register",id);
		resources.put(id, res);
	}

	@Override
	public XmlConfig createDefaultConfig() {
		return new XmlConfig();
	}

	@Override
	public AquaContainer getUiContainer(AquaRequest request) throws MException {
		return null;
	}

}
