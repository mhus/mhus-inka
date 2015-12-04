package de.mhus.aqua.cao;

import de.mhus.aqua.Activator;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.config.IConfig;

public class AquaConfig extends CaoForm {

	private IConfig config;
	private String baseDir;

	@Override
	public void fromUrl(String url) throws CaoException {
		try {
//			Map<String, String> map = Rfc1738.explode(url);
//			baseDir = map.get("directory");
//			File   configFile = new File(baseDir + Aqua.CONFIG_PATH);
//			InputStream is = new FileInputStream(configFile);
//			config = new XmlConfig(null,MXml.loadXml(is).getDocumentElement());
			config = Activator.getAqua().getConfig().getConfig("cao");
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public String toUrl() {
		return null;
	}

	public IConfig getConfig() {
		return config;
	}

	public String getBaseDir() {
		return baseDir;
	}
	
}
