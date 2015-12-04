/*
 *
 * Date: 16:02:20 14.05.2003
 * Author hummel
 *
 * Copyright: Virtueller Campus Bayern GmbH (c) 2003
 *
 *
 *
 */
package de.mhu.test.web;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import de.mhu.shore.ifc.Page;
import de.mhu.shore.ifc.PageResult;
import de.mhu.shore.ifc.tree.Document;
import de.mhu.shore.ifc.tree.Node;


/**
 * @author hummel
 *

 *
 */
public class ObjectPage
    extends Page
{
    //~ Instance fields --------------------------------------------------------

    private Vector selected = new Vector(  );

    //~ Methods ----------------------------------------------------------------

    /* (non-Javadoc)
     * @see de.mhu.shore.ifc.Page#init()
     */
    public PageResult init(  )
    {
        Document doc = ( Document ) this.request.getSession(  ).getAttribute( "ObjectTree" );
        if ( doc != null )
        {
            Vector nodes = doc.getSelectedNodes(  );
            selected.clear(  );
            for ( int i = 0; i < nodes.size(  ); i++ )
            {
                Hashtable h = new Hashtable(  );
                h.put( "name", ( ( Node ) nodes.elementAt( i ) ).getTitle(  ) );
                selected.addElement( h );
            }
        }
        
        return null;
    }

    public Collection getSelected(  )
    {
        return selected;
    }
}
