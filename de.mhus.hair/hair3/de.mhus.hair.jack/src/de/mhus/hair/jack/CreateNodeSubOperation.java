package de.mhus.hair.jack;

import de.mhus.lib.cao.CaoOperation;


public interface CreateNodeSubOperation {

	public CaoOperation create(JackElement element, String name, boolean doSave) throws Exception;
	
}
