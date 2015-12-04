package de.mhus.aqua.mod.uiapp.wui;

import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.api.IUser;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.aqua.mod.uiapp.AjaxAction;
import de.mhus.aqua.mod.uiapp.AjaxActionDefinition;
import de.mhus.aqua.mod.uiapp.UiBox;
import de.mhus.lib.MException;
import de.mhus.lib.config.IConfig;

public class WLogin extends UiBox {

	
	private AjaxAction login;
	private AquaRequest request;

	@Override
	public void initWElement(AquaRequest request, String id, IConfig config) throws MException {
		this.request = request;
		super.initWElement(request, id, config);
	}
	
	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WLogin");
		setNls(new WNls(request,Activator.instance(),"login"));
		login = new AjaxAction(this) {
			
			@Override
			public AjaxActionDefinition[] doRequest(AquaRequest request)
					throws Exception {

				String mode = request.getParameter("mode");
				StringComponent result = new StringComponent();
				if ("login".equals(mode)) {
					IUser user = request.getSession().changeUser(request);
					if (user != null) {
						try {
							result.setContent("<script>window.location.reload();</script>");
						} catch (Exception e) {
							result.setContent("<script>alert('Login failed');</script>"); //TODO !!! How to send errors ...
						}
					} else {
						result.setContent("<script>alert('Login failed');</script>"); //TODO !!! How to send errors ...						
					}
				} else
				if ("logout".equals(mode)) {
					
					request.getSession().setDefaultUser(); //TODO ?? User for http session (automatic relogin)
					result.setContent("<script>window.location.reload();</script>");
				} else
				if ("on".equals(mode)) {
					request.getSession().setAdminActive(true);
					result.setContent("<script>window.location.reload();</script>");
				} else
				if ("off".equals(mode)) {
					request.getSession().setAdminActive(false);
					result.setContent("<script>window.location.reload();</script>");
				}
				return new AjaxActionDefinition[] {new AjaxActionDefinition(ACTION.FIRST, "body", result)};
			}
		};
		//login.setExtra("+ \"&user=\" + document.forms['aqualogin'].user.value");
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		attr.put("need_login", request.getSession().isDefaultUser() );
		attr.put("login_action", login.getAddress());
		attr.put("is_admin", request.getSession().getUser().isAdmin());
		attr.put("is_admin_mode", request.getSession().isAdminActive());
	}

	@Override
	public String getTitle() {
		return nls.find("title=Login");
	}

	@Override
	public boolean canChangeHeight() {
		return false;
	}

	@Override
	public void setHeight(int height) {
	}

	@Override
	public int getHeight() {
		return 0;
	}

}
