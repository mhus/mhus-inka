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
import java.util.Map;
import java.util.Vector;

/**
 * @author hummel
 * 

 * 
 */
public class Node {

	private Vector childs = new Vector();
	private Node   parent = null;
	private int    currentChild = 0;
	private boolean open = false;
	private boolean leaf = true;
	private String title = "???";
	private boolean clickable = false;
	private boolean clickableImage = false;
	private Document doc = null;
	private Map tableValue = new Hashtable();
	
	private NodeListener listener = null;
	private Object       obj      = null;

	private Hashtable images   = new Hashtable();
	private boolean   selected = false;
	
	public Node() {
	}
		
	public Node( String _title ) {
		setTitle( _title );
	}
	
	
	public void setDocument( Document _doc ) {
		doc = _doc;
	}
	
	public Document getDocument() {
		if ( doc == null ) {
			if ( parent == null)
				return null;
			else
				return parent.getDocument(); 
		}
		return doc;
	}
	
	public void setTitle( String _title ) {
		title = _title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void addChild( Node _child ) {
		childs.addElement( _child );
		_child.setParent( this );
		leaf = false;
	}
	
	public void removeChilds() {
		childs.removeAllElements();
	}
	
	public void removeChild( int nr ) {
		childs.removeElementAt( nr );
	}

	public void insertChild( int nr, Node _child ) {
		childs.insertElementAt( _child, nr );
	}

	public void setParent( Node _node ) {
		parent = _node;
	}
	
	public void setOpen( boolean _open ) {
		open = _open;
	}
	
	public void setLeaf( boolean _leaf ) {
		leaf = _leaf;
	}
	
	public boolean isLeaf() {
		return leaf;
	}

	public Node getParent() {
		return parent;
	}
	
	public boolean hasChilds() {
		return ( childs.size() != 0 );
	}
	
	public void resetChilds() {
		currentChild = 0;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public Node getNextChild() {
		if ( ! hasNextChild() ) return null;
		return (Node)childs.elementAt( currentChild++ );
	}
	
	public boolean hasNextChild() {
		return ( currentChild < childs.size() );
	}
	
	public int getChildNumber( Node _child ) {
		for ( int i = 0; i < childs.size(); i++ )
			if ( childs.elementAt(i) == _child )
				return i;
		return -1;
	}
	
	public String getImage( String _type ) {
		if ( images.get( _type ) == null ) {
			if ( _type.equals( Document.EXTRA ) ) return null;
			if ( parent != null ) {
				return parent.getImage( _type );
			} else {
				return null;
			}
		}
		return getDocument().getImagePath() +  (String)images.get( _type );			
	}
	
	public void setImage( String _type, String _src ) {
		images.put( _type, _src );
	}
	
	public void setImageIfNotSet( String _type, String _src ) {
		if ( images.get( _type) != null ) return;
		setImage( _type, _src );
	}
	
	public boolean isClickable() {
		return clickable;
	}
	
	public void setClickable( boolean _clickable ) {
		clickable = _clickable;
	}

	public boolean isClickableImage() {
		return clickableImage;
	}
	
	public void setClickableImage( boolean _clickable ) {
		clickableImage = _clickable;
	}
	
	public void clickEvent( String _ref, int _index ) {
		int pos = _ref.indexOf( "_", _index );
		if ( pos < 0 ) {
			if ( listener != null )
				listener.clickEvent( this );
		} else {
			int nr = new Integer( _ref.substring( _index, pos ) ).intValue();
			if ( nr < childs.size() )
				((Node)childs.elementAt( nr )).clickEvent( _ref, pos+1 );				
		}
	}

	public void refreshEvent( Object _reason ) {
		if ( listener != null )
			listener.refreshEvent( this, _reason );
		for ( int i = 0; i < childs.size(); i++ )
			((Node)childs.elementAt( i )).refreshEvent( _reason );

	}


	public void imgClickEvent( String _ref, int _index ) {
		int pos = _ref.indexOf( "_", _index );
		if ( pos < 0 ) {
			if ( listener != null )
				listener.imgClickEvent( this );
		} else {
			int nr = new Integer( _ref.substring( _index, pos ) ).intValue();
			if ( nr < childs.size() )
				((Node)childs.elementAt( nr )).imgClickEvent( _ref, pos+1 );				
		}
	}

	public void expandEvent( String _ref, int _index ) {
		int pos = _ref.indexOf( "_", _index );

		if ( pos < 0 ) {
			setOpen( ! isOpen() );
			if ( listener != null ) {
				if ( isOpen() )
					listener.expandEvent( this );
				else
					listener.collapsEvent( this );
			}
		} else {			
			int nr = new Integer( _ref.substring( _index, pos ) ).intValue();
			if ( !isOpen() ) {
				setOpen( true );
				listener.expandEvent( this );
			}
			if ( nr < childs.size() )
				((Node)childs.elementAt( nr )).expandEvent( _ref, pos+1 );				
		}
	}
	
	public void setListener( NodeListener _listener ) {
		listener = _listener;
	}

	public NodeListener getListener() {
		return listener;
	}
	
	public void setObject( Object _obj ) {
		obj = _obj;
	}
	
	public Object getObject() {
		return obj;
	}
	
	public String getValue( String _key ) {
		Object o = tableValue.get( _key );
		if ( o == null ) return "";
		return o.toString();
	}
	
	public void setValue( String _key, String _value ) {
		tableValue.put( _key, _value );
	}
	
	public void setValues( Map _map ) {
		tableValue = _map;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected( boolean _sel ) {
		selected = _sel;
	}
	
	public void clearSelection() {
		setSelected( false );
		for ( int i = 0; i < childs.size(); i++ )
			((Node)childs.elementAt( i )).clearSelection();
	}

	public void selectAll() {
		setSelected( true );
		for ( int i = 0; i < childs.size(); i++ )
			((Node)childs.elementAt( i )).selectAll();
	}

	public Node getSelected() {
		if ( isSelected() ) return this;
		for ( int i = 0; i < childs.size(); i++ ) {
			Node out = ((Node)childs.elementAt( i )).getSelected();
			if ( out != null ) return out;
		}
		return null;
	}

	public void fillSelected( Vector _v ) {
		if ( isSelected() ) _v.addElement( this );
		for ( int i = 0; i < childs.size(); i++ )
			((Node)childs.elementAt( i )).fillSelected( _v );		
	}
		
}
