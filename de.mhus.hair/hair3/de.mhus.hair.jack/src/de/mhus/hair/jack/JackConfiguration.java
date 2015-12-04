package de.mhus.hair.jack;

import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.MPassword;
import de.mhus.lib.cao.CaoForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.util.Rfc1738;

@FormElement("name='jack' nls='form' title='Jackrabbit Configuration'")
public class JackConfiguration extends CaoForm {

	private String uri;
	private String user;
	private String password;
	private String workspace;

	@FormSortId(1)
	@FormElement("input title='URI'")
	public void setUri(String in) {
		uri = in;
	}
	
	public String getUri() {
		return uri;
	}
	
	@FormSortId(2)
	@FormElement("input title='User' nls='user'")
	public void setUser(String in) {
		user = in;
	}
	
	public String getUser() {
		return user;
	}
	
	@FormSortId(3)
	@FormElement("password title='Password' nls='password'")
	public void setPassword(String in) {
		password = in;
	}
	
	public String getPassword() {
		return password;
	}
	
	@Override
	public String toUrl(boolean secrets) {
		HashMap<String, String> parts = new HashMap<String,String>();
		parts.put("uri", uri);
		parts.put("user", getUser());
		parts.put("workspace", getWorkspace());
		if (secrets) parts.put("password", MPassword.encode(getPassword()));
			
		return Rfc1738.implode(parts);
	}

	@Override
	public void fromUrl(String url) {
		Map<String, String> parts = Rfc1738.explode(url);
		setUri(parts.get("uri"));
		setUser(parts.get("user"));
		setWorkspace(parts.get("workspace"));
		setPassword(MPassword.decode(parts.get("password")));

	}

	@FormSortId(4)
	@FormElement("input title='Workspace' nls='workspace' value='default'")
	public void setWorkspace(String in) {
		workspace = in;
	}

	public String getWorkspace() {
		return workspace;
	}

}
