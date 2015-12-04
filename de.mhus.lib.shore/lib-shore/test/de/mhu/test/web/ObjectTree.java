/*
 *
 * Date: 16:29:10 13.05.2003
 * Author hummel
 *
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 *
 *
 *
 */
package de.mhu.test.web;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhu.shore.ifc.Tree;
import de.mhu.shore.ifc.tree.Document;
import de.mhu.shore.ifc.tree.Node;
import de.mhu.shore.ifc.tree.NodeListener;


/**
 * @author hummel
 *

 *
 */
public class ObjectTree
    extends Tree
{
    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.Tree#getDocument(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public Document getDocument( HttpServletRequest  arg0,
                                 HttpServletResponse arg1 )
    {
        Document doc = ( Document ) arg0.getSession(  ).getAttribute( "ObjectTree" );
        if ( doc == null )
        {
            doc = initDocument(  );
            arg0.getSession(  ).setAttribute( "ObjectTree", doc );
        }

        return doc;
    }

    private Document initDocument(  )
    {
        Document doc = new Document(  );
        doc.setImagePath( getImagePath(  ) );

        ThreadGroup root = Thread.currentThread(  ).getThreadGroup(  );

        // find root
        while ( root.getParent(  ) != null )
        {
            root = root.getParent(  );
        }

        Node node = new Node( "root" );
        node.setLeaf( false );
        node.setListener( new Listener(  ) );
        node.setObject( root );
        node.setClickable( true );

        doc.setRoot( node );

        return doc;
    }
}


class Listener
    implements NodeListener
{
    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.tree.NodeListener#clickEvent(de.mhu.shore.ifc.tree.Node)
     */
    public void clickEvent( Node arg0 )
    {
        if ( arg0.isSelected(  ) )
        {
            arg0.clearSelection(  );
            arg0.getDocument().setReloadPage();
        }
        else
        {
            arg0.selectAll(  );
			arg0.getDocument().setReloadPage();
        }
    }

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.tree.NodeListener#expandEvent(de.mhu.shore.ifc.tree.Node)
     */
    public void expandEvent( Node arg0 )
    {
        if ( arg0.isLeaf(  ) )
        {
            return;
        }

        Object o = arg0.getObject(  );
        if ( o.getClass(  ).equals( ThreadGroup.class ) )
        {
            ThreadGroup g = ( ThreadGroup ) o;

            int         nr = g.activeGroupCount(  );
            ThreadGroup tg[] = new ThreadGroup[ nr ];
            nr = g.enumerate( tg, false );

            for ( int i = 0; i < nr; i++ )
            {
                Node child = new Node( "ThreadGroup: " + tg[ i ].getName(  ) );
                child.setListener( this );
                child.setObject( tg[ i ] );
                child.setLeaf( false );
                child.setClickable( true );
                arg0.addChild( child );
            }

            nr = g.activeCount(  );
            Thread t[] = new Thread[ nr ];
            nr = g.enumerate( t, false );
            for ( int i = 0; i < nr; i++ )
            {
                Node child = new Node( "Thread: " + t[ i ].getName(  ) );
                child.setListener( this );
                child.setLeaf( false );
                child.setObject( t[ i ] );
                child.setClickable( true );
                arg0.addChild( child );
            }
        }
        else
        {
            Method m[] = o.getClass(  ).getMethods(  );

            for ( int i = 0; i < m.length; i++ )
            {
                Node child = new Node( m[ i ].getName(  ) + "(...) " );
                child.setClickable( true );
                child.setValue( "return", "" + m[ i ].getReturnType(  ) );
                child.setListener( this );

                if ( m[ i ].getParameterTypes(  ).length == 0 )
                {
                    String name = m[ i ].getName(  );
                    if ( name.startsWith( "is" ) )
                    {
                        try
                        {
                            Object ret = m[ i ].invoke( o, new Class[ 0 ] );
                            child.setValue( "value", "" + ret );
                        }
                        catch ( Exception e ) {}
                    }
                    else if ( name.startsWith( "get" ) )
                    {
                        try
                        {
                            Object ret = m[ i ].invoke( o, new Class[ 0 ] );
                            child.setObject( ret );
                            child.setLeaf( false );
                            child.setValue( "value", "" + ret );
                        }
                        catch ( Exception e ) {}
                    }
                }

                arg0.addChild( child );
            }
        }

        if ( arg0.isSelected(  ) )
        {
            arg0.selectAll(  );
			arg0.getDocument().setReloadPage();
        }
    }

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.tree.NodeListener#collapsEvent(de.mhu.shore.ifc.tree.Node)
     */
    public void collapsEvent( Node arg0 )
    {
        arg0.removeChilds(  );
		arg0.getDocument().setReloadPage();
    }

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.tree.NodeListener#imgClickEvent(de.mhu.shore.ifc.tree.Node)
     */
    public void imgClickEvent( Node arg0 ) {}

	/* (non-Javadoc)
	 * @see de.mhu.shore.ifc.tree.NodeListener#refreshEvent(de.mhu.shore.ifc.tree.Node, java.lang.Object)
	 */
	public void refreshEvent(Node _src, Object _reason) {
		
	}
}
