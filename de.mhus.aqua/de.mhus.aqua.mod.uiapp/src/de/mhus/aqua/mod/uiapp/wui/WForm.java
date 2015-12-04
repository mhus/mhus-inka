package de.mhus.aqua.mod.uiapp.wui;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.Activator;
import de.mhus.lib.MException;
import de.mhus.lib.form.MFormModel;
import de.mhus.lib.form.builders.FormLayoutSimpleBuilder;
import de.mhus.lib.form.layout.LComposite;
import de.mhus.lib.form.layout.LField;
import de.mhus.lib.form.layout.LGroup;
import de.mhus.lib.form.layout.LObject;
import de.mhus.lib.form.layout.LPage;
import de.mhus.lib.form.layout.LTabbed;
import de.mhus.lib.form.objects.FBoolean;
import de.mhus.lib.form.objects.FDate;
import de.mhus.lib.form.objects.FEMail;
import de.mhus.lib.form.objects.FHtml;
import de.mhus.lib.form.objects.FObject;
import de.mhus.lib.form.objects.FPassword;
import de.mhus.lib.form.objects.FString;

public class WForm extends IWTplBox {

	private String title;
	private IFormSource source;
	private String height = "250";
	private String width  = "250";
	private Action completeAction = null;
	private boolean autorender = true;
	
	@Override
	protected void doInit() throws MException {
		setTplName(Activator.instance().getId() + "/WForm");
	}

	@Override
	protected void doFillAttributes(AquaRequest data, Map<String, Object> attr) {
		// fill data for form definition
		MFormModel model = source.getModel();
		if (model.getLayout() == null)
			try {
				model.setLayout(new FormLayoutSimpleBuilder(model));
			} catch (ParserConfigurationException e) {
				e.printStackTrace(); // should never be thrown
			}
		LinkedList<Map<String, Object>> allFields = new LinkedList<Map<String, Object>>();
		doFillAttributes(model.getLayout().getRoot(), attr,allFields);
		attr.put("allfields", allFields);
	}
	
	protected void doFillAttributes(LComposite data, Map<String, Object> attr, LinkedList<Map<String, Object>> allFields) {
		
		LinkedList<Map<String, Object>> form = new LinkedList<Map<String, Object>>();
		
		for ( LObject object : data ) {
			
			HashMap<String, Object> fe = new HashMap<String, Object>();
			fe.put("title", WUtil.toText(nls,object.getTitle()));
			// fe.put("description", WUtil.toText(nls,object.get));
			
			if (object instanceof LTabbed) {
				fe.put("xtype", "tabbed");
				Map<String, Object> composite = new HashMap<String, Object>();
				doFillAttributes((LComposite)object, composite, allFields);
				fe.put("composite", composite);
				
			} else
			if (object instanceof LPage) {
				fe.put("xtype", "page");
				Map<String, Object> composite = new HashMap<String, Object>();
				doFillAttributes((LComposite)object, composite, allFields);
				fe.put("composite", composite);
				
			} else
			if (object instanceof LGroup) {
				fe.put("xtype", "group");
				Map<String, Object> composite = new HashMap<String, Object>();
				doFillAttributes((LComposite)object, composite, allFields);
				fe.put("composite", composite);
				
			} else
			if (object instanceof LComposite) {
				fe.put("xtype", "composite");
				Map<String, Object> composite = new HashMap<String, Object>();
				doFillAttributes((LComposite)object, composite, allFields);
				fe.put("composite", composite);
				
			} else
			if (object instanceof LField) {
				fe.put("xtype", "field");
				fe.put("id", WUtil.toText(((LField)object).getObject().getId()));
				doFillAttributes(((LField)object).getObject(), fe);
				if (!fe.containsKey("type")) {
					fe.remove("xtype");
				}
			}
			
			if (fe.containsKey("xtype")) {
				form.add(fe);
				allFields.add(fe);
			}
			
		}
		
		attr.put("form", form);
		
		// more ...
		attr.put("title", WUtil.toText(nls,title));
		attr.put("height", WUtil.toSize( height ) );
		attr.put("width", WUtil.toSize( width ) );
		attr.put("source", source.getRequest() );
		if (completeAction!=null) attr.put("actioncomplete", completeAction.paint());
		attr.put("autorender",isAutorender());
		
	}
	
	
	protected void doFillAttributes(FObject object, HashMap<String, Object> fe) {
					
		fe.put("id", WUtil.toText(object.getName()));
		fe.put("description", WUtil.toText(nls,object.findDescription()));
		
		if (object instanceof FBoolean)
			fe.put("type", "combobox" );
		else
		if (object instanceof FPassword) {
			fe.put("type", "textfield" );
			fe.put("inputType", "password" );
		} else
		if (object instanceof FEMail) {
			fe.put("type", "email" );
		} else
		if (object instanceof FHtml) {
			fe.put("type", "htmleditor" );
		} else
		if (object instanceof FString) {
			fe.put("type", "textfield" );
			fe.put("inputType", "text" );
		} else
		if (object instanceof FDate) {
			fe.put("type", "datefield" );
		}
		
	}

	@Override
	public void processTplRequest(AquaRequest req, Map<String, Object> params,
			PrintWriter writer) throws MException {
		
		if ("tabbed".equals(params.get("name").toString() )) {
			engine.execute(this, "tabbed", params, writer);
			return;
		}
		if ("group".equals(params.get("name").toString() )) {
			engine.execute(this, "group", params, writer);
			return;
		}
		if ("page".equals(params.get("name").toString() )) {
			engine.execute(this, "page", params, writer);
			return;
		}
		if ("composite".equals(params.get("name").toString() )) {
			engine.execute(this, "composite", params, writer);
			return;
		}
		
		super.processTplRequest(req, params, writer);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setSource(IFormSource source) {
		this.source = source;
	}

	public IFormSource getSource() {
		return source;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		return height;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getWidth() {
		return width;
	}

	public void onCompleteAction(Action completeAction) {
		this.completeAction = completeAction;
	}

	public Action getCompleteAction() {
		return completeAction;
	}

	public Action getLoadAction() {
		return new StringAction("fs_$uid$.loadForm();", getId());
	}
	
	public Action getSaveAction(Action onSuccess, Action onFailure) {
		return new StringAction(
				"var listener=function() {};" + 
				(onSuccess != null ? "listener.success=function() {" + onSuccess.paint() + "};" : "") +
				(onFailure != null ? "listener.failure=function() {" + onFailure.paint() + "};" : "") +				
				"fs_$uid$.saveForm(listener);", getId());
	}

	public Action getRenderAction() {
		return new StringAction("fs_$uid$.render('$uid$');", getId());
	}
	
	public void setAutorender(boolean autorender) {
		this.autorender = autorender;
	}

	public boolean isAutorender() {
		return autorender;
	}
	
	
}
