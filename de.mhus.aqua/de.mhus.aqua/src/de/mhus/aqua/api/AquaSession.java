package de.mhus.aqua.api;

import de.mhus.aqua.Activator;
import de.mhus.aqua.aaa.User;
import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.aqua.core.Aqua;
import de.mhus.lib.config.IConfig;

public abstract class AquaSession {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(AquaSession.class);

	private AquaElement node;
	private IUser user;
	// private HttpSession session;
	private String defLocale;

	private boolean adminActive;

	private String locale;

	public AquaSession() throws Exception  {
		this.user = Activator.getAqua().getUserForSession(this);
		defLocale = Activator.getAqua().getConfig().getConfig("localisation").getString("default", "en_US");
		locale = defLocale;
	}
	
	public AquaElement getNode() {
		return node;
	}

	public abstract Object getAttribute(String name);

	public abstract void setAttribute(String name, Object value);
//	{
//		if (name.equals(USER)) {
//			log.t("set user",value,user);
//			adminActive = false;
//			LinkedList<String> names = new LinkedList<String>();
//			for ( Enumeration<String> en = session.getAttributeNames();en.hasMoreElements();) {
//				String n = en.nextElement();
//				if (!SESSION.equals(n) && !LOCALE.equals(n))
//					names.add(n);
//			}
//			for (String n : names)
//				session.removeAttribute(n);
//			user = (User)value;
//		} else
//			session.setAttribute(name, value);
//	}	
	
	public String getLocale()
	{
		return locale;
	}

	public void setLocale(String locale)
	{
		for (IConfig cloc : Activator.getAqua().getConfig().getConfig("localisation").getConfigBundle("locale")) {
			if (cloc.getString("name", "").equals(locale)) {
				this.locale = locale;
				return;
			}
		}
		log.d("locale not found",locale);
		return;
	}

	public String getDefaultLocale() {
		return defLocale;
	}

	public final void setAdminActive(boolean in) {
		if (!user.isAdmin()) return;
		adminActive = in;
	}
	
	public final boolean isAdminActive() {
		return adminActive;
	}
	
	public final IUser getUser() {
		return user;
	}

	public final IUser changeUser(AquaRequest request) throws Exception {
		IUser newUser = Activator.getAqua().getUser(request);
		if (newUser!=null) {
			adminActive = false;
			cleanSession();
			user = newUser;
		}
		return newUser;
	}

	protected abstract void cleanSession();

	public final IUser setDefaultUser() throws Exception {
		IUser newUser = Activator.getAqua().getDefaultUser();
		if (newUser!=null) {
			adminActive = false;
			cleanSession();
			user = newUser;
		}
		return newUser;
	}

	public final boolean isDefaultUser() {
		return Aqua.DEFAULT_USER.equals(getUser().getId());
	}
	
}
