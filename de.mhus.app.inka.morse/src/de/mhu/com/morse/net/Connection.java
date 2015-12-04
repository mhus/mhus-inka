package de.mhu.com.morse.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import de.mhu.com.morse.mql.IQuery;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class Connection extends AfPlugin {

	private Client client;
	private int port = 6666;
	private String host = "localhost";
	
	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub

	}

	protected void apEnable() throws AfPluginException {
		try {
			IThreadArray tea = (IThreadArray)getSinglePpi( IThreadArray.class );
			SocketAddress address = new InetSocketAddress( host, port );
			SocketChannel socket = SocketChannel.open( address );
			client = tea.addNewConnection( socket, new ProtocolBin() );
		} catch ( Exception e ) {
			throw new AfPluginException( 1, e );
		}
	}

	protected void apInit() throws Exception {
	}

	public Client getClient() {
		return client;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
