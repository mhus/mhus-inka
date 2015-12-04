package de.mhu.com.morse.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import de.mhu.lib.io.Bits;
import de.mhu.lib.io.ByteBuffer;
import de.mhu.lib.io.ByteBufferArray;
import de.mhu.com.morse.utils.MorseException;

public class ProtocolBin implements IProtocol {

	public static final int MODE_NEXT = 1;
	public static final int MODE_CNT_1 = 2;
	public static final int MODE_CNT_2 = 3;
	public static final int MODE_VAL = 4;

	public static final byte NEXT_ATTR = 1;
	public static final byte NEXT_MSG_END = 0;
	
	
	int mode = MODE_NEXT;
	int cnt = 0;
	
	private ByteBufferArray actualAttr = null;
	private MyMessage  actualMsg  = new MyMessage();
	private LinkedList waitingMsg = new LinkedList();
	
	public void encodeMsg(IMessage msg, OutputStream os) throws IOException {
		
		for ( int i = 0; i < msg.getCount(); i++ ) {
			int size = msg.getSize( i );
			byte b1 = (byte)(size % 256 - 128);
			byte b2 = (byte)(size / 256 - 128);
			os.write( NEXT_ATTR );
			os.write( b2 );
			os.write( b1 );
			msg.write( i, os );
		}
		os.write( NEXT_MSG_END );
		
	}

	public boolean hasMessage()
	{
		return !waitingMsg.isEmpty();
	}
	
	
	public IMessage nextMessage()
	{
		return (IMessage)waitingMsg.removeFirst();
	}

	public void write(byte[] bs, int offset, int len) {
		
		if ( len == 0 ) return;
		int endOffset = offset + len;
		
		while ( true ) {
			if ( mode == MODE_NEXT ) {
				int next = bs[offset];
				offset++;
				if ( next == NEXT_MSG_END ) {
					if ( actualAttr != null )
						actualMsg.add( actualAttr );
					actualAttr = null;
					waitingMsg.add( actualMsg );
					actualMsg = new MyMessage();
					
				} else
				if ( next == NEXT_ATTR ) {
					
					if ( actualAttr != null )
						actualMsg.add( actualAttr );
					actualAttr = new ByteBufferArray( cnt );
					mode = MODE_CNT_1;
				}
				
			} else
			if ( mode == MODE_CNT_1 ) {
				cnt = ( (int)bs[offset] + 128 ) * 256;
				offset++;
				mode = MODE_CNT_2;
			} else
			if ( mode == MODE_CNT_2 ) {
				cnt = cnt + ( (int)bs[offset] + 128 );
				offset++;
				mode = MODE_VAL;
			} else
			if ( mode == MODE_VAL ) {
				
				int size = len - offset;
				if ( size > cnt ) {
					actualAttr.append( bs, offset, cnt );
					offset=offset+cnt;
					cnt = 0;
				} else {
					actualAttr.append( bs, offset, endOffset - offset );
					cnt = cnt - size;
					offset = len;
				}
				
				if ( cnt <= 0 )
					mode = MODE_NEXT;
			}
				
			if ( offset == len ) return;
			
		}
		
	}

	class MyMessage implements IMessage {

		private Client client;
		private LinkedList<byte[]> attr = new LinkedList<byte[]>();
		private long byteCount = 0;
		
		public Client getClient() {
			return client;
		}

		public int getCount() {
			return attr.size();
		}

		public double getDouble(int index) {
			return Bits.getDouble( (byte[])attr.get( index ), 0 );
		}

		public int getInteger(int index) {
			return Bits.getInt( (byte[])attr.get( index ), 0 );
		}
		
		public long getLong(int index) {
			return Bits.getLong( (byte[])attr.get( index ), 0 );
		}

		public int getSize(int index) {
			return ((byte[])attr.get( index )).length;
		}

		public InputStream getStream(int index) {
			return new ByteArrayInputStream( (byte[])attr.get( index ) );
		}

		public String getString(int index) {
			return new String( (byte[])attr.get( index ) );
		}

		public void setClient(Client pClient) {
			client = pClient;
		}

		public void shiftParameter() {
			byteCount-=((byte[])attr.getFirst()).length;
			attr.removeFirst();
		}

		public void unshift(String in) {
			byte[] b = in.getBytes();
			byteCount+=b.length;
			attr.addFirst( b );
		}

		public void write(int index, OutputStream os) throws IOException {
			os.write( (byte[])attr.get( index ) );
		}
		
		private void add( ByteBufferArray buffer ) {
			byteCount+=buffer.getSize();
			if ( buffer.isCurrentlyFull() )
				attr.add( buffer.getInternalBuffer() );
			else
				attr.add( buffer.toByte() );
		}
		
		public String toString()
  	  	{
			StringBuffer ret = new StringBuffer();
			for (int i=0;i<getCount();i++) {
				if ( i != 0 ) ret.append( ',' );
				ret.append( getVisibleData(i) );
			}
			return ret.toString();
	  }

		private String getVisibleData(int index) {
			byte[] out = (byte[])attr.get( index );
			
			if ( out == null ) return "[null]";
			if ( out.length == 0 ) return "[0]";
			if ( out.length > 100 ) return "[" + out.length + ']';
			
			if ( out.length == 4 && out[0] == 0 )
				return "i:" + String.valueOf( getInteger( index ) );
			if ( out.length == 8 && out[0] == 0 )
				return "l:" + String.valueOf( getLong( index ) );
			return getString( index );
		}

		public void append(String in) {
			if ( in == null ) {
				attr.add( new byte[0] );
				return;
			}
			byte[] b = in.getBytes();
			byteCount+=b.length;
			attr.add( b );
		}

		public void append(int in) {
			byte[] buf = new byte[4];
			Bits.putInt( buf, 0, in );
			byteCount+=4;
			attr.add( buf );
		}

		public void append(long in) {
			byte[] buf = new byte[8];
			Bits.putLong( buf, 0, in );
			byteCount+=8;
			attr.add( buf );
		}

		public void append(double in) {
			byte[] buf = new byte[8];
			Bits.putDouble( buf, 0, in );
			byteCount+=8;
			attr.add( buf );
		}

		public void unshift(int in) {
			byte[] buf = new byte[4];
			Bits.putInt( buf, 0, in );
			byteCount+=4;
			attr.addFirst( buf );
		}

		public void unshift(long in) {
			byte[] buf = new byte[8];
			Bits.putLong( buf, 0, in );
			byteCount+=8;
			attr.addFirst( buf );
		}

		public void unshift(double in) {
			byte[] buf = new byte[8];
			Bits.putDouble( buf, 0, in );
			byteCount+=8;
			attr.addFirst( buf );

		}

		public void append(byte[] in) {
			byteCount+=in.length;
			attr.add( in );
		}

		public void unshift(byte[] in) {
			byteCount+=in.length;
			attr.addFirst( in );
		}

		public void append(byte in) {
			byteCount+=1;
			attr.add( new byte[] { in } );
		}

		public byte getByte(int index) {
			return ((byte[])attr.get( index ))[0];
		}

		public byte shiftByte() {
			byte ret = getByte(0);
			shiftParameter();
			return ret;
		}

		public byte[] getByteArray(int index) throws MorseException {
			return (byte[])attr.get( index );
		}

		public byte[] shiftByteArray() throws MorseException {
			byte[] ret = getByteArray(0);
			shiftParameter();
			return ret;
		}
		
		public double shiftDouble() {
			double ret = getDouble(0);
			shiftParameter();
			return ret;
		}

		public int shiftInteger() {
			int ret = getInteger(0);
			shiftParameter();
			return ret;
		}

		public long shiftLong() {
			long ret = getLong(0);
			shiftParameter();
			return ret;
		}

		public InputStream shiftStream() {
			InputStream ret = getStream(0);
			shiftParameter();
			return ret;
		}

		public String shiftString() {
			String ret = getString(0);
			shiftParameter();
			return ret;
		}
		
		public void unshift(byte in) {
			byteCount+=1;
			attr.addFirst( new byte[] { in } );
		}

		public long getCalculatedByteCount() {
			return byteCount;
		}

	}

	public IMessage createMessage() {
		return new MyMessage();
	}
}
