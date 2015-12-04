package de.mhus.hair.jack.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import de.mhus.hair.jack.CreateNodeSubOperation;
import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoApplication;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.form.objects.IDynOptionsProvider;
import de.mhus.lib.form.objects.SimpleDynOptionsProvider;

@FormElement("name='jack_new_node' title='Create New Node'")
public class NewNodeOperation extends CaoOperation implements MForm {

	private static final String NT_FILE = "NT: FILE";
	private static final String NT_FOLDER = "NT: FOLDER";
	private static final String SLING_FOLDER = "SLING: FOLDER";

	private static final String SERVICE_CREATE_NODE = "de.mhus.hair.jack.createnode";
	
	private CaoList sources;
	private String primaryType = NT_FILE;
	private String name;
	private boolean useTransaction;
	private HashMap<String,IConfigurationElement> services = new HashMap<String,IConfigurationElement>();

	public NewNodeOperation(CaoList list) {
		setSources(list);
	}

	@Override
	public void initialize() throws CaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() throws CaoException {
		
		if (primaryType.equals("---")) return;
		
		Session session = ((JackConnection)sources.getConnection()).getSession();
		
		JackElement parent = (JackElement) sources.getElements().next();
		
		try {
			monitor.beginTask("create", 1);
			monitor.log().d("create",name,primaryType);
			
			IConfigurationElement config = services.get(primaryType);
			if (config != null) {
				CreateNodeSubOperation subOperation = (CreateNodeSubOperation)config.createExecutableExtension("class");
				nextOperation = subOperation.create(parent, name, !useTransaction);
				
			} else {
				Node newNode = parent.getNode().addNode(name,primaryType);
				if (!useTransaction) session.save();
				if (newNode != null) {
					sources.getConnection().fireElementCreated(newNode.getIdentifier());
					sources.getConnection().fireElementLink(parent.getId(), newNode.getIdentifier());
				}
			}
			monitor.worked(1);
		} catch (Throwable e) {
			monitor.log().i(e);
			try {
				session.refresh(false);
			} catch (RepositoryException e1) {
				monitor.log().i(e);
			}
			throw new CaoException(e);
		}
		
	}

	@Override
	public void dispose() throws CaoException {
	}

	public void setSources(CaoList list) {
		sources = list;
	}
	
	@FormSortId(1)
	@FormElement("input title='Name' value='folder'")
	public void setName(String in) {
		name = in;
	}
	
	public String getName() {
		return name;
	}
	
	@FormSortId(2)
	@FormElement("select title='Primary Type' value='sling:OrderedFolder'")
	public void setPrimaryType(String in) {
		primaryType = in;
	}
		
	public IDynOptionsProvider getPrimaryTypeDataProvider() {
		
		try {
			LinkedList<String> names = new LinkedList<String>();
			LinkedList<String> uniq = new LinkedList<String>();
			
			JackElement first = (JackElement) sources.getElements().next();
			CaoApplication app = first.getApplication();

			services.clear();
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(SERVICE_CREATE_NODE);
			for (IConfigurationElement e : config) {
				String title = e.getAttribute("title");
				String appClass = e.getAttribute("application_class");
				boolean ok = true;
				if (!MString.isEmptyTrim(appClass)) {
					ok = false;
//					if (!ok) {
						for (Class<?> c = app.getClass(); c != null; c = c.getSuperclass())
							if (c.getName().equals(appClass)) {
								ok = true;
								break;
							}

//					}
					
					if (!ok) {
						for (Class<?> i : app.getClass().getInterfaces()) {
							if (i.getName().equals(appClass)) {
								ok = true;
								break;
							}
						}
					}
					
				}
				
				if (ok) {
					//String clazz = e.getAttribute("class");
					String id = UUID.randomUUID().toString();
					//factory.registerDriver(id,title,new Activator(e));
					names.add(title);
					uniq.add(id);
					services.put(id, e);
				}
			}
			
			names.add("---");
			uniq.add("---");
			
			for  ( NodeTypeIterator typeIter = first.getNode().getSession().getWorkspace().getNodeTypeManager().getPrimaryNodeTypes();typeIter.hasNext(); ) {
				NodeType type = typeIter.nextNodeType();
				names.add(type.getName());
				uniq.add(type.getName());
			}
			return new SimpleDynOptionsProvider(uniq.toArray(new String[uniq.size()]),names.toArray(new String[names.size()]));
		} catch (Throwable e) {}
		
		return new SimpleDynOptionsProvider(new String[] {}, new String[] {});
	}

	public String getPrimaryType() {
		return primaryType;
	}
	
	@FormSortId(3)
	@FormElement("checkbox title='Use Transaction' value='0'")
	public void setTransaction(boolean in) {
		useTransaction = in;
	}
	
	public boolean isTransaction() {
		return useTransaction;
	}
	
}
