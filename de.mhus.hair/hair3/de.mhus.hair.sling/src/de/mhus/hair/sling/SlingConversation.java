package de.mhus.hair.sling;

import java.io.IOException;
import java.util.Map;

import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import de.mhus.hair.jack.JackConfiguration;
import de.mhus.hair.jack.JackConnection;
import de.mhus.lib.MCast;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.config.ConfigUtil;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.MConfigFactory;

public class SlingConversation extends WebConversation {

	private SlingApplication app;

	@SuppressWarnings("deprecation")
	public SlingConversation(SlingApplication app) {
		this.app = app;
		
		HttpUnitOptions.setScriptingEnabled(false);

		JackConfiguration c = ((JackConnection)app.getConnection()).getConfiguration();
		setAuthorization(c.getUser(), c.getPassword());
		setUserAgent("cao");
		IConfig proxy = app.getConfig().getConfig("proxy");
		if (proxy != null) {
			setProxyServer(proxy.getExtracted("host"), MCast.toint(proxy.getExtracted("port", "8080"),8080), proxy.getExtracted("user"), proxy.getExtracted("password"));
		}
	}

	public WebResponse request(CaoElement element, String suffix) throws IOException, SAXException {
		 WebRequest req = new GetMethodWebRequest( app.getUri(element) + suffix );
		 return getResponse( req );
	}

	public IConfig requestConfig(CaoElement element, String suffix) throws Exception {
		 WebRequest req = new GetMethodWebRequest( app.getUri(element) + suffix );
		 WebResponse res = getResponse( req );
		 if ( res.getResponseCode() != 200 )
			 return null;
		 String content = res.getText();
		 IConfig config = MConfigFactory.getInstance().toConfig(content);
		 return config;
	}
	
	public IConfig requestConfig(CaoElement element, String suffix, Map<String, String> post) throws Exception {
		 WebRequest req = new PostMethodWebRequest( app.getUri(element) + suffix );
		 if (post != null) {
			 for (Map.Entry<String, String> entry : post.entrySet())
				 req.setParameter(entry.getKey(), entry.getValue());
		 }
		 WebResponse res = getResponse( req );
		 if ( res.getResponseCode() != 200 )
			 return null;
		 String content = res.getText();
		 IConfig config = MConfigFactory.getInstance().toConfig(content);
		 return config;
	}
	
	public IConfig requestConfig(String path, Map<String, String> post) throws Exception {
		 WebRequest req = new PostMethodWebRequest( app.getUri() + path);
		 if (post != null) {
			 for (Map.Entry<String, String> entry : post.entrySet())
				 req.setParameter(entry.getKey(), entry.getValue());
		 }

		 WebResponse res = getResponse( req );
		 if ( res.getResponseCode() != 200 )
			 return null;
		 String content = res.getText();
		 IConfig config = MConfigFactory.getInstance().toConfig(content);
		 return config;
	}
	
	public IConfig requestConfig(String path) throws Exception {
		 WebRequest req = new GetMethodWebRequest( app.getUri() + path );
		 WebResponse res = getResponse( req );
		 if ( res.getResponseCode() != 200 )
			 return null;
		 String content = res.getText();
		 IConfig config = MConfigFactory.getInstance().toConfig(content);
		 return config;
	}

	public WebResponse request(String path, Map<String, String> post) throws Exception {
		 WebRequest req = new PostMethodWebRequest( app.getUri() + path);
		 if (post != null) {
			 for (Map.Entry<String, String> entry : post.entrySet())
				 req.setParameter(entry.getKey(), entry.getValue());
		 }

		 return getResponse( req );
	}

}
