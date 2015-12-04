package de.mhus.aqua.mod.uiapp.wui;

import java.util.Map;

import de.mhus.aqua.tpl.Engine;
import de.mhus.aqua.tpl.TplTask;
import de.mhus.lib.MException;
import de.mhus.lib.logging.Log;

/**
 * Represent a include to the page. It will handle the template transformation be itself.
 * Main point is that the template can be recreated on call (if needed) or cached. Maybe detect change of the
 * template file. Cache in a soft link will optimize for server mode.
 * 
 * @author mikehummel
 *
 */
public class IWTplInclude extends WInclude {

	private static Log log = Log.getLog(IWTplInclude.class);
	private Engine engine;
	private TplTask task;
	private String section;
	private Map<String, Object> attributes;

	public IWTplInclude(Engine engine, TplTask task, String section, Map<String,Object> attributes) {
		this.engine = engine;
		this.task = task;
		this.section = section;
		this.attributes = attributes;
	}

	public String toString() {
		try {
			return engine.execute(task,section,attributes); //TODO cache
		} catch (MException e) {
			log.d(engine,task,section,attributes,e);
			return "";
		}
	}
	
}
