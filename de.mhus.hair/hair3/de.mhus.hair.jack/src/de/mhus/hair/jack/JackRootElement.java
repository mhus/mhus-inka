package de.mhus.hair.jack;

import javax.jcr.Node;

import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoException;

public class JackRootElement extends JackElement {

	private String name;

	public JackRootElement(CaoApplication app, Node node, String name,
			String[] attributes) throws CaoException {
		super(app, node, attributes);
		this.name = name;
	}
	
	public String getName() throws CaoException {
		return name;
	}

}
