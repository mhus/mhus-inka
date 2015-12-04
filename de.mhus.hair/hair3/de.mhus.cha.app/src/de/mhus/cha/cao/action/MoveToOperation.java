package de.mhus.cha.cao.action;

import java.io.File;

import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.cap.core.Access;
import de.mhus.cha.cao.ChaConnection;
import de.mhus.cha.cao.ChaElement;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;

@FormElement("name='cha_move_to_folder' title='Move'")
public class MoveToOperation extends CaoOperation implements MForm {

	private CaoList<Access> sources;
	private ChaElement target;

	public MoveToOperation(CaoList<Access> list, CaoElement<Access> target) {
		this.target = (ChaElement) target;
	}

	@Override
	public void dispose() throws CaoException {
	}

	@Override
	public void execute() throws CaoException {
		monitor.beginTask("move", sources.size());
		int cnt = 0;
		for (CaoElement<Access> element : sources.getElements()) {
			monitor.worked(cnt);
			File from = ((ChaElement)element).getFile();
			//String fromId = element.getId();
			File to   = new File( target.getFile(), ((ChaElement)element).getFile().getName() );
			from.renameTo( to );
			((ChaConnection)element.getConnection()).changeIdPath(element.getId(), to.getAbsolutePath());
			element.getConnection().fireElementUnlink(element.getParent().getId(), element.getId());
			element.getConnection().fireElementLink(target.getId(), element.getId());
			cnt++;
		}
	}

	@Override
	public void initialize() throws CaoException {
	}

	public void setSources(CaoList<Access> list) {
		sources = list;
	}

}
