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

package de.mhu.shore.ifc.tree;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author hummel
 * 

 * 
 */
public class Document {

	public static final String LINE   = "l";
	public static final String SPACE  = "s";
	public static final String TEE    = "t";
	public static final String CORNER = "e";
	
	public static final String CORNER_PLUS   = "ep";
	public static final String CORNER_MINUS  = "em";
	public static final String TEE_PLUS      = "tp";
	public static final String TEE_MINUS     = "tm";
	
	public static final String LEAF          = "leaf";	

	public static final String FOLDER_OPEN   = "fo";
	public static final String FOLDER_CLOSED = "fc";
	
	public static final String EXTRA         = "extra";		

	private Node root = null;
	private Node current = null;
	private boolean showRoot = true;
	private Vector levels = new Vector();
	private String path = "";
	
	private boolean reloadPage = false;
	
	private String referenceCache = null;
	
	private Hashtable objects = null;
	
	public void expandEvent( String _ref ) {
		if ( root != null ) root.expandEvent( _ref, 0 );
	}
	
	public void clickEvent( String _ref ) {
		if ( root != null ) root.clickEvent( _ref, 0 );
	}

	public void imgClickEvent( String _ref ) {
		if ( root != null ) root.imgClickEvent( _ref, 0 );
	}

	public void setImagePath( String _path ) {
		path = _path + '/';
	}
	
	public String getImagePath() {
		return path;
	}
	
	public void setRoot( Node _root ) {
		root = _root;
		
		root.setDocument( this );
		
		// set default images
		root.setImageIfNotSet( LINE , "line.gif" );
		root.setImageIfNotSet( SPACE, "space.gif" );
		root.setImageIfNotSet( TEE  , "tee.gif" );
		root.setImageIfNotSet( TEE_MINUS,  "tee_minus.gif" );
		root.setImageIfNotSet( TEE_PLUS, "tee_plus.gif" );
		root.setImageIfNotSet( CORNER, "corner.gif" );
		root.setImageIfNotSet( CORNER_MINUS, "corner_minus.gif" );
		root.setImageIfNotSet( CORNER_PLUS, "corner_plus.gif" );

		root.setImageIfNotSet( LEAF, "link_com.gif" );
		
		root.setImageIfNotSet( FOLDER_OPEN, "folder_open.gif" );
		root.setImageIfNotSet( FOLDER_CLOSED, "folder_closed.gif" );
		
	}

	public void reset() {
		current        = null;
		referenceCache = null;
		levels.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean next() {
		
		// destroy cached elements
		referenceCache = null;
		
		// First Node
		if ( current == null && root == null ) return false;
		if ( current == null && ! showRoot ) {
			current = root;
			return next();
		}
		if ( current == null ) {
			current = root;
			return true;
		}
		
		// has childs ?
		if ( current.hasChilds() && current.isOpen() ) {
			current.resetChilds();
			Node old = current;
			current = current.getNextChild();
			changeLevelAfterChild( old, current );
			
			return true;
		}
		
		while ( ( current = current.getParent() ) != null ) {
			
			levels.removeElementAt( levels.size()-1 );
				
			if ( current.hasNextChild() ) {
				Node old = current;
				current = current.getNextChild();
				changeLevelAfterChild( old, current );
				return true;
			}
			
		}
		
		return false;

	}

	private void changeLevelAfterChild( Node _old, Node _current ) {
		
		Node p = _old.getParent();
		if ( p != null ) {
			if ( p.hasNextChild() )
				levels.setElementAt( LINE, levels.size()-1 );
			else
				levels.setElementAt( SPACE, levels.size()-1 );
		}
		// add link to this child
		if ( _old.hasNextChild() ) {
			if ( _current.isLeaf() ) {
				levels.addElement( TEE );
			} else {
				if ( _current.isOpen() ) {
					levels.addElement( TEE_MINUS );
				} else {
					levels.addElement( TEE_PLUS );
				}
			}
		} else {
			if ( _current.isLeaf() ) {
				levels.addElement( CORNER );
			} else {
				if ( _current.isOpen() ) {
					levels.addElement( CORNER_MINUS );
				} else {
					levels.addElement( CORNER_PLUS );
				}
			}
		}			
	}
	
	public Node current() {
		return current;
	}

	public int getLevels() {
		return levels.size();
	}
	
	public String getLevelAt( int _level ) {
		return (String)levels.elementAt( _level );
	}
	
	public String getNodeReference() {
		
		if ( referenceCache == null ) {
			Node n = current;
			Node x = null;
			StringBuffer buffer = new StringBuffer();
			while ( n != null ) {
				x = n.getParent();
				if ( x != null )
					buffer.insert( 0, "" + x.getChildNumber( n ) + "_" );
				n = x;
			}
			referenceCache = buffer.toString();
		}
		return referenceCache;
		
	}

	public void setReloadPage() {
		reloadPage = true;
	}
	
	public boolean getReloadPage() {
		boolean b = reloadPage;
		reloadPage = false;
		return b;
	}
	
	public Node getSelectedNode() {
		return root.getSelected();
	}
	
	public Vector getSelectedNodes() {
		Vector v = new Vector();
		root.fillSelected( v );
		return v;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public Hashtable getObjects() {
		if ( objects == null ) objects = new Hashtable(); // create only on demand
		return objects;
	}

}
