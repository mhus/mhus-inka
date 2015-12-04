<%@page language="java" %>
<%@page import="java.util.*" %><%

try {

  String ac = request.getParameter( "saf_ac" );
  String saf_id = request.getParameter( "saf_id" );
  
  StringBuffer sb = new StringBuffer();
  for ( Enumeration keys = request.getParameterNames(); keys.hasMoreElements(); ) {
  
    String key = (String)keys.nextElement();
    if ( key.startsWith( "safp_" ) ) {
      sb.append( '&' );
      sb.append( de.mhu.lib.Rfc1738.encode( key.substring( 5 ) ) );
      sb.append( '=' );
      sb.append( de.mhu.lib.Rfc1738.encode( request.getParameter( key ) ) );
    }
  
  }
  if ( sb.length() != 0 )
    sb.setCharAt( 0, '?' );
    
  String parameters = sb.toString();

  Hashtable messages = new Hashtable();
  <t:messages>
  messages.put( "{v:key_p}", "{v:value_p}" );
  </t:messages>

  {v:formClass} form = new {v:formClass}();

  	    try {
	  	    org.apache.commons.fileupload.DiskFileUpload fu = new org.apache.commons.fileupload.DiskFileUpload();
	        
	        fu.setSizeMax( {v:uploadsMax} );
	        fu.setSizeThreshold( {v:uploadsCache} );
	        fu.setRepositoryPath( "{v:uploadsTmp_p}" );
	
	        java.util.List fileItems = fu.parseRequest( request );
	        for ( java.util.Iterator i = fileItems.iterator(); i.hasNext(); ) {
	        	org.apache.commons.fileupload.FileItem fi = ((org.apache.commons.fileupload.FileItem)i.next());
				String fieldName = fi.getFieldName();
	        	<t:uploads.multipart>
	        	if ( fieldName.equals( "{v:name_p}" ) ) {
	        	  form.{v:setter}( fi );
	        	} else
	        	</t:uploads.multipart>
                <t:parameters.multipart>
                if ( fieldName.equals( "{v:name_p}" ) ) {
                  form.{v:setter}( fi.getString() );
                } else
                </t:parameters.multipart>
                if ( fieldName.equals( "saf_ac" ) ) {
                  ac = fi.getString();
                } else
                if ( fieldName.equals( "saf_id" ) ) {
                  saf_id = fi.getString();
                } else
	        	System.err.println( getClass() + ": Setter for filename [" + fieldName + "] not found [" + fi.getString() + "]" );
	        }
		} catch ( org.apache.commons.fileupload.FileUploadException fuex ) {
			System.err.println( getClass() + ": " + fuex );
		}
  	  
  if ( form instanceof de.mhu.shore.ifc.FormIfc ) {
    form.setFormId( saf_id );
    de.mhu.shore.ifc.FormResult formResult = form.verify( request, response );
    formResult.setMessages( messages );

    if ( formResult.isError() ) {
      request.setAttribute( "SHORE_FORM_OBJECT", form );
      request.setAttribute( "SHORE_ERROR", formResult.getErrorMsg() );
      String uri = request.getRequestURI();
      int pos = uri.lastIndexOf( '/' );
      if ( pos >= 0 ) uri = uri.substring( 0, pos+1 );
      javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( uri + "{v:rootFile_p}" + parameters );
      rd.forward( request, response );
      form = null;
    }
  }

  if ( form != null ) {
    de.mhu.shore.ifc.ActionIfc    action       = new {v:actionClass}();
    de.mhu.shore.ifc.ActionResult actionResult = action.execute( form, request, response );
    
    if ( actionResult != null ) {
      actionResult.setMessages( messages );
      if ( actionResult.isError() ) {
        request.setAttribute( "SHORE_FORM_OBJECT", form );
        request.setAttribute( "SHORE_ERROR", actionResult.getErrorMsg() );
	    String uri = request.getRequestURI();
	    int pos = uri.lastIndexOf( '/' );
        if ( pos >= 0 ) uri = uri.substring( 0, pos+1 );
        javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( uri + "{v:rootFile_p}" + parameters );
        rd.forward( request, response );
      } else
      if ( actionResult.goAhead() ) {
        if ( actionResult.isRedirect() ) {
          response.sendRedirect( ac );
        } else {
  	      String uri = request.getRequestURI();
  	      int pos = uri.lastIndexOf( '/' );
          if ( pos >= 0 ) uri = uri.substring( 0, pos+1 );
          javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( uri + ac );
          rd.forward( request, response );
        }
      } else
      if ( actionResult.goForward() ) {
        if ( actionResult.isRedirect() ) {
          response.sendRedirect( actionResult.getForwardURL().toString() );
        } else {
          javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( actionResult.getForwardURL().toString() );
          rd.forward( request, response );
        }
      }
    }
  }
} catch( Exception e ) { e.printStackTrace(); }
%>
