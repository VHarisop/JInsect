package gr.demokritos.iit.jinsect.storage;

/** 
 * This interface describes all classes that can load a file, given its
 * identifier.
 *
 * @author ggianna
 */
public interface IFileLoader<Type> {
	/** 
	 * Loads the file and represents it using type <Type>. 
	 * @param sID The identifier of the file.
	 * @return The representation of the file.
	 */
	public Type loadFile(String sID);
}
