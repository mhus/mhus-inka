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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuCast;
import de.mhu.shore.Plugin;
import de.mhu.shore.PluginHelper;
import de.mhu.shore.ShoreUtil;
import de.mhu.shore.ifc.Tree;

/**
 * @author hummel
 * 

 * 
 */
public class ShoreTree implements Plugin {

	private static final String TREE_CLASS   = "tree.class";
	private static final String TREE_NAME    = "name";
	private static final String TREE_TAG     = "iframe";
	private static final String TREE_TAG_END = "/iframe";
	private static final String TREE_IMAGES  = "tree.images";
	private static final String TREE_INCLUDE = "include";
	private static final String HTML_WIDTH   = "width";
	private static final String HTML_HEIGHT  = "height";
	private static final String TREE_STYLE   = "tree.style";

	private static final String BG_COLOR1    = "tree.bgcolor1";
	private static final String BG_COLOR2    = "tree.bgcolor2";
	private static final String HTML_COLOR   = "bgcolor";
	
	private static final String TREE_SELECTED_STYLE   = "tree.selected.style";
	private static final String TREE_WIDTH   = "tree.width";
	private static final String TITLE_STYLE  = "title.style";
	private static final String TITLE        = "title";
	
	private static final String COLUMN       = "column";
	private static final String COLUMN_NAME  = "name";
	private static final String COLUMN_STYLE = "style";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_WIDTH = "width";
	private static final String COLUMN_SELECTED_STYLE = "selected.style";
	
	private static final String HTML_SRC     = "src";
	/* (non-Javadoc)
	 * @see de.mhu.shore.Plugin#execute(de.mhu.shore.PluginHelper)
	 */
	public void execute(PluginHelper _helper) throws Exception {

		_helper.resetTag();
		
		while ( _helper.nextTag() ) {
			
			if ( _helper.getTagName().equals( TREE_TAG ))
				parseTree( _helper );
			
					
		}
		
	}

	private void parseTree( PluginHelper _helper ) {
		
		// get parameter
		String clazz  = (String)_helper.getTagParameter().get( TREE_CLASS );
		String name   = (String)_helper.getTagParameter().get( TREE_NAME );
		String images = (String)_helper.getTagParameter().get( TREE_IMAGES );				
		boolean include = MhuCast.toboolean( (String)_helper.getTagParameter().get( TREE_INCLUDE ), false );

		String style  = (String)_helper.getTagParameter().get( TREE_STYLE );
		if ( style == null ) style = "";
		String selectedStyle = (String)_helper.getTagParameter().get( TREE_SELECTED_STYLE );
		if ( selectedStyle == null ) selectedStyle = style;
		String titleStyle = (String)_helper.getTagParameter().get( TITLE_STYLE );
		if ( titleStyle == null ) titleStyle = "";

		String title = (String)_helper.getTagParameter().get( TITLE );
		if ( title == null ) title = "";
		String width = (String)_helper.getTagParameter().get( TREE_WIDTH );
		if ( width == null ) width = "";		
		
		String bgColor = (String)_helper.getTagParameter().get( HTML_COLOR );
		if ( bgColor == null ) bgColor = "#ffffff";
		String bgColor1 = (String)_helper.getTagParameter().get( BG_COLOR1 );
		if ( bgColor1 == null ) bgColor1 = bgColor;
		String bgColor2 = (String)_helper.getTagParameter().get( BG_COLOR2 );
		if ( bgColor2 == null ) bgColor2 = bgColor;
		
		// remove parameters
		_helper.getTagParameter().remove( TREE_CLASS );
		_helper.getTagParameter().remove( TREE_IMAGES );
		_helper.getTagParameter().remove( TREE_INCLUDE );
		_helper.getTagParameter().remove( TREE_STYLE );
		_helper.getTagParameter().remove( TREE_SELECTED_STYLE );
		_helper.getTagParameter().remove( TREE_WIDTH );
		_helper.getTagParameter().remove( TITLE_STYLE );
		_helper.getTagParameter().remove( TITLE );
		_helper.getTagParameter().remove( BG_COLOR1 );
		_helper.getTagParameter().remove( BG_COLOR2 );
		_helper.getTagParameter().remove( HTML_COLOR );
		
		// get options
		Vector options = new Vector();
		for ( Enumeration e = _helper.getTagParameter().keys(); e.hasMoreElements(); ) {
			
			String key = (String)e.nextElement();
			if ( key.startsWith( "option." )) {
				Hashtable h = new Hashtable();
				ShoreUtil.addTemplateParameter( h, "key", key.substring( 7 ) );
				ShoreUtil.addTemplateParameter( h, "value", (String)_helper.getTagParameter().get( key ) );
				options.addElement( h );
			}
		}
		// remove options
		for ( int i = 0; i < options.size(); i++ ) {
			_helper.getTagParameter().remove( "option." + (String)((Hashtable)options.elementAt( i )).get( "key") );
		}
		
		// check parameter
		if ( clazz == null )
			return;
		
		// verbose
		boolean verbose = MhuCast.toboolean( _helper.getParameter( "verbose" ),
						  MhuCast.toboolean( _helper.getShoreParameter( "jsp.tree.verbose"), true ) ); 

		if ( name == null ) {
			name = "shoreTree" + _helper.getUniqueId();
			System.out.println( "  Tree has no name, use " + name );
		}

		if ( verbose ) {			
			try {

				// check formClass
				Object tree = Class.forName( clazz ).newInstance();
				if ( !( tree instanceof Tree ) )
					throw new Exception( "Class is no Instance of ifc.Tree");
			} catch ( Exception e ) {
				System.out.println( "  Tree Verbose check: " + _helper.getSourceName() + ":" + name );
				System.err.println( "    Tree Verbose: " + e );			
			}
		}
		
		if ( images == null ) {
			images = "";
			System.out.println( "  Images not set" );
		}
		
		// prepare parameters
		String file       = _helper.getSourceName();
		String parentFile = _helper.getSourceName();
		String tempName   = "TreeInclude.tpl";

		Vector table = new Vector();
		for ( int i = 1; _helper.getTagParameter().get( COLUMN + i + "." + COLUMN_NAME ) != null; i++ ) {
			
			Hashtable h = new Hashtable();
			ShoreUtil.addTemplateParameter( h, COLUMN_NAME, (String)_helper.getTagParameter().get( COLUMN + i + "." + COLUMN_NAME ) );
			
			String value = null;
			// title
			value = (String)_helper.getTagParameter().get( COLUMN + i + "." + COLUMN_TITLE );
			if ( value == null ) value = "";
			ShoreUtil.addTemplateParameter( h, COLUMN_TITLE, value );
			// style
			value = (String)_helper.getTagParameter().get( COLUMN + i + "." + COLUMN_STYLE );
			if ( value == null ) value = style;
			ShoreUtil.addTemplateParameter( h, COLUMN_STYLE, value );
			// width
			value = (String)_helper.getTagParameter().get( COLUMN + i + "." + COLUMN_WIDTH );
			if ( value == null ) value = "";
			ShoreUtil.addTemplateParameter( h, COLUMN_WIDTH, value );
			// selected style
			value = (String)_helper.getTagParameter().get( COLUMN + i + "." + COLUMN_SELECTED_STYLE );
			if ( value == null ) value = selectedStyle;
			ShoreUtil.addTemplateParameter( h, COLUMN_SELECTED_STYLE, value );

			table.addElement( h );
			
			// remove parameter from tag
			_helper.getTagParameter().remove( COLUMN + i + "." + COLUMN_NAME );
			_helper.getTagParameter().remove( COLUMN + i + "." + COLUMN_TITLE );
			_helper.getTagParameter().remove( COLUMN + i + "." + COLUMN_STYLE );
			_helper.getTagParameter().remove( COLUMN + i + "." + COLUMN_WIDTH );
			_helper.getTagParameter().remove( COLUMN + i + "." + COLUMN_SELECTED_STYLE );
			
		}
		
		if ( ! include ) {
			file = _helper.newFileName();
			_helper.getTagParameter().put( HTML_SRC, file );
			tempName = "TreeIFrame.tpl";
		}
		
		try {
			// get Template
			Template temp = new Template( _helper.getTemplatePath( "tree/" + tempName ) );
			// Insert container parameter
			ShoreUtil.addTemplateParameter( temp, "class"   , clazz );
			ShoreUtil.addTemplateParameter( temp, "name"    , name );
			ShoreUtil.addTemplateParameter( temp, "images"  , images );
			ShoreUtil.addTemplateParameter( temp, "file"    , file );
			ShoreUtil.addTemplateParameter( temp, "width"   , (String)_helper.getTagParameter().get( HTML_WIDTH ) );
			ShoreUtil.addTemplateParameter( temp, "height"  , (String)_helper.getTagParameter().get( HTML_HEIGHT ) );
			ShoreUtil.addTemplateParameter( temp, "style"   , style );
			ShoreUtil.addTemplateParameter( temp, "parentFile"    , parentFile );
			ShoreUtil.addTemplateParameter( temp, "title"   , title );
			ShoreUtil.addTemplateParameter( temp, "title.style"   , titleStyle );
			ShoreUtil.addTemplateParameter( temp, "selected.style", selectedStyle );
			ShoreUtil.addTemplateParameter( temp, "tree.width"    , width );
			ShoreUtil.addTemplateParameter( temp, "bgcolor"       , bgColor );
			ShoreUtil.addTemplateParameter( temp, "bgcolor1"      , bgColor1 );
			ShoreUtil.addTemplateParameter( temp, "bgcolor2"      , bgColor2 );
			ShoreUtil.addTemplateParameter( temp, "pageVariable"  , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_VARIABLE ) );
			ShoreUtil.addTemplateParameter( temp, "pageClass"     , ""+_helper.getModuleInfo( ShorePage.MODULE_MAIN_CLASS    ) );
			
			for ( int i = 0; i < options.size(); i++ ) {
				temp.loopAppend( "options", (Hashtable)options.elementAt( i ) );
			}
			
			for ( int i = 0; i < table.size(); i++ ) {
				Hashtable h = (Hashtable)table.elementAt( i );
				ShoreUtil.addTemplateParameter( h, "title.style"   , titleStyle );
				ShoreUtil.addTemplateParameter( h, "selected.style", (String)h.get( COLUMN_SELECTED_STYLE ) );
				ShoreUtil.addTemplateParameter( h, "bgcolor1"      , bgColor1 );
				ShoreUtil.addTemplateParameter( h, "bgcolor2"      , bgColor2 );
				
				temp.loopAppend( "table", h );
			} 
			
			if ( include ) {
				_helper.replaceTag( temp.toString().trim() );
				// remove text to end tag
				int pos = _helper.getTagCursor();
				while ( _helper.nextTag() && ! _helper.getTagName().equals( TREE_TAG_END ) )
					{}
				if ( _helper.getTagName() != null && _helper.getTagName().equals( TREE_TAG_END ) )
					_helper.replaceText( pos, _helper.getTagCursor(), "" );
				else
					System.err.println( "  ShoreJspTree: IFRAME without end tag: " + name );
			} else {
				_helper.newFile( file, temp.toString() );
				_helper.replaceTag();						
			}

		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
		
	}
}
