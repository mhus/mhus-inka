package de.mhus.inka.tryjavascript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class RhinoTest02BlowfishLoading {

	public static void main(String[] args) throws Exception {
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        
        // load js
        engine.eval(new java.io.FileReader("js/blowfish/Blowfish.js"));
        
        // evaluate JavaScript code from String
        
        
        engine.eval("var bf = new Blowfish('12345678');\n"
        		+ "var a = bf.encrypt('abcdefgh');\n"
        		+ "print(bf.base64Encode(a));");
    }

	
}
