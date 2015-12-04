package de.mhus.aqua.mod.uiapp;

import java.io.PrintWriter;
import java.util.LinkedList;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.aqua.mod.uiapp.wui.IFormSource;
import de.mhus.lib.MException;
import de.mhus.lib.MSingleton;
import de.mhus.lib.MString;
import de.mhus.lib.form.FormBuilder;
import de.mhus.lib.form.FormException;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.MFormModel;
import de.mhus.lib.form.objects.FObject;
import de.mhus.lib.form.objects.FString;

public class FormSource extends AjaxSource implements IFormSource {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(FormSource.class);

	private MForm form;
	private MFormModel model;

	public FormSource(UiBox box, MForm form) throws FormException {
		super(box);
		this.form = form;
		this.model = FormBuilder.buildForm(MSingleton.instance().getActivator(), form);
	}

	@Override
	public MForm getForm() {
		return form;
	}

	@Override
	public MFormModel getModel() {
		return model;
	}

	@Override
	public void processAjax(AquaRequest request, PrintWriter writer)
			throws MException {
		log.t("request",request);
		String formAction = request.getParameter("form");
		if ("load".equals(formAction)) {
			writer.print("<message success=\"true\"><data>");
			for (FObject obj : model.getList()) {
				if (obj instanceof FString) {
					writer.print("<" + obj.getId() + ">");
					String val = ((FString)obj).getValue();
					if (val == null) val = "";
					writer.print( MString.replaceAll(val, "\"", "\\\"") );
					writer.print("</" + obj.getId() + ">");
				}
			}
			writer.print("</data></message>");
		} else
		if ("save".equals(formAction)) {
			LinkedList<String[]> errors = new LinkedList<String[]>();
			FObject first = null;
			for (FObject obj : model.getList()) {
				String val = request.getParameter(obj.getId());
				if (val != null) {
					if (first==null) first = obj;
					if (obj instanceof FString) {
						try {
							((FString)obj).setValue(val);
						} catch (FormException e) {
							errors.add(new String[] {obj.getId(),e.getMessage()});
						}
					}
				}
			}
			try {
				//model.validateTarget();
				model.saveToTarget(true);
			} catch (FormException e) {
				if (first != null) errors.add(new String[] {first.getId(),e.getMessage()});
			}
			if (first == null || errors.size()==0) {
				writer.print("<message success=\"true\">");
				writer.print("</message>");
				model.setError(false);
			} else {
				writer.print("<message success=\"false\">");
				for (String[] error : errors) {
					writer.print("<field><id>" + error[0] + "</id><msg>");
					writer.print( MString.replaceAll(error[1], "\"", "\\\"") );
					log.t(error[0],error[1]);
					writer.print("</msg></field>");
				}
				writer.print("</message>");
				model.setError(true);
			}
		}
		
	}

}
