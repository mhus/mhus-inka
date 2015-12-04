package de.mhus.hair.jack.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import de.mhus.cao.model.fs.IoElement;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='jack_import_fs' title='Import'")
public class ImportFsOperation extends CaoOperation implements MForm {

	private JackElement target;
	private CaoList sources;
	private String folderType;
	private String fileType;
	private boolean hidden;
	private boolean merge;
	private boolean overwrite;

	public ImportFsOperation(JackElement target) {
		this.target = target;
	}

	public void setSources(CaoList list) {
		this.sources = list;
	}

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		
		for (CaoElement item : sources.getElements()) {
			try {
				imp(item,target);
			} catch (Exception e) {
				monitor.log().e(item,e);
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void imp(CaoElement src, JackElement dst) throws Exception {
		
		
		monitor.log().d("imp",src,dst);
		
		File file = ((IoElement)src).getFile();
		if (!file.exists()) {
			monitor.log().d("imp","not exists",file);
			return;
		}
		if (!file.canRead()) {
			monitor.log().d("imp","no read access",file);
			return;
		}
		if (!hidden && file.isHidden()) {
			monitor.log().d("imp","hidden",file);
			return;
		}

		String name = src.getName();
		Node dstNode = dst.getNode();
		String type = fileType;
		if (src.isNode())
			type = folderType;

		Node newNode = null;
		boolean created = false;
		
		// find it
		if (merge || overwrite) {
			try {
				newNode = dstNode.getNode(name);
			} catch (PathNotFoundException e) {
				
			}
		}
		
		// create it
		if (newNode == null) {
			newNode = dstNode.addNode(name,type);
			created = true;
		}
		
		// its a file
		if (!src.isNode()) {
			
			if (created || overwrite) {
				FileInputStream fis = new FileInputStream(file);
				Node content = null;
				if (created)
					content = newNode.addNode("jcr:content","nt:resource");
				else {
					try {
						content = newNode.getNode("jcr:content");
					} catch (PathNotFoundException e) {
						content = newNode.addNode("jcr:content","nt:resource");
					}
				}
				content.setProperty("jcr:data", fis);
			
				String mimeType = new MimetypesFileTypeMap().getContentType(name);
				content.setProperty("jcr:mimeType", mimeType);
			}
		}
		
		
		Session session = ((JackConnection)target.getConnection()).getSession();
		session.save();
		
		if (newNode != null) {
			if (created) {
				dst.getConnection().fireElementCreated(newNode.getIdentifier());
				dst.getConnection().fireElementLink(dst.getId(), newNode.getIdentifier());
			} else {
				dst.getConnection().fireElementUpdated(newNode.getIdentifier());
			}
		}

		JackElement newDst = new JackElement(dst,newNode,dst.getAttributes());
		
		if (src.isNode()) {
			try {
				for (CaoElement children : src.getChildren().getElements()) {
					imp(children,newDst);
				}
			} catch (Exception e) {
				monitor.log().e(src,e);
			}
		}
	}

	@Override
	public void dispose() throws CaoException {
	}

	@FormSortId(1)
	@FormElement("input title='Folder Type' value='nt:folder'")
	public void setFolderType(String in) {
		folderType = in;
	}

	@FormSortId(2)
	@FormElement("input title='File Type' value='nt:file'")
	public void setFileType(String in) {
		fileType = in;
	}
	
	@FormSortId(3)
	@FormElement("checkbox title='Import Hidden' value='false'")
	public void setHidden(boolean in) {
		hidden = in;
	}

	@FormSortId(4)
	@FormElement("checkbox title='Merge Into' value='true'")
	public void setMerge(boolean in) {
		merge = in;
	}
	
	@FormSortId(5)
	@FormElement("checkbox title='Overwrite File Content' value='true'")
	public void setOverwrite(boolean in) {
		overwrite = in;
	}
	
}
