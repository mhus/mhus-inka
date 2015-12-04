/*
 *
 * Date: 15:22:44 17.07.2003
 * Author hummel
 * 
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 * 
 * 
 * 
 */
package de.mhu.test.fileupload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhu.shore.ifc.Action;
import de.mhu.shore.ifc.ActionResult;

/**
 * @author hummel
 * 

 * 
 */
public class FileUploadAction extends Action {

	public ActionResult execute( Object _form, HttpServletRequest _request, HttpServletResponse _response ) {
		
		System.out.println( ((FileUploadForm)_form).getNote() );
		System.out.println( ((FileUploadForm)_form).content );
		
		return new ActionResult( ActionResult.ACTION );
		
	}

}
