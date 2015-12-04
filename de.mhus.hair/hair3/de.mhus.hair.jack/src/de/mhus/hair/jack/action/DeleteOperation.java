package de.mhus.hair.jack.action;

import de.mhus.hair.jack.JackElement;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;

@FormElement("name='cao_delete' title='Delete'")
public class DeleteOperation extends CaoOperation implements MForm {

	private CaoList sources;

	@Override
	public void initialize() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		for (CaoElement source : sources.getElements()) {
			try {
				String sourceId = source.getId();
				CaoElement parent = source.getParent();
				((JackElement)source).getNode().remove();
				((JackElement)source).getNode().getSession().save();
				source.getConnection().fireElementUnlink(parent.getId(), sourceId);
				source.getConnection().fireElementDeleted(sourceId);
			} catch (Exception e) {
				monitor.log().e(e);
			}
			
		}
	}

	@Override
	public void dispose() throws CaoException {
	}

	public void setSources(CaoList list) {
		sources = list;
	}

}
