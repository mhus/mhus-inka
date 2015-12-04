package de.mhu.com.morse.pack.mc;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.mhu.lib.AFile;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.BtoObject;
import de.mhu.com.morse.obj.IBtoAutoExtension;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class McDocument extends McFolder implements IBtoAutoExtension {

	public void setLanguage(String string) throws MorseException {
		setString( "mc_lang", string );
	}

	public void saveRendition( IConnection con, InputStream is, String format ) throws MorseException {
		
		ObjectUtil.assetId( getObjectId() ); // maybe its new object
		
		IQueryResult res = new Query( con, "RENDITION " + getObjectId() + " SAVE DEFAULT FORMAT `" + format + "`" ).execute();
		OutputStream os = res.getOutputStream();
		AFile.copyFile( is, os );
		try {
			os.close();
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		res.close();
		
	}

	public void appendRendition( IConnection con, InputStream is, String format ) throws MorseException {
		
		ObjectUtil.assetId( getObjectId() ); // maybe its new object
		
		IQueryResult res = new Query( con, "RENDITION " + getObjectId() + " SAVE APPEND FORMAT `" + format + "`" ).execute();
		OutputStream os = res.getOutputStream();
		AFile.copyFile( is, os );
		try {
			os.close();
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		res.close();
		
	}
	
	public void setChronicleId(String chronicleId) throws MorseException {
		setString( CMc.V_CHRONICLE_ID, chronicleId );
	}

	public BtoObject autoExtend(IType t, IQueryResult res) {
		
		// check for images
		
		return this;
	}

}
