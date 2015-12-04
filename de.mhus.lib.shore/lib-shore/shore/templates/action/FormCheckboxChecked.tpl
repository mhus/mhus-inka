<%
if (true ) {
  String[] selected = {v:name}_form.{v:getter}();
  boolean ok = false;
  if ( selected != null )
    for ( int i = 0; i < selected.length; i++ )
      if ( selected[i] != null && selected[i].equals( "{v:value_p}" ) ) ok = true;
  
  
  if ( ok )
    out.print( "checked" );
  else
    out.print( "not_checked" );
}
%>