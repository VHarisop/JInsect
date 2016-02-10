package gr.demokritos.iit.jinsect.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** 
 * A file database that uses a single file per stored object.
 *
 * @author ggianna
 */
public class INSECTFileDB<TObjectType extends Serializable> 
	extends INSECTDB<TObjectType>
	implements Serializable 
{
	static final long serialVersionUID = 1L;

	/**
	 * A custom prefix to be used for the files of the database
	 */
	private String Prefix;

	/**
	 * The base directory of the database
	 */
	private String BaseDir;

	/**
	 * Creates a new instance of INSECTFileDB using the default parameters:
	 * an empty file prefix and the working directory as base directory.
	 */
	public INSECTFileDB() {
		this(null, null);
	}

	/** 
	 * Creates a new instance of INSECTFileDB given a file prefix and a base
	 * directory. If any of the parameters of the constructor is null, a 
	 * default value is used (empty prefix and current directory).
	 *
	 * @param sPrefix the file prefix
	 * @param sBaseDir the base directory for the database
	 */
	public INSECTFileDB(String sPrefix, String sBaseDir) {
		if (sPrefix == null)
			Prefix = "";
		else
			Prefix = sPrefix;

		if (sBaseDir == null)
			BaseDir = "./";
		else
			BaseDir = sBaseDir;
	}

	/** 
	 * Returns the filename of the corresponding object of a given category.
	 *
	 * @param sObjectName The name of the object.
	 * @param sObjectCategory The category of the object.
	 * @return A string representing the filename of the object in the db.
	 */
	public String getFileName(String sObjectName, String sObjectCategory) {
		return BaseDir + System.getProperty("file.separator") + Prefix + 
			String.valueOf((sObjectName).hashCode()) + '.' + sObjectCategory;
	}

	/**
	 * @see INSECTDB#saveObject(Serializable, String, String) saveObject
	 */
	@Override
	public void saveObject(
			Serializable oObj,
			String sObjectName,
			String sObjectCategory) 
	{         
		try { 
			FileOutputStream fsOut = 
				new FileOutputStream(getFileName(sObjectName, sObjectCategory));
			GZIPOutputStream gzOut = 
				new GZIPOutputStream(fsOut);
			ObjectOutputStream oOut = 
				new ObjectOutputStream(gzOut);            

			oOut.writeObject(oObj);
			oOut.close();
			// Complete the GZIP file
			gzOut.finish();
			fsOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see INSECTDB#loadObject(String, String) loadObject
	 */
	@Override
	public TObjectType loadObject(String sObjectName, String sObjectCategory) {
		FileInputStream fsIn = null;
		GZIPInputStream gzIn = null;
		ObjectInputStream iIn = null;
		try {
			fsIn = new FileInputStream(getFileName(sObjectName, sObjectCategory));
			gzIn = new GZIPInputStream(fsIn);
			iIn = new ObjectInputStream(gzIn);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Object oRes;
		try {
			oRes = iIn.readObject();
			iIn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		try {
			fsIn.close();
			gzIn.close();
			iIn.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return (TObjectType) oRes;
	}

	/**
	 * @see INSECTDB#deleteObject(String, String) deleteObject
	 */
	@Override
	public void deleteObject(String sObjectName, String sObjectCategory) {
		if (existsObject(sObjectName, sObjectCategory)) {
			// Delete File
			File f = new File(getFileName(sObjectName, sObjectCategory));
			f.delete(); // Might fail. No testing.
		}
	}

	/**
	 * @see INSECTDB#existsObject(String, String) existsObject
	 */
	@Override
	public boolean existsObject(String sObjectName, String sObjectCategory) {
		return new File(getFileName(sObjectName, sObjectCategory)).exists();
	}

	/**
	 * @see INSECTDB#getObjectList(String) getObjectList
	 */
	@Override
	public String[] getObjectList(String sObjectCategory) {
		File dDir = new File(BaseDir);
		ObjectTypeFileFilter f = new ObjectTypeFileFilter(sObjectCategory);

		String[] sFiles = dDir.list(f);
		for (int iCnt = 0; iCnt < sFiles.length; iCnt++) {
			// Remove category type suffix (.CATEGORYNAME) from name
			sFiles[iCnt] = sFiles[iCnt].substring(
					Prefix.length(),
					sFiles[iCnt].length() - (sObjectCategory.length() + 1));
		}
		// Return actual object names
		return sFiles;
	}

	/**
	 * @see INSECTDB#getObjDataToString(Object) getObjDataToString
	 */
	@Override
	public String getObjDataToString(Object oObject) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream os = new ObjectOutputStream(bos);
			os.writeObject(oObject);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null; // Failed
		}

		return bos.toString();
	}

	/**
	 * @see INSECTDB#getStringToObjData(String) getStringToObjData
	 */
	@Override
	public TObjectType getStringToObjData(String sData) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream bos = new ObjectOutputStream(baos);
			bos.writeBytes(sData);    
		}
		catch (IOException e) {
			e.printStackTrace();
			return null; // Failed
		}

		ByteArrayInputStream bin = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois;
		Object oRes;
		try {
			ois = new ObjectInputStream(bin);
			oRes = ois.readObject();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null; // Failed
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			return null; // Class not found
		}
		return (TObjectType)oRes;
	}    

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException {
		out.defaultWriteObject();
	}    
}

/**
 * A custom filename filter that filters object based on a specified category.
 */
class ObjectTypeFileFilter implements FilenameFilter {
	private String ObjectCategory;
	public ObjectTypeFileFilter(String sCategory) {
		ObjectCategory = sCategory;
	}

	@Override
	public boolean accept(File pathname, String sName) {
		return (sName.matches(".*\\Q." + ObjectCategory + "\\E"));
	}
}
