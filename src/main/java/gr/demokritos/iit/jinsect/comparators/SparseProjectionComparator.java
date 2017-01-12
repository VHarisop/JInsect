package gr.demokritos.iit.jinsect.comparators;

import java.util.ArrayList;
import java.util.Arrays;
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
		SIGN_CONSISTENT, RANDOM, POSITIVE;
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
			case POSITIVE:
			default:
				for (int i = 0; i < vecA.length; ++i) {
					simSum += Math.min(vecA[i], vecB[i])
						/ Math.max(vecA[i], vecB[i]);
				}
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
				createSCProjection(featureDim, sigma);
				break;
			case POSITIVE:
			default:
				createPositiveProjection(featureDim, sigma);
				break;
		}
	}

	/**
	 * Creates a strictly positive projection matrix, given the sparsity
	 * parameter and the original dimension.
	 * @param featureDim the original dimension
	 * @param sigma the sparsity parameter
	 */
	private void
	createPositiveProjection(final int featureDim, final double sigma) {
		IntStream.range(0, finalDim)
			.parallel()
			.forEach(index -> {
				/* Every column of the projection matrix is assigned
				 * to a possibly separate thread
				 */
				final Set<Integer> currPos = positives.get(index);
				for (int i = 0; i < featureDim; ++i) {
					/* Coin toss to decide which element we'll use */
					final double toss = Math.random();
					/* P1: 1/s */
					if (toss < (1 / sigma)) {
						currPos.add(i * finalDim + index);
					}
					/* P2: 1 - 1/s */
				}
			});

	}

	/**
	 * Creates a random projection matrix, given the sparsity parameter
	 * and the original dimension.
	 * @param featureDim the original dimension
	 * @param sigma the sparsity parameter
	 */
	private void
	createRandomProjection(final int featureDim, final double sigma) {
		/* Use a parallel stream to create the columns of the projection
		 * matrix in parallel fashion.
		 */
		IntStream.range(0, finalDim)
			.parallel()
			.forEach(index -> {
				/* Every column of the projection matrix is assigned
				 * to a possibly different thread
				 */
				final Set<Integer> currPos = positives.get(index);
				final Set<Integer> currNeg = negatives.get(index);
				for (int i = 0; i < featureDim; ++i) {
					/* Coin toss to decide the element we'll use */
					final double toss = Math.random();
					/* P1: 1/2s */
					if (toss < (1 / (2 * sigma))) {
						currPos.add(i * finalDim + index);
					}
					/* P2: > 1/2s, < 1/s */
					else if (toss < (1 / sigma)) {
						currNeg.add(i * finalDim + index);
					}
					/* P3: 1 - 1/s */
				}
			});
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
		 * in a given column. Use a parallel stream to create the
		 * columns of the projection matrix in a parallel fashion.
		 */
		IntStream.range(0, finalDim)
			.parallel()
			.forEach(index -> {
				/* Retrieve the referenced set only once, based on a coin toss
				 * uniformly distributed in [0, 1] that determines the sign of
				 * this column's entries.
				 */
				final Set<Integer> referencedSet = (Math.random() > 0.5) ?
						positives.get(index) : negatives.get(index);
				for (int i = 0; i < featureDim; ++i) {
					/* Coin toss to decide which element we'll use */
					final double toss = Math.random();
					/* P1: 1/s */
					if (toss < (1 / sigma)) {
						referencedSet.add(i * finalDim + index);
					}
					/* P2: 1 - 1/s */
				}
			});
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
	 * Retrieves the {@link SparseVector} of a {@link UniqueVertexGraph}
	 * using the serial method and assigning a desired label to it.
	 *
	 * @see #getProjectedVectorSerial(UniqueVertexGraph)
	 * @param uvg the graph whose vector is requested
	 * @param label the desired label
	 * @return the resulting SparseVector
	 */
	public final SparseVector
	getSparseVectorSerial(final UniqueVertexGraph uvg, final String label) {
		final double[] projected = getProjectedVectorSerial(uvg);
		return new SparseVector(label, projected);
	}

	/**
	 * Retrieves the {@link SparseVector} of a {@link UniqueVertexGraph}
	 * using the parallel method and assigning a desired label to it.
	 *
	 * @see #getProjectedVectorParallel(UniqueVertexGraph)
	 * @param uvg the graph whose vector is requested
	 * @param label the desired label
	 * @return the resulting SparseVector
	 */
	public final SparseVector
	getSparseVectorParallel(final UniqueVertexGraph uvg, final String label) {
		final double[] projected = getProjectedVectorParallel(uvg);
		return new SparseVector(label, projected);
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
			if ((indexFrom >= 0) && (indexTo >= 0)) {
				/* If unseen character was found, skip this step */
				final int totalIndex = indexFrom * nGramCount + indexTo;
				adjacencyVector.add(
						new Pair<>(totalIndex, e.edgeWeight()));
			}
		});
		return adjacencyVector;
	}

	/**
	 * Returns the numeric index of a given n-gram, using a provided character
	 * to index mapping.
	 *
	 * @param ngram the n-gram
	 * @return the numeric index of the n-gram
	 */
	public final int getNGramIndex(final String ngram) {
		/* Alphabet size is the number of keys in the index */
		int totalIndex = 0;
		final int n = ngram.length();
		try {
			for (int i = 0; i < n; ++i) {
				final int index = charIndex.get(ngram.charAt(i));
				totalIndex += JUtils.intPow(alphabetSize, i) * index;
			}
		}
		/* If character is unseen, return -1 as flag value */
		catch (final NullPointerException ex) {
			totalIndex = -1;
		}
		return totalIndex;
	}

	/**
	 * @return the rank of this comparator
	 */
	public final int getRank() {
		return rank;
	}

	/**
	 * A class that implements a sparse vector, intended to store
	 * results after the projection.
	 * @author vharisop
	 *
	 */
	public static class SparseVector {
		private final String label;
		private final double[] vector;

		/**
		 * Creates a new SparseVector, given a label and an array
		 * of doubles which is the actual vector.
		 * @param label the vector's label
		 * @param content the vector's content
		 */
		public SparseVector(final String label, final double[] content) {
			this.label = label;
			vector = Arrays.copyOf(content, content.length);
		}

		/**
		 * Simple getter for the vector's label.
		 * @return the label of this vector
		 */
		public final String getLabel() {
			return label;
		}

		/**
		 * Simple getter for the vector's content.
		 * @return the vector's content as a double array
		 */
		public final double[] getVector() {
			return vector;
		}
	}
}
