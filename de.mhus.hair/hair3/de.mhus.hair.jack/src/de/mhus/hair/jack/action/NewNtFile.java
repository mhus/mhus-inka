package de.mhus.hair.jack.action;

import java.io.ByteArrayInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Node;
import javax.jcr.Session;

import de.mhus.hair.jack.CreateNodeSubOperation;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.cao.CaoOperation;

public class NewNtFile implements CreateNodeSubOperation {

	@Override
	public CaoOperation create(JackElement parent, String name, boolean doSave) throws Exception {
		Node newNode = parent.getNode().addNode(name,"nt:file");
		Node content = newNode.addNode("jcr:content","nt:resource");
		content.setProperty("jcr:data", new ByteArrayInputStream(new byte[0]));
		
		String mimeType = new MimetypesFileTypeMap().getContentType(name);
		content.setProperty("jcr:mimeType", mimeType);
		
		Session session = ((JackConnection)parent.getConnection()).getSession();
		if (doSave) session.save();
		if (newNode != null) {
			parent.getConnection().fireElementCreated(newNode.getIdentifier());
			parent.getConnection().fireElementLink(parent.getId(), newNode.getIdentifier());
		}
		return null;
	}

}
