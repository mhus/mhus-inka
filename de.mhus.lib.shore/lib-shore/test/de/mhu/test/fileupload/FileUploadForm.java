/*
 *
 * Date: 11:02:01 17.07.2003
 * Author hummel
 * 
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 * 
 * 
 * 
 */
package de.mhu.test.fileupload;

import org.apache.commons.fileupload.FileItem;

import de.mhu.shore.ifc.Form;

/**
 * @author hummel
 * 

 * 
 */
public class FileUploadForm extends Form {

		public String content = null;
		public String note     = null;
		
		public void setUfile( FileItem _fi ) {
			
			content = _fi.getString();
			
		}
		
		public void setNote( String _in ) {
			note = _in;
		}
		
		public String getNote() {
			return note;
		}
		
		
		
}
