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

public class HtmlUnitTest02BlowfishLoading {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {

		String file = new File("js/blowfish/Blowfish.js").getAbsolutePath();
		String htmlCode = "<!DOCTYPE html><html><script src='file:/"+file+"'></script></html>";
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
		
		String js = "var bf = new Blowfish('12345678');\n"
//        		+ "bf.base64Encode(bf.encrypt('abcdefgh'));";
		+ "bf.base64Encode(bf.encrypt('abcdefgh'));";
		
		
		Object res = webClient.getJavaScriptEngine().execute(page, js, "Test", 0);
		System.out.println(res);
		
		webClient.close();
	}

}
