package de.mhus.cha.cao.action;

import java.util.LinkedList;

import de.mhus.cap.core.Access;
import de.mhus.cap.core.CapCore;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.annotations.FormSortId;

@FormElement("name='cao_dump' title='Dump'")
public class DumpOperation extends CaoOperation implements MForm {

	private CaoList<Access> sources;

	@Override
	public void initialize() throws CaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() throws CaoException {
		
		CaoElement<Access> root = sources.getElements().next();
		
		try {
			monitor.beginTask("dump", -1);
			dump(root,"");
			monitor.worked(1);
		} catch (Throwable e) {
			monitor.log().i(e);
			throw new CaoException(e);
		}
		
	}
	
	private void dump(CaoElement<Access> node, String level) {
		//  node
		if (level.length() > 100*2) {
			System.out.println( "<<BREAK>>");
			return;
		}
		try {
			System.out.println( level + "node \"" + node.getId() + "\" (");
			for (CaoMetaDefinition meta : node.getMetadata()) {
				System.out.println( level + "  attribute \"" + meta.getName() + "\" " + meta.getType() +" (");
				System.out.println( level + "    " + node.getString(meta.getName()));
				System.out.println( level + "  )" );
			}
		} catch (Exception e) {
			monitor.log().i(e);
		}
		try {
			for (CaoElement<Access> child : node.getChildren(CapCore.getInstance().getAccess()).getElements()) {
				dump(child,level + "  ");
			}
		} catch (Exception e) {
			monitor.log().i(e);
		}
		
	}
	
	
	@Override
	public void dispose() throws CaoException {
	}

	public void setSources(CaoList<Access> list) {
		sources = list;
	}
	
}
