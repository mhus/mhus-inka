package de.mhus.aqua.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import de.mhus.aqua.Activator;
import de.mhus.aqua.api.AquaRequest;
import de.mhus.lib.MFile;
import de.mhus.lib.logging.Log;

public class FileRes extends AquaRes {

	private static Log log = Log.getLog(FileRes.class);
	private String path;

	@Override
	public void process(AquaRequest request) throws Exception {
		
		if (path == null) {
			path = config.getExtracted("path");
			if (path.startsWith("web:")) {
				path = Activator.getAqua().getBaseDir() + "/" + path.substring(4);
			}

		}
		String file = path + request.getExtPath();
		
		File f = new File(file);
		
		if (!f.exists() || !f.isFile()) {
			log.t("file not found",f);
			request.sendErrorNotFound();
			return;
		}
		
		//FileReader is = new FileReader(f);
		//PrintWriter pw = request.getWriter();
		//MFile.copyFile(is, pw);
		FileInputStream is = new FileInputStream(f);
		OutputStream os = request.getOutputStream();
		MFile.copyFile(is, os);
		is.close();
		
	}

}
