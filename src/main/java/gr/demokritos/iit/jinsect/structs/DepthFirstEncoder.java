package gr.demokritos.iit.jinsect.structs;

import java.lang.UnsupportedOperationException;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class DepthFirstEncoder 
extends BaseGraphEncoder implements GraphEncoding {

	private String encodedString = "";
	private Deque<JVertex> stack;

	// default separator for DFS coding
	private String SEPARATOR = "";

	/**
	 * Creates a new DepthFirstEncoder object to operate
	 * on a given UniqueJVertexGraph.
	 *
	 * @param uvg the graph to be encoded
	 * @return a new DepthFirstEncoder object
	 */
	public DepthFirstEncoder(UniqueJVertexGraph uvg) {
		super(uvg);
		stack = new ArrayDeque<JVertex>();
	}

	/**
	 * Creates a new DepthFirstEncoder object to operate on
	 * a given UniqueJVertexGraph starting from a provided node.
	 *
	 * @param uvg the graph to be encoded
	 * @param vFrom the vertex to start encoding from
	 * @return a new DepthFirstEncoder object
	 */
	public DepthFirstEncoder(UniqueJVertexGraph uvg, JVertex vFrom) {
		super(uvg, vFrom);
		stack = new ArrayDeque<JVertex>();
	}
	
	/**
	 * @see GraphEncoding.getEncoding()
	 */
	public String getEncoding() {
		if (vStart != null) {
			return getEncoding(vStart);
		}
		else {
			return getEncoding(chooseStart());
		}
	}

	/* DFS encoding for NGramJGraph should start from the 
	 * lexicographically minimum vertex */
	@Override
	protected JVertex chooseStart() {
		JVertex vMin = null;

		for (JVertex vCur: nGraph.vertexSet()) {
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
	public String getEncoding(JVertex vFrom) {
		encodedString = encodeFrom(vFrom);
		return encodedString;
	}

	/**
	 * Encodes the graph starting from a provided vertex.
	 *
	 * @param source the node to start encoding from.
	 * @return the string representation of the encoded graph
	 */
	private String encodeFrom(JVertex vFrom) {
		String sEncoded = ""; JVertex vNext;

		// handle null start case - usually for NGG representations
		// of strings with length < NGG size
		if (vFrom == null) {
			return sEncoded;
		}


		StringBuilder fwdLabels = new StringBuilder();
		StringBuilder bwdLabels = new StringBuilder();

		/* custom comparator for two edges 
		 * that sorts them according to lexicographic order */
		Comparator<Edge> eComp = new Comparator<Edge>(){
			@Override
			public int compare(final Edge e1, final Edge e2) {
				return e1.getLabels().compareTo(e2.getLabels());
			}
		};

		/* custom comparator for vertices that compares them
		 * based on the lexicographic ordering of their labels */
		Comparator<JVertex> vComp = new Comparator<JVertex>(){
			@Override
			public int compare(final JVertex v1, final JVertex v2) {
				return v1.getLabel().compareTo(v2.getLabel());
			}
		};

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
			if (!(visitNode(vNext))) {
				return sEncoded;
			}

			/* acquire a sorted list of edges */
			List<Edge> eList = outgoingEdgeList(vNext);
			Collections.sort(eList, eComp);

			for (Edge e: eList) {
				vAdj = nGraph.getEdgeTarget(e);

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
			if (fwdLabels.length() > 0)
				fwdLabels.append(this.SEPARATOR);
			if (bwdLabels.length() > 0)
				bwdLabels.append(this.SEPARATOR);

			sEncoded += bwdLabels.toString() + fwdLabels.toString();
			fwdLabels.setLength(0); bwdLabels.setLength(0);
	
		} while (!stack.isEmpty());

		// if unvisited nodes remain, explore them
		if (unvisited.size() != 0) {
			List<JVertex> unvList = new ArrayList<JVertex>(unvisited);
			Collections.sort(unvList, vComp);
			for (JVertex v: unvList) {
				sEncoded += encodeFrom(v);
			}
		}
		
		return sEncoded;
	}
}
