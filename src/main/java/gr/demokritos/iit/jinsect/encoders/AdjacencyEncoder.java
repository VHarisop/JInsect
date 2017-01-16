package gr.demokritos.iit.jinsect.encoders;

import java.util.Map;

import gr.demokritos.iit.jinsect.JUtils;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

public class AdjacencyEncoder implements GraphEncoding {

	protected final Map<Character, Integer> charIndex;
	protected final int rank;

	/**
	 * Creates a new AdjacencyEncoder, given a map from characters to
	 * numeric indices and the rank of the graphs to be encoded.
	 * @param charIndex a {@link Map} from chars to numerals
	 * @param rank the n-gram rank
	 */
	public AdjacencyEncoder(
		final Map<Character, Integer> charIndex, final int rank) {
		this.charIndex = charIndex;
		this.rank = rank;
	}

	@Override
	public final String getEncoding(final UniqueVertexGraph uvg) {
		final int numLabels = JUtils.intPow(charIndex.size(), rank);
		final byte[] adjMat = new byte[numLabels * numLabels];
		for (Edge e: uvg.edgeSet()) {
			/* Get the index that this edge designates and set it
			 * equal to 1
			 */
			final int iFrom = getNGramIndex(e.getSourceLabel());
			final int iTo = getNGramIndex(e.getTargetLabel());
			adjMat[iFrom * numLabels + iTo] = 0x1;
		}
		/* Convert byte array to string */
		StringBuilder sb = new StringBuilder();
		for (byte b: adjMat) {
			sb.append(b == 0x1 ? '1': '0');
		}
		return sb.toString();
	}


	@Override
	public final String getEncoding(
		final UniqueVertexGraph uvg, final JVertex vStart) {
		return getEncoding(uvg);
	}

	/**
	 * Returns the adjacency vector encoding of a given graph in a packed
	 * format, using the {@link JUtils#packCharArray(String)} method.
	 * @param uvg the graph to encode
	 * @return the adjacency vector encoding of the graph as a packed
	 * string
	 */
	public final String getPackedEncoding(final UniqueVertexGraph uvg) {
		return JUtils.packCharArray(getEncoding(uvg));
	}

	/**
	 * Returns the numeric index of a given n-gram, using a provided character
	 * to index mapping.
	 *
	 * @param ngram the n-gram
	 * @return the numeric index of the n-gram
	 */
	protected final int getNGramIndex(final String ngram) {
		/* Alphabet size is the number of keys in the index */
		int totalIndex = 0;
		final int n = ngram.length();
		try {
			for (int i = 0; i < n; ++i) {
				final int index = charIndex.get(ngram.charAt(i));
				totalIndex += JUtils.intPow(n, i) * index;
			}
		}
		/* If character is unseen, return -1 as flag value */
		catch (final NullPointerException ex) {
			totalIndex = -1;
		}
		return totalIndex;
	}

}
