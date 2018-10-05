package de.mhus.inka.tryjavascript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class RhinoTest03Enhanced {

	public static void main(String[] args) throws Exception {
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        
        // load js
        engine.eval(new java.io.FileReader("js/blowfish/Blowfish.js"));
        engine.eval(new java.io.FileReader("js/forge/forge.js"));
//        engine.eval(new java.io.FileReader("js/forge/prime.worker.js"));
        engine.eval(new java.io.FileReader("js/pem.js"));

        // evaluate JavaScript code from String
        {
	        	System.out.println(">>> blowfish encode");
	        	String js = "var bf = new Blowfish('12345678');\n" +
	        			"print(bf.base64Encode(bf.encrypt('abcdefgh')));";
	        	
	        	System.out.println(js);
    			System.out.println("----");
	        	engine.eval(js);
        }
        {
        		System.out.println(">>> forge cretae rsa key");
			String js = "" +
					"		var keylen = 1024;\n" + 
					"		var keys = forge.pki.rsa.generateKeyPair(keylen);\n" + 
					"		var privateKeyP12Pem = forge.pki.privateKeyToPem(keys.privateKey);\n" + 
					"		var publicKeyP12Pem = forge.pki.publicKeyToPem(keys.publicKey);\n" + 
					"print(publicKeyP12Pem);";
			System.out.println(js);
  		    System.out.println("----");
	        engine.eval(js);
        }
        {
        		System.out.println(">>> encrypt rsa");
        		String js = ""
        				+ "var pki = forge.pki;\n" + 
        				"var publicKey = pki.publicKeyFromPem(\"-----BEGIN PUBLIC KEY-----\\n"
        				+ "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCx9ol5FM+eJY\\n" + 
        				"se6IQAwe25/olYiXcP+wIGB0X5YROk5EXIqj5Rrqqp7EwczjNA\\n" + 
        				"gpIflFX4aPognfIGU878yFOxI1Q+713Z9V5zKrDhLPqCip2th5\\n" + 
        				"CBg+C8v54rATg57DxIN+/5B89AAURhl3PXEDCWqk82ELlqhdJu\\n" + 
        				"3IkzVHZoFQIDAQAB\\n"
        				+ "-----END PUBLIC KEY-----\");\n" + 
        				"var encrypted = publicKey.encrypt(\"HelloWorld\", 'RSA-OAEP');\n" 
        				+ "print(bf.base64Encode(encrypted));";
    			System.out.println(js);
    			System.out.println("----");
    	        engine.eval(js);
        }
        {
    		System.out.println(">>> decrypt rsa");
    		String js = ""
    				+ "var pki = forge.pki;\n" + 
    				  "var privateKey = pki.privateKeyFromPem(\"-----BEGIN PRIVATE KEY-----\\n"
    				+ "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALH2iXkUz54lix7ohADB7bn+iViJdw/7AgYHRflhE6TkRciqPlGuqqnsTBzOM0CCkh+UVfho+iCd8gZTzvzIU7EjVD7vXdn1XnMqsOEs+oKKna2HkIGD4Ly/nisBODnsPEg37/kHz0ABRGGXc9cQMJaqTzYQuWqF0m7ciTNUdmgVAgMBAAECgYBKwa+aB133pvm78ByTXOOKAByd7pDvS5fcBG4mhdsEp0eRNcKb+W2Dl9mZOB1bef37+gnrId4AmZizg70tftF4oW2Py8zzis1GBnV4oD0Einq+JG2t7ccrlczZMHR6o+hy7uoR6MYOuUq8MIjqg2eiIQvEWGb1X3k/DusVMpCDwQJBAO2WYxyUXwDVBlOBpeoCB9FYmthP1BXL7wI0il3RDjyITCX+IKsE3MNQRSXroOLC0KCg5lLnHwv6ruINAJVHCu0CQQC/wTtsKCsUXmtMp0oWOjc30Qjybg2RS6Lc24yKWs+8GQTo9tcgjWQoy0/pH/Xv9h57qxmG7U0SQ+3NWMSmi6TJAkBvzlq741LF+HmuqI9kjSoSqWjNLNv4mezTE9idh7j0YYu8QVgsQvCE9WxlCPrAW7+EJ9Pb5anloEXWWRoSJcmNAkEApK/ytxtwlFfbU8RBrwFktz2Cr6OxZ15Mi97Lv2/rBiN+wg5uCPkmUpr7EL9wXB1HZPM4Q1e/X7aToE9i5Z79cQJBAMb7nRekp+PZL7anyuW05JEQFbHO6LakoBJO5DD/WO5iy9+oiPukvsYEOlKM2cnyHrWurH4yhktIDx2VWzMJ4Ss=\\n-----END PRIVATE KEY-----\");\n" + 
    				"var decrypted = privateKey.decrypt(encrypted, 'RSA-OAEP');\n" 
    				+ "print(decrypted);";
			System.out.println(js);
			System.out.println("----");
	        engine.eval(js);
        }
    }

	/*
encrypted="Zp4uEWP+L63CNtmcnjNiwYED/vpSyHwLI+meU12TGW/7muDYMGQCuMtEGqEe7GskC5pup8y68H2+zkCX8CPSTa0g5EOWMffSfkn/SW8s5QNiTwN7TFrVQ/T0Z5JZ4IdKP2Aw9qcUxsCwaAA1M63ZyAe3eJQv5lRCZXHhDHqzRYQ=";
encrypted = bf.base64Decode(encrypted);
	 */
}
