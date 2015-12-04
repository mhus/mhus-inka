package de.mhus.inka.constgenerator;

import japa.parser.ParseException;

import java.io.FileNotFoundException;

public class Test {

	public static void main(String[] args) throws FileNotFoundException, ParseException {
		CG.main(new String[] {"src"});
	}
}
