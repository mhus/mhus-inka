package de.mhu.com.morse.net;

import java.io.IOException;
import java.io.OutputStream;

public interface IProtocol {

	public void encodeMsg(IMessage msg, OutputStream os ) throws IOException;
	public boolean hasMessage();
	public IMessage nextMessage();
	public void write(byte[] bs, int i, int j);
	public IMessage createMessage();
}
