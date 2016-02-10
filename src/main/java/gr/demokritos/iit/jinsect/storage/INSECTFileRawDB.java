/*
 * Under LGPL
 * by George Giannakopoulos
 */

package gr.demokritos.iit.jinsect.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A file database which stores object in their raw format.
 * @author ggianna
 */
public class INSECTFileRawDB<TObjectType extends Serializable> 
	extends INSECTFileDB<TObjectType> 
{
	static final long serialVersionUID = 1L;

	/** 
	 * Creates a new instance of INSECTFileRawDB using a given
	 * prefix and base directory.
	 *
	 * @param sPrefix the custom prefix for the filenames
	 * @param sBaseDir the base directory of the database
	 */
	public INSECTFileRawDB(String sPrefix, String sBaseDir) {
		super(sPrefix, sBaseDir);
	}

	/**
	 * @see INSECTFileDB#saveObject(Serializable, String, String) saveObject
	 */
	@Override
	public void saveObject(Serializable oObj, String sObjectName, String sObjectCategory) {
		try {
			FileOutputStream fsOut = new FileOutputStream(getFileName(sObjectName, sObjectCategory));

			ObjectOutputStream oOut = new ObjectOutputStream(fsOut);
			oOut.writeObject(oObj);
			// Complete the GZIP file
			fsOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see INSECTFileDB#loadObject(String, String) loadObject
	 */
	@SuppressWarnings("unchecked")
	@Override
	public TObjectType loadObject(String sObjectName, String sObjectCategory) {
		FileInputStream fsIn = null;
		ObjectInputStream iIn = null;
		try {
			fsIn = new FileInputStream(getFileName(sObjectName, sObjectCategory));
			iIn = new ObjectInputStream(fsIn);
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

			/* closed iIn in try {} block to fix resource leak 
			 * in which iIn was still open when returning null 
			 */
			return null;
		}

		try {
			fsIn.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return (TObjectType)oRes;
	}

}
