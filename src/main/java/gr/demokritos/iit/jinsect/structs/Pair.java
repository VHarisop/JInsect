/*
 * Pair.java
 *
 * Created on May 6, 2008, 1:48 PM
 *
 */

package gr.demokritos.iit.jinsect.structs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;

/** 
 * Represents a pair of elements of any type (as a templated class).
 *
 * @author ggianna
 */
public class Pair<ObjTypeFirst, ObjTypeSecond> implements Serializable {
	static final long serialVersionUID = 1L;

    protected ObjTypeFirst first;
    protected ObjTypeSecond second;
    
    /** 
	 * Creates a new instance of Pair, given two objects. 
     * @param oFirst The first object.
     * @param oSecond The second object.
     */
    public Pair(ObjTypeFirst oFirst, ObjTypeSecond oSecond) {
        first = oFirst;
        second = oSecond;
    }

    @Override
    public int hashCode() {
		return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return first.toString() + ", " + second.toString();
    }

    @Override
    public boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (!(obj instanceof Pair))
			return false;

		Pair<?, ?> otherP = (Pair<?, ?>) obj;
		return (first.equals(otherP.getFirst()) && 
				second.equals(otherP.getSecond()));
    }



    /** 
	 * Returns the first object of the pair. 
     * @return The first object. 
     */
    public ObjTypeFirst getFirst() {
        return first;
    }

    /** 
	 * Returns the second object of the pair. 
     * @return The second object. 
     */
    public ObjTypeSecond getSecond() {
        return second;
    }

    private void writeObject(ObjectOutputStream out)
         throws IOException {
        out.writeObject(first);
        out.writeObject(second);
    }

	@SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         first = (ObjTypeFirst) in.readObject();
         second = (ObjTypeSecond) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        first = null;
        second = null;
    }
}
