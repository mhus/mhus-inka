/*
 *  mhu-shore JSP creation Framework
 *    shore help you to create JSP files using MVC (Model-View-Controll) design.
 *    mhu-shore is a ant task and generate JSP files from - nearly - simple
 *    HTML files. A special Servlet or active server component is not needed.
 * 
 *  Copyright (C) 2003  Mike Hummel
 *  
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *  
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  WWW: http://code.mikehummel.de/
 *  E-mail: code@mikehummel.de
 */

package de.mhu.shore.plugins;

// import java.util.Enumeration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuCast;
import de.mhu.shore.*;
import de.mhu.shore.ifc.ActionIfc;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShoreAction implements Plugin {

	private static final String ACTION_CLASS    = "action.class";
	private static final String FORM_CLASS      = "form.class";
	private static final String INPUT_REFERENCE = "action";
	private static final String ERROR_MSG_TAG   = "errormsg";
	private static final String ERROR_TAG   = "action.error";
	private static final String ERROR_ELSE_TAG   = "action.error.else";
	private static final String ERROR_TAG_END   = "/action.error";
	private static final String OPTIONS_IMPORT  = "options.import";
	private static final String VALUE_STATIC    = "value.static";
	private static final String IGNORE    = "ignore";
	private static final String FORM_ID         = "form.id";
	private static final String FORM_RELOAD     = "form.reload";
	
	private static final String HTML_NAME    = "name";
	private static final String HTML_FORM    = "form";
	private static final String HTML_FORM_END= "/form";
	private static final String HTML_INPUT   = "input";
	private static final String HTML_ACTION  = "action";
	private static final String HTML_VALUE   = "value";
	private static final String HTML_SELECT  = "select";
	private static final String HTML_SELECT_END = "/select";
	private static final String HTML_OPTION  = "option";
	private static final String HTML_TYPE    = "type";
	private static final String HTML_ENCTYPE    = "enctype";
	
	private static final String HTML_ENCTYPE_UPLOAD    = "multipart/form-data";
	
	private static final String HTML_TYPE_TEXT     = "text";
	private static final String HTML_TYPE_SUBMIT   = "submit";
	private static final String HTML_TYPE_IMAGE    = "image";
	private static final String HTML_TYPE_RESET    = "reset";
	private static final String HTML_TYPE_RADIO    = "radio";
	private static final String HTML_TYPE_CHECKBOX = "checkbox";
	private static final String HTML_TYPE_HIDDEN   = "hidden";
	private static final String HTML_TYPE_FILE   = "file";
	
	private static final String HTML_CHECKED  = "checked";
	private static final String HTML_SELECTED = "selected";
	private static final String HTML_MULTIPLE = "multiple";
	private static final String HTML_TEXTAREA = "textarea";
	
	private static final String PARAM_VERBOSE = "verbose";
	private static final String SHORE_PARAM_VERBOSE = "action.verbose";
	
	private static final String SHORE_PARAM_UPLOAD_TMP = "upload.tmp";
	private static final String SHORE_PARAM_UPLOAD_CACHE = "upload.cache";
	private static final String SHORE_PARAM_UPLOAD_MAX = "upload.max";
	
	
	private static final Class  CLASS_STRING = java.lang.String.class;
	private static final Class  ARRAY_STRING = java.lang.String[].class;
	
	private Hashtable parms = null; 

	private Hashtable forms = new Hashtable();

	/**
	 * Execute Plugin for one jsp page.
	 * @param _helper  
	 * @see de.mhu.shore.Plugin#execute(java.lang.String, java.util.Hashtable)
	 */
	public void execute( PluginHelper _helper ) throws Exception {
		
		// iterate tags in page
		_helper.resetTag();
		while ( _helper.nextTag() ) {
			
			String name = _helper.getTagName();
			
			// check error msg tag, print error msg
			if ( name.equals( ERROR_MSG_TAG ) ) {
				setErrorMsg( _helper );
			} else
			if ( name.equals( ERROR_TAG )) {
				parseError( _helper );
			} else
			// check form tag, parse form
			if ( name.equals( HTML_FORM ) ) {
				parseForm( _helper );
			}
			
		}
		
	}
	
	/**
	 * Replace errormsg-tag with code to print msg out
	 * 
	 * @param _helper
	 */
	private void setErrorMsg(PluginHelper _helper) {

		// execute template and replace tag
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "action/FormPrintError.tpl" ) );
			// Insert container parameter
			_helper.replaceTag( temp.toString().trim() );
					
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
	}

	private void parseError(PluginHelper _helper) {
		// execute template and replace tag
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "action/ErrorHeader.tpl" ) );
			// Insert container parameter
			_helper.replaceTag( temp.toString().trim() );
					
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		while ( _helper.nextTag() ) {
			
			String name = _helper.getTagName();
			
			// check error msg tag, print error msg
			if ( name.equals( ERROR_MSG_TAG ) ) {
				setErrorMsg( _helper );
			} else
			if ( name.equals( ERROR_ELSE_TAG )) {

				// execute template and replace tag
				try {
					// get Template
					Template temp = new Template( _helper.getTemplatePath( "action/ErrorElse.tpl" ) );
					// Insert container parameter
					_helper.replaceTag( temp.toString().trim() );
					
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			
			} else
			if ( name.equals( ERROR_TAG_END )) {
				break;				
			}
			
		}

		// execute template and replace tag
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "action/ErrorFooter.tpl" ) );
			// Insert container parameter
			_helper.replaceTag( temp.toString().trim() );
					
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
	}

	private void parseForm( PluginHelper _helper ) {
		
		// get parameters from form tag
		String actionClass = (String)_helper.getTagParameter().get( ACTION_CLASS );
		String formClass   = (String)_helper.getTagParameter().get( FORM_CLASS   );
		String formName    = (String)_helper.getTagParameter().get( HTML_NAME    );
		String action      = (String)_helper.getTagParameter().get( HTML_ACTION  );
		String formId      = (String)_helper.getTagParameter().get( FORM_ID      );
		boolean formReload = MhuCast.toboolean( (String)_helper.getTagParameter().get( FORM_RELOAD ), false );
		String enctype = (String)_helper.getTagParameter().get( HTML_ENCTYPE );
		boolean uploadType = false;
		if ( enctype != null && enctype.toLowerCase().equals( HTML_ENCTYPE_UPLOAD ) )
			uploadType = true;
				
		FormContainer container = null;
		
		// check and change form tag
		
		if ( actionClass != null ) {

			// no action ?
			if ( action == null ) {
				action = _helper.getSourceName();
				System.out.println( "  " + getClass() + ": form without action, use: " + action );
			} 
 				
			// no name ?
			if ( formName == null ) {
				formName = "shoreForm" + _helper.getUniqueId();
				System.out.println( "  " + getClass() + ": form without name, create name: " + formName );
				_helper.getTagParameter().put( HTML_NAME, formName );
			}
			// no form class ?
			if ( formClass == null) {
				formClass = _helper.getShoreParameter( "action.default.form.class" );
				if ( formClass == null ) {
					System.err.println( "  " + getClass() + ": form without form.class" );
					return;
				} else {
					System.out.println( "  " + getClass() + ": form without form.class: use " + formClass );
				}
								
			}
			
			// all right, init form
			container = initForm( _helper, actionClass, formClass, formName, action );
			container.uploadType = uploadType;
			_helper.replaceTag();
			
			// Insert form instanciate.jsp
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "action/FormInstanciate.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "action"     , container.action );
				ShoreUtil.addTemplateParameter( temp, "actionClass", container.actionClass );
				ShoreUtil.addTemplateParameter( temp, "formClass"  , container.formClass );
				ShoreUtil.addTemplateParameter( temp, "fileName"   , container.fileName );
				ShoreUtil.addTemplateParameter( temp, "name"       , container.name );
				ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
				ShoreUtil.addTemplateParameter( temp, "id"         , formId );
				ShoreUtil.addTemplateParameter( temp, "formReload" , ""+formReload );
				ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
				ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
				_helper.insertAfterTag( temp.toString().trim() );
				
			} catch ( Exception e ) {
				e.printStackTrace();
				return;
			}
							
		
		}
		
		// error? ... return
		if ( container == null )
			return;
		
		// find all inputs, selects ....	
		while ( _helper.nextTag() ) {
			
			String name = _helper.getTagName();
			
			String ignore = (String)_helper.getTagParameter().get( IGNORE );
			if ( ignore == null || ! MhuCast.toboolean( ignore, false ) ) {
			
				// check form tags (form in form?)
				if ( name.equals( HTML_FORM ) ) {
					parseForm( _helper );
				}
				else
				// check input tags
				if ( name.equals( HTML_INPUT ) ) {
					parseInput( _helper, container );
				}
				else
				// check select tags
				if ( name.equals( HTML_SELECT ) ) {
					parseSelect( _helper, container );
				}
				else
				if ( name.equals( HTML_TEXTAREA ) ) {
					parseTextarea( _helper, container );
				}
				else
				// form ends? ... return
				if ( name.equals( HTML_FORM_END ) ) {
					finishForm( _helper, container );
					return;
				}
				
			}	
				
		}
		
		// error, no </form> found
		System.err.println( "  " + getClass() + ": form without end-tag: ignore" );
		finishForm( _helper, container );
	}
	
	/**
	 * @param _helper
	 * @param action
	 * @param form
	 * @param formName
	 */
	private FormContainer initForm(PluginHelper _helper, String actionClass, String formClass, String formName, String action ) {
		
		// remove internal parameter 
		_helper.getTagParameter().remove( ACTION_CLASS );
		_helper.getTagParameter().remove( FORM_CLASS   );
		_helper.getTagParameter().remove( FORM_ID      );
		_helper.getTagParameter().remove( FORM_RELOAD  );
		
		// set new action
		String newFile = _helper.newFileName();
		_helper.getTagParameter().put( HTML_ACTION, newFile );
		
		// remember this form
		FormContainer container = new FormContainer();
		container.name        = formName;
		container.action      = action;
		container.actionClass = actionClass;
		container.formClass   = formClass;
		container.fileName    = newFile;
		
		return container;
	}

	/**
	 * @param _helper
	 * @param container
	 */
	private void finishForm(PluginHelper _helper, FormContainer container) {

		// verbose
		boolean verbose = MhuCast.toboolean( _helper.getParameter( PARAM_VERBOSE ),
						  MhuCast.toboolean( _helper.getShoreParameter( SHORE_PARAM_VERBOSE ), true ) ); 
		if ( verbose ) {
			try {
				System.out.println( "  Form Verbose check: " + _helper.getSourceName() + ":" + container.name );

				// check formClass
				Object form = Class.forName( container.formClass ).newInstance();
				
				// check fileUpload setter
				for ( Enumeration e = container.fileUploads.keys(); e.hasMoreElements(); ) {
					String name = (String)e.nextElement();
					InputContainer ic = (InputContainer)container.fileUploads.get( name );
					try {
						form.getClass().getMethod( "set" + ShoreUtil.toParameterName( name ), new Class[] { org.apache.commons.fileupload.FileItem.class } );
					} catch ( Exception e2 ) {
						System.err.println( "    Form Verbose: " + e2 );
					}
				}
				
				// check input getter/setter
				for ( Enumeration e = container.inputs.keys(); e.hasMoreElements(); ) {
					String name = (String)e.nextElement();
					InputContainer ic = (InputContainer)container.inputs.get( name );
					try {
						Method m = form.getClass().getMethod( "get" + ShoreUtil.toParameterName( name ), new Class[0] );
						if ( m.getReturnType() != ic.type )
							System.err.println( "    Form Verbose: wrong type in: " + m.getName() + " is " + m.getReturnType() + " should be " + ic.type );
					} catch ( Exception e2 ) {
						System.err.println( "    Form Verbose: " + e2 );
					}
					
					try {
						form.getClass().getMethod( "set" + ShoreUtil.toParameterName( name ), new Class[] { ic.type } );
					} catch ( Exception e2 ) {
						System.err.println( "    Form Verbose: " + e2 );
					}
					
					if ( ic.needOptionsFunction ) { 
						try {
							Method m = form.getClass().getMethod( "get" + ShoreUtil.toParameterName( name ) + "Options", new Class[0] );
							if ( m.getReturnType() != String[].class )
								System.err.println( "    Form Verbose: wrong type in: " + m.getName() + " is " + m.getReturnType() + " should be String[]"  );
						} catch ( Exception e2 ) {
							System.err.println( "    Form Verbose: " + e2 );
						}
					}
					
					// check actionClass
					Object action = Class.forName( container.actionClass ).newInstance();
					if ( ! ( action instanceof ActionIfc ) ) {
						System.err.println( "    Form Verbose: Action don't implements " + ActionIfc.class.getName() );	
					}
					
				}
				
			} catch ( Exception e ) {
				System.err.println( "    Form Verbose: " + e );			
			}
		}
		
		// generate pages
		try {
			
			// get Template
			Template temp = null;
			if ( container.uploadType )
				temp = new Template( _helper.getTemplatePath( "action/ActionRequestUpload.tpl" ) );
			else
				temp = new Template( _helper.getTemplatePath( "action/ActionRequest.tpl" ) );
			
			// Insert messages
			File messagesFile = null;
			// set default name
			if ( _helper.getParameter( "message.properties" ) == null )
				messagesFile = new File( _helper.getSourceDir(), _helper.getSourceName() + ".properties" );
			else
				messagesFile = new File( _helper.getSourceDir(), _helper.getParameter( "message.properties" ) ); // relative to jsp
			Properties messagesProps = new Properties();
			// load messages
			try {
				InputStream is = new FileInputStream( messagesFile );
				messagesProps.load( is );
			} catch ( IOException ioe ) {}
			
			for ( Enumeration e = messagesProps.keys(); e.hasMoreElements(); ) {
				String key = (String)e.nextElement();
				Hashtable messages = new Hashtable();
				ShoreUtil.addTemplateParameter( messages, "key", key );
				ShoreUtil.addTemplateParameter( messages, "value", messagesProps.getProperty( key ) );
				temp.loopAppend( "messages", messages );
			}

			// Insert file-upload names

			for ( Enumeration e = container.fileUploads.keys(); e.hasMoreElements(); ) {
				String name = (String)e.nextElement();
				InputContainer ic = (InputContainer)container.fileUploads.get( name );
				Hashtable parameters = new Hashtable();
				ShoreUtil.addTemplateParameter( parameters, "setter", "set" + ShoreUtil.toParameterName( name ) );
				ShoreUtil.addTemplateParameter( parameters, "getter", "get" + ShoreUtil.toParameterName( name ) );
				ShoreUtil.addTemplateParameter( parameters, "name", name );
				temp.loopAppend( "uploads", parameters );
			}
			
			// ShoreUtil.addTemplateParameter( temp, "uploadsEnabled", container.fileUploads.size() == 0 ? null : "\"\"" );
			// Upload for enctype multipart .... def. POST dont works with this enctype 
			ShoreUtil.addTemplateParameter( temp, "uploadsEnabled", container.uploadType ? "\"\"" : null );
			ShoreUtil.addTemplateParameter( temp, "uploadsTmp", _helper.getShoreParameter( SHORE_PARAM_UPLOAD_TMP ) );
			ShoreUtil.addTemplateParameter( temp, "uploadsCache", _helper.getShoreParameter( SHORE_PARAM_UPLOAD_CACHE ));
			ShoreUtil.addTemplateParameter( temp, "uploadsMax", _helper.getShoreParameter( SHORE_PARAM_UPLOAD_MAX ));
			
			
			// Insert parameter names
			
			for ( Enumeration e = container.inputs.keys(); e.hasMoreElements(); ) {
				String name = (String)e.nextElement();
				InputContainer ic = (InputContainer)container.inputs.get( name );
				Hashtable parameters = new Hashtable();
				ShoreUtil.addTemplateParameter( parameters, "setter", "set" + ShoreUtil.toParameterName( name ) );
				ShoreUtil.addTemplateParameter( parameters, "getter", "get" + ShoreUtil.toParameterName( name ) );
				ShoreUtil.addTemplateParameter( parameters, "name", name );
				ShoreUtil.addTemplateParameter( parameters, "type", ic.type.getName() );
				if ( ic.type == CLASS_STRING )
					ShoreUtil.addTemplateParameter( parameters, "requestFunction", "getParameter" );
				else
					ShoreUtil.addTemplateParameter( parameters, "requestFunction", "getParameterValues" );
				temp.loopAppend( "parameters", parameters );
			}
			
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "action"     , container.action );
			ShoreUtil.addTemplateParameter( temp, "actionClass", container.actionClass );
			ShoreUtil.addTemplateParameter( temp, "formClass"  , container.formClass );
			ShoreUtil.addTemplateParameter( temp, "fileName"   , container.fileName );
			ShoreUtil.addTemplateParameter( temp, "name"       , container.name );
			ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
			ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
			
			// create
			_helper.newFile( container.fileName, temp.toString() );
			
			
		} catch (Exception e) {
			System.err.println( getClass() + ".finishForm ["+ _helper.getSourceName() +"]: " + e );
		}

	}
	
	/**
	 * @param _helper
	 * @param ref
	 */
	private void parseInput(PluginHelper _helper, FormContainer _container ) {

		String ref  = (String)_helper.getTagParameter().get( HTML_NAME  );
		String type = (String)_helper.getTagParameter().get( HTML_TYPE  );
		if ( type == null )
			type = HTML_TYPE_TEXT;
		type = type.toLowerCase();
		boolean valueStatic = MhuCast.toboolean( (String)_helper.getTagParameter().get( VALUE_STATIC ), false );

		// remove internal parameters
		_helper.getTagParameter().remove( VALUE_STATIC );
		
		// remember
		if ( ref != null && ! ( type.equals( HTML_TYPE_SUBMIT ) ||
								type.equals( HTML_TYPE_IMAGE ) ||
								type.equals( HTML_TYPE_RESET ) ) ) {
			
			
			InputContainer ic = new InputContainer();
			ic.parameter = _helper.getTagParameter();
			
			if ( type.equals( HTML_TYPE_FILE ) ) {
				
				_container.fileUploads.put( ref, ic );
				
			} else {
				
				_container.inputs.put( ref,  ic );

				if ( ! valueStatic ) {
					if ( type.equals( HTML_TYPE_TEXT ) || type.equals( HTML_TYPE_HIDDEN ) ) {	
					
						// Insert form Set Text Value
						try {
							// get Template
							Template temp = new Template( _helper.getTemplatePath( "action/FormTextValue.tpl" ) );
							// Insert container parameter
							ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
							ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
							ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
							ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
							ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
							ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
							ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
							ShoreUtil.addTemplateParameter( temp, "value"      , (String)_helper.getTagParameter().get( HTML_VALUE ) );
							ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
							ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
							_helper.getTagParameter().put( HTML_VALUE, temp.toString().trim() );
								
						} catch ( Exception e ) {
							e.printStackTrace();
						}
											
					} else
					if ( type.equals( HTML_TYPE_CHECKBOX )) {
						
						// remove checked (if set)
						_helper.getTagParameter().remove( HTML_CHECKED );
						
						// Insert form Set Checkbox checked
						try {
							// get Template
							Template temp = new Template( _helper.getTemplatePath( "action/FormCheckboxChecked.tpl" ) );
							// Insert container parameter
							ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
							ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
							ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
							ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
							ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
							ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
							ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
							ShoreUtil.addTemplateParameter( temp, "value"      , (String)_helper.getTagParameter().get( HTML_VALUE ));
							ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
							ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
							_helper.getTagParameter().put( temp.toString().trim(), HTML_CHECKED );
							
							ic.type = ARRAY_STRING;
								
						} catch ( Exception e ) {
							e.printStackTrace();
						}
						
					} else
					if ( type.equals( HTML_TYPE_RADIO )) {
						
						// remove checked (if set)
						_helper.getTagParameter().remove( HTML_CHECKED );
						
						// Insert form Set Checkbox checked
						try {
							// get Template
							Template temp = new Template( _helper.getTemplatePath( "action/FormRadioChecked.tpl" ) );
							// Insert container parameter
							ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
							ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
							ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
							ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
							ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
							ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
							ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
							ShoreUtil.addTemplateParameter( temp, "value"      , (String)_helper.getTagParameter().get( HTML_VALUE ));
							ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
							ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
							_helper.getTagParameter().put( temp.toString().trim(), HTML_CHECKED );
							
						} catch ( Exception e ) {
							e.printStackTrace();
						}
						
					}
					
				} // endif: valueStatic

			} // endif: HTML_TYPE_FILE
						
			_helper.replaceTag();
			
		}
		
		
	}	
	
	/**
	 * @param _helper
	 * @param container
	 */
	private void parseSelect(PluginHelper _helper, FormContainer _container) {
		
		String  ref           = (String)_helper.getTagParameter().get( HTML_NAME );
		boolean importOptions = MhuCast.toboolean( (String)_helper.getTagParameter().get( OPTIONS_IMPORT ), false );
		String multiple       = (String)_helper.getTagParameter().get( HTML_MULTIPLE );
		
		if ( ref == null ) {
			System.err.println( "  " + getClass() + ".parseSelect: SELECT without name" );
			return;
		}
		
		InputContainer ic = new InputContainer();
		ic.parameter = _helper.getTagParameter();
		if ( multiple != null ) ic.type = ARRAY_STRING;
		_container.inputs.put( ref, ic );

		if ( importOptions ) {
			
			// for verbose mode
			ic.needOptionsFunction = true;
			
			// Insert form Import Options
			try {
				// get Template
				String tempName = null;
				if ( multiple != null )
					tempName = "action/FormOptionsMultiInsert.tpl";
				else
					tempName = "action/FormOptionsInsert.tpl";
				Template temp = new Template( _helper.getTemplatePath( tempName ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
				ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
				ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
				ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
				ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
				ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
				ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
				ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
				ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
				_helper.insertAfterTag( temp.toString() );
					
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		
		
		} else {
			
			while ( _helper.nextTag() ) {
				
				String name = _helper.getTagName();
				if ( name.equals( HTML_OPTION ) ) {
					
					// Insert form Set Option Selected
					try {
						// get Template
						String tempName = null;
						if ( multiple != null )
							tempName = "action/FormOptionMultiSelected.tpl";
						else
							tempName = "action/FormOptionSelected.tpl";
						Template temp = new Template( _helper.getTemplatePath( tempName ) );
						// Insert container parameter
						ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
						ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
						ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
						ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
						ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
						ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
						ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
						ShoreUtil.addTemplateParameter( temp, "value"      , (String)_helper.getTagParameter().get( HTML_VALUE ));
						ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
						ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
						_helper.getTagParameter().put( temp.toString().trim(), HTML_SELECTED );
					
					} catch ( Exception e ) {
						e.printStackTrace();
					}
					_helper.replaceTag();
					
				} else
				if ( name.equals( HTML_SELECT_END )) {
					return;
				}
			}
		}		
		
	}
	

	/**
	 * @param _helper
	 * @param container
	 */
	private void parseTextarea(PluginHelper _helper, FormContainer _container) {

		String  ref = (String)_helper.getTagParameter().get( HTML_NAME );
		if ( ref == null ) {
			System.err.println( "  " + getClass() + ".parseTextarea: TEXTAREA without name" );
			return;
		}
		boolean valueStatic = MhuCast.toboolean( (String)_helper.getTagParameter().get( VALUE_STATIC ), false );

		// remove internal parameters
		_helper.getTagParameter().remove( VALUE_STATIC );
		
		InputContainer ic = new InputContainer();
		ic.parameter = _helper.getTagParameter();
		_container.inputs.put( ref, ic );
		
		if ( ! valueStatic ) {		
			// Insert form Set Option Selected
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "action/FormTextareaValue.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "action"     , _container.action );
				ShoreUtil.addTemplateParameter( temp, "actionClass", _container.actionClass );
				ShoreUtil.addTemplateParameter( temp, "formClass"  , _container.formClass );
				ShoreUtil.addTemplateParameter( temp, "fileName"   , _container.fileName );
				ShoreUtil.addTemplateParameter( temp, "name"       , _container.name );
				ShoreUtil.addTemplateParameter( temp, "rootFile"   , _helper.getSourceName() );
				ShoreUtil.addTemplateParameter( temp, "getter"     , "get" + ShoreUtil.toParameterName( ref ) );
				ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
				ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
				_helper.insertAfterTag( temp.toString().trim() );
						
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}		
	}
	
	class FormContainer {
		
		public boolean uploadType;
		String name        = null;
		String action      = null;
		String actionClass = null;
		String formClass   = null;
		String fileName    = null;
		Hashtable inputs   = new Hashtable();
		Hashtable fileUploads = new Hashtable();
		
	}
	
	class InputContainer {
		Hashtable parameter = null;
		Class     type = CLASS_STRING;
		boolean   needOptionsFunction = false;
	}
	
}
