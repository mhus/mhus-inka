<%
Object {v:label} = {v:localLabel}.get( {v:name_c} );
if (   {v:label} == null || 
     ( {v:label} instanceof java.util.Collection && ((java.util.Collection){v:label}).size() == 0 ) ||
     ( {v:label} instanceof java.util.Map && ((java.util.Map){v:label}).size() == 0 ) 
   ) {
%>