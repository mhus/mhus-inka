package de.mhu.com.morse.channel;

import java.util.Iterator;

import de.mhu.com.morse.utils.MorseException;

public interface IQueryWhereFunction {

	public String getSingleResult(Object[] attr) throws MorseException;

	public Iterator<String> getRepeatingResult(Object[] attr) throws MorseException;

}
