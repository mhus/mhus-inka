package de.mhus.inka.tryjavascript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

// https://docs.oracle.com/javase/7/docs/technotes/guides/scripting/programmer_guide/

public class RhinoTest01 {

	public static void main(String[] args) throws Exception {
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        // evaluate JavaScript code from String
        engine.eval("print('Hello, World')");
    }

	
	
}
