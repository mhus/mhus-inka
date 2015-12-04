package de.mhu.com.morse.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.mhu.com.morse.utils.MorseException;


public interface IMessage {

	public int getCount();

	public void shiftParameter();
	
	public void write(int index, OutputStream os) throws IOException;
	
	public void setClient(Client client);
	public Client getClient();
	
	public int		 	getSize( int index );
	
	public String 		getString(int index);
	public int 			getInteger(int index);
	public long 		getLong(int index);
	public double 		getDouble(int index);
	public byte 		getByte( int index );
	public InputStream 	getStream( int index );
	public byte[]       getByteArray( int index ) throws MorseException;

	public String 		shiftString();
	public int 			shiftInteger();
	public long 		shiftLong();
	public double 		shiftDouble();
	public byte 		shiftByte();
	public InputStream 	shiftStream();
	public byte[]       shiftByteArray() throws MorseException;
	
	public void unshift(String in);
	public void unshift( int in );
	public void unshift( long in );
	public void unshift( double in );
	public void unshift( byte[] in );
	public void unshift( byte in );
	
	public void append(String in);
	public void append( int in );
	public void append( byte in );
	public void append( long in );
	public void append( double in );
	public void append( byte[] in );

	public long getCalculatedByteCount();
	
	
}
