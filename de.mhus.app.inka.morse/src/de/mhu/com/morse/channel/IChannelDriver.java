package de.mhu.com.morse.channel;

import java.util.Iterator;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.IAfPpi;
import de.mhu.lib.utils.Properties;

/**
 * The channelDriver is used by IConnection to create a 
 * new channel by specified type. 
 * 
 * @author mike
 *
 */

public interface IChannelDriver extends IAfPpi {

	public static final String CT_DB = "db";
	public static final String CT_FS = "fs";
	public static final String C_SYS = "sys";
	public static final String C_INIT = "init";
	public static final String C_DB = "db";
	public static final String C_FS = "fs";
	public static final String C_EXEC = "exec";

	public static final String CT_DISTRIBUTOR = "*";
	public static final String CT_EXEC = "exec";
	public static final String CT_IDX = "idx";
	
	
	public IChannel createChannel( IConnection pConnection ) throws MorseException;
	public String getName();
	public String getType();
	public Iterator<String> getObjectIds();
	public Properties getFeatures();
	public void setFeatures( Properties features );
	
}
