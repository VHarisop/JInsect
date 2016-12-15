package gr.demokritos.iit.jinsect;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gr.demokritos.iit.jinsect.storage.IFileLoader;

/**
 * A class including a set of useful, general purpose functions.
 *
 * @author ggianna
 */
public final class Utils {
	private static final Logger logger =
			Logging.getLogger(Utils.class.getName());

	/**
	 * Converts milliseconds to a string representation of x hours, y min, z sec.
	 * @param lMillis The long number of milliseconds.
	 * @return The formated string.
	 */
	public static final String millisToMinSecString(long lMillis) {
		return String.format("%d hours %d min %d sec", lMillis / (1000 * 60 * 60), (lMillis / (1000 * 60)) % 60,
				(lMillis / 1000) % 60);
	}

	/**
	 * Repeatedly randomizes a given list.
	 *
	 * @param l The input list to randomize.
	 * @param repeat The times to perform randomization. Higher values allow
	 * more shuffling.
	 */
	public static final void shuffleList(List<?> l, int repeat) {
		for (int iCnt = 0 ; iCnt < repeat; iCnt++) {
			shuffleList(l);
		}
	}

	/**
	 * Randomizes the order of items in a given list.
	 * @param l The input list that will be modified.
	 */
	public static final void shuffleList(List<?> l) {
		final Random d = new Random();
		Collections.shuffle(l, d);
	}

	/**
	 * Splits a given string to its words, without stemming. Words
	 * are considered to be everything but sequences of whitespace
	 * and punctuation.
	 *
	 * @param sStr The input string.
	 * @return An array of String containing the words of the given string.
	 */
	public static final String[] splitToWords(String sStr) {
		final String[] sRes = sStr.split("(\\s|\\p{Punct})+");
		return sRes;
	}

	/**
	 * Calculates the logarithm of a number using a given base.
	 * @param dNumber The number whose logarithm is meant to be calculated.
	 * @param dBase The base of the logarithm.
	 * @return The logarithm base <tt>dBase</tt> of <tt>dNumber</tt>.
	 */
	public static final double logX(double dNumber, double dBase) {
		return Math.log(dNumber)/Math.log(dBase);
	}

	/**
	 * Creates a formatted string representation of an iterable object, sorting
	 * the string representation of its parts at the output.
	 * @param iIterable The iterable object.
	 * @param sSeparator The separator to use for elements.
	 * @return The ordered string representation of the iterable object.
	 */
	public static final String
	printSortIterable(Iterable<?> iIterable, String sSeparator) {
		// Use treeset to sort
		final TreeSet<String> tsItems = new TreeSet<>();
		for (final Object obj: iIterable) {
			tsItems.add(obj.toString());
		}
		/* collect all items to a delimited string */
		return tsItems.stream().collect(Collectors.joining(sSeparator));
	}

	/**
	 * Creates a formatted string representation of an iterable
	 * object using a given separator.
	 * @param iIterable The (possibly nested) iterable object.
	 * @param sSeparator The separator to use for elements of the same level.
	 * @return The string representation of the (possibly nested)
	 * iterable object.
	 */
	public static final String
	printIterable(Iterable<?> iIterable, String sSeparator) {
		final StringBuffer sbRes = new StringBuffer();
		final Iterator<?> iIter = iIterable.iterator();
		while (iIter.hasNext()) {
			final Object oNext = iIter.next();
			sbRes.append(oNext.toString());
			if (iIter.hasNext()) {
				sbRes.append(sSeparator);
			}
		}

		return sbRes.toString();
	}

	/**
	 * Returns the system encoding String.
	 * @return A String indicating the System default encoding.
	 */
	public static String getSystemEncoding() {
		final String defaultEncoding = new InputStreamReader(
				new ByteArrayInputStream(new byte[0])).getEncoding();

		return defaultEncoding;

	}

	/**
	 * Converts a string to UTF-8 encoding.
	 * @param sStr The input string.
	 * @return A UTF-8 encoded version of the input string.
	 */
	public static String toUTF8(String sStr) {
		final byte[] baBytes = sStr.getBytes();
		try {
			return new String(baBytes, "UTF-8");
		}
		catch (final Exception e) {
			logger.warning(e.getMessage());
			return new String(baBytes);
		}
	}

	/**
	 * The sign function.
	 * @param dNum The input number.
	 * @return 1 if the input number is positive, -1 if negative
	 * and zero otherwise.
	 */
	public static double sign(double dNum) {
		return dNum == 0.0 ? dNum : dNum / Math.abs(dNum);
	}


	/**
	 * Returns the sum of a sequence of numbers in a specified range
	 * @param iStart The minimum term of the sequence
	 * @param iEnd The maximum term of the sequence
	 */
	public static int sumFromTo(int iStart, int iEnd) {
		return IntStream.rangeClosed(iStart, iEnd).sum();
	}

	/**
	 * Gets the sum of the values in a histogram.
	 *
	 * @param the histogram to sum from
	 * @return the sum of the histogram's values.
	 */
	public double getHistogramTotal(HashMap<?, Double> hHist) {
		return hHist.values()
			.stream()
			.mapToDouble(v -> v.doubleValue())
			.sum();
	}

	/**
	 * Loads the contents of a file into a string, <i>without preserving newlines</i>.
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToString(String sFilename) {
		final StringBuffer sb = new StringBuffer();
		try {
			final BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (final Exception e) {
			logger.warning("Could not load file:" + sFilename);
			logger.warning(e.getMessage());
		}

		return sb.toString();
	}

	/**
	 * Loads the contents of a file into a string, <i>without preserving newlines</i>.
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToString(String sFilename, int iMaxLen) {
		final StringBuffer sb = new StringBuffer();
		try {
			final BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while (((line = in.readLine()) != null) &&
					(sb.length() + line.length() < iMaxLen)) {
				sb.append(line);
			}
			in.close();
		} catch (final Exception e) {
			logger.warning("Could not load file:" + sFilename);
			logger.warning(e.getMessage());
		}

		return sb.toString();
	}

	/**
	 * Loads the contents of a file into a string, preserving newlines.
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToStringWithNewlines(String sFilename) {
		final StringBuffer sb = new StringBuffer();
		try {
			final BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			in.close();
		} catch (final Exception e) {
			logger.warning("Could not load file:" + sFilename);
			logger.warning(e.getMessage());
		}

		return sb.toString();
	}

	/**
	 * Loads the contents of a set of files into a string, by calling repeatedly
	 * <code>loadFileToString</code>. Each file is separated from another by a
	 * zero character (char(0)).
	 * @param ssFiles The set of string filenames to load.
	 * @return A String containing the concatenation of the contents of the
	 * given files.
	 */
	public static String loadFileSetToString(Set<String> ssFiles) {
		final StringBuffer sbRes = new StringBuffer();
		for (final String sCurFile : ssFiles) {
			sbRes.append(loadFileToString(sCurFile)).append((char)0);
		}

		return sbRes.toString();
	}

	/**
	 * Loads the contents of a set of files into a string, by calling repeatedly
	 * the <code>loadFile</code> function of a {@link IFileLoader}.
	 * Each file is separated from another by a zero character (char(0)).
	 * @param ssFiles The set of string filenames to load.
	 * @param lLoader The loader to use for loading the files
	 * @return A String containing the concatenation of the contents of the
	 * given files.
	 */
	public static String loadFileSetToString(Set<String> ssFiles,
			IFileLoader<String> lLoader) {
		final StringBuffer sbRes = new StringBuffer();
		for (final String sCurFile : ssFiles) {
			sbRes.append(lLoader.loadFile(sCurFile)).append((char)0);
		}

		return sbRes.toString();
	}

	/**
	 * Repeats a given string a specified number of times.
	 * @param sStr The string to repeat.
	 * @param iTimes The times to repeat the string.
	 * @return A string containing the given string
	 * concatenated the specified number of times.
	 */
	public static final String repeatString(String sStr, int iTimes) {
		return String.join("", Collections.nCopies(iTimes, sStr));
	}

	/**
	 * Returns a reversed version of a given string.
	 *
	 * @param source the string to reverse
	 * @return the reversed string
	 */
	public static String reverseString(String source) {
		final int len = source.length();
		final StringBuffer dest = new StringBuffer(len);
		for (int i = (len - 1); i >= 0; i--) {
			dest.append(source.charAt(i));
		}
		return dest.toString();
	}

	/**
	 * Returns a reversed (by means of item index) version of a given list.
	 *
	 * @param l The list to reverse.
	 * @return The reversed list.
	 */
	public static List<?> reverseList(List<?> l) {
		final LinkedList<?> lRes = new LinkedList<Object>(l);
		Collections.reverse(lRes);
		return lRes;
	}

	/**
	 * Returns the portion of filename after the last directory separator.
	 * If there is no file name there, an empty string is returned.
	 *
	 * @param sFilepath The path to the file.
	 * @return The filename stripped of directories.
	 */
	public static final String getFilenameOnly(String sFilepath) {
		return new File(sFilepath).getName();
	}

	/**
	 * Returns a string based on a constant change between
	 * vowels and consonants and blanks. Considered "normal".
	 * @return The random "normal" string.
	 */
	public static String getNormalString() {
		// Both the set of vowels and consonants also include
		// black characters to allow for space in the string.
		final String sVowels = "aeiuoy ";
		final String sConsonants = "qwrtpsdf jklhzxcvbnm ";
		final StringBuffer sbRes = new StringBuffer();
		final int iLen = (int)(7.0 +
				(3.0 * new Random().nextGaussian()));

		// Randomly initialize to vowel or consonant
		boolean bVowel = new Random().nextBoolean();
		for (int iCharCnt=0; iCharCnt<iLen; iCharCnt++) {
			if (bVowel) {
				final int iStart =
					Math.abs(new Random().nextInt()) % (sVowels.length() - 1);
				sbRes.append(sVowels.substring(iStart, iStart + 1));
			}
			else {
				final int iStart =
					Math.abs(new Random().nextInt()) % (sConsonants.length() - 1);
				sbRes.append(sConsonants.substring(iStart, iStart + 1));
			}
			bVowel = !bVowel;
		}
		return sbRes.toString();
	}

	/**
	 * Returns a random string, based on random selection from
	 * an alphabet of characters and symbols.
	 * @return The random string.
	 */
	public static String getAbnormalString() {
		final String sAlphabet = "aeiuoy qwrtpsdfjklhzxcvbnm1234567890!@#";
		final StringBuffer sbRes = new StringBuffer();
		final int iLen = (int)(12.0 +
				(11.0 * new Random().nextGaussian()));

		// Randomly generate from alphabet
		for (int iCharCnt=0; iCharCnt<iLen; iCharCnt++) {
			final int iStart =
				Math.abs(new Random().nextInt()) % (sAlphabet.length() - 1);
			sbRes.append(sAlphabet.substring(iStart, iStart + 1));
		}
		return sbRes.toString();
	}
}
