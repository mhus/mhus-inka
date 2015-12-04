package de.mhu.com.morse.net;

import java.nio.channels.SocketChannel;

import de.mhu.lib.plugin.IAfPpi;

public interface IThreadArray extends IAfPpi {

	public Client addNewConnection(SocketChannel clientChannel, IProtocol protocol );

}
