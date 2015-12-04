package de.mhu.com.morse.obj;

import java.util.Map;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.utils.MorseException;

public interface IFunctionConfig {

	public void initFunction( Map<String, String> config, String accessAcl, String name ) throws MorseException;
	
}
