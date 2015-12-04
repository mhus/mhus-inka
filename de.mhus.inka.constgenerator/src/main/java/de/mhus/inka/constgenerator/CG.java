package de.mhus.inka.constgenerator;

import japa.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.TreeSet;

public class CG {

	public static void main(String[] args) throws FileNotFoundException, ParseException {
		
//		File f = new File("src/de/mhus/inka/sourcecodegenerator/SCGFile.java");
//		SCGFile p = new SCGFile(new FileInputStream(f));
//		
//		TreeSet<String> names = new TreeSet<String>();
		
		if (args.length < 1) {
			System.out.println("Usage: <source directory> [<destination directory>]");
		}
		
		File f = new File(args[0]);
		Crawler crawler = new Crawler(f);
		if (args.length > 1) {
			f = new File(args[1]);
			crawler.setDestination(f);
		}
		crawler.doCrawl();
		
	}
	
}
