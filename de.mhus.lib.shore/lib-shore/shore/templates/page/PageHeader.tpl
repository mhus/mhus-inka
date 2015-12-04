<%
// try {

{v:pageClass} {v:mainLabel} = new {v:pageClass}();
de.mhu.shore.ifc.PageResult pr = {v:mainLabel}.init( request, response );

if ( pr != null && ! pr.goAhead() ) {

  if ( pr.isError() ) {
  	response.sendError( 500, pr.getErrorMsg() );
  	return;
  }
  
  if ( pr.goForward() ) {
    if ( pr.isRedirect() ) {
      response.sendRedirect( pr.getForwardURL().toString() );
    } else {
      javax.servlet.RequestDispatcher rd = request.getRequestDispatcher( pr.getForwardURL().toString() );
      rd.forward( request, response );
    }
  }
  
  
}


  StringBuffer {v:mainLabel}Sb = new StringBuffer();
  for ( java.util.Enumeration {v:mainLabel}Keys = request.getParameterNames(); {v:mainLabel}Keys.hasMoreElements(); ) {
  
  	
    String k = (String){v:mainLabel}Keys.nextElement();
    
    if ( ! k.startsWith( "sp_shoreVariable" ) ) {
    
	    {v:mainLabel}Sb.append( '&' );
	    {v:mainLabel}Sb.append( de.mhu.lib.Rfc1738.encode( k ) );
	    {v:mainLabel}Sb.append( '=' );
	    {v:mainLabel}Sb.append( de.mhu.lib.Rfc1738.encode( request.getParameter( k ) ) );
	    
	}    
  
  }
  
  String {v:mainLabel}ParamAdd = {v:mainLabel}Sb.toString();
  
  if ( {v:mainLabel}Sb.length() != 0 )
    {v:mainLabel}Sb.setCharAt( 0, '?' );
    
  String {v:mainLabel}Parameter = {v:mainLabel}Sb.toString();

%>