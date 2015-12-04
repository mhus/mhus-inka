package de.mhus.aqua.tpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MException;
import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class ProcessDirective implements TemplateDirectiveModel {

	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(ProcessDirective.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(Environment env,
            Map params, TemplateModel[] loopVars,
            TemplateDirectiveBody body)
            throws TemplateException, IOException {

		TplTask task = (TplTask)((StringModel)env.getGlobalVariable("_tpltask")).getWrappedObject();
		PrintWriter writer = (PrintWriter)((StringModel)env.getGlobalVariable("_stream")).getWrappedObject();
	    AquaRequest req = (AquaRequest)((StringModel)env.getGlobalVariable("_request")).getWrappedObject();
		try {
			task.processTplRequest(req, params,writer);
		} catch (MException e) {
			log.i(req,params,e);
		}
				
	}

}
