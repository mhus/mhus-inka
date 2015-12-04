package de.mhus.hair.sling;

import java.util.LinkedList;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import de.mhus.hair.jack.CreateNodeSubOperation;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.form.objects.IDynOptionsProvider;
import de.mhus.lib.form.objects.SimpleDynOptionsProvider;

@FormElement("name='jack_new_sling_folder' title='Create New Sling Folder'")
public class NewSlingFolder extends CaoOperation implements MForm,CreateNodeSubOperation {

	private JackElement parent;
	private String name;
	private boolean doSave;
	private boolean ordered;
	private String slingResourceType;
	private boolean accessControllable;
	private String slingRedirect;

	@Override
	public CaoOperation create(JackElement parent, String name,boolean doSave) throws Exception {
		
		this.parent = parent;
		this.name   = name;
		this.doSave = doSave;
		
		return this;
	}

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {

		try {
			Node newNode = parent.getNode().addNode(name, ordered ? "sling:OrderedFolder" : "sling:Folder");
	
			if (accessControllable) {
				newNode.addMixin("rep:AccessControllable");
			}
			
			if (!MString.isEmpty(slingResourceType)) {
				newNode.addMixin("sling:Resource");
				newNode.setProperty("sling:resourceType", slingResourceType);
			}
			
			if (!MString.isEmpty(slingRedirect)) {
				newNode.addMixin("sling:Redirect");
				newNode.setProperty("sling:target", slingRedirect);
			}

			Session session = ((JackConnection)parent.getConnection()).getSession();
			if (doSave) session.save();
			if (newNode != null) {
				parent.getConnection().fireElementCreated(newNode.getIdentifier());
				parent.getConnection().fireElementLink(parent.getId(), newNode.getIdentifier());
			}
		} catch (Exception e) {
			throw new CaoException(e);
		}
	}

	@Override
	public void dispose() throws CaoException {
	}

//	@FormSortId(1)
//	@FormElement("label title='Name'")
	public String getName() {
		return name;
	}
	
	@FormSortId(2)
	@FormElement("checkbox title='Ordered' value='1'")
	public void setOrdered(boolean in) {
		ordered = in;
	}
	
	@FormSortId(3)
	@FormElement("select title='Sling Resource Type' value=''")
	public void setSlingResourceType(String in) {
		slingResourceType = in;
	}
	
	@FormSortId(4)
	@FormElement("checkbox title='Access Controllable' value='1'")
	public void setAccessControllable(boolean in) {
		accessControllable = in;
	}
	
	@FormSortId(5)
	@FormElement("input title='Redirect' value=''")
	public void setRedirect(String in) {
		slingRedirect = in;
	}
	
	public IDynOptionsProvider getSlingResourceTypeDataProvider() {
		
		LinkedList<String> names = new LinkedList<String>();
		LinkedList<String> uniq = new LinkedList<String>();
		
		names.add("<none>");
		uniq.add("");
		
		try {
			Session session = ((JackConnection)parent.getConnection()).getSession();
			QueryManager qm = session.getWorkspace().getQueryManager();
			Query query = qm.createQuery("/jcr:root/apps//element(*,sling:Folder)", Query.XPATH);
			QueryResult res = query.execute();
			for (RowIterator iter = res.getRows(); iter.hasNext();) {
				Row row = iter.nextRow();
				Value path = row.getValue("jcr:path");
				
				names.add(path.getString());
				uniq.add(path.getString());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SimpleDynOptionsProvider(uniq.toArray(new String[uniq.size()]),names.toArray(new String[names.size()]));
	}

}
