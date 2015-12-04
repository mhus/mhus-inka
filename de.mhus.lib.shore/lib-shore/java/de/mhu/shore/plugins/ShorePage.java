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

import java.io.File;
import java.util.Vector;

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuCast;
import de.mhu.shore.Plugin;
import de.mhu.shore.PluginHelper;
import de.mhu.shore.ReplaceListener;
import de.mhu.shore.ShoreUtil;
import de.mhu.shore.ifc.PageIfc;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShorePage implements Plugin, ReplaceListener {
	
	public static final String MODULE_MAIN_VARIABLE = "ShorePage.main.variable";
	public static final String MODULE_MAIN_CLASS    = "ShorePage.main.class";
	
	private static final String PAGE_DEFAULT_CLASS  = "page.default.class";
	
	private static final String LOGIC_TYPE    = "type";
	private static final String LOGIC_TYPE_ITERATOR  = "iterator";
	private static final String LOGIC_TYPE_EMPTY     = "empty";
	private static final String LOGIC_TYPE_NOT_EMPTY = "notempty";
	private static final String LOGIC_TYPE_IS        = "is";
	private static final String LOGIC_TYPE_IS_NOT    = "isnot";
	private static final String LOGIC_TYPE_HAS_MORE  = "hasmore";
	private static final String LOGIC_TYPE_LIST      = "list";
	private static final String LOGIC_TYPE_PAGE    = "page";
	private static final String LOGIC_TYPE_USE    = "use";
	
	private static final String LOGIC_NAME       = "name";
	private static final String LOGIC_MAX        = "max";
	private static final String LOGIC_PAGE_ITEMS = "page.items";
	private static final String LOGIC_CLASS = "class";

	private static final String LOGIC_GO_BACK = "go.back";
	private static final String LOGIC_GO_NEXT = "go.next";
	private static final String LOGIC_NO_BACK = "no.back";
	private static final String LOGIC_NO_NEXT = "no.next";
	
	private static final String LOGIC_INDEX_STYLE = "index.style";
	
	private static final String LOGIC_TAG     = "logic";
	private static final String LOGIC_TAG_END = "/logic";
	private static final String LOGIC_TAG_ELSE = "logic.else";

	private static final String LOGIC_ITERATE_TAG     = "iterate";
	private static final String LOGIC_ITERATE_TAG_END = "/iterate";

	
	private static final String HTML_HTML     = "html";
	private static final String HTML_HTML_END = "/html";

	private String  pageClass  = null;
	private boolean verbose    = true;
	private PageIfc pageObject = null;
	
	private Vector  maps       = new Vector();
	
	private String  mainLabel  = null;
	private PluginHelper helper = null;
	
	private boolean debug = true;
	
	/**
	 * 
	 */
	public ShorePage() {
	}

	private String newLabel() {
		return "shoreVariable" + helper.getUniqueId();
	}

	/* (non-Javadoc)
	 * @see de.mhu.shore.Plugin#execute(de.mhu.shore.PluginHelper)
	 */
	public void execute(PluginHelper _helper) throws Exception {

		helper = _helper;
		
		// check parameter
		pageClass = _helper.getParameter( "class" );
		if ( pageClass == null ) pageClass = _helper.getShoreParameter( PAGE_DEFAULT_CLASS );
		if ( pageClass == null ) throw new Exception( "class not set" );
		
		// debug mode
		debug   = MhuCast.toboolean( _helper.getParameter( "debug" ),
				  MhuCast.toboolean( _helper.getShoreParameter( "page.debug"), false ) ); 
		
		// verbose mode
		verbose = MhuCast.toboolean( _helper.getParameter( "verbose" ),
				  MhuCast.toboolean( _helper.getShoreParameter( "page.verbose"), true ) ); 
		
		if ( verbose ) {
			try {
				Object obj = Class.forName( pageClass ).newInstance();
				pageObject = (PageIfc)obj;
			} catch ( Exception e ) {
				System.err.println( "  Page Verbose: " + e );
				verbose = false; // deaktivate, dont show more errors....
			}
		}
		
		mainLabel = newLabel();
		_helper.setModuleInfo( MODULE_MAIN_VARIABLE, mainLabel );
		_helper.setModuleInfo( MODULE_MAIN_CLASS, pageClass );
		
		// add first map
		maps.add( mainLabel );
		
		// parse html
		_helper.resetTag();
		
		// search for <html> tag
		if ( ! parseUntil( _helper, HTML_HTML ) ) throw new Exception( "<html> tag not found" );

		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/PageHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			_helper.insertAfterTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
		parseUntil( _helper, HTML_HTML_END );

		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/PageFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			_helper.insertAfterTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}
	
	private boolean parseUntil( PluginHelper _helper, String _tag ) throws Exception {
		return ( parseUntil( _helper, new String[] { _tag } ) == 0 );
	}
			
    private int parseUntil( PluginHelper _helper, String[] _tags ) throws Exception {
    	
		if ( debug ) if ( _tags != null ) for ( int i = 0; i < _tags.length; i++ ) System.err.println( "  until: " + _tags[i] );
		
		int start = _helper.getTagCursor();

		while( _helper.nextTag() ) {
			
			String name = _helper.getTagName();
			for ( int i = 0; i< _tags.length; i++ ) {
				
				if ( name.equals( _tags[i] ) ) {
					int stop = _helper.getTagCursor();
					_helper.replaceValues( start, stop, this );
					if ( debug ) System.err.println( "  until end: " + _tags[i] );			
					return i;
				}				
			}
			
			if ( name.equals( LOGIC_TAG ) ) {
				parseLogic( _helper );
			}			
						
		}
				
		return -1;
	}

	/**
	 * @param _helper
	 */
	private void parseLogic(PluginHelper _helper) throws Exception {
		
		String type = (String)_helper.getTagParameter().get( LOGIC_TYPE );
		if ( type == null ) throw new Exception( "<logic> has no type" );
		
		type = type.toLowerCase();
		
		if ( type.equals( LOGIC_TYPE_ITERATOR ) )
			parseLogicIterator( _helper );
		else	
		if ( type.equals( LOGIC_TYPE_EMPTY ) )
			parseLogicEmpty( _helper );
		else
		if ( type.equals( LOGIC_TYPE_NOT_EMPTY ) )
			parseLogicNotEmpty( _helper ); 
		else
		if ( type.equals( LOGIC_TYPE_IS ) )
			parseLogicNotEmpty( _helper );
		else
		if ( type.equals( LOGIC_TYPE_IS_NOT ) )
			parseLogicEmpty( _helper ); 
		else
		if ( type.equals( LOGIC_TYPE_HAS_MORE ) )
			parseLogicHasMore( _helper ); 
		else
		if ( type.equals( LOGIC_TYPE_LIST ) )
			parseLogicList( _helper ); 
		else
		if ( type.equals( LOGIC_TYPE_PAGE ) )
			parseLogicPage( _helper ); 
		else
		if ( type.equals( LOGIC_TYPE_USE ) )
			parseLogicUse( _helper ); 
			
	}

	/**
	 * @param _helper
	 */
	private void parseLogicNotEmpty(PluginHelper _helper) throws Exception {
		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		name = _helper.replaceValues( name, 0, -1, this );
		if ( name == null ) throw new Exception( "NotEmpty: name not set" );
		
		String   label = newLabel();
		String[] value = findLabel( name ); 
		
		// verbose
		/*
		if ( verbose ) {
			try {
				Class.forName( pageClass ).getMethod( "get" + ShoreUtil.toParameterName( name ), new Class[0] );
			} catch ( Exception e ) {
				System.err.println( "Page Verbose: NotEmpty " + e );
			}
		}
		*/
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IsNotEmptyHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label    );
			
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		if ( parseUntil( _helper, new String [] { LOGIC_TAG_END, LOGIC_TAG_ELSE } ) == 1 ) {
			// set footer template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/IsNotEmptyElse.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
				ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
				ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
				ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
				ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			parseUntil( _helper, LOGIC_TAG_END );
			
		}
		
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IsNotEmptyFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

	/**
	 * @param _helper
	 */
	private void parseLogicEmpty(PluginHelper _helper) throws Exception {
		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		name = _helper.replaceValues( name, 0, -1, this );
		if ( name == null ) throw new Exception( "Empty: name not set" );
		
		String   label = newLabel();
		String[] value = findLabel( name );
		
		/*
		// verbose
		if ( verbose ) {
			try {
				Class.forName( pageClass ).getMethod( "get" + ShoreUtil.toParameterName( name ), new Class[0] );
			} catch ( Exception e ) {
				System.err.println( "Page Verbose: Empty " + e );
			}
		}
		*/
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IsEmptyHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		if ( parseUntil( _helper, new String [] { LOGIC_TAG_END, LOGIC_TAG_ELSE } ) == 1 ) {
			// set footer template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/IsEmptyElse.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
				ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
				ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
				ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
				ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			parseUntil( _helper, LOGIC_TAG_END );
			
		}
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IsEmptyFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

	/**
	 * @param _helper
	 */
	private void parseLogicIterator(PluginHelper _helper) throws Exception {

		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		name = _helper.replaceValues( name, 0, -1, this );
		if ( name == null ) throw new Exception( "Iterator: name not set" );
		
		int max = de.mhu.lib.MhuCast.toint( (String)_helper.getTagParameter().get( LOGIC_MAX ), -1);
		
		String   label = newLabel();
		String[] value = findLabel( name );
				
		maps.add( label );
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IteratorHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "max"       , "" + max );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
		int tagFound = parseUntil( _helper, new String[] { LOGIC_TAG_END, LOGIC_TAG_ELSE } );
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/IteratorFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		if ( tagFound == 1 ) {
			// set footer template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/IteratorElseHeader.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
				ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
				ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
				ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
				ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			parseUntil( _helper, LOGIC_TAG_END );			

			// set footer template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/IteratorElseFooter.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
				ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
				ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
				ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
				ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}

		}

		// remove last map	
		maps.remove( maps.size() - 1 );

	}


	/**
	 * @param _helper
	 */
	private void parseLogicPage(PluginHelper _helper) throws Exception {

		String clazz = (String)_helper.getTagParameter().get( LOGIC_CLASS );
		
		if ( clazz == null ) throw new Exception( "Page: class is null" );

		if ( verbose ) {
			try {
				Object obj = Class.forName( clazz ).newInstance();
				pageObject = (PageIfc)obj;
			} catch ( Exception e ) {
				System.err.println( "  Page Verbose: " + e );
			}
		}
				
		String   label = newLabel();
		
		maps.add( label );
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/InnerPageHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "pageClass" , clazz );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		if ( parseUntil( _helper, new String[] { LOGIC_TAG_END, LOGIC_TAG_ELSE } ) == 1 ) {
			// set else template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/InnerPageElse.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
				ShoreUtil.addTemplateParameter( temp, "pageClass" , clazz );
				ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			parseUntil( _helper, LOGIC_TAG_END );
			
		}
		
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/InnerPageFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "pageClass" , clazz );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// remove last map	
		maps.remove( maps.size() - 1 );

	}

	/**
	 * @param _helper
	 */
	private void parseLogicUse(PluginHelper _helper) throws Exception {

		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		
		if ( name == null ) throw new Exception( "Use: name is null" );
				
		String   label = newLabel();
		
		String[] value = findLabel( name );
		
		maps.add( label );
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/UseHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		if ( parseUntil( _helper, new String[] { LOGIC_TAG_END, LOGIC_TAG_ELSE } ) == 1 ) {
			// set else template
			try {
				// get Template
				Template temp = new Template( _helper.getTemplatePath( "page/UseElse.tpl" ) );
				// Insert container parameter
				ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
				ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
				ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
				ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
				ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
				ShoreUtil.addTemplateParameter( temp, "label"     , label );
				_helper.replaceTag( temp.toString().trim() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			parseUntil( _helper, LOGIC_TAG_END );
			
		}
		
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/UseFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "rootClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// remove last map	
		maps.remove( maps.size() - 1 );

	}


	/**
	 * @param _helper
	 */
	private void parseLogicList(PluginHelper _helper) throws Exception {

		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		name = _helper.replaceValues( name, 0, -1, this );
		if ( name == null ) throw new Exception( "List: name not set" );
		
		int pageItems = de.mhu.lib.MhuCast.toint( (String)_helper.getTagParameter().get( LOGIC_PAGE_ITEMS ), -1 );
		if ( pageItems == -1 ) throw new Exception( "List: page.items not set" );
		
		String goBack = (String)_helper.getTagParameter().get( LOGIC_GO_BACK );
		String goNext = (String)_helper.getTagParameter().get( LOGIC_GO_NEXT );
		String noBack = (String)_helper.getTagParameter().get( LOGIC_NO_BACK );
		String noNext = (String)_helper.getTagParameter().get( LOGIC_NO_NEXT );
		
		String indexStyle = (String)_helper.getTagParameter().get( LOGIC_INDEX_STYLE );
		
		String   label = newLabel();
		String[] value = findLabel( name );
		
		maps.add( label );
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/ListHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "pageItems"       , "" + pageItems );
			ShoreUtil.addTemplateParameter( temp, "file", _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "goBack", goBack );
			ShoreUtil.addTemplateParameter( temp, "goNext", goNext );
			ShoreUtil.addTemplateParameter( temp, "noBack", noBack );
			ShoreUtil.addTemplateParameter( temp, "noNext", noNext );
			ShoreUtil.addTemplateParameter( temp, "indexStyle", indexStyle );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		
		parseUntil( _helper, LOGIC_ITERATE_TAG );
		
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/ListContentHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "pageItems"       , "" + pageItems );
			ShoreUtil.addTemplateParameter( temp, "file", _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "goBack", goBack );
			ShoreUtil.addTemplateParameter( temp, "goNext", goNext );
			ShoreUtil.addTemplateParameter( temp, "noBack", noBack );
			ShoreUtil.addTemplateParameter( temp, "noNext", noNext );
			ShoreUtil.addTemplateParameter( temp, "indexStyle", indexStyle );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		parseUntil( _helper, LOGIC_ITERATE_TAG_END );

		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/ListContentFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "pageItems"       , "" + pageItems );
			ShoreUtil.addTemplateParameter( temp, "file", _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "goBack", goBack );
			ShoreUtil.addTemplateParameter( temp, "goNext", goNext );
			ShoreUtil.addTemplateParameter( temp, "noBack", noBack );
			ShoreUtil.addTemplateParameter( temp, "noNext", noNext );
			ShoreUtil.addTemplateParameter( temp, "indexStyle", indexStyle );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		parseUntil( _helper, LOGIC_TAG_END );
		
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/ListFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "pageItems"       , "" + pageItems );
			ShoreUtil.addTemplateParameter( temp, "file", _helper.getSourceName() );
			ShoreUtil.addTemplateParameter( temp, "goBack", goBack );
			ShoreUtil.addTemplateParameter( temp, "goNext", goNext );
			ShoreUtil.addTemplateParameter( temp, "noBack", noBack );
			ShoreUtil.addTemplateParameter( temp, "noNext", noNext );
			ShoreUtil.addTemplateParameter( temp, "indexStyle", indexStyle );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// remove last map	
		maps.remove( maps.size() - 1 );

	}

	private void parseLogicHasMore(PluginHelper _helper) throws Exception {

		String name = (String)_helper.getTagParameter().get( LOGIC_NAME );
		name = _helper.replaceValues( name, 0, -1, this );
		if ( name == null ) throw new Exception( "HasMore: name not set" );
		
		int max = de.mhu.lib.MhuCast.toint( (String)_helper.getTagParameter().get( LOGIC_MAX ), -1);
		if ( max == -1 ) return;
		
		String   label = newLabel();
		String[] value = findLabel( name );
				
		// set header template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/HasMoreHeader.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass" , pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"      , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"    , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel" , mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			ShoreUtil.addTemplateParameter( temp, "max"       , "" + max );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		parseUntil( _helper, LOGIC_TAG_END );
		
		// set footer template
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "page/HasMoreFooter.tpl" ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "name"     , value[1] );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( value[1] ) );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", value[0] );
			ShoreUtil.addTemplateParameter( temp, "label"     , label );
			_helper.replaceTag( temp.toString().trim() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

	private String[] findLabel( String _value ) {
		
		// init
		String[] out = new String[]{null,"no value"};
		String   value = _value;
		int map = maps.size() - 1;
		String   current   = (String)maps.elementAt( map );
		
		try {
			if ( value == null ) throw new Exception( "value is null ");			
			
			// test, if start at root:
			if ( value.startsWith( "/") ) {
			  map = 0;
			  current   = (String)maps.elementAt( map );
			  value = value.substring( 1 );
			}
			
			int pos = -1;
			while ( ( pos = value.indexOf( '/' ) ) >= 0 ) {
				String path = value.substring( 0, pos );
				value = value.substring( pos + 1 );
				
				if ( path.equals( "." )) {
					// nothing
				} else
				if ( path.equals( ".." )) {
					// back
					map--;
					if ( map < 0 ) throw new Exception( "path to deep");
					current = (String)maps.elementAt( map );
				} else {
					// forward ( into next map )
					//TODO: Hardcoded jsp ... change to generic code
					current = "((java.util.Map)" + current + ".get( \"" + path + "\" ))";

					// map++;
					//if ( map >= maps.size() )
					//	throw new Exception( "path to hight" );					
				}
				
			}
			
			out[0] = current;
			out[1] = value;
			
		} catch ( Exception e ) {
			out[0] = null;
			out[1] = e.toString() + ": " + _value;
		}
				
		return out;
	}
	
	/* (non-Javadoc)
	 * @see de.mhu.shore.ReplaceListener#replace(java.lang.String)
	 */
	public String replace(String _value) {
		
		if ( debug ) System.err.println( "  replace: " + _value );
		// find type
		int pos = _value.indexOf( ':' );
		if ( pos < 1 ) {
			System.err.println( "  Type not found in: " + _value );
			return null;
		}
		String type  = _value.substring( 0, pos );
		String value = _value.substring( pos+1 );
		
		// find label
		String[] label = findLabel( value );
		if ( label[0] == null ) {
			System.err.println( "  Label: " + label[1] );
			return null;			
		}
		
		String tempName = helper.getTemplatePath( "page/ReplaceValue_" + type + ".tpl" );
		if ( ! (new File( tempName )).exists() ) {
			System.err.println( "  Type not found in : " + _value );
			return null;
		}
		
		
		try {
			// get Template
			Template temp = new Template( tempName );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "pageClass", pageClass );
			ShoreUtil.addTemplateParameter( temp, "getter"   , "get" + ShoreUtil.toParameterName( _value ) );
			ShoreUtil.addTemplateParameter( temp, "name", label[1] );
			ShoreUtil.addTemplateParameter( temp, "mainLabel", mainLabel );
			ShoreUtil.addTemplateParameter( temp, "localLabel", label[0] );
			return temp.toString().trim();

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return null;
	}

}
