/*
 *  JavaBY Template Engine enforce separation of code from layout
 *  Copyright (C) 2002  Alexey Popov
 *  
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *  
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  WWW: http://javaby.sf.net/
 *  E-mail: javaby@yahoo.co.uk
 *  Paper mail: 220017 Belarus, Minsk, Kuncevschina str. 38-144
 */
 
package org.javaby.jbyte;

import java.util.*;
import java.io.*;

/**
 * JavaBY Template Engine is a general template engine used for generating any type 
 * of text document from a template. JavaBY Template Engine is used mostly for generating
 * HTML from JSPs or servlets but it can also be used for generating XML, RTF, WML,
 * e-mail text, source code and configuration files. <br>
 * Template has existed in different forms for several years, its concepts
 * are proven, its implementation is robust and it performs well. <br>
 * Using JavaBY Template Engine in cooperation with JavaServer Pages 
 * instead of standalone JavaServer Pages means 
 * that you can completely separate your content from your markup, as the
 * originators of markup languages intended. <br>
 * The difference between JavaBY Template Engine and JSP is 
 * that content can be generated outside a webserver and that the layout is
 * completely separated from the application code. <br>
 * JavaBY Template Engine defines a well-formed tag language.
 * There is no scripting language involved. No calls are made
 * from the template to the application and no calls are made from the
 * application code to the template. The result of this is that an application
 * becomes easier to maintain and you get a higher degree of reuse since the 
 * same template can be used with different application code and the same 
 * application code can be used with different templates.<br>
 * <p>
 * Example of HTML template:<p>
 * <code> &lt;html&gt;<br>
 * &lt;head&gt;<br>
 * &lt;title&gt;<b>{v:title}</b>&lt;/title&gt; <br>
 * &lt;/head&gt; <br>
 * &lt;body&gt;<br>
 * Welcome <b>{v:name}</b> to Simple Template Example<br>
 * &lt;table&gt;<br>
 * &lt;tr bgcolor=&quot;d0d0d0&quot;&gt;&lt;th&gt;Author&lt;/th&gt;&lt;th&gt;Title&lt;/th&gt;&lt;th&gt;Year&lt;/th&gt;&lt;/tr&gt;<br>
 * <b>&lt;t:book&gt;</b><br>
 * &lt;tr&gt;<br>
 * &lt;td&gt;&lt;a href=&quot;#&quot;&gt;<b>{v:author}</b>&lt;/a&gt;&lt;/td&gt;<br>
 * &lt;td&gt;&lt;a href=&quot;#&quot;&gt;<b>{v:title}</b>&lt;/a&gt;&lt;/td&gt;<br>
 * &lt;td&gt;&lt;a href=&quot;#&quot;&gt;<b>{v:year}</b>&lt;/a&gt;&lt;/td&gt;<br>
 * &lt;/tr&gt;<br>
 * <b>&lt;/t:book&gt;</b><br>
 * &lt;/table&gt;<br>
 * &lt;/body&gt; <br>
 * &lt;/html&gt; </code> 
 * <p>
 * Example of use template: <br>
 * 
<pre>
 *  //First create instance of this Template using constructor and point filename as parameter.
 *  Template 
 *    store = new Template("store.html"),
 *    book = store.get("book");
 *
 *  //Secondly fill and develop this document with a few methods such as "set", "get" and &quot;append&quot;.
 *  store.set("title", "Simple Book Template Example");
 *  store.set("name", "JavaBY");
 * 
 *  book.set("author", "Alexey Popov");
 *  book.set("title", "Patterns of using JavaBY Template Engine.");
 *  book.set("year", "2002");
 *  store.append("book", book);
 *
 *  book.set("author", "Garmash Viacheslav");
 *  book.set("title", "Creating web shop using JavaBY Template Engine.");
 *  book.set("year", "2002");
 *  store.append("book", book);
 *
 *  //Out of result.
 *  System.out.println(store);
 * </pre>
 * @author Alexey Popov
 * @created Wed, 21 August, 2002.
 *
 */
 
public class Template{

    protected HashMap   variables = new HashMap(); 
    protected Hashtable templates = new Hashtable();
    
    protected StringBuffer value = new StringBuffer();

    protected String closeTag = "";
    
      /**
      * Create Template document from file which specified by pathname<p>
      * 
      * @param pathname The path name to template document.
      * @exception java.io.IOException If an I/O error occurs.
      * @exception TemplateCreationException If can't create Template
      */
   public Template(HashMap variables, Hashtable templates, StringBuffer value){
       this.variables = variables;
       this.templates = templates;
       this.value = value;
   }
    
      /**
      * Create Template document from file which specified by pathname<p>
      * 
      * @param pathname The path name to template document.
      * @exception java.io.IOException If an I/O error occurs.
      * @exception TemplateCreationException If can't create Template
      */
   public Template(String pathname) throws IOException, FileNotFoundException, TemplateCreationException{
      FileReader fileReader = new FileReader(pathname);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      Template template;
      try{        
         template = new Template(bufferedReader);
         variables = template.variables;
         templates = template.templates;
         value = template.value;
      }
      catch (TemplateCreationException e){
         bufferedReader.close();
         fileReader.close();
         throw e;
      }
      bufferedReader.close();
        fileReader.close();
   }

      /**
      * Create a Template document from Buffered Reader.<p>
      *
      * @param reader A BufferedReader for reading template document.
      * @exception java.io.IOException If an I/O error occurs.
      * @exception TemplateCreationException If can't create Template
      */
   public Template(BufferedReader reader) throws IOException, TemplateCreationException{
      String line;
      while ((line = reader.readLine()) != null){
         line = line.trim();
         int match;
         String name = "";
         if ((match = line.indexOf("<t:")) != -1){
            name = line.substring(match+3, line.indexOf(">", match));
            value.append("{v:").append(name).append("}");
            variables.put(name, new StringBuffer());
            Template template = new Template(reader);
            if (!variables.containsKey(template.closeTag))
                 throw new TemplateCreationException(name, template.closeTag);
            templates.put(name, template);
         }else{            
            if ((match = line.indexOf("</t:")) != -1) {
               closeTag = line.substring(match+4, line.indexOf(">", match));
               break;
            }
            value.append(line).append("\n");
         }
         
      }
   }
     
       /**
       * Removing of all assigned variables<p>
       *
       * @return Cleared Template
       */
   public Template clear(){
       HashMap variables = new HashMap();
       Iterator iterator = templates.keySet().iterator();  
       while(iterator.hasNext())
          variables.put((String)iterator.next(), new StringBuffer());
       return new Template(variables, templates, value);
   }
 
      /**
      * Return Template by name<p>
      *
      * @param name A name of variable.
      * @return Template by name
      */
   public Template get(String name){
      return ((Template)templates.get(name)).clear();
   }
     
      /**
      * All occurrence of variables name of the template get replaced by the same value.<br>
      * Return preview value<p>
      *
      * @param name A name of variable.
      * @param value A value of variable.
      * @return preview value of variable name
      */
   public StringBuffer set(String name, Object value){
      return (StringBuffer)variables.put(name, new StringBuffer().append(value));
   }
   
      /**
      * Append value to the name variable.<br>
      * Return preview value<p>
      *
      * @param name A name of variable.
      * @param value A value of variable.
      * @return preview value of variable name
      */
   public StringBuffer append(String name, Object value){
      if (!variables.containsKey(name))
           return set(name, value);
      return ((StringBuffer)variables.get(name)).append(value);
   }

   /**
    * Append values to all Templates, starting with name + '.'.<p>
    * 
    * @param name The beginning of the name of the templates.
    * @param value Map of values.
    */
   public void loopAppend(String name, Hashtable value){
   	  String start = name + '.';
   	  for ( Enumeration e = templates.keys(); e.hasMoreElements(); ) {
   	  	String key = (String)e.nextElement();
   	  	
   	  	if ( key.startsWith( start ) || key.equals( name ) ) {
   	  		Template t = get( key );
   	  		for ( Enumeration v = value.keys(); v.hasMoreElements(); ) {
   	  			Object vKey = v.nextElement();
   	  			t.set( vKey.toString(), value.get( vKey ) );
   	  		}
   	  		append( key, t );
   	  	}
   	  	
   	  }
   	
   }
   
      /**
      *
      * @return a String representation of Template
      */
   public String toString(){
      StringBuffer value = new StringBuffer(this.value.toString());
      Iterator iterator = variables.keySet().iterator();    

      while(iterator.hasNext()){
         String key = (String)iterator.next();
         StringBuffer variable = (StringBuffer)variables.get(key);
         key = "{v:" + key + "}";
         int 
            begin, 
            length = key.length(), 
            end = value.length();
         while ((begin = value.toString().lastIndexOf(key, end)) != -1){
            value = value.replace(begin, begin + length, variable.toString());
            end = begin;
         }
      }
      return value.toString();
   }
   
}
