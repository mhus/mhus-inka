package de.mhu.com.morse.aaa;

import java.util.Map;

import de.mhu.com.morse.channel.IConnection;

public interface IAuth {

	public byte[] getQuestion();

	public void setAnswer(byte[] string);

	public boolean isAllow();

	public void init( IConnection con, Map<String, String> attr);

}
