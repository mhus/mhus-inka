<%
if ( true ) {
  String[] selected = {v:name}_form.{v:getter}();
  boolean ok = false;
  for ( int i = 0; i < selected.length; i++ )
    if ( selected != null && selected.equals( "{v:value_p}" ) ) ok = true;
    
  if ( ok )
    out.print( "selected" );
  else
    out.print( "not_selected" );
}
%>