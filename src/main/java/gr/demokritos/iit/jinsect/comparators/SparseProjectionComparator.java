package gr.demokritos.iit.jinsect.comparators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import gr.demokritos.iit.jinsect.JUtils;
import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.structs.Pair;
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
	protected List<Set<Integer>> positives;
	protected List<Set<Integer>> negatives;

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
		createIndexMaps();
		createProjectionMatrix();
	}

	/**
	 * Initializes {@link #positives} and {@link #negatives} with new
	 * sets of integers.
	 */
	private final void createIndexMaps() {
		positives = new ArrayList<>(finalDim);
		negatives = new ArrayList<>(finalDim);
		for (int i = 0; i < finalDim; ++i) {
			positives.add(new HashSet<>());
			negatives.add(new HashSet<>());
		}
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
		final double[] vecA = createProjectedVector(uvgA);
		final double[] vecB = createProjectedVector(uvgB);
		double sum = 0.0;
		for (int i = 0; i < vecA.length; ++i) {
			sum += Math.abs(vecA[i] - vecB[i]);
		}
		return sum;
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
		final double[] vecA = createProjectedVector(uvgA);
		final double[] vecB = createProjectedVector(uvgB);
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
					final double wMin = Math.min(wA, wB);
					if (wMin == 0.0) {
						continue;
					}
					simSum += wMin / Math.max(wA, wB);
				}
				break;
		}
		return simSum;
	}

	/**
	 * Creates the final vector after projection. This method is intended to
	 * be overriden in order to specify serial or parallel calculation of the
	 * final vector.
	 * @param uvg the graph whose vector is sought
	 * @return the final vector
	 */
	protected double[] createProjectedVector(final UniqueVertexGraph uvg) {
		return getProjectedVectorParallel(uvg);
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
			/* Retrieve sets for current index */
			Set<Integer> currPos = positives.get(j);
			Set<Integer> currNeg = negatives.get(j);
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
			/* Retrieve sets for current index */
			Set<Integer> currPos = positives.get(j);
			Set<Integer> currNeg = negatives.get(j);
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
	 * Projects the adjacency vector of a {@link UniqueVertexGraph} to a
	 * space of lower dimensionality.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @return the projected vector as a {@link double[]}
	 */
	public final double[] getProjectedVectorSerial(
		final UniqueVertexGraph uvg) {
		/* Get the adjacency vector, initialize the projection */
		final List<Pair<Integer, Double>> adjVec =
			generateAdjacencyVector(uvg);
		final double[] projVec = new double[finalDim];
		for (int j = 0; j < finalDim; ++j) {
			/* For every position, check if it belongs to the positive
			 * or negative factors */
			final Set<Integer> currPos = positives.get(j);
			final Set<Integer> currNeg = negatives.get(j);
			final int index = j;

			adjVec.forEach(pair -> {
				/* Check if key is in currPos or currNeg. If it
				 * is, modify the relevant value.
				 */
				if (currPos.contains(pair.getFirst())) {
					projVec[index] += pair.getSecond();
				}
				else if (currNeg.contains(pair.getFirst())) {
					projVec[index] -= pair.getSecond();
				}
			});
		}
		return projVec;
	}

	/**
	 * Projects the adjacency vector of a {@link UniqueVertexGraph} to a
	 * space of lower dimensionality. This method uses a parallel stream
	 * to compute the value at each position of the final vector.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @return the projected vector as a {@link double[]}
	 */
	public final double[] getProjectedVectorParallel(
		final UniqueVertexGraph uvg) {
		/* Get the adjacency vector, initialize the projection */
		final List<Pair<Integer, Double>> adjVec =
			generateAdjacencyVector(uvg);
		final double[] projVec = new double[finalDim];
		IntStream.range(0,  finalDim).parallel().forEach(index -> {
			/* For every position, check if it belongs to the positive
			 * or negative factors */
			final Set<Integer> currPos = positives.get(index);
			final Set<Integer> currNeg = negatives.get(index);
			adjVec.forEach(pair -> {
				/* Check if key is in currPos or currNeg. If it
				 * is, modify the relevant value.
				 */
				if (currPos.contains(pair.getFirst())) {
					projVec[index] += pair.getSecond();
				}
				else if (currNeg.contains(pair.getFirst())) {
					projVec[index] -= pair.getSecond();
				}
			});
		});
		return projVec;
	}

	/**
	 * Generates the adjacency vector for a given {@link UniqueVertexGraph},
	 * given the underlying character indices and the rank of the n-grams in
	 * the graph. The vector is created as a {@link List} of index-value pairs
	 * as it is expected to be sparse.
	 *
	 * @param uvg the {@link UniqueVertexGraph}
	 * @param rank the n-gram rank
	 * @return a {@link List<Pair<Integer, Double>>} containing the adjacency
	 * vector
	 */
	public final List<Pair<Integer, Double>>
	generateAdjacencyVector(final UniqueVertexGraph uvg) {
		/* Note: adjacency vector is implemented as a map. */
		final List<Pair<Integer, Double>> adjacencyVector =
			new ArrayList<>(uvg.edgeSet().size());
		uvg.edgeSet().forEach(e -> {
			final int indexFrom = getNGramIndex(e.getSourceLabel());
			final int indexTo = getNGramIndex(e.getTargetLabel());
			final int totalIndex = indexFrom * nGramCount + indexTo;
			adjacencyVector.add(
				new Pair<>(totalIndex, e.edgeWeight()));
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
		final int n = ngram.length();
		for (int i = 0; i < n; ++i) {
			final int index = charIndex.get(ngram.charAt(i));
			totalIndex += JUtils.intPow(alphabetSize, i) * index;
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
