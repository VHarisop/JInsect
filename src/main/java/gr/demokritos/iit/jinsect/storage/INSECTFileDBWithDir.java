/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.demokritos.iit.jinsect.storage;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author panagiotis
 * 
 * Contains the override methods of INSECTFileDB
 * 
 */
public class INSECTFileDBWithDir
	extends INSECTFileDB<ArrayList<String>>
{
	static final long serialVersionUID = 1L;

	/**
	 * Creates a new INSECTFileDBWithDir object with a given prefix
	 * and base directory.
	 *
	 * @param sPrefix the prefix to be used
	 * @param sBaseDir the base directory of the database
	 */
	public INSECTFileDBWithDir(String sPrefix, String sBaseDir) {
		super(sPrefix, sBaseDir);
	}

	/**
	 * Creates a new INSECTFileDBWithDir object with using the
	 * default prefix and base directory.
	 * @see INSECTFileDB#INSECTFileDB
	 */
	public INSECTFileDBWithDir() {
		super();
	}

	public static final String ListCategoryName = "nameList";

	/**
	 * Returns a String array with all names for the category requested
	 * @param sObjectCategory the category name
	 * @return a String array containing all the names 
	 */
	@Override
	public String[] getObjectList(String sObjectCategory) {
		if ((super.getObjectList(sObjectCategory).length == 0) ||
			 super.getObjectList(sObjectCategory).length == 1) 
		{
			String[] tableList = new String[0];
			return tableList;
		}
		else {
			ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName);
			String[] tableList = new String [nlist.size()];

			return nlist.toArray(tableList);
		}
	}

	/**
	 * save object with a given name
	 * @param oObj the object to be saved
	 * @param sObjectName the object name 
	 * @param sObjectCategory  the category name
	 */
	@Override
	public void saveObject(Serializable oObj, String sObjectName, String sObjectCategory) {
		super.saveObject(oObj, sObjectName, sObjectCategory);
		if (existsObject(sObjectCategory, ListCategoryName)){
			//create a name list that contains all names of the object to be saved
			ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName);
			// add name in the name list
			nlist.add(sObjectName); 
			// save the name list
			super.saveObject(nlist, sObjectCategory, ListCategoryName); 
		} 
		else {
			ArrayList<String>  nlist = new ArrayList<String>();
			nlist.add(sObjectName);
			super.saveObject(nlist, sObjectCategory, ListCategoryName);
		}
	}

	/**
	 * Deletes an object and updates the name list.
	 *
	 * @param sObjectName the name of the object
	 * @param sObjectCategory the category of the object
	 */
	@Override
	public void deleteObject(String sObjectName, String sObjectCategory) {
		int index;
		// delete the object
		super.deleteObject(sObjectName, sObjectCategory);
		// load the name list
		ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName);
		// find the index in the name list
		index= nlist.indexOf(sObjectName);
		// remove name from the name list
		nlist.remove(index);
		// save the new name list
		super.saveObject(nlist, sObjectCategory, ListCategoryName);
	}



}
