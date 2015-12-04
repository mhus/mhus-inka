<%
  if ( true ) {
	  Object errorMsg__ = request.getAttribute( "SHORE_ERROR" );
	  if ( errorMsg__ != null )
	    out.print( errorMsg__.toString() );
   }
%>