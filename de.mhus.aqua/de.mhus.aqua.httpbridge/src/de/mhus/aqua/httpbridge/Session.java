package de.mhus.aqua.httpbridge;

import java.util.Enumeration;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import de.mhus.aqua.api.AquaSession;

public class Session  extends AquaSession {

	private HttpSession session;

	public Session(HttpSession session) throws Exception {
		this.session = session;
	}

	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void cleanSession() {
		LinkedList<String> names = new LinkedList<String>();
		for ( Enumeration<String> en = session.getAttributeNames();en.hasMoreElements();) {
			String n = en.nextElement();
			if (!Activator.ATTRIBUTE_SESSION.equals(n))
				names.add(n);
		}
		for (String n : names)
			session.removeAttribute(n);

	}

	public String toString() {
		return session.getId();
	}
	
}
