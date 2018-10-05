package de.mhus.inka.tryjavascript;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.XHtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

// http://htmlunit.sourceforge.net/faq.html

public class HtmlUnitTest03Enhanced {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		String file1 = new File("js/blowfish/Blowfish.js").getAbsolutePath();
		String file2 = new File("js/forge/forge.js").getAbsolutePath();
//		String file3 = new File("js/forge/prime.worker.js").getAbsolutePath();
		String file4 = new File("js/pem.js").getAbsolutePath();

		String htmlCode = "<!DOCTYPE html><html>"
				+ "<script src='file:/"+file1+"'></script>"
				+ "<script src='file:/"+file2+"'></script>"
//				+ "<script src='file:/"+file3+"'></script>"
				+ "<script src='file:/"+file4+"'></script>"
				+ "</html>";
		
		StringWebResponse response = new StringWebResponse(htmlCode, new URL("http://htmlunit.sourceforge.net//test.html"));
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
		webClient.getOptions().setThrowExceptionOnScriptError(true);
				
		webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
			
			@Override
			public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
				System.out.println("timeoutError");
			}
			
			@Override
			public void scriptException(HtmlPage page, ScriptException scriptException) {
				System.out.println("scriptException: " + scriptException);
			}
			
			@Override
			public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
				System.out.println("malformedScriptURL: " + url + " " + malformedURLException);
			}
			
			@Override
			public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
				System.out.println("loadScriptError: " + scriptUrl + " " + exception);
			}
		});
		
		XHtmlPage page = HTMLParser.parseXHtml(response, webClient.getCurrentWindow());

        {
	        	System.out.println(">>> blowfish encode");
	        	String js = "var bf = new Blowfish('12345678');\n" +
	        			"bf.base64Encode(bf.encrypt('abcdefgh'));";
	        	
	        	System.out.println(js);
			System.out.println("----");
			Object res = webClient.getJavaScriptEngine().execute(page, js, "Test", 0);
			System.out.println(res);
        }

//		{
//			System.out.println(">>> forge cretae rsa key");
//			String js = "" +
//					"		var keylen = 1024;\n" + 
//					"		var keys = forge.pki.rsa.generateKeyPair(keylen);\n" + 
//					"		var privateKeyP12Pem = forge.pki.privateKeyToPem(keys.privateKey);\n" + 
//					"		var publicKeyP12Pem = forge.pki.publicKeyToPem(keys.publicKey);\n" + 
//					"publicKeyP12Pem;";
//			
//			System.out.println(js);
//			System.out.println("----");
//			Object res = webClient.getJavaScriptEngine().execute(page, js, "Test", 0);
//			System.out.println(res);
//		}

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
    				+ "bf.base64Encode(encrypted);";
			System.out.println(js);
			System.out.println("----");
			Object res = webClient.getJavaScriptEngine().execute(page, js, "Test", 0);
			System.out.println(res);
    }
    {
		System.out.println(">>> decrypt rsa");
		String js = ""
				+ "var pki = forge.pki;\n" + 
				  "var privateKey = pki.privateKeyFromPem(\"-----BEGIN PRIVATE KEY-----\\n"
				+ "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALH2iXkUz54lix7ohADB7bn+iViJdw/7AgYHRflhE6TkRciqPlGuqqnsTBzOM0CCkh+UVfho+iCd8gZTzvzIU7EjVD7vXdn1XnMqsOEs+oKKna2HkIGD4Ly/nisBODnsPEg37/kHz0ABRGGXc9cQMJaqTzYQuWqF0m7ciTNUdmgVAgMBAAECgYBKwa+aB133pvm78ByTXOOKAByd7pDvS5fcBG4mhdsEp0eRNcKb+W2Dl9mZOB1bef37+gnrId4AmZizg70tftF4oW2Py8zzis1GBnV4oD0Einq+JG2t7ccrlczZMHR6o+hy7uoR6MYOuUq8MIjqg2eiIQvEWGb1X3k/DusVMpCDwQJBAO2WYxyUXwDVBlOBpeoCB9FYmthP1BXL7wI0il3RDjyITCX+IKsE3MNQRSXroOLC0KCg5lLnHwv6ruINAJVHCu0CQQC/wTtsKCsUXmtMp0oWOjc30Qjybg2RS6Lc24yKWs+8GQTo9tcgjWQoy0/pH/Xv9h57qxmG7U0SQ+3NWMSmi6TJAkBvzlq741LF+HmuqI9kjSoSqWjNLNv4mezTE9idh7j0YYu8QVgsQvCE9WxlCPrAW7+EJ9Pb5anloEXWWRoSJcmNAkEApK/ytxtwlFfbU8RBrwFktz2Cr6OxZ15Mi97Lv2/rBiN+wg5uCPkmUpr7EL9wXB1HZPM4Q1e/X7aToE9i5Z79cQJBAMb7nRekp+PZL7anyuW05JEQFbHO6LakoBJO5DD/WO5iy9+oiPukvsYEOlKM2cnyHrWurH4yhktIDx2VWzMJ4Ss=\\n-----END PRIVATE KEY-----\");\n" + 
				"var decrypted = privateKey.decrypt(encrypted, 'RSA-OAEP');\n" 
				+ "decrypted;";
		System.out.println(js);
		System.out.println("----");
		Object res = webClient.getJavaScriptEngine().execute(page, js, "Test", 0);
		System.out.println(res);
    }
		
		webClient.close();
	}

}
