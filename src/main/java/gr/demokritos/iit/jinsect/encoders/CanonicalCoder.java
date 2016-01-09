package gr.demokritos.iit.jinsect.encoders;

import java.lang.UnsupportedOperationException;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;

public class CanonicalCoder 
extends BaseGraphEncoder implements GraphEncoding, Iterable<String> {

	// 2 strings, one for each pass
	private String fwdEncodedString = "";
	private String bwdEncodedString = "";

	// default separator for DFS coding
	private String SEPARATOR = "";

	/**
	 * Initializes only the unvisited set with a tree set structure that
	 * contains the graph vertices in lexicographic order for ordered 
	 * iteration when producing the canonical code. 
	 */
	@Override
	protected void initSets() {
		// comparator that sorts on lexicographic ordering
		Comparator<JVertex> fwdComp = new Comparator<JVertex>() {
			@Override
			public int compare(JVertex vOne, JVertex vTwo) {
				return vOne.getLabel().compareTo(vTwo.getLabel());
			}
		};

		// only unvisited is initialized
		unvisited = new TreeSet<JVertex>(fwdComp);
	}

	/**
	 * Creates a new CanonicalCoder object to operate
	 * on a given UniqueJVertexGraph.
	 *
	 * @param uvg the graph to be encoded
	 * @return a new CanonicalCoder object
	 */
	public CanonicalCoder(UniqueJVertexGraph uvg) {
		super(uvg);
		unvisited.addAll(uvg.vertexSet());
	}

	/**
	 * Creates a new CanonicalCoder object to operate on
	 * a given UniqueJVertexGraph starting from a provided node.
	 *
	 * @param uvg the graph to be encoded
	 * @param vFrom the vertex to start encoding from
	 */
	public CanonicalCoder(UniqueJVertexGraph uvg, JVertex vFrom) {
		super(uvg, vFrom);
		unvisited.addAll(uvg.vertexSet());
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

	/* Fwd encoding for NGramJGraph should start from the 
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
		return encodeFrom(vFrom);
	}

	/**
	 * Encodes the graph starting from a provided vertex.
	 *
	 * @param source the node to start encoding from.
	 * @return the string representation of the encoded graph
	 */
	private String encodeFrom(JVertex vFrom) {
		String sEncoded = "";
		JVertex vCurr;

		// handle null start case - usually for NGG representations
		// of strings with length < NGG size
		if (vFrom == null) {
			return sEncoded;
		}
	
		/* labels for forward and backward canonical code */
		StringBuilder fwdLabels = new StringBuilder();
		StringBuilder bwdLabels = new StringBuilder();
		
		/* Iterators over vertices */
		Iterator<JVertex> fwdIterator = unvisited.iterator();
		Deque<JVertex> alreadySeen = new ArrayDeque<JVertex>(); 
		
		while (fwdIterator.hasNext()) {
			vCurr = fwdIterator.next();
	
			// push to already seen
			alreadySeen.push(vCurr);

			// add curr vertex label to fwd and bwd
			fwdLabels.append(vCurr.getLabel() + " ");
			bwdLabels.append(vCurr.getLabel() + " ");

			// iterate over all seen - so - far vertices, build canonical codes
			for (JVertex vOpp: alreadySeen) {
				Edge eFwd = nGraph.getEdge(vCurr, vOpp);
				Edge eBwd = nGraph.getEdge(vOpp, vCurr);

				if (eFwd == null) 
					fwdLabels.append("0 ");
				else
					fwdLabels.append(String.valueOf(eFwd.edgeWeight()) + " ");

				if (eBwd == null)
					bwdLabels.append("0 ");
				else
					bwdLabels.append(String.valueOf(eBwd.edgeWeight()) + " ");
			}
		}

		sEncoded = fwdLabels.toString() + bwdLabels.toString();
		return sEncoded;
	}

	@Override
	public Iterator<String> iterator() {
		Iterator<String> it = new Iterator<String>() {
			private int curInd = 0;
			private int finalInd = unvisited.size(); // size of treemap
			
			private Deque<JVertex> alrSeen = new ArrayDeque();
			private Iterator<JVertex> fwdIter = unvisited.iterator();

			@Override
			public boolean hasNext() {
				return curInd < finalInd;
			}

			@Override 
			public String next() {
				String retFwd, retBwd;
				JVertex vCurr = fwdIter.next();

				// init label of code line
				retFwd = vCurr.getLabel() + " ";
				retBwd = vCurr.getLabel() + " ";

				// push to deque
				alrSeen.push(vCurr);

				for (JVertex vOpp: alrSeen) {
					Edge eFwd = nGraph.getEdge(vCurr, vOpp);
					Edge eBwd = nGraph.getEdge(vOpp, vCurr);
					if (eFwd == null) 
						retFwd += "0 ";
					else
						retFwd += String.valueOf(eFwd.edgeWeight()) + " ";
					if (eBwd == null)
						retBwd += "0 ";
					else
						retBwd += String.valueOf(eBwd.edgeWeight()) + " ";
				}

				// increment index
				curInd++;

				return retFwd + retBwd;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return it;
	}
}
