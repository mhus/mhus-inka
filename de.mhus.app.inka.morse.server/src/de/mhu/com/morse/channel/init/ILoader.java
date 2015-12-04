package de.mhu.com.morse.channel.init;

import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.utils.Properties;

public interface ILoader {

	public void init(String schema) throws MorseException;

	public LinkedList<String> getTypes() throws MorseException;

	public LinkedList<String> getIds(String type) throws MorseException;

	public void fill(String type, String id, Properties value) throws MorseException;

}
