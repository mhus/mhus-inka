/*
 *
 * Date: 08:26:06 22.07.2003
 * Author hummel
 * 
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 * 
 * 
 * 
 */
package de.mhu.shore.ifc;

import de.mhu.lib.MhuCast;

/**
 * @author hummel
 * 

 * 
 */
public class RemoveForm extends Form {

		private boolean really = false;
		
		public void setReally( String[] _in ) {
			really = ! ( _in == null || _in.length < 1 || ! MhuCast.toboolean( _in[0], false ) );
		}
		
		public String[] getReally() {
			return new String[] { "" + really };
		}
		
		public boolean isReally() {
			return really;
		}
		
}
