package de.mhus.hair.jack.action;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import de.mhus.hair.jack.JackConnection;
import de.mhus.hair.jack.JackElement;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;
import de.mhus.lib.form.objects.IDynOptionsProvider;
import de.mhus.lib.form.objects.SimpleDynOptionsProvider;

@FormElement("name='jack_add_property' title='Add Property'")
public class AddPropertyOperation extends CaoOperation implements MForm {

	private String type;
	private String name;
	private String value;
	private CaoList sources;

	public void setSources(CaoList list) {
		sources = list;
	}

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		
		Session session = ((JackConnection)sources.getConnection()).getSession();
		
		monitor.beginTask("addproperty", sources.size());

		for (CaoElement source : sources.getElements()) {
			try {
				
				monitor.log().i(source.getName());
				
//				boolean isMhus = false;
//				for ( NodeType mix : ((JackElement)source).getNode().getMixinNodeTypes()) {
//					if (mix.getName().equals("mhus:flexible")) {
//						isMhus = true;
//						break;
//					}
//				}
//				if (!isMhus) {
//					monitor.log().i("set mix type");
//					((JackElement)source).getNode().addMixin("mhus:flexible");
//				}
				((JackElement)source).getNode().setProperty(name, value);
				
				session.save();
				
				sources.getConnection().fireElementStructurChanged(source.getId());
				
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
		
	}

	@Override
	public void dispose() throws CaoException {
	}

	@FormSortId(1)
	@FormElement("input title='Name' value='folder'")
	public void setName(String in) {
		name = in;
	}

	@FormSortId(2)
	@FormElement("select title='Type' value='string'")
	public void setType(String in) {
		type = in;
	}

	public IDynOptionsProvider getTypeDataProvider() {
		return new SimpleDynOptionsProvider(
				new String[] {"string"},
				new String[] {"String"}
				);
	}
	
	
	@FormSortId(3)
	@FormElement("input title='Value' value=''")
	public void setValue(String in) {
		value = in;
	}

}
