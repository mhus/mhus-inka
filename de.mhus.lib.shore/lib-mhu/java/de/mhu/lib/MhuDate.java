/*
 *  mhu-lib Generic Application Framework
 *  Copyright (C) 2003  Mike Hummel
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
 *  WWW: http://code.mikehummel.de/
 *  E-mail: code@mikehummel.de
 */

package de.mhu.lib;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author hummel
 * 

 * 
 */
public class MhuDate {

	private static final DateFormat formater = DateFormat.getInstance();

	public static Date create( String _date ) {
		if ( _date == null ) return null;
		try {
			return formater.parse( _date );
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String toString( Date _date ) {
		return formater.format( _date );
	}
	
}
