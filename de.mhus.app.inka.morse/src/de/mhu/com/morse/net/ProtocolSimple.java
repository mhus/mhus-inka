/***********************************************************
GNU Lesser General Public License

JMorseCore - Permanent Connection Messaging Service
Copyright (C) 2004-2005 Rise s.a.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
************************************************************/
/*
 * Created on 2005-08-03
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import de.mhu.lib.io.ByteBuffer;

public class ProtocolSimple {
	
}

//public class ProtocolSimple implements IProtocol
//{
//	private static final byte PARAMETER_SEPERATOR =',';
//	private static final byte END_OF_MESSAGE = '\n';
//	// private static final char JOKER = '#';
//	
//	private LinkedList waitingMsg = new LinkedList();
//	private ByteBuffer actualMsg  = null;
//	
//	public ProtocolSimple() {
//	}
//	
//	/**
//	 * @param msg
//	 * @return
//	 * @throws IOException 
//	 */
//	public void encodeMsg(IMessage msg, OutputStream os ) throws IOException {
//	
//	    for (int i=0;i<msg.getCount();i++)
//	    {
//	    	if ( i != 0 ) os.write( ProtocolSimple.PARAMETER_SEPERATOR );
//	    	msg.write( i, os );
//	    }
//	    os.write( ProtocolSimple.END_OF_MESSAGE );
//	    
//	}
//	/*
//	private static String decodeParam( String in ) {
//		
//		if ( in.indexOf( JOKER ) < 0 ) return in;
//		int len = in.length();
//		StringBuffer sb = new StringBuffer();
//		for ( int j = 0; j < len; j++ ) { // could be optimized ..... start at pos
//			char c = in.charAt(j);
//			if ( c == JOKER ) {
//				
//				// System.out.println( "FOUND CODE: " + Integer.parseInt( in.substring( j+1, j+5 ), 16 ) + ": " + (char)Integer.parseInt( in.substring( j+1, j+5 ), 16 ) + ": " + (int)(char)Integer.parseInt( in.substring( j+1, j+5 ), 16 ) );
//				sb.append( (char)Integer.parseInt( in.substring( j+1, j+5 ), 16 ) );
//				
//				j+=4;
//			} else
//				sb.append( c );
//		}
//		return sb.toString();
//		
//	}
//	*/
//	public boolean hasMessage()
//	{
//		return !waitingMsg.isEmpty();
//	}
//	
//	
//	public IMessage nextMessage()
//	{
//		return (IMessage)waitingMsg.removeFirst();
//	}
//
//	public void write(byte[] bs, int i, int j) {
//		
//		j = j + i;
//		while ( i < j ) {
//			
//			if ( actualMsg == null )
//				actualMsg = new ByteBuffer();
//		
//			boolean found = false;
//			for ( int k = i; k < j; k++ )
//				if ( bs[k] == END_OF_MESSAGE ) {
//					actualMsg.append( bs, i, k-i );
//					waitingMsg.addLast( new MyMessage( actualMsg ) );
//					actualMsg = null;
//					i = k+1;
//					found = true;
//					break;
//				}
//			if ( ! found ) {
//				actualMsg.append( bs, i, j - i );
//				return;
//			}
//		}
//	}	
//	
//		
//	class MyMessage implements IMessage {
//
//		private byte[] buffer;
//		private Vector indexes = new Vector();
//		private Client client;
//		private Hashtable stringCache = new Hashtable();
//		private LinkedList shifted;
//
//		MyMessage() {
//			
//		}
//		
//		MyMessage( ByteBuffer bytes ) {
//			buffer = bytes.toByte();
//			indexes.add( new Integer(0) );
//			for ( int i = 0; i < buffer.length; i++ )
//				if ( buffer[i] == PARAMETER_SEPERATOR )
//					indexes.add( new Integer( i+1 ) );
//			indexes.add( new Integer( buffer.length + 1 ) );
//		}
//		
//		public Client getClient() {
//			return client;
//		}
//
//		public int getCount() {
//			return indexes.size()-1;
//		}
//
//		public int getInteger(int index)
//		{
//			return Integer.parseInt(getString(index));
//		}
//		
//		public long getLong(int index)
//		{
//			return Long.parseLong(getString(index));
//		}
//		
//		public double getDouble(int index)
//		{
//			return Double.parseDouble(getString(index));
//		}
//
//		public int getSize(int index) {
//			if ( shifted != null )
//				return shifted.size() + ((Integer)indexes.get( index+1 )).intValue() - ((Integer)indexes.get( index )).intValue() - 1;
//				
//			return ((Integer)indexes.get( index+1 )).intValue() - ((Integer)indexes.get( index )).intValue() - 1;
//		}
//
//		public InputStream getStream(int index) {
//			if ( shifted != null && shifted.size() != 0 ) {
//				if ( index < shifted.size() )
//					return new ByteArrayInputStream( (byte[])shifted.get( index ) );
//				index = index - shifted.size();
//			}
//			
//			int start = ((Integer)indexes.get( index )).intValue();
//			int stop  = ((Integer)indexes.get( index + 1 )).intValue()-1;
//			return new ByteArrayInputStream( buffer, start, stop - start );
//		}
//
//		public String getString(int index) {
//			if ( shifted != null && shifted.size() != 0 ) {
//				if ( index < shifted.size() )
//					return new String( (byte[])shifted.get( index ) );
//				index = index - shifted.size();
//			}
//			
//			Integer start = (Integer)indexes.get( index );
//			String out = (String)stringCache.get( start );
//			if ( out == null ) {
//				int stop  = ((Integer)indexes.get( index + 1 )).intValue()-1;
//				out = new String( buffer, start.intValue(), stop - start.intValue());
//				stringCache.put( start, out );
//			}
//			return out;
//		}
//
//		public void setClient(Client in) {
//			client = in;
//		}
//
//		public void shiftParameter() {
//			if ( shifted != null && shifted.size() != 0 ) {
//				shifted.removeFirst();
//				return;
//			}
//			
//			indexes.remove( 0 );
//		}
//
//		public void write(int index, OutputStream os) throws IOException {
//			if ( shifted != null && shifted.size() != 0 ) {
//				if ( index < shifted.size() ) {
//					os.write( (byte[])shifted.get( index ) );
//					return;
//				}
//				index = index - shifted.size();
//			}
//			
//			int start = ((Integer)indexes.get( index )).intValue();
//			int stop  = ((Integer)indexes.get( index + 1 )).intValue()-1;
//			os.write( buffer, start, stop - start );
//		}
//		
//		public String toString()
//		  {
//		    StringBuffer ret = new StringBuffer();
//		    for (int i=0;i<getCount();i++) {
//		    	if ( i != 0 ) ret.append( ',' );
//		      ret.append( getString(i) );
//		    }
//		    return ret.toString();
//		  }
//
//		public void unshift(String in) {
//			if ( shifted == null )
//				shifted = new LinkedList();
//			shifted.addFirst( in.getBytes() );
//		}
//
//		public void append(String in) {
//			shifted.add( in.getBytes() );
//		}
//
//		public void append(int in) {
//			append( String.valueOf( in ) );
//		}
//
//		public void append(long in) {
//			append( String.valueOf( in ) );
//		}
//
//		public void append(double in) {
//			append( String.valueOf( in ) );
//		}
//
//		public void unshift(int in) {
//			unshift( String.valueOf( in ) );
//		}
//
//		public void unshift(long in) {
//			unshift( String.valueOf( in ) );
//		}
//
//		public void unshift(double in) {
//			unshift( String.valueOf( in ) );
//		}
//
//		public void append(byte[] in) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void unshift(byte[] in) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
//
//
//	public IMessage createMessage() {
//		return new MyMessage();
//	}
	
//}
