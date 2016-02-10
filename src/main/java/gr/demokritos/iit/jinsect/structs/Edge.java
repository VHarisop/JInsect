package gr.demokritos.iit.jinsect.structs;

import java.util.Objects;
import org.jgrapht.graph.*;

/**
 * Simple proxy class for DefaultWeightedEdge in order 
 * to keep compatibility with JGraphT and provide a few
 * custom methods.
 *
 * @author VHarisop
 */
public class Edge extends DefaultWeightedEdge
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new <tt>Edge</tt> object, which is created by
	 * directly calling the superclass' constructor. This is a
	 * separate constructor mainly to avoid "EdgeFactory failed"
	 * errors/warnings.
	 *
	 * @return a new <tt>Edge</tt> object
	 *  
	 */
	public Edge() {
		super();
	}
	
	/**
	 * Simple getter for the label of the source object.
	 * @return the source object's label
	 */
	public String getSourceLabel() {
		return String.valueOf(getSource());
	}
	
	/**
	 * Simple getter for the label of the target object.
	 * @return the label that the target object represents
	 */
	public String getTargetLabel() {
		return String.valueOf(getTarget());
	}

	/**
	 * Returns a label for the whole edge that consists of the
	 * source and target labels connected via an arrow showing
	 * the edge's direction. 
	 * 
	 * @return a string with the edge labels and an arrow indicating
	 * the orientation of the edge
	 */
	public String getLabels() {
		return getSourceLabel() + "->" + getTargetLabel();
	}

	/**
	 * Simple getter for the weight of the edge.
	 * @return a double containing the edge weight.
	 */
	public double edgeWeight() {
		return super.getWeight();
	}

	/**
	 * Override superclass string representation by adding info for
	 * the edge's weight in square brackets.
	 */
	@Override 
	public String toString() {
		return super.toString() + "[" + String.valueOf(getWeight()) + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSource(), getTarget());
	}

	/**
	 * In JInsect's scope, two edges are considered equal if they connect
	 * the same vertices and have the same orientation. It is useful not to
	 * include the weights in this method, since it makes some retrieval tasks
	 * easier using java classes like HashMap / TreeMap.
	 */
	@Override
	public boolean equals(Object oOther) {
		if (!(oOther instanceof Edge)) 
			return false;

		Edge eOther = (Edge) oOther;
		return this.getLabels().equals(eOther.getLabels());
	}
}
