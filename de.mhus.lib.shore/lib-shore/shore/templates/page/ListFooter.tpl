<%

 	out.print( "</td></tr>" );
 	
 	

  out.print( "<tr><td align=\"center\" valign=\"top\">" );
  
  out.print( "<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" >" );

  // Page / Of
  out.print( "<tr><td><nobr><div style=\"{v:indexStyle_p}\">" );
  out.print( ({v:label}Offset / {v:label}PageItems + 1) + " / " + ((((java.util.Collection){v:label}Obj).size()-1) / {v:label}PageItems + 1) );
  
  // Back
  out.print( "</div></nobr></td><td width=\"100%\">&nbsp;" );
  out.print( "</td><td>" );
  
  if (   {v:label}Offset != 0 )
  	out.print( "<a href=\"{v:file_url}?sp_{v:label_url}_off=" + ({v:label}Offset - {v:label}PageItems) + {v:mainLabel}ParamAdd + "\" >{v:goBack_p}</a>" );
  else
  	out.print( "{v:noBack_p}" );
  
  // Next
  out.print( "</td><td>&nbsp;" );
  out.print( "</td><td>" );
    
  if (   ({v:label}Offset / {v:label}PageItems) <  ((((java.util.Collection){v:label}Obj).size()-1) / {v:label}PageItems) )
  	out.print( "<a href=\"{v:file_url}?sp_{v:label_url}_off=" + ({v:label}Offset + {v:label}PageItems) + {v:mainLabel}ParamAdd + "\" >{v:goNext_p}</a>" );
  else
  	out.print( "{v:noNext_p}" );
  
  out.print( "</td></tr>" );
  
  out.print( "</table>" );
  
  out.print( "</td></tr>" );

 	
  	
 	out.print( "</table>" );
}
%>