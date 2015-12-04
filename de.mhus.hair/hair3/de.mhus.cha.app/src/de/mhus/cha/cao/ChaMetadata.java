package de.mhus.cha.cao;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoException;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetadata;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.MCast;
import de.mhus.lib.MXml;

public class ChaMetadata extends CaoMetadata {

	public ChaMetadata(CaoDriver driver) {
		super(driver);
		
		// default definitions
		
		definition.add(new CaoMetaDefinition(this, "name", TYPE.STRING, "name", 255 ));
		definition.add(new CaoMetaDefinition(this, "type", TYPE.STRING, "type", 255 ));
		definition.add(new CaoMetaDefinition(this, "modified", TYPE.DATETIME, "modified", 0 ));
		definition.add(new CaoMetaDefinition(this, "created", TYPE.DATETIME, "created", 0 ));
		definition.add(new CaoMetaDefinition(this, "description", TYPE.TEXT, "description", 0 ));
		definition.add(new CaoMetaDefinition(this, "active", TYPE.BOOLEAN, "active", 0 ));

		
//		definition.add(new CaoMetaDefinition(this, "readable", TYPE.BOOLEAN, "readable", 0 ));
//		definition.add(new CaoMetaDefinition(this, "writable", TYPE.BOOLEAN, "writable", 0 ));
//		definition.add(new CaoMetaDefinition(this, "executable", TYPE.BOOLEAN, "executeable", 0 ));
//		definition.add(new CaoMetaDefinition(this, "hidden", TYPE.BOOLEAN, "hidden", 0 ));
	}

	public ChaMetadata(ChaElement chaElement, File path) {
		this(chaElement.getConnection().getDriver());
		
		// custom definitions
		
		try {
			Element defs = chaElement.loadXml(ChaElement.NAME_METADATA);
			for (Element def : MXml.getLocalElementIterator(defs, "attribute")) {

				String name = def.getAttribute("name");
				TYPE type = TYPE.valueOf( def.getAttribute("type") );
				String nls  = def.getAttribute("nls");
				int    size = MCast.toint(def.getAttribute("size"),0);
				
				NodeList catList = MXml.getLocalElements(def, "category");
				String[] categories = new String[catList.getLength()];
				for (int i = 0; i < catList.getLength(); i++)
					categories[i] = MXml.getValue((Element) catList.item(i), false);
				
				definition.add(new CaoMetaDefinition(this, name, type, nls, size, categories));
				
			}
		} catch (CaoException e) {
			
		}
		
		
	}

}
