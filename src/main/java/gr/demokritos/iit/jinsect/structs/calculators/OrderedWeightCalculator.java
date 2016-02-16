package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A class that creates a list of weight - vertex pairs from a
 * {@link UniqueJVertexGraph} object, and sorts it by descending
 * weight order.
 *
 * @author VHarisop
 */
public class OrderedWeightCalculator {
	/**
	 * The graph this calculator operates on.
	 */
	protected UniqueJVertexGraph graph;

	/**
	 * The list of weight - vertex pairs.
	 */
	protected List<Pair<JVertex, Double>> orderedPairs;

	/**
	 * Creates a new OrderedWeightCalculator object that operates on
	 * a specified graph.
	 *
	 * @param uvg the graph to operate on
	 */
	public OrderedWeightCalculator(UniqueJVertexGraph uvg) {
		this.graph = uvg;

		/* initialize the list of pairs */
		orderedPairs = 
			new ArrayList<Pair<JVertex, Double>>(uvg.vertexSet().size());

		/* vertices are sorted on initialization to enable optimal performance
		 * on successive calls to #getOrderedPairs() */
		sortVertices();
	}

	/**
	 * Utility function, sorts the vertices of the graph according to their weight
	 * importance. 
	 */
	private void sortVertices() {
		/* populate the list of pairs */
		for (JVertex v: graph.vertexSet()) {
			orderedPairs.add(new Pair<JVertex, Double>(v, graph.weightSumOf(v)));
		}

		Collections.sort(orderedPairs, new Comparator<Pair<JVertex, Double>>() {
			@Override
			public int compare(Pair<JVertex, Double> a, Pair<JVertex, Double> b)
			{
				/* first compare weights and, only if they are equal, enforce
				 * a lexicographic ordering among vertices of same importance
				 */
				int ret = Double.compare(a.getSecond(), b.getSecond());
				if (ret == 0) {
					String la = a.getFirst().getLabel(),
						   lb = b.getFirst().getLabel();

					return la.compareTo(lb);
				}
				else {
					return ret;
				}
			}
		});
	}

	/**
	 * Returns the sorted list of ordered vertex - weight pairs in a
	 * non modifiable view.
	 *
	 * @return an unmodifiable sorted list of pairs
	 */
	public List<Pair<JVertex, Double>> getOrderedPairs() {
		return Collections.unmodifiableList(orderedPairs);
	}
}
