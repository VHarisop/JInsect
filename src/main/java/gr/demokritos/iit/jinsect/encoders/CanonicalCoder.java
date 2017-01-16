package gr.demokritos.iit.jinsect.encoders;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

public class CanonicalCoder
extends BaseGraphEncoder implements GraphEncoding {

	/**
	 * Creates a new CanonicalCoder object.
	 */
	public CanonicalCoder() {
		super();
	}

	/* Fwd encoding for NGramJGraph should start from the
	 * lexicographically minimum vertex */
	@Override
	protected JVertex chooseStart(final UniqueVertexGraph uvg) {
		JVertex vMin = null;

		for (JVertex vCur: uvg.vertexSet()) {
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

	public String getEncoding(final UniqueVertexGraph uvg) {
		return encodeFrom(uvg, chooseStart(uvg));
	}

	public String getEncoding(
		final UniqueVertexGraph uvg, final JVertex vFrom) {
		return encodeFrom(uvg, vFrom);
	}

	/**
	 * Encodes the graph starting from a provided vertex.
	 *
	 * @param source the node to start encoding from.
	 * @return the string representation of the encoded graph
	 */
	private String encodeFrom(
		final UniqueVertexGraph uvg,
		final JVertex vFrom) {

		Set<JVertex> unvisited = new TreeSet<>(
			(v1, v2) -> v1.getLabel().compareTo(v2.getLabel()));
		unvisited.addAll(uvg.vertexSet());

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
				Edge eFwd = uvg.getEdge(vCurr, vOpp);
				Edge eBwd = uvg.getEdge(vOpp, vCurr);

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

	/**
	 * Returns an iterator over a given {@link UniqueVertexGraph} that returns
	 * the graph's canonical code.
	 * @param uvg the graph to iterate over
	 * @return an iterator over the graph's canonical code
	 */
	public static Iterator<String> iterator(final UniqueVertexGraph uvg) {
		/* Create a new set of unvisited vertices */
		final Set<JVertex> unvisited = new TreeSet<>(
				(v1, v2) -> v1.getLabel().compareTo(v2.getLabel()));
		unvisited.addAll(uvg.vertexSet());
		final Deque<JVertex> alrSeen = new ArrayDeque<>();
		Iterator<JVertex> fwdIter = unvisited.iterator();

		Iterator<String> iter = new Iterator<String>() {
			private int curInd = 0;
			private final int finalInd = unvisited.size();

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
					Edge eFwd = uvg.getEdge(vCurr, vOpp);
					Edge eBwd = uvg.getEdge(vOpp, vCurr);
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
		};
		return iter;
	}
}
