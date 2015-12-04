package de.mhus.hair.jack.action;

import java.io.PrintStream;

import de.mhus.hair.jack.JackElement;
import de.mhus.lib.MString;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoList;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoOperation;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.annotations.FormElement;
import de.mhus.lib.form.objects.IDynOptionsProvider;
import de.mhus.lib.form.objects.SimpleDynOptionsProvider;

@FormElement("name='cao_dump' title='Dump'")
public class DumpOperation extends CaoOperation implements MForm {

	private CaoList sources;
	private String outputType = "system";
	private boolean binaryAlso;
	private String file;

	@Override
	public void initialize() throws CaoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() throws CaoException {
		
		CaoElement root = sources.getElements().next();
		
		try {
			
			PrintStream out = System.out;
			if (!MString.isEmpty(file)) {
				out = new PrintStream(file);
			}
			
			monitor.beginTask("dump", -1);
			
			if ("system".equals(outputType)) {
				((JackElement)root).getNode().getSession().exportSystemView(((JackElement)root).getNode().getPath(), out, !binaryAlso, false);
			} else
			if ("document".equals(outputType)) {
				((JackElement)root).getNode().getSession().exportDocumentView(((JackElement)root).getNode().getPath(), out, !binaryAlso, false);
			} else
			if ("text".equals(outputType)) {
				dump(root,"", out);				
			}
			monitor.worked(1);
		} catch (Throwable e) {
			monitor.log().i(e);
			throw new CaoException(e);
		}
		
	}
	
	private void dump(CaoElement node, String level, PrintStream out) {
		//  node
		if (level.length() > 100*2) {
			out.println( "<<BREAK>>");
			return;
		}
		try {
			out.println( level + "node \"" + node.getName() + "\" (");
			for (CaoMetaDefinition meta : node.getMetadata()) {
				String aName = meta.getName();
				out.print  ( level + "  attribute \"" + aName + "\"" );
				for (int i = aName.length(); i < 20; i++) out.print(" ");
				out.print  ( " " + meta.getType() +" ( \"");
				if (meta.getType().equals(TYPE.LIST)) {
					CaoList list = node.getList(meta.getName(), null);
					if (list == null) {
						out.print  ("null");						
					} else {
						out.println();
						for (CaoElement el : list.getElements()) {
							out.println(level + "                    [");
							dump(el, level + "                      ", out);
							out.println(level + "                    ]");
						}
					}
				} else
				if (meta.getType().equals(TYPE.BINARY))
					if (binaryAlso)
						out.print  ( node.getString(meta.getName()));
					else
						out.print  ("<<?>>");						
				else
					out.print  ( node.getString(meta.getName()));
				out.println( "\"  )" );
			}
		} catch (Exception e) {
			e.printStackTrace();
			monitor.log().i(e);
		}
		try {
			for (CaoElement child : node.getChildren().getElements()) {
				dump(child,level + "  ",out);
			}
		} catch (Exception e) {
			monitor.log().i(e);
		}

		out.println( level + ")");
		
	}
	
	
	@Override
	public void dispose() throws CaoException {
	}

	public void setSources(CaoList list) {
		sources = list;
	}
	
	@FormElement("select title='Option'")
	public void setOutputType(String index) {
		outputType = index;
	}

	public String getOutputType() {
		return outputType;
	}

	public IDynOptionsProvider getOutputTypeDataProvider() {
		return new SimpleDynOptionsProvider(new String[] {"system","document","text"}, new String[] {"XML System","XML Document","Lesbarer Text"});
	}

	@FormElement("checkbox title='Binary Data'")
	public void setBinary(boolean in) {
		binaryAlso = in;
	}
	
	public boolean isBinary() {
		return binaryAlso;
	}
	
	@FormElement("file title='File' nls='file'")
	public void setFile(String in) {
		file = in;
	}
	
	public String getFile() {
		return file;
	}
	
	
}
