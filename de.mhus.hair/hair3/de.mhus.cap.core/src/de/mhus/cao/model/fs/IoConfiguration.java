package de.mhus.cao.model.fs;

import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.util.Rfc1738;

@FormElement("name='fs' title='Documentum Configuration'")
public class IoConfiguration extends CaoForm {
		
	private String path;
	
	@FormSortId(1)
	@FormElement("file mode='dir' nls='dir' title='Filepath'")
	public void setPath(String in) {
		path = in;
	}
	
	public String getPath() {
		return path;
	}

	@Override
	public String toUrl(boolean secrets) {
		HashMap<String, String> parts = new HashMap<String,String>();
		parts.put("path", getPath());
			
		return Rfc1738.implode(parts);
	}

	@Override
	public void fromUrl(String url) {
		Map<String, String> parts = Rfc1738.explode(url);
		setPath(parts.get("path"));
	}

}
