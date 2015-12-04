package de.mhus.aqua.api;

import java.io.StringWriter;
import java.sql.ResultSet;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.aqua.cao.AquaConnection;
import de.mhus.aqua.cao.AquaElement;
import de.mhus.lib.MException;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.config.IConfig;
import de.mhus.lib.config.JsonConfig;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbStatement;

public abstract class AquaContainer {
	private static de.mhus.lib.logging.Log log = de.mhus.lib.logging.Log
			.getLog(AquaContainer.class);

	protected AquaApplication app;
	protected IConfig config;
	protected AquaElement node;
	protected AquaSession session;

	public AquaContainer(AquaApplication application, AquaElement node, AquaSession session) throws MException {
		app = application;
		this.node = node;
		this.session = session;
		
		loadConfig();
	}
	
	public void loadConfig() throws MException {
		
		config = node.getApplicationConfig(session.getUser());
		
	}


	public IConfig getConfig() {
		return config;
	}
	
	public void saveConfig() throws Exception {

		/*
		Element ele = config.getDocument().getDocumentElement();
		StringWriter sw = new StringWriter();
		MXml.saveXml(ele, sw, false);
		String config = sw.getBuffer().toString();
		node.saveSessionConfig(config);
		*/
		
		if (config instanceof JsonConfig) {
			JsonConfig jc = (JsonConfig)config;
			ObjectNode jn = jc.getNode();
			ObjectMapper mapper = new ObjectMapper();
			StringWriter writer = new StringWriter();
			mapper.writeValue(writer, jn);
			// node.saveSessionConfig(writer.toString());
		} else {
			//TODO clone config
		}
		
	}
	
	public void saveSessionConfig() throws CaoException {
		log.t("save session");
		node.setApplicationConfig(session.getUser(),config);
	}
	
	public void saveSessionConfigAsDefault() throws CaoException {
		log.t("save session");
		node.setApplicationConfig(config);
	}
	
	public AquaElement getNode() {
		return node;
	}
	
	public AquaSession getSession() {
		return session;
	}
	
	public abstract void process(AquaRequest request) throws Exception;

	public void initContainer() throws MException {
		
	}
	
}
