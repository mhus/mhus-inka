package de.mhus.app.inka.ant.dctm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfVirtualDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.IDfCheckinNode;
import com.documentum.operations.IDfCheckinOperation;
import com.documentum.operations.IDfCheckoutNode;
import com.documentum.operations.IDfCheckoutOperation;
import com.documentum.operations.IDfFormatRecognizer;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.operations.IDfOperationMonitor;
import com.documentum.operations.IDfOperationNode;
import com.documentum.operations.IDfOperationStep;

import de.mhus.lib.MFile;
import de.mhus.lib.MSql;
import de.mhus.lib.MString;

public class ImportFiles extends Task {

	public enum IMPORT_TYPE {IGNORE_EXISTING,SKIP_EXISTING,VERSION_NOT_SET,NEXT_MAJOR,NEXT_MINOR,BRANCH_VERSION,OVERWRITE};
	private String session;
	private DMConnection con;
	private boolean importRelations = false;
	private boolean importNew;
	private IMPORT_TYPE importType = IMPORT_TYPE.NEXT_MINOR;
	private String dctmPath;
	private String docType;
    private Vector<FileSet> sourceFileSets  = new Vector<FileSet>();

	public void execute() throws BuildException {
		
		con = Connect.getSession(session);
		
		try {
			for (FileSet fileSet : sourceFileSets) {
				for (String filePath : fileSet.toString().split(";")) {
					File file = new File(filePath);
					imp(file);
				}
			}
			
		} catch (Exception e) {
			throw new BuildException(session + ":" + e.getMessage(),e);
		}
	}
	
	public void setSession(String in) {
		session = in;
	}
	
	public void imp(File file) throws DfException {
		
		IMPORT_TYPE curImportType = importType;
		
		IDfPersistentObject dctmTarget;
		String dirPath;
		
		if (dctmPath.startsWith("/")) {
			dctmTarget = con.getExistingObject(dctmPath, "");
			if (!(dctmTarget instanceof IDfFolder)) {
				log("Target not a folder");
				return;
			}
			dirPath = dctmPath;
		} else {
			dctmTarget = con.getExistingObject(dctmPath);
			dirPath = getPath(dctmTarget) + '/';
		}
		

			try {
				log(">>> IMPORT: " + file.getPath());

				Properties prop = null;

				if (importRelations) {
					// find prop file
					File propFile = new File(file.getAbsolutePath()
							+ ".properties");
					if (!propFile.exists() || !propFile.isFile()) {
						log("*** Property File not found");
						return;
					}

					prop = new Properties();
					FileInputStream fis = new FileInputStream(propFile);
					prop.load(fis);
					fis.close();

					// fix lang
					if (prop.getProperty("s.language_code", "").equals(
							""))
						prop.setProperty("s.language_code", " ");

				}

				boolean create = curImportType.equals(IMPORT_TYPE.IGNORE_EXISTING);

				if (!create && importNew) {
					String path = dirPath + file.getName();
					log("--- CHECK " + path);
					if (prop != null)
						create = (con.getExistingObject(dirPath
								+ prop.getProperty("s.object_name"),
								prop.getProperty("s.language_code")) == null);
					else
						create = (con.getSession()
								.getObjectByPath(path) == null);
					if (!create && curImportType.equals(IMPORT_TYPE.SKIP_EXISTING)) {
						log("--- SKIP");
						return;
					}
				}

				if (create) {
					log("--- CREATE");

					if (prop == null) {
						IDfFormatRecognizer formatRec = DMConnection.clientx
								.getFormatRecognizer(con.getSession(),file.getName(), "crtext");
						IDfList formatList = formatRec
								.getSuggestedFileFormats();
						String format = "crtext";
						String suffix = MFile.getFileSuffix(file)
								.toLowerCase();
						for (int i = 0; i < formatList.getCount(); i++) {
							format = formatList.getString(i);
							System.out.println("--- "
									+ formatList.getString(i));
							if (format.equals(suffix))
								break;
							// do something with the Error
						}

						IDfDocument document = (IDfDocument) con
								.getSession().newObject(docType);
						document.setObjectName(file.getName());
						document.setContentType(format);
						// setFileEx parameters
						// (fileName,formatName,pageNumber,otherFile)
						document.setFileEx(file.toString(), format,
								0, null);

						// Specify the folder in which to create the
						// document
						document.link(dctmTarget.getObjectId()
								.toString());

						// Save the document in the Docbase
						document.save();

					} else {

						IDfDocument document = (IDfDocument) con
								.getSession()
								.newObject(
										prop
												.getProperty("s.object_type"));
						document.setObjectName(prop
								.getProperty("s.object_name"));
						document.setContentType(prop
								.getProperty("s.content_type"));
						document.setString("language_code", prop
								.getProperty("s.language_code"));
						document.setTitle(prop.getProperty("s.title"));
						document.setSubject(prop
								.getProperty("s.subject"));

						document.link(dctmTarget.getObjectId()
								.toString());

						document.save();

						// relations
						int cnt = 0;
						while (true) {
							String relName = prop.getProperty("rel."
									+ cnt + ".name");
							if (relName == null)
								break;
							try {
								String relPath = prop
										.getProperty("rel." + cnt
												+ ".path");
								String relLang = prop
										.getProperty("rel." + cnt
												+ ".language_code");
								if (relLang.length() == 0)
									relLang = " "; // fix lang
								String relDesc = prop
										.getProperty("rel." + cnt
												+ ".description");

								log("--- Relation: "
										+ relName + " " + relPath);
								IDfSysObject relObj = (IDfSysObject) con
										.getExistingObject(relPath,
												relLang);
								if (relObj == null)
									log("*** Relation Object not found !!");
								else {
									String dql = "CREATE dm_relation OBJECT "
											+ "SET relation_name='"
											+ relName
											+ "' "
											+ "SET parent_id='"
											+ document.getObjectId()
													.getId()
											+ "' "
											+ "SET child_id='"
											+ relObj.getChronicleId()
													.getId()
											+ "' "
											+ "SET description='"
											+ MSql.escape(relDesc)
											+ "'";
									IDfQuery query = con
											.createQuery(dql);
									IDfCollection res = query.execute(
											con.getSession(),
											IDfQuery.READ_QUERY);
									res.close();
								}

							} catch (Exception e) {
								log("*** ERROR: " + e);
							}
							cnt++;
						}

					}

					curImportType = IMPORT_TYPE.OVERWRITE;

				}

				if (curImportType.equals(IMPORT_TYPE.NEXT_MAJOR) || 
						curImportType.equals(IMPORT_TYPE.NEXT_MINOR) || 
						curImportType.equals(IMPORT_TYPE.VERSION_NOT_SET) || 
						curImportType.equals(IMPORT_TYPE.BRANCH_VERSION) ) {
					log("--- CO / CI");
					IDfSysObject sysObj = (IDfSysObject) con
							.getSession().getObjectByPath(
									dirPath + file.getName());

					if (sysObj == null) {
						log("+++ not found");
						return;
					}

					// try to cancel a check out
					if (sysObj.isCheckedOut()) {
						log("+++ LOCKED: "
								+ file.getAbsolutePath());
						sysObj.cancelCheckout();
						sysObj.save();
						if (sysObj.isCheckedOut()) {
							log("*** LOCKED ");
							return;
						}
					}

					// check out
					IDfCheckoutOperation coOperation = DMConnection.clientx
							.getCheckoutOperation();
					IDfCheckoutNode coNode;
					if (sysObj.isVirtualDocument()) {
						IDfVirtualDocument vDoc = sysObj
								.asVirtualDocument("CURRENT", false);
						coNode = (IDfCheckoutNode) coOperation
								.add(vDoc);
						log("--- is Virtual");
					} else {
						coNode = (IDfCheckoutNode) coOperation
								.add(sysObj);
					}
					executeOperation(coOperation);

					String coFilePath = coNode.getFilePath();
					log("--- Copy: " + file.getPath()
							+ " -> " + coFilePath);
					// copy existinf gile over co file
					File coFile = new File(coFilePath);
					MFile.copyFile(file, coFile);

					// check in
					IDfCheckinOperation ciOperation = DMConnection.clientx
							.getCheckinOperation();
					IDfCheckinNode ciNode;
					if (sysObj.isVirtualDocument()) {
						IDfVirtualDocument vDoc = sysObj
								.asVirtualDocument("CURRENT", false);
						ciNode = (IDfCheckinNode) ciOperation.add(vDoc);
					} else {
						ciNode = (IDfCheckinNode) ciOperation
								.add(sysObj);
					}
					String versionLabels = "";
					for (int i = 1; i < sysObj
							.getValueCount("r_version_label"); i++) {
						if (i != 1)
							versionLabels += ",";
						versionLabels += sysObj.getRepeatingString(
								"r_version_label", i);
					}
					log("--- Versions: " + versionLabels);
					ciOperation.setVersionLabels(versionLabels);
					// Other options for setCheckinVersion:
					// VERSION_NOT_SET, NEXT_MAJOR,
					// NEXT_MINOR, BRANCH_VERSION
					if (curImportType.equals(IMPORT_TYPE.VERSION_NOT_SET))
						ciOperation
								.setCheckinVersion(IDfCheckinOperation.VERSION_NOT_SET);
					else if (curImportType.equals(IMPORT_TYPE.NEXT_MAJOR))
						ciOperation
								.setCheckinVersion(IDfCheckinOperation.NEXT_MAJOR);
					else if (curImportType.equals(IMPORT_TYPE.NEXT_MINOR))
						ciOperation
								.setCheckinVersion(IDfCheckinOperation.NEXT_MINOR);
					else if (curImportType.equals(IMPORT_TYPE.BRANCH_VERSION))
						ciOperation
								.setCheckinVersion(IDfCheckinOperation.BRANCH_VERSION);

					// see sample: Operations- Execute and Check Errors
					executeOperation(ciOperation);

					IDfSysObject newObj = (IDfSysObject) con.getExistingObject(ciNode
							.getNewObjectId());
					log("--- New ID: "
							+ newObj.getObjectId().toString());

				} else if (curImportType.equals(IMPORT_TYPE.OVERWRITE)) {
					log("--- OVERWRITE");
					IDfSysObject sysObj = null;
					if (prop != null)
						sysObj = (IDfSysObject) con
								.getExistingObject(
										dirPath
												+ prop
														.getProperty("s.object_name"),
										prop
												.getProperty("s.language_code"));
					else
						sysObj = (IDfSysObject) con.getSession()
								.getObjectByPath(
										dirPath + file.getName());

					if (sysObj == null) {
						log("+++ not found");
						return;
					}

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					FileInputStream fis = new FileInputStream(file);
					MFile.copyFile(fis, baos);

					sysObj.setContent(baos);
					sysObj.save();

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void executeOperation(IDfOperation operation)
	throws DfException {
	// see the operation monitor sample
	operation.setOperationMonitor(new Progress());
	
	// Execute the operation
	boolean executeFlag = operation.execute();
	
	// Check if any errors occurred during the execution of the operation
	if (executeFlag == false) {
		// Get the list of errors
		IDfList errorList = operation.getErrors();
		String message = "";
		IDfOperationError error = null;
	
		// Iterate through the errors and concatenate the error messages
		for (int i = 0; i < errorList.getCount(); i++) {
			error = (IDfOperationError) errorList.get(i);
			message += error.getMessage();
		}
	
		if (!MString.isEmpty(message))
			log("Errors:" + message);
	}
	}
	
	public class Progress implements IDfOperationMonitor {
		public int progressReport(IDfOperation op, int iPercentOpDone,
				IDfOperationStep step, int iPercentStepDone,
				IDfOperationNode node) throws DfException {
			String strStep = iPercentOpDone + "%   " + iPercentStepDone + "% "
					+ step.getName() + " - " + step.getDescription();

			String strNode = "     "
					+ (node != null ? node.getId().toString() : "?");

			log(strStep);
			log(strNode);
			return IDfOperationMonitor.CONTINUE;
		}

		public int reportError(IDfOperationError error) throws DfException {
			return IDfOperationMonitor.CONTINUE;
		}

		public int getYesNoAnswer(IDfOperationError Question)
				throws DfException {
			return IDfOperationMonitor.YES;
		}

	}
	
	public void addSourcefiles(final FileSet fileset) {
        sourceFileSets.add(fileset);
    }
	
	public void setImportRelations(boolean in) {
		importRelations = in;
	}
	
	public void setImportNew(boolean in) {
		importNew = in;
	}
	
	public void setDctmPath(String in) {
		dctmPath = in;
	}

	public void setImportType(String in) {
		importType = IMPORT_TYPE.valueOf(in.toUpperCase());
	}
	
	public void setDocumentType(String in) {
		docType = in;
	}
	
	public static String getPath(IDfPersistentObject obj) throws DfException {
		if (obj instanceof IDfSysObject) {

			IDfSysObject sys = (IDfSysObject) obj;

			if (sys.getFolderIdCount() == 0)
				return "/" + sys.getObjectName();
			IDfFolder folder = (IDfFolder) sys.getSession().getObject(
					sys.getFolderId(0));

			if (folder.getFolderPathCount() == 0)
				return "/" + folder.getObjectName() + "/" + sys.getObjectName();

			return folder.getFolderPath(0) + "/" + sys.getObjectName();
		} else
			return obj.getObjectId().toString();
	}

	
	
}
