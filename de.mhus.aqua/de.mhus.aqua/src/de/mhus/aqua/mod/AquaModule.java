package de.mhus.aqua.mod;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.IAqua;
import de.mhus.aqua.res.AquaRes;
import de.mhus.aqua.res.ResApplication;
import de.mhus.lib.MXml;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.XmlConfig;

public class AquaModule implements BundleActivator {

	protected BundleContext context;
	private IAqua aqua;
	private XmlConfig config;
	private String contentDir;
	private String templateDir;
	private JmxAquaModule jmx;
	protected static HashMap<String, AquaRes> resources = new HashMap<String, AquaRes>();

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		
		//URL x = context.getBundle().getEntry("config.xml");
		//InputStream is = x.openStream();

		ServiceReference reference = context.getServiceReference(IAqua.class.getName());
		aqua = (IAqua) context.getService(reference);

		FileInputStream is = new FileInputStream(new File(getBaseDir(),"config.xml"));
		config = new XmlConfig(MXml.loadXml(is).getDocumentElement());
		config.setString("_path", getBaseDir().getAbsolutePath());
		is.close();
				
		// init directories to publish
		IConfig cp = aqua.getConfig().getConfig("deploy");
		contentDir = cp.getExtracted("content") + "/" + getId();
		templateDir = cp.getExtracted("templates") + "/" + getId();
		
		getConfiguration().setProperty("_contentdir", contentDir);
		getConfiguration().setProperty("_templatedir", templateDir);
		
		// publish
		publish();
		
		// load resources
		loadResources();

		jmx = new JmxAquaModule(this);
		Activator.getAqua().getJmxManager().register(jmx);
		
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		Activator.getAqua().getJmxManager().unregister(jmx);
		jmx = null;
		context = null;
		aqua = null;
	}

	public void loadResources() throws Exception {
		ResApplication resApp = (ResApplication)getAqua().getAquaApplication(ResApplication.class.getCanonicalName());
		IConfig config = getConfiguration();
		IConfig resources = config.getConfig("resources");
		if (resources!=null) {
			for ( IConfig resource : resources.getConfigBundle("resource") ) {
				AquaRes res = getAqua().createAquaRes(resource.getProperty("type"));
				res.setConfig(resource);
				resApp.register(res);
				this.resources.put(resource.getProperty("name"),res);
			}
		}
	}
	
	public void publish() throws Exception {
		
		getContentFile(null).mkdirs();
		getTemplateFile(null).mkdirs();
		
		// publish
		IConfig cdeploy = getConfiguration().getConfig("deploy");
		if (cdeploy != null) {
			for (IConfig cpublish : cdeploy.getConfigBundle("content")) {
				publish(cpublish,getContentFile(null));
			}
			for (IConfig cpublish : cdeploy.getConfigBundle("templates")) {
				publish(cpublish,getTemplateFile(null));
			}
			for (IConfig cpublish : cdeploy.getConfigBundle("other")) {
				publish(cpublish,null);
			}
		}
		
	}

	private void publish(IConfig config, File dir) throws Exception {
		String clazz = config.getString("class", null);
		Publisher publisher = null;
		if (clazz == null)
			publisher = new PathPublisher();
		else
			publisher = getAqua().createPublisher(clazz);

		publisher.publish(config,dir);
		
	}

	public IConfig getConfiguration() {
		return config;
	}

	public String getId() {
		return context.getBundle().getSymbolicName();
	}
	
	public File getBaseDir() {
		return new File(getAqua().getBaseDir(),"modules/"+getId());
	}

	public IAqua getAqua() {
		return aqua;
	}

	public File getContentFile(String path) {
		if (path==null) return new File(contentDir);		
		return new File(contentDir,path);
	}
		
	public File getTemplateFile(String path) {
		if (path==null) return new File(templateDir);
		return new File(templateDir,path);
	}
	
	public AquaRes getRes(String name) {
		return resources.get(name);
	}

}
