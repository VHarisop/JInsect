package gr.demokritos.iit.jinsect.comparators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
 * [2]: Zhu et. al. - Sparse sign-consistent Johnson–Lindenstrauss matrices:
 * Compression with neuroscience-based constraints
 * @author vharisop
 *
 */
public class SparseProjectionComparator {
	protected final int rank;
	protected final int finalDim;
	protected final int nGramCount;
	protected final int alphabetSize;
	protected final Map<Character, Integer> charIndex;

	/* 2 maps holding the positive and negative factors
	 * for every index of the final vector
	 */
	protected Map<Integer, Set<Integer>> positives =
		new HashMap<>();
	protected Map<Integer, Set<Integer>> negatives =
		new HashMap<>();

	protected Projection projType;

	/**
	 * An enum containing the available projection types for this
	 * projection comparator.
	 * @author vharisop
	 */
	public static enum Projection {
		SIGN_CONSISTENT, RANDOM;
	}

	/**
	 * Creates a new {@link SparseProjectionComparator} with the intention
	 * of projecting to a feature space of a specified dimension.
	 *
	 * @param charIndex a character-to-index mapping
	 * @param rank the rank of each n-gram
	 * @param finalDimension the desired dimension of the projected feature space
	 * @param projType the type of the projection
	 */
	public SparseProjectionComparator(
		final Map<Character, Integer> characterIndex,
		final int nGramRank,
		final int finalDimension,
		final Projection projectionType)
	{
		rank = nGramRank;
		finalDim = finalDimension;
		charIndex = characterIndex;
		projType = projectionType;
		nGramCount = (int) Math.pow(charIndex.size(), rank);
		alphabetSize = charIndex.size();
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
		double simSum = 0.0;
		switch (projType) {
			case RANDOM:
				for (int i = 0; i < vecA.length; ++i) {
					final double wA = vecA[i];
					final double wB = vecB[i];
					if ((wA == 0.0) || (wB == 0.0)) {
						continue;
					}
					simSum += Math.min(wA, wB) / Math.max(wA, wB);
				}
				break;
			case SIGN_CONSISTENT:
			default:
				for (int i = 0; i < vecA.length; ++i) {
					final double wA = Math.abs(vecA[i]);
					final double wB = Math.abs(vecB[i]);
					if ((wA == 0.0) || (wB == 0.0)) {
						continue;
					}
					simSum += Math.min(wA, wB) / Math.max(wA, wB);
				}
				break;
		}
		return simSum;
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
		final int featureDim = nGramCount * nGramCount;
		final double sigma = nGramCount;

		switch (projType) {
			case RANDOM:
				createRandomProjection(featureDim, sigma);
				break;
			case SIGN_CONSISTENT:
			default:
				createSCProjection(featureDim, sigma);
				break;
		}
	}

	/**
	 * Creates a random projection matrix, given the sparsity parameter
	 * and the original dimension.
	 * @param featureDim the original dimension
	 * @param sigma the sparsity parameter
	 */
	private void
	createRandomProjection(final int featureDim, final double sigma) {
		for (int j = 0; j < finalDim; ++j) {
			/* Retrieve sets for current index and
			 * initialize them if necessary
			 */
			Set<Integer> currPos = getPositiveIndexFactors(j);
			Set<Integer> currNeg = getNegativeIndexFactors(j);
			for (int i = 0; i < featureDim; ++i) {
				/* Coin toss to decide which element we'll use */
				final double toss = Math.random();
				/* P1: 1/2s */
				if (toss < (1 / (2 * sigma))) {
					currPos.add(i * finalDim + j);
				}
				/* P2: > 1/2s, < 1/s */
				else if (toss < (1 / sigma)) {
					currNeg.add(i * finalDim + j);
				}
				/* P3: 1 - 1/s */
			}
		}
	}

	/**
	 * Creates a random, sign-consistent projection matrix, given the
	 * sparsity parameter and the original dimension.
	 * @param featureDim the original dimension
	 * @param sigma the sparsity parameter
	 */
	private void
	createSCProjection(final int featureDim, final double sigma) {
		/* Sign consistent projection: same sign for all coefficients
		 * in a given column.
		 */
		for (int j = 0; j < finalDim; ++j) {
			/* Retrieve sets for current index and
			 * initialize them if necessary
			 */
			Set<Integer> currPos = getPositiveIndexFactors(j);
			Set<Integer> currNeg = getNegativeIndexFactors(j);
			/* Fair coin toss, decide sign */
			final int sign = Math.random() > 0.5 ? 1 : -1;
			for (int i = 0; i < featureDim; ++i) {
				/* Coin toss to decide which element we'll use */
				final double toss = Math.random();
				/* P1: 1/s */
				if (toss < (1 / sigma)) {
					if (sign > 0)
						currPos.add(i * finalDim + j);
					else
						currNeg.add(i * finalDim + j);
				}
				/* P2: 1 - 1/s */
			}
		}
	}

	/**
	 * Retrieves the set of positive factor indices for a given index in the
	 * projection vector, initializing it if necessary.
	 * @param index the index of the projection vector
	 * @return the {@link Set} of positive factors for that index
	 */
	private Set<Integer> getPositiveIndexFactors(final int index) {
		Set<Integer> currPos = positives.get(index);
		if (currPos == null) {
			currPos = new HashSet<>();
			positives.put(index, currPos);
		}
		return currPos;
	}

	/**
	 * Retrieves the set of negative factor indices for a given index in the
	 * projection vector, initializing it if necessary.
	 * @param index the index of the projection vector
	 * @return the {@link Set} of negative factors for that index
	 */
	private Set<Integer> getNegativeIndexFactors(final int index) {
		Set<Integer> currNeg = negatives.get(index);
		if (currNeg == null) {
			currNeg = new HashSet<>();
			negatives.put(index, currNeg);
		}
		return currNeg;
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
		final Map<Integer, Double> adjVec = generateAdjacencyVector(uvg);
		final double[] projVec = new double[finalDim];
		for (int j = 0; j < finalDim; ++j) {
			/* For every position, check if it belongs to the positive
			 * or negative factors */
			final Set<Integer> currPos = positives.get(j);
			final Set<Integer> currNeg = negatives.get(j);
			final int index = j;

			adjVec.forEach((k, v) -> {
				/* Check if key is in currPos or currNeg. If it
				 * is, modify the relevant value.
				 */
				if (currPos.contains(k)) {
					projVec[index] += v;
				}
				else if (currNeg.contains(k)) {
					projVec[index] += v;
				}
			});
		}
		return projVec;
	}

	/**
	 * Generates the adjacency vector for a given {@link UniqueVertexGraph},
	 * given the underlying character indices and the rank of the n-grams in
	 * the graph. The vector is created as a {@link Map}, since it is always
	 * expected to be sparse.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @param rank the n-gram rank
	 * @return a {@link Map<Integer, Double>} containing the adjacency vector
	 */
	public final Map<Integer, Double>
	generateAdjacencyVector(final UniqueVertexGraph uvg) {
		/* Note: adjacency vector is implemented as a map. */
		final Map<Integer, Double> adjacencyVector = new HashMap<>();
		uvg.edgeSet().forEach(e -> {
			final int indexFrom = getNGramIndex(e.getSourceLabel());
			final int indexTo = getNGramIndex(e.getTargetLabel());
			final int totalIndex = indexFrom * nGramCount + indexTo;
			final double currValue =
				adjacencyVector.getOrDefault(totalIndex, 0.0);
			adjacencyVector.put(totalIndex, currValue + e.edgeWeight());
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
