package gr.demokritos.iit.jinsect.documentModel.representations;

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
	 * GraphUtils is intended to be used as a static class.
	 */
	private GraphUtils() {}

	/**
	 * Merges an array of {@link NGramJGraph} objects, using a copy of the first
	 * graph of the array as the basis for the merging process. The learning
	 * rate used for merging is the weighted average. Returns <tt>null</tt> if
	 * the array is empty.
	 *
	 * @param toMerge the array of graphs to merge
	 * @return the graph that results from merging all graphs in the array
	 */
	public static NGramJGraph mergeGraphs(NGramJGraph[] toMerge) {
		if (toMerge.length == 0) 
			return null;

		/* make a shallow copy of the first graph to be used
		 * as a basis for merging */
		NGramJGraph nggInit = (NGramJGraph) toMerge[0].clone();
		double lr = 0.5;
		for (int index = 1; index < toMerge.length; ++index) {
			/* update "learning" rate */
			lr = 1 - (((double) index) / (index + 1));
			nggInit.merge(toMerge[index], lr);
		}

		return nggInit;
	}

	/**
	 * Merges a list of {@link NGramJGraph} objects.
	 * @see #mergeGraphs(NGramJGraph[])
	 */
	public static NGramJGraph mergeGraphs(List<NGramJGraph> toMerge) {
		if ((toMerge == null) || (toMerge.size() == 0)) {
			return null;
		}

		/* proxy call to merge graphs with array parameter */
		return mergeGraphs(toMerge.toArray(new NGramJGraph[toMerge.size()]));
	}

	/**
	 * Performs noise filtering on an array of {@link NGramJGraph} objects by
	 * computing their intersection first and then applying the all-not-in 
	 * operator on each graph of the array. Returns <tt>null</tt> if the array
	 * is empty.
	 *
	 * @param graphs the array of graphs to be filtered
	 * @return the array of noise-filtered graphs
	 */
	public static NGramJGraph[] removeNoise(NGramJGraph[] graphs) {
		if (graphs.length == 0)
			return null;

		/* create a base graph, store the intersection of all graphs in it */
		NGramJGraph nggBase = (NGramJGraph) graphs[0].clone();
		for (int index = 1; index < graphs.length; ++index) {
			nggBase = nggBase.intersectGraph(graphs[index]);
		}

		/* create a new array, store the filtered versions there */
		NGramJGraph[] filtered = new NGramJGraph[graphs.length];
		for (int index = 0; index < filtered.length; ++index) {
			filtered[index] = graphs[index].allNotIn(nggBase);
		}

		return filtered;
	}

	/**
	 * Performs noise filtering on a list of {@link NGramJGraph} objects by
	 * computing their intersection first and then applying the all-not-in 
	 * operator on each graph of the initial list. Returns <tt>null</tt> if 
	 * the list is empty or has zero size.
	 *
	 * @param graphs the list of graphs to be filtered
	 * @return the list of noise-filtered graphs
	 */
	public static List<NGramJGraph> removeNoise(List<NGramJGraph> graphs) {
		if ((graphs == null) || (graphs.size() == 0))
			return null;

		/* create a base graph and store the intersection of graphs in it */
		NGramJGraph nggBase = (NGramJGraph) graphs.get(0).clone();
		for (int index = 1; index < graphs.size(); ++index) {
			nggBase = nggBase.intersectGraph(graphs.get(index));
		}

		/* create a new list, store filtered versions there */
		List<NGramJGraph> filtered = new ArrayList<NGramJGraph>(graphs.size());
		for (int index = 0; index < graphs.size(); ++index) {
			filtered.add(index, graphs.get(index).allNotIn(nggBase));
		}

		return filtered;
	}
}
