package de.mhus.hair.jack.action;

import java.io.StringReader;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.cnd.CndImporter;

import de.mhus.hair.jack.JackConnection;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='jack_add_definition' title='Add Definition'")
public class AddDefinitionOperation extends CaoOperation implements MForm {

	private CaoList sources;
	private String definition;

	public void setSource(CaoList list) {
		this.sources = list;
	}

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		
		monitor.beginTask("adddefinition", sources.size());

		for (CaoElement source : sources.getElements()) {
			Session session = ((JackConnection)sources.getConnection()).getSession();
			try {
				
				monitor.log().i(source.getName());
				
				StringReader sr = new StringReader(definition);
				NodeType[] nodeTypes = CndImporter.registerNodeTypes(sr, session);
				for (NodeType nt : nodeTypes) {
				  monitor.log().i(session,"Registered",nt.getName());
				}

				
				session.save();
								
			} catch (Throwable e) {
				monitor.log().i(e);
				try {
					session.refresh(false);
				} catch (RepositoryException e1) {
					monitor.log().i(e);
				}
//				throw new CaoException(e);
			}
			monitor.nextFinished();
		}
		
		sources.getConnection().fireElementStructurChanged(null);

	}

	@Override
	public void dispose() throws CaoException {
	}

	
	@FormSortId(1)
	@FormElement("text title='Definition' value='[mhus:flexible]\nmix\n- *'")
	public void setDefinition(String in) {
		definition = in;
	}
	
}
