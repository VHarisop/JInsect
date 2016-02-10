package gr.demokritos.iit.jinsect.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An in-memory database, in which the objects are kept in a
 * {@link java.util.HashMap}.
 *
 * @author ggianna
 */
public class INSECTMemoryDB<TObjectType extends Serializable> 
	extends INSECTDB<TObjectType> {

	/** 
	 * A {@link HashMap} kept in memory to look up objects.
	 */
	protected HashMap<String, Serializable> ObjectMap;

	/** 
	 * Creates a new instance of INSECTMemoryDB.
	 */
	public INSECTMemoryDB() {
		ObjectMap = new HashMap<String, Serializable>();
	}

	/**
	 * Returns the full name of the object, by which it is stored in the
	 * database, given its original name and its category.
	 * @param sObjectName the name of the object
	 * @param sObjectCategory the object's category
	 * @return the object's full name in the database
	 */
	protected String getObjectName(String sObjectName, String sObjectCategory) {
		return sObjectName + "." + sObjectCategory;
	}

	/**
	 * @see INSECTDB#saveObject(Serializable, String, String) saveObject
	 */
	public void saveObject(Serializable obj, String objName, String objCategory) {
		ObjectMap.put(getObjectName(objName, objCategory), obj);
	}

	/**
	 * @see INSECTDB#loadObject(String, String) loadObject
	 */
	@SuppressWarnings("unchecked")
	public TObjectType loadObject(String sObjectName, String sObjectCategory) {
		return (TObjectType)ObjectMap.get(getObjectName(sObjectName, sObjectCategory));
	}

	/**
	 * @see INSECTDB#deleteObject(String, String) deleteObject
	 */
	public void deleteObject(String sObjectName, String sObjectCategory) {
		if (existsObject(sObjectName, sObjectCategory))
			ObjectMap.remove(getObjectName(sObjectName, sObjectCategory));
	}

	/**
	 * @see INSECTDB#existsObject(String, String) existsObject
	 */
	@Override
	public boolean existsObject(String sObjectName, String sObjectCategory) {
		return ObjectMap.containsKey(getObjectName(sObjectName, sObjectCategory));
	}

	/**
	 * @see INSECTDB#getObjectList(String) getObjectList
	 */
	@Override
	public String[] getObjectList(String sObjectCategory) {
		ArrayList<String> lList = new ArrayList<String>();

		for (String fullName: ObjectMap.keySet()) {
			lList.add(fullName.substring(0, fullName.length() - sObjectCategory.length() - 1));
		}
		// Return actual object names
		String[] aRes = new String[lList.size()];
		if (lList.size() > 0)            
			return (String [])lList.toArray(aRes);
		else
			return aRes;
	}

	/**
	 * @see INSECTDB#getObjDataToString(Object) getObjDataToString
	 */
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
	@SuppressWarnings("unchecked")
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
}
