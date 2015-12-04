package de.mhus.inka.constgenerator;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class FileGenerator {

	private CGFile file;
	private TreeMap<String,String> names;

	public FileGenerator(CGFile file) {
		this.file = file;
	}
	
	public void doParse() {
		names = new TreeMap<String, String>();
		
		// TODO parameterize
		for (CGEntry entry : file.getEntries()) {
			
			String name = entry.toString().replace('.', '_');
			String id = entry.getId();
			
			names.put(entry.getType().toString().toUpperCase() + "_" + name.toUpperCase(), name);
			names.put("" + id.toUpperCase(), id);
			
//			names.put(entry.getType().toString().toUpperCase() + "_" + name.toUpperCase() + "_u", name.toUpperCase());
			names.put("" + id.toUpperCase() + "_upper", id.toUpperCase());

//			names.put(entry.getType().toString().toUpperCase() + "_" + name.toUpperCase() + "_l", name.toLowerCase());
			names.put("" + id.toUpperCase() + "_lower", id.toLowerCase());
			
		}
		
	}
	
	public void doGenerate(PrintWriter out) {
		
		out.println("package " + file.getPackage() + ";");
		out.println();
		out.println("// auto generated");
		out.println("public class " + file.getMainClass().getName() + "Const {");
		out.println();
		for (Map.Entry<String, String> entry : names.entrySet()) {
			out.println("  public static final String " + entry.getKey() + " = \"" + entry.getValue().replaceAll("\"", "\\\"") + "\";" );
		}
		out.println();
		out.println("}");
		
	}
	
}
