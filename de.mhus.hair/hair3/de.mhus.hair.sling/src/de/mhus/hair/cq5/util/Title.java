package de.mhus.hair.cq5.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class Title extends Paragraph {

	private String title;

	@Override
	public void createContent(Node par) throws RepositoryException {
		String name = findName("title",par);
		Node cnt = par.addNode(name,"nt:unstructured");
		cnt.setProperty("sling:resourceType","foundation/components/title");
		cnt.setProperty("jcr:title", title);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
