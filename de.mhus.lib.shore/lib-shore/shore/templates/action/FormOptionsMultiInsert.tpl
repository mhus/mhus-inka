<%
if ( true ) {

	String[] sel = {v:name}_form.{v:getter}();
    int[] selected = new int[ sel.length ];
    for ( int i = 0; i < selected.length ) {
    	selected[i] = -1;     
        try {
  	      selected[i] = new Integer( sel[i] ).intValue();
      	} catch ( Exception e ) {};
    }
	String[] options = {v:name}_form.{v:getter}Options();
	for ( int i = 0; i < options.length; i++ ) {
	  out.println( "<option value=" + i );
	  boolean ok = false;
	  for ( int j = 0; j < selected.length; j++ )
	  	if ( i == selected[j] ) ok = true;
	  	
	  if ( ok ) out.print( " selected=\"seleted\"" );
	  out.println( ">" + options[i] + "</option>" );
	}
}
%>