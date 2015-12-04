package de.mhus.cao.model.fs;

import de.mhus.lib.cao.CaoDriver;
import de.mhus.lib.cao.CaoMetaDefinition;
import de.mhus.lib.cao.CaoMetaDefinition.TYPE;
import de.mhus.lib.cao.CaoMetadata;

public class IoMetadata extends CaoMetadata {

	public IoMetadata(CaoDriver driver) {
		super(driver);
		definition.add(new CaoMetaDefinition(this, "name", TYPE.STRING, "name", 255 ));
		definition.add(new CaoMetaDefinition(this, "path", TYPE.STRING, "path", 1024, CaoDriver.CATEGORY_ID ));
		definition.add(new CaoMetaDefinition(this, "modified", TYPE.DATETIME, "modified", 0 ));
		definition.add(new CaoMetaDefinition(this, "directory", TYPE.BOOLEAN, "directory", 0 ));
		definition.add(new CaoMetaDefinition(this, "readable", TYPE.BOOLEAN, "readable", 0 ));
		definition.add(new CaoMetaDefinition(this, "writable", TYPE.BOOLEAN, "writable", 0 ));
		definition.add(new CaoMetaDefinition(this, "executable", TYPE.BOOLEAN, "executeable", 0 ));
		definition.add(new CaoMetaDefinition(this, "hidden", TYPE.BOOLEAN, "hidden", 0 ));
	}

}
