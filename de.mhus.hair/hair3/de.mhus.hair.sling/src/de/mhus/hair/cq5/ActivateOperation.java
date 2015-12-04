package de.mhus.hair.cq5;

import java.util.HashMap;

import com.meterware.httpunit.WebResponse;

import de.mhus.hair.sling.SlingConversation;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;

@FormElement("name='cq5_activate' title='Activate'")
public class ActivateOperation extends CaoOperation implements MForm {

	private CaoList sources;

	@Override
	public void initialize() throws CaoException {
		
	}

	@Override
	public void execute() throws CaoException {
		monitor.beginTask("activate", sources.size());
		
		for (CaoElement source : sources.getElements()) {
			Cq5Application app = (Cq5Application) source.getApplication();
			try {
				
				monitor.log().i(source.getName());

				SlingConversation con = app.createConversation();
				
				HashMap<String, String> post = new HashMap<String, String>();
				post.put("path", source.getName());
				post.put("cmd", "Activate");
				WebResponse response = con.request("/bin/replicate.json",post);
				if (response.getResponseCode() == 200) {
					System.out.println( response.getText() );
				}
			} catch (Throwable e) {
				monitor.log().i(e);
			}
			monitor.nextFinished();
		}
	}

	@Override
	public void dispose() throws CaoException {
		
	}

	public void setSource(CaoList list) {
		this.sources = list;
	}

}
