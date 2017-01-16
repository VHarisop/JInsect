package gr.demokritos.iit.jinsect.encoders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

public class DepthFirstEncoder
extends BaseGraphEncoder implements GraphEncoding {

	// default separator for DFS coding
	private final String SEPARATOR = "";

	/**
	 * Creates a new DepthFirstEncoder object.
	 */
	public DepthFirstEncoder() {
	}

	/**
	 * @see GraphEncoding.getEncoding(UniqueVertexGraph)
	 */
	public String getEncoding(final UniqueVertexGraph uvg) {
		return getEncoding(uvg, chooseStart(uvg));
	}

	/* DFS encoding for NGramJGraph should start from the
	 * lexicographically minimum vertex */
	@Override
	protected JVertex chooseStart(final UniqueVertexGraph uvg) {
		JVertex vMin = null;
		for (final JVertex vCur: uvg.vertexSet()) {
			if (vMin == null) {
				vMin = vCur;
			}

			// if vCur's label is lexicographically smaller
			if (vCur.getLabel().compareTo(vMin.getLabel()) < 0) {
				vMin = vCur;
			}
		}
		// return minimum vertex
		return vMin;
	}

	/**
	 * @see GraphEncoding.getEncoding(JVertex)
	 */
	@Override
	public String getEncoding(
		final UniqueVertexGraph uvg, final JVertex vFrom) {
		// Create visited and unvisited vertex sets
		final Set<JVertex> unvisited = new TreeSet<>(
			(vA, vB) -> vA.getLabel().compareTo(vB.getLabel()));
		final Set<JVertex> visited = new TreeSet<>(
			(vA, vB) -> vA.getLabel().compareTo(vB.getLabel()));
		unvisited.addAll(uvg.vertexSet());
		final Deque<JVertex> stack = new ArrayDeque<>();
		return encodeFrom(uvg, vFrom, visited, unvisited, stack);
	}

	/**
	 * Encodes the graph starting from a provided vertex.
	 *
	 * @param source the node to start encoding from.
	 * @return the string representation of the encoded graph
	 */
	private String encodeFrom(
		final UniqueVertexGraph uvg, final JVertex vFrom,
		final Set<JVertex> visited, final Set<JVertex> unvisited,
		final Deque<JVertex> stack) {
		String sEncoded = ""; JVertex vNext;

		// handle null start case - usually for NGG representations
		// of strings with length < NGG size
		if (vFrom == null) {
			return sEncoded;
		}

		final StringBuilder fwdLabels = new StringBuilder();
		final StringBuilder bwdLabels = new StringBuilder();

		/* custom comparator for two edges
		 * that sorts them according to lexicographic order */
		final Comparator<Edge> eComp = (e1, e2) ->
			e1.getLabels().compareTo(e2.getLabels());

		/* custom comparator for vertices that compares them
		 * based on the lexicographic ordering of their labels */
		final Comparator<JVertex> vComp = (v1, v2) ->
			v1.getLabel().compareTo(v2.getLabel());

		/* push starting point on the stack */
		stack.push(vFrom);
		JVertex vAdj;
		/* iterate until graph is explored */
		do {
			vNext = stack.pop();

			/* if node is already visited, return string so far
			 * This return is required so that exploration for
			 * unvisited nodes (after end of while loop) does not
			 * produce duplicates.
			 */
			if (!(visitNode(visited, unvisited, vNext))) {
				return sEncoded;
			}

			/* acquire a sorted list of edges */
			final List<Edge> eList = outgoingEdgeList(uvg, vNext);
			Collections.sort(eList, eComp);

			for (final Edge e: eList) {
				vAdj = uvg.getEdgeTarget(e);

				/* if node has not been visited, add it to the
				 * stack and create a forward edge label.
				 * Otherwise, create a backward edge label */
				if (!(visited.contains(vAdj))) {
					stack.push(vAdj);
					fwdLabels.append(e.getLabels() + "|");
				}
				else {
					bwdLabels.append(e.getLabels() + "|");
				}
			}

			// add separator if strings aren't empty
			if (fwdLabels.length() > 0) {
				fwdLabels.append(this.SEPARATOR);
			}
			if (bwdLabels.length() > 0) {
				bwdLabels.append(this.SEPARATOR);
			}

			sEncoded += bwdLabels.toString() + fwdLabels.toString();
			fwdLabels.setLength(0); bwdLabels.setLength(0);

		} while (!stack.isEmpty());

		// if unvisited nodes remain, explore them
		if (unvisited.size() != 0) {
			final List<JVertex> unvList = new ArrayList<>(unvisited);
			Collections.sort(unvList, vComp);
			for (final JVertex v: unvList) {
				sEncoded += encodeFrom(uvg, v, visited, unvisited, stack);
			}
		}
		return sEncoded;
	}
}
