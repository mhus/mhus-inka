package de.mhus.inka.constgenerator;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Crawler {

	private File source;
	private File destination;
	private boolean debug;

	public Crawler(File dir) {
		setSource(dir);
		setDestination(dir);
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public File getDestination() {
		return destination;
	}

	public void setDestination(File destination) {
		this.destination = destination;
	}
	
	public void doCrawl() {
		crawl(source,destination);
	}
	
	protected void crawl(File src, File dest) {
		for (File f : src.listFiles()) {
			if (f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")) {
				crawl(f, new File(dest, f.getName()));
			} else {
				try {
					parse(f, dest);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void parse(File src) throws IOException, ParseException {
		parse(src, src.getParentFile());
	}
	
	public void parse(File src, File dst) throws IOException, ParseException {
		
		if (!src.isFile() || !src.getName().endsWith(".java") || src.getName().endsWith("Cons.java") || !dst.isDirectory()) return;
		
		FileInputStream fis = new FileInputStream(src);
		CGFile file = new CGFile(fis);
		fis.close();
		
		FileGenerator generator = new FileGenerator(file);
		file.setDebug(isDebug());
		file.doParse();
		if (!file.shouldProceed()) return;
		System.out.println(src.getPath());
		generator.doParse();
		
		if (!dst.exists()) dst.mkdirs();
		FileWriter fw = new FileWriter(new File(dst, file.getMainClass().getName() + "Const.java") );
		generator.doGenerate(new PrintWriter(fw));
		fw.close();
		
		
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
