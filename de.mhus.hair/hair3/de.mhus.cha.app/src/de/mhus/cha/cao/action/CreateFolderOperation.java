package de.mhus.cha.cao.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cha.cao.ChaConnection;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.MXml;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='cha_create_folder' title='Create Node'")
public class CreateFolderOperation extends CaoOperation implements MForm {

	private String folderName;
	private CaoElement target;
	
	public CreateFolderOperation(CaoList list) {
	}

	@Override
	public void dispose() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		monitor.beginTask("Create", 1);
		String newId = ((ChaConnection)target.getConnection()).createUID();
		File newFile = new File( ((ChaElement)target).getFile(), newId );
		if ( newFile.mkdir() ) {
			try {
				
				// write xml file
				
				Document xml = MXml.createDocument();
				
				Element root = xml.createElement("root");
				xml.appendChild(root);
				
				Element name = xml.createElement("name");
				name.setTextContent(folderName);
				root.appendChild(name);
				
				FileOutputStream fos = new FileOutputStream(new File(newFile,"data.xml"));
				MXml.saveXml(xml.getDocumentElement(), fos);
				fos.close();
				
				((ChaConnection)target.getConnection()).addIdPath(newId, newFile.getAbsolutePath());
				target.getConnection().fireElementCreated( newId );
				target.getConnection().fireElementLink(target.getId(), newId );
			} catch (Exception ioe) {
				monitor.log().warn(ioe);
			}
		}
		monitor.worked(1);
	}

	@Override
	public void initialize() throws CaoException {
	}

	@FormSortId(1)
	@FormElement("input title='New Node Name'")
	public void setFolderName(String in) {
		folderName = in;
	}

	public void setTarget(CaoElement target) {
		this.target = target;
	}
	
	
}
