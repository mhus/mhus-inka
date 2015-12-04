package org.apache.sling;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.security.user.UserAccessControlProvider;

public class Test {

	public static void main(String[] args) throws RepositoryException {
		
		Repository repo = JcrUtils.getRepository("http://localhost:4502/crx/server");
		Session session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));

		Workspace workspace = session.getWorkspace();
		
		System.out.println(workspace.getName());
		
		Node node = session.getNode("/content/geometrixx_mobile/en/products");
		
		System.out.println(node.getName());

		UserAccessControlProvider acp = new UserAccessControlProvider();
		HashMap<String, String> config = new HashMap<String, String>();
		acp.init(session, config);
		
	}
	
}
