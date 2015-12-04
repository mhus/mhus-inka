package de.mhu.com.morse.obj;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.types.IType;

public interface IBtoAutoExtension {

	public BtoObject autoExtend(IType t, IQueryResult res);

}
