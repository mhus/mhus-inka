package de.mhus.hair.jack;

import javax.jcr.Property;

import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetadata;

public class ValueMeta extends CaoMetadata {

	public ValueMeta(String name, CaoMetadata caoMetadata, Property p ) {
		super(caoMetadata.getDriver());
		CaoMetaDefinition meta = caoMetadata.getDefinition(name);
		
		try {
			definition.add(new CaoMetaDefinition(this,name,JackMeta.getTypeForJcr(p),meta.getNls(),Integer.MAX_VALUE));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
