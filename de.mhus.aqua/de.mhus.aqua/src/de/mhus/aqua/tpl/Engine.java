package de.mhus.aqua.tpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.IConfig;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class Engine {

	private Configuration cfg;

	public Engine() throws IOException {
		
		cfg = new Configuration();
		
//		String configFile = aquaConnection.getConfig().getBaseDir() + Aqua.TPL_CONFIG;
//		Properties prop = new Properties();
//		FileInputStream is = new FileInputStream(configFile);
//		prop.load(is);
//		is.close();
		
		IConfig config = Activator.getAqua().getConfig().getConfig("deploy");
		
		// Specify the data source where the template files come from.
		// Here I set a file directory for it:
		// cfg.setDirectoryForTemplateLoading(new File(aquaConnection.getConfig().getBaseDir() + "/" + prop.getProperty("template.dir")));
		cfg.setDirectoryForTemplateLoading(new File(config.getExtracted("templates")));
		// Specify how templates will see the data-model. This is an advanced topic...
		// but just use this:
		cfg.setObjectWrapper(new DefaultObjectWrapper());  
		
		cfg.setSharedVariable("process", new ProcessDirective());
	}
	
	public String execute(TplTask task, String section, Map<String,Object> attributes) throws MException {
		if (attributes==null)
			attributes = new HashMap<String, Object>();
		attributes.put("uid", task.getId());
		attributes.put("_tpltask", task);
		return execute(task.getTplFileName(section), attributes);
	}
	
	public void execute(TplTask task, String section, Map<String,Object> attributes, PrintWriter stream) throws MException {
		if (attributes==null)
			attributes = new HashMap<String, Object>();
		attributes.put("uid", task.getId());
		attributes.put("_tpltask", task);
		execute(task.getTplFileName(section), attributes, stream);		
	}
	
	public String execute(String tpl,Map<String,Object> attributes) throws MException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		execute(tpl, attributes, pw);
		pw.flush();
		return sw.toString();
	}

	public void execute(String tpl, Map<String,Object> attributes, PrintWriter stream) throws MException {
		try {
			Template temp = cfg.getTemplate(tpl);
			attributes.put("_stream", stream);
			temp.process(attributes, stream);
			// stream.flush();
		} catch (Exception e) {
			throw new CaoException(e);
		}
		
	}
	
	public Map<String, Object> createAttributes(AquaRequest req) {
		HashMap<String, Object> ret = new HashMap<String, Object>();
		ret.put("baseurl", Activator.getAqua().getConfig().getExtracted("WEB_PATH",""));
		if ( req!=null ) {
			ret.put("_request", req);
			ret.put("path", req.getPath());
		}
		return ret;
	}

}
