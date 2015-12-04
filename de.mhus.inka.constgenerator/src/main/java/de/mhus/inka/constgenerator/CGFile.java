package de.mhus.inka.constgenerator;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.mhus.inka.constgenerator.CGEntry.TYPE;


// http://code.google.com/p/javaparser/source/browse/trunk/JavaParser/src/japa/parser/ast/visitor/VoidVisitor.java

@GenerateConstFile
public class CGFile {

	private LinkedList<CGEntry> list = new LinkedList<CGEntry>();
	private CompilationUnit cu;
	
	private CGEntry currentClass = null;
	private CGEntry currentMethod = null;
	private CGEntry mainClass = null;
	private CGEntry pack = null;
	private boolean debug = false;

	public CGFile(InputStream in) throws ParseException {

        cu = JavaParser.parse(in);
        // parse
        
	}
	
	public void doParse() {
		list.clear();
		currentClass = null;
		currentMethod = null;
		pack = null;
		mainClass = null;
		new MethodVisitor().visit(cu, null);
	}
	
	@Hidden
	public boolean shouldProceed() {
		return mainClass != null && mainClass.hasAnnotation("GenerateConstFile");
	}
	
	
	
		
	private class MethodVisitor extends VoidVisitorAdapter<Object> {

		@Override
		public void visit(PackageDeclaration n, Object arg) {
			if (pack == null) {
				pack = new CGEntry(CGEntry.TYPE.PACKAGE, null, null, n.getName().toString());
				if (isDebug()) System.out.println("P:" + pack);
				list.add(pack);
			}
	    	super.visit(n, arg);
		}
		 	
	    @Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
	    	
	    	currentClass = new CGEntry(CGEntry.TYPE.CLASS, currentClass, null, n.getName());
	    	if (mainClass == null)  {
	    		mainClass = currentClass;
	    		mainClass.setMainClass(true);
	    		mainClass.setPackage(pack);
	    	}
	    	if (isDebug()) System.out.println("C:" + currentClass);
	    	
			list.add(currentClass);
			
			List<AnnotationExpr> annos = n.getAnnotations();
	    	if (annos != null) {
	    		for (AnnotationExpr anno : annos) {
	    			if (isDebug()) System.out.println("A:" + anno.getName());
	    			currentClass.addAnnotation(new CGEntry(TYPE.ANNOTATION, currentClass, currentMethod, anno.getName().getName()));
	    		}
	    	}
	    	
	    	super.visit(n, arg);
	    	if (currentClass != null ) currentClass = currentClass.getClazz();
	    	
	    }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this 
            // CompilationUnit, including inner class methods
        	currentMethod = new CGEntry(TYPE.METHOD, currentClass, currentMethod, n.getName());
        	if (isDebug()) System.out.println("M:" + currentMethod);
        	
            list.add(currentMethod);
            
	    	List<AnnotationExpr> annos = n.getAnnotations();
	    	if (annos != null) {
	    		for (AnnotationExpr anno : annos) {
	    			if (isDebug()) System.out.println("A:" + anno.getName());
	    			currentMethod.addAnnotation(new CGEntry(TYPE.ANNOTATION, currentClass, currentMethod, anno.getName().getName()));
	    		}
	    	}

	    	
	    	super.visit(n, arg);
	    	if (currentMethod != null) currentMethod = currentMethod.getMethod();
        }
        
        @Override
		public void visit(FieldDeclaration n, Object arg) {
        	
        	for (VariableDeclarator var : n.getVariables()) {
	        	CGEntry currentVariable = new CGEntry(TYPE.VARIABLE, currentClass, currentMethod, var.getId().getName());
	        	if (isDebug()) System.out.println("V:" + currentVariable);
	        	
	        	list.add(currentVariable);
	        	
		    	List<AnnotationExpr> annos = n.getAnnotations();
		    	if (annos != null) {
		    		for (AnnotationExpr anno : annos) {
		    			if (isDebug()) System.out.println("A:" + anno.getName());
		    			currentVariable.addAnnotation(new CGEntry(TYPE.ANNOTATION, currentClass, currentMethod, anno.getName().getName()));
		    		}
		    	}
	    	}

        	super.visit(n, arg);
        }

    }




	public List<CGEntry> getEntries() {
		return list;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public CGEntry getMainClass() {
		return mainClass;
	}

	public String getPackage() {
		return pack.toString();
	}
		
}
