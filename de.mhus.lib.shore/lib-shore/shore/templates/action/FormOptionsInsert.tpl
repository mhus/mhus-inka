<%
if ( true ) {
    int selected = -1;
    try {
  	  selected = new Integer( {v:name}_form.{v:getter}() ).intValue();
  	} catch ( Exception e ) {};
	String[] options = {v:name}_form.{v:getter}Options();
	for ( int i = 0; i < options.length; i++ ) {
	  out.println( "<option value=" + i );
	  if ( i == selected ) out.print( " selected=\"seleted\"" );
	  out.println( ">" + de.mhu.shore.ShoreUtil.text2web( options[i], true ) + "</option>" );
	}
}
%>