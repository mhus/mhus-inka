package de.mhu.com.morse.channel;

import java.util.Date;
import java.util.Map;

import de.mhu.com.morse.utils.MorseException;

public interface IChannelDriverServer extends IChannelDriver {

	public IChannelServer createChannel( IConnectionServer pConnection ) throws MorseException;
	
	public void setChannelFeatures( Map<String,String> features ) throws MorseException;

	public void setChannel(String in);
	
	public void setAccessAcl( String in );

	public String toValidDate(Date date);
	
	public boolean canTransaction();
	
}
