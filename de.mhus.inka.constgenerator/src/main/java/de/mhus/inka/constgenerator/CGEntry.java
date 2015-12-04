package de.mhus.inka.constgenerator;

import java.util.LinkedList;

public class CGEntry {

	public enum TYPE {METHOD,VARIABLE, PACKAGE, CLASS, ANNOTATION};
	
	private TYPE type;
	private CGEntry clazz;
	private CGEntry method;
	private CGEntry pack;
	private String name;
	private LinkedList<CGEntry> annotations = new LinkedList<CGEntry>();
	private boolean mainClass;
	
	public CGEntry(TYPE type, CGEntry clazz, CGEntry method, String name) {
		this.type = type;
		this.clazz = clazz;
		this.method = method;
		this.name = name;
	}
	
	public TYPE getType() {
		return type;
	}
	public void setType(TYPE type) {
		this.type = type;
	}
	public CGEntry getClazz() {
		return clazz;
	}
	public void setClazz(CGEntry clazz) {
		this.clazz = clazz;
	}
	public CGEntry getMethod() {
		return method;
	}
	public void setMethod(CGEntry method) {
		this.method = method;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void addAnnotation(CGEntry anno) {
		annotations.add(anno);
	}
	
	public CGEntry[] getAnnotations() {
		return annotations.toArray(new CGEntry[annotations.size()]);
	}
	
	public boolean hasAnnotation(String name) {
		for (CGEntry entry : annotations)
			if (entry.getName().equals(name)) return true;
		return false;
	}
	
	@Override
	public String toString() {
		if (method != null) return method + "." + name;
		if (clazz != null && !clazz.isMainClass()) return clazz + "." + name;
		if (pack != null) return pack + "." + name;
		return name;
	}

	public boolean isMainClass() {
		return mainClass;
	}

	public void setMainClass(boolean mainClass) {
		this.mainClass = mainClass;
	}

	public CGEntry getPackage() {
		return pack;
	}

	public void setPackage(CGEntry pack) {
		this.pack = pack;
	}

	public boolean shouldProceed() {
		return !hasAnnotation("Hidden");
	}

	public String getId() {
		String n = name;
		if (type == TYPE.METHOD) {
			if (n.startsWith("get") || n.startsWith("set")) n = n.substring(3);
			if (n.startsWith("is") || n.startsWith("do")) n = n.substring(2);
		}
		n = n.replace('.', '_');
		if (method != null) return method.getId() + "_" + n;
		if (clazz != null && !clazz.isMainClass()) return clazz.getId() + "_" + n;
		if (pack != null) return pack.getId() + "_" + n;
		return n;
	}

}
