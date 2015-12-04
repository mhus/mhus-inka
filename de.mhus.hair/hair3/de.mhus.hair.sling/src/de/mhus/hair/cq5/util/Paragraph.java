package de.mhus.hair.cq5.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


public abstract class Paragraph {

	public abstract void createContent(Node par) throws RepositoryException;
	
	public static String findName(String string, Node par) throws RepositoryException {
		String name = string;
		int cnt = -1;
		while (par.hasNode(name)) {
			cnt++;
			name = string + "_" + cnt;
		}
		return name;
	}
	
}
