package de.mhus.cha.cao;

import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.cao.CaoForm;
import de.mhus.cap.app.gui.CapGuiConfiguration;
import de.mhus.lib.MString;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.util.Rfc1738;

@FormElement("name='fs' title='Documentum Configuration'")
public class ChaConfiguration extends CaoForm implements CapGuiConfiguration {
		
	private String path;
	private String gui;
	
	@FormSortId(1)
	@FormElement("file mode='dir' nls='dir' title='Filepath'")
	public void setPath(String in) {
		path = in;
	}
	
	public String getPath() {
		return path;
	}
	
//	@FormIdx(2)
//	@FormElement("input title='GUI Configuration'")
	public void setGui(String in) {
		gui = in;
	}
	
	public String getGui() {
		return gui;
	}
	
	@Override
	public String toUrl() {
		HashMap<String, String> parts = new HashMap<String,String>();
		parts.put("path", getPath());
		if (!MString.isEmpty(getGui()))
			parts.put("gui", getGui());
			
		return Rfc1738.implode(parts);
	}

	@Override
	public void fromUrl(String url) {
		Map<String, String> parts = Rfc1738.explode(url);
		setPath(parts.get("path"));
		setGui(parts.get("gui"));
	}

}
