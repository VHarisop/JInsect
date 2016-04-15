package gr.demokritos.iit.jinsect.representations;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that performs commonly needed actions on NGramGraphs.
 *
 * @author VHarisop
 */
public final class GraphUtils {
	/**
	 * Creates a blank GraphUtils object. This constructor is private, because
	 * GraphUtils is intended to be used as a utility class with helper methods.
	 */
	private GraphUtils() {}

	/**
	 * Merges an array of {@link NGramJGraph} objects, using a copy of the first
	 * graph of the array as the basis for the merging process - no graph in the
	 * array is modified. The learning rate used for merging is the weighted
	 * average. Returns <tt>null</tt> if the array is empty.
	 *
	 * @param toMerge the array of graphs to merge
	 * @return the graph that results from merging all graphs in the array
	 */
	public static NGramGraph mergeGraphs(NGramGraph[] toMerge) {
		if (toMerge.length == 0) 
			return null;

		/* make a shallow copy of the first graph to be used
		 * as a basis for merging */
		NGramGraph nggInit = (NGramGraph) toMerge[0].clone();
		double lr = 0.5;
		for (int index = 1; index < toMerge.length; ++index) {
			/* update "learning" rate */
			lr = 1 - (((double) index) / (index + 1));
			nggInit.merge(toMerge[index], lr);
		}

		return nggInit;
	}

	/**
	 * Merges a list of {@link NGramGraph} objects.
	 * @see #mergeGraphs(NGramGraph[])
	 */
	public static NGramGraph mergeGraphs(List<NGramGraph> toMerge) {
		if ((toMerge == null) || (toMerge.size() == 0)) {
			return null;
		}

		/* proxy call to merge graphs with array parameter */
		return mergeGraphs(toMerge.toArray(new NGramGraph[toMerge.size()]));
	}

	/**
	 * Performs noise filtering on an array of {@link NGramJGraph} objects by
	 * computing their intersection first and then applying the all-not-in 
	 * operator on each graph of the array - all the graphs in the original
	 * array remain unmodified. Returns <tt>null</tt> if the array
	 * is empty.
	 *
	 * @param graphs an array of graphs to be filtered
	 * @return the array of noise-filtered graphs
	 */
	public static NGramGraph[] removeNoise(NGramGraph[] graphs) {
		if (graphs.length == 0)
			return null;

		/* create a base graph, store the intersection of all graphs in it */
		NGramGraph nggBase = (NGramGraph) graphs[0].clone();
		for (int index = 1; index < graphs.length; ++index) {
			nggBase = nggBase.intersectGraph(graphs[index]);
		}

		/* create a new array, store the filtered versions there */
		NGramGraph[] filtered = new NGramGraph[graphs.length];
		for (int index = 0; index < filtered.length; ++index) {
			filtered[index] = graphs[index].allNotIn(nggBase);
		}

		return filtered;
	}

	/**
	 * Performs noise filtering on a list of {@link NGramGraph} objects by
	 * computing their intersection first and then applying the all-not-in 
	 * operator on each graph of the initial list - all the graphs in the
	 * original list remain unmodified. Returns <tt>null</tt> if 
	 * the list is null or empty.
	 *
	 * @param graphs the list of graphs to be filtered
	 * @return the list of noise-filtered graphs
	 */
	public static List<NGramGraph> removeNoise(List<NGramGraph> graphs) {
		if ((graphs == null) || (graphs.size() == 0))
			return null;

		/* create a base graph and store the intersection of graphs in it */
		NGramGraph nggBase = (NGramGraph) graphs.get(0).clone();
		for (int index = 1; index < graphs.size(); ++index) {
			nggBase = nggBase.intersectGraph(graphs.get(index));
		}

		/* create a new list, store filtered versions there */
		List<NGramGraph> filtered = new ArrayList<NGramGraph>(graphs.size());
		for (int index = 0; index < graphs.size(); ++index) {
			filtered.add(index, graphs.get(index).allNotIn(nggBase));
		}

		return filtered;
	}
}
