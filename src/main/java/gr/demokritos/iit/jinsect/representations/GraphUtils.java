package gr.demokritos.iit.jinsect.representations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
	 * Merges an array of {@link NGramJGraph}s, using a copy of the first
	 * graph of the array as the basis for the merging process - no graph
	 * in the array is modified. The learning rate used for merging is the
	 * weighted average. Returns <tt>null</tt> if the array is empty.
	 *
	 * @param toMerge the array of graphs to merge
	 * @return the graph that results from merging all graphs in the array
	 */
	public static NGramGraph mergeGraphs(final NGramGraph[] toMerge) {
		if (toMerge.length == 0)
			return null;

		/* make a shallow copy of the first graph to be used
		 * as a basis for merging */
		final NGramGraph nggInit = toMerge[0].clone();
		double lr = 0.5;
		for (int index = 1; index < toMerge.length; ++index) {
			/* update "learning" rate */
			lr = 1 - (((double) index) / (index + 1));
			nggInit.merge(toMerge[index], lr);
		}

		return nggInit;
	}

	/**
	 * Merges a list of {@link NGramJGraph}s, using a copy of the first
	 * graph of the list as the basis for the merging process - no graph
	 * in the list is modified. The learning rate used for merging is the
	 * weighted average. Returns <tt>null</tt> if the list is empty.
	 *
	 * @param toMerge the list of graphs to merge
	 * @return the graph that results from merging all graphs in the list
	 */
	public static NGramGraph mergeGraphs(final List<NGramGraph> toMerge) {
		if ((toMerge == null) || (toMerge.size() == 0)) {
			return null;
		}
		final NGramGraph nggInit = toMerge.get(0).clone();
		double lr = 0.5;
		for (int index = 1, n = toMerge.size(); index < n; ++index) {
			/* Update "learning" rate */
			lr = 1 - (((double) index) / (index + 1));
			nggInit.merge(toMerge.get(index), lr);
		}
		return nggInit;
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
	public static NGramGraph[] removeNoise(final NGramGraph[] graphs) {
		if (null == graphs || graphs.length == 0)
			return null;

		/* create a base graph, store the intersection of all graphs in it */
		NGramGraph nggBase = graphs[0].clone();
		for (int index = 1; index < graphs.length; ++index) {
			nggBase = nggBase.intersectGraph(graphs[index]);
		}

		/* create a new array, store the filtered versions there */
		final NGramGraph[] filtered = new NGramGraph[graphs.length];
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
	public static List<NGramGraph> removeNoise(final List<NGramGraph> graphs) {
		if ((graphs == null) || (graphs.size() == 0))
			return null;

		/* create a base graph and store the intersection of graphs in it */
		NGramGraph nggBase = graphs.get(0).clone();
		for (int index = 1; index < graphs.size(); ++index) {
			nggBase = nggBase.intersectGraph(graphs.get(index));
		}

		/* create a new list, store filtered versions there */
		final List<NGramGraph> filtered = new ArrayList<>(graphs.size());
		for (final NGramGraph ngg: graphs) {
			filtered.add(ngg.allNotIn(nggBase));
		}

		return filtered;
	}

	/**
	 * Creates a callable task that merges a list of graphs into a
	 * model graph. The graphs are first denoised.
	 * @param toMerge the list of graphs to merge
	 * @return a callable task that will perform the merging
	 */
	public static Callable<NGramGraph>
	mergeTask(final List<NGramGraph> toMerge) {
		return () -> {
			return mergeGraphs(removeNoise(toMerge));
		};
	}
}
