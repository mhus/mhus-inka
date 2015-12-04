/*
 *
 * Date: 16:18:39 12.05.2003
 * Author hummel
 *
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 *
 *
 *
 */
package de.mhu.test.testtree;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhu.shore.ifc.Tree;
import de.mhu.shore.ifc.tree.Document;
import de.mhu.shore.ifc.tree.Node;


/**
 * @author hummel
 *

 *
 */
public class PageTree
    extends Tree
{
    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.Tree#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public Document getDocument( HttpServletRequest  arg0,
                             HttpServletResponse arg1 )
    {
        Document doc = new Document(  );
        doc.setImagePath( getImagePath() );

        Node     root = new Node( "root" );
        root.setOpen( true );

        Node child = new Node( "colors" );
        child.setOpen( true );
        Node child2 = new Node( "blue" );
        child2.setOpen( true );
        child.addChild( child2 );
		child2 = new Node( "red" );
		child2.setOpen( true );
		child.addChild( child2 );
		child2 = new Node( "green" );
		child2.setOpen( true );
		child.addChild( child2 );        
        root.addChild( child );
        
		child = new Node( "nudeln" );
		child2 = new Node( "spagetti" );
		child2.setOpen( true );
		child.addChild( child2 );
		child2 = new Node( "rigatoni" );
		child2.setOpen( true );
		child.addChild( child2 );
		child2 = new Node( "lasagne" );
		child2.setOpen( true );
		child.addChild( child2 );
		child2 = new Node( "tortelini" );
		child2.setOpen( true );
		Node child3 = new Node( "fleisch" );
		child3.setOpen( true );
		child2.addChild( child3 );
		child3 = new Node( "spinat" );
		child3.setClickable( true );
		child3.setOpen( true );
		child2.addChild( child3 );
		
		child.addChild( child2 );
        
        
        child.setOpen( true );
        root.addChild( child );

        doc.setRoot( root );

        return doc;
    }
}
