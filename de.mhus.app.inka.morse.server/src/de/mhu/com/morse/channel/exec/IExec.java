package de.mhu.com.morse.channel.exec;

import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;

public interface IExec {

	public Object[] execute(Exec exec, LinkedList<Object> attr) throws ExecException, MorseException; 

}
