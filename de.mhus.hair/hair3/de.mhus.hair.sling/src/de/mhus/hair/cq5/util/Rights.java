package de.mhus.hair.cq5.util;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

public class Rights {

	private Node node;
	private Map<String, Info> privileges = new HashMap<String, Info>();

	public Rights(Node node) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
		this.node = node;
		findRights();
	}

	public void findRights() throws AccessDeniedException, ItemNotFoundException, RepositoryException {
		Node parent = node;

		privileges.clear();
		
		while (parent != null) {
			
			// look for policy note
			if (parent.hasNode("rep:policy")) {
				Node policy = parent.getNode("rep:policy");
				// found it, iterate all policies
				for (NodeIterator ni = policy.getNodes(); ni.hasNext();) {
					Node policyACE = ni.nextNode();
					
					String principalName = policyACE.getProperty("rep:principalName").getString();
					boolean isGlob = policyACE.hasProperty("rep:glob");
					boolean allow = policyACE.getName().startsWith("allow");
					for (PropertyIterator pi = policyACE.getProperties( "rep:privileges" ); pi.hasNext();) {
						Property acePrivileg = pi.nextProperty();
						String priv = acePrivileg.getString();
						
						String privId = principalName + "|" + priv + "|" + isGlob;
						
						// if not already exists, it's a new right.
						if (!privileges.containsKey(privId)) {
							privileges.put(privId, new Info(priv,principalName,isGlob,allow,parent));
						}
						
					}
					
				}
			}
			
			parent = parent.getParent();
		}
	}
	
	private class Info {

		private boolean allow;

		public Info(String priv, String principalName, boolean isGlob,
				boolean allow, Node parent) {
			this.allow = allow;
		}
		
	}
	
}
