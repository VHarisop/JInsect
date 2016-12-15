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
import java.util.logging.Logger;

import gr.demokritos.iit.jinsect.Logging;

/**
 * A file database which stores object in their raw format.
 * @author ggianna
 */
public class INSECTFileRawDB<TObjectType extends Serializable>
	extends INSECTFileDB<TObjectType>
{
	static final long serialVersionUID = 1L;

	private static final Logger logger =
			Logging.getLogger(INSECTFileRawDB.class.getName());

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
			final FileOutputStream fsOut = new FileOutputStream(
					getFileName(sObjectName, sObjectCategory));
			final ObjectOutputStream oOut = new ObjectOutputStream(fsOut);
			oOut.writeObject(oObj);
			// Complete the GZIP file
			fsOut.close();
		}
		catch (final Exception e) {
			logger.severe(e.getMessage());
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
		catch (final Exception e) {
			logger.severe(e.getMessage());
			return null;
		}

		Object oRes;
		try {
			oRes = iIn.readObject();
			iIn.close();
		}
		catch (final Exception e) {
			logger.severe(e.getMessage());

			/* closed iIn in try {} block to fix resource leak
			 * in which iIn was still open when returning null
			 */
			return null;
		}

		try {
			fsIn.close();
		} catch (final IOException ex) {
			logger.warning(ex.getMessage());
		}
		return (TObjectType)oRes;
	}

}
