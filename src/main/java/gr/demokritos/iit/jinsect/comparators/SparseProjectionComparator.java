package gr.demokritos.iit.jinsect.comparators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * A class that implements a comparator for {@link NGramGraph} objects.
 * Comparison is done by first projecting the adjacency matrix to a low
 * dimensional feature vector and then taking the manhattan distance of
 * the feature vectors.
 *
 * References:
 * [1]: Ping Li et. al. - Very sparse random projections (2006)
 * @author vharisop
 *
 */
public class SparseProjectionComparator {
	protected final int rank;
	protected final int finalDim;
	protected final Map<Character, Integer> charIndex;

	protected List<Integer> positiveFactors = new LinkedList<>();
	protected List<Integer> negativeFactors = new LinkedList<>();

	/**
	 * Creates a new {@link SparseProjectionComparator} with the intention
	 * of projecting to a feature space of a specified dimension.
	 *
	 * @param charIndex a character-to-index mapping
	 * @param rank the rank of each n-gram
	 * @param finalDimension the desired dimension of the projected feature space
	 */
	public SparseProjectionComparator(
		final Map<Character, Integer> characterIndex,
		final int nGramRank,
		final int finalDimension)
	{
		rank = nGramRank;
		finalDim = finalDimension;
		charIndex = characterIndex;
		createProjectionMatrix();
	}

	/**
	 * Compares two {@link UniqueVertexGraph}s and returns the distance
	 * between them as the manhattan distance between their projected
	 * adjacency vectors.
	 * @param uvgA the first graph
	 * @param uvgB the second graph
	 * @param rank the n-gram rank
	 * @return the graph distance
	 */
	public final double getDistance(
		final UniqueVertexGraph uvgA, final UniqueVertexGraph uvgB)
	{
		/* Get the projected vectors of both graphs and
		 * calculate their manhattan distance.
		 */
		final double[] vecA = getProjectedVector(uvgA);
		final double[] vecB = getProjectedVector(uvgB);
		return IntStream.range(0, vecA.length)
			.mapToDouble(i -> Math.abs(vecA[i] - vecB[i]))
			.sum();
	}

	/**
	 * Compares two {@link UniqueVertexGraph}s and returns the similarity
	 * between them as the total value similarity between their projected
	 * adjacency vectors.
	 * @param uvgA the first graph
	 * @param uvgB the second graph
	 * @param rank the n-gram rank
	 * @return the graph similarity
	 */
	public final double getSimilarity(
		final UniqueVertexGraph uvgA, final UniqueVertexGraph uvgB)
	{
		/* Get the projected vectors of both graphs and
		 * calculate their manhattan distance.
		 */
		final double[] vecA = getProjectedVector(uvgA);
		final double[] vecB = getProjectedVector(uvgB);
		return IntStream.range(0, vecA.length)
			.mapToDouble(i -> {
				final double wA = vecA[i];
				final double wB = vecB[i];
				if ((wA == 0.0) || (wB == 0.0)) {
					return 0.0;
				}
				else {
					return Math.min(wA, wB) / Math.max(wA, wB);
				}
			})
			.sum();
	}

	/**
	 * Creates a sparse random projection matrix using the distribution
	 * mentioned in [1]. Values of {-1, 1} are chosen with a probability
	 * of 1 / sqrt(D) each, where D is the feature dimension. The matrix
	 * is not stored itself, but the indices with positive or negative
	 * factors are stored in
	 * {@link #positiveFactors} and {@link #negativeFactors}, respectively.
	 */
	private void createProjectionMatrix() {
		final int nGramCount = (int) Math.pow(charIndex.size(), rank);
		final int featureDim = nGramCount * nGramCount;
		final double sigma = Math.sqrt(featureDim);
		for (int i = 0; i < featureDim; ++i) {
			for (int j = 0; j < finalDim; ++j) {
				/* Coin toss to decide which element we'll use */
				final double toss = Math.random();
				/* P1: 1/2s */
				if (toss < (1 / (2 * sigma))) {
					positiveFactors.add(i * finalDim + j);
				}
				/* P2: > 1/2s, < 1/s */
				else if (toss < (1 / sigma)) {
					negativeFactors.add(i * finalDim + j);
				}
				/* P3: 1 - 1/s */
			}
		}
	}

	/**
	 * Projects the adjacency vector of a {@link UniqueVertexGraph} to a
	 * space of lower dimensionality.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @return the projected vector as a {@link double[]}
	 */
	public final double[] getProjectedVector(final UniqueVertexGraph uvg) {
		/* Get the adjacency vector, initialize the projection */
		final double[] adjVec = generateAdjacencyVector(uvg);
		final double[] projVec = new double[finalDim];
		/* Add all positive factors */
		positiveFactors.forEach(i -> {
			final int featIndex = i / finalDim;
			final int projIndex = i % finalDim;
			projVec[projIndex] += adjVec[featIndex];
		});
		/* Add all negative factors */
		negativeFactors.forEach(i -> {
			final int featIndex = i / finalDim;
			final int projIndex = i % finalDim;
			projVec[projIndex] -= adjVec[featIndex];
		});
		return projVec;
	}

	/**
	 * Generates the adjacency vector for a given {@link UniqueVertexGraph},
	 * given the underlying character indices and the rank of the n-grams in
	 * the graph.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @param rank the n-gram rank
	 * @return a {@link double[]} vector containing the adjacency matrix
	 */
	public final double[]
	generateAdjacencyVector(final UniqueVertexGraph uvg) {
		final int adjRowSize = (int) Math.pow(charIndex.size(), rank);
		/* Note: double array components are initialized to zero. */
		final double[] adjacencyVector = new double[adjRowSize * adjRowSize];
		uvg.edgeSet().forEach(e -> {
			final int indexFrom = getNGramIndex(e.getSourceLabel());
			final int indexTo = getNGramIndex(e.getTargetLabel());
			final int totalIndex = indexFrom * adjRowSize + indexTo;
			adjacencyVector[totalIndex] += e.edgeWeight();
		});
		return adjacencyVector;
	}

	/**
	 * Returns the numeric index of a given n-gram, using a provided character
	 * to index mapping.
	 *
	 * @param charIndex a {@link Map<Character, Integer>}
	 * @param ngram the n-gram
	 * @return the numeric index of the n-gram
	 */
	public final int getNGramIndex(final String ngram) {
		/* Alphabet size is the number of keys in the index */
		final int alphabetSize = charIndex.size();
		int totalIndex = 0;
		for (int i = 0, n = ngram.length(); i < n; ++i) {
			final int index = charIndex.get(ngram.charAt(i));
			totalIndex += ((int) Math.pow(alphabetSize, i)) * index;
		}
		return totalIndex;
	}

	/**
	 * @return the rank of this comparator
	 */
	public final int getRank() {
		return rank;
	}
}
