package de.mhus.cap.ui.attreditor;

import javax.xml.parsers.ParserConfigurationException;

import de.mhus.lib.MActivator;
import de.mhus.lib.cao.CaoElement;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.form.MForm;
import de.mhus.lib.form.MFormModel;
import de.mhus.lib.form.builders.FormLayoutSimpleBuilder;
import de.mhus.lib.form.objects.FBinary;
import de.mhus.lib.form.objects.FBoolean;
import de.mhus.lib.form.objects.FObject;
import de.mhus.lib.form.objects.FString;
import de.mhus.lib.form.objects.FStringList;

public class CaoFormModel extends MFormModel {

	public CaoFormModel(MActivator activator, MForm target, CaoElement element) {
		super(activator, target);
		
		
		int sort = 0;
		for ( CaoMetaDefinition meta : element.getMetadata() ) {

			try {
				FObject next = null;
				switch (meta.getType()) {
				case BOOLEAN:
					next = new FBoolean();
					((FBoolean)next).setValue(false);
					break;
				case DATETIME:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case DOUBLE:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case LONG:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
				case STRING:
					next = new FString(); //TODO
					((FString)next).setValue("");
					break;
//				case TEXT:
//					next = new FStringArea(); //TODO
//					((FString)next).setValue("");
//					break;	
				case BINARY:
				case OBJECT:
					next = new FBinary(); //TODO
					// next.setFormValue("");
					break;
				case LIST:
					next = new FStringList();
					//((FStringList)next).set
				}
				
				if (next!=null) {
					next.initialize(this);
					next.setTitle(meta.getName() + " (" + meta.getType().name() + ")");
					next.setName(meta.getName());
					next.setId(meta.getName());
					next.setSortId(sort);
	
					//next.setEnabled(false);
					
					sort++;
//					next.setNls(meta.getNls());
					this.getList().add(next);
				}
			} catch ( Exception e ) {
				log().warn(e);
			}
						
		}
		
		try {
			setLayout(new FormLayoutSimpleBuilder(this));
		} catch (ParserConfigurationException e) {
			log().warn(e);
		}
		
	}

}
