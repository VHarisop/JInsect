package gr.demokritos.iit.jinsect;

import gr.demokritos.iit.jinsect.storage.IFileLoader;

import java.io.*;
import java.util.*;

/** 
 * A class including a set of useful, general purpose functions.
 *
 * @author ggianna
 */
public final class utils {
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
		for (int iCnt = 0 ; iCnt < repeat; iCnt++)
			shuffleList(l);
	}

	/**
	 * Randomizes the order of items in a given list.
	 * @param l The input list that will be modified.
	 */
	public static final void shuffleList(List<?> l) {
		Random d = new Random();
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
		String[] sRes = sStr.split("(\\s|\\p{Punct})+");
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
		// Init buffer
		StringBuffer sOut = new StringBuffer();
		Iterator<?> iIter = iIterable.iterator();
		// Use treeset to sort
		TreeSet<String> tsItems = new TreeSet<String>();
		while (iIter.hasNext()) {
			tsItems.add(iIter.next().toString());
		}

		// Add all string representations to buffer, using separator.
		for (Iterator<String> isCur = tsItems.iterator(); isCur.hasNext();) {
			sOut.append(isCur.next());
			if (isCur.hasNext())
				sOut.append(sSeparator);
		}
		return sOut.toString();
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
		StringBuffer sbRes = new StringBuffer();
		Iterator<?> iIter = iIterable.iterator();
		while (iIter.hasNext()) {
			Object oNext = iIter.next();
			sbRes.append(oNext.toString());
			if (iIter.hasNext())
				sbRes.append(sSeparator);
		}

		return sbRes.toString();
	}


	/**
	 * Returns the system encoding String.
	 * @return A String indicating the System default encoding.
	 */
	public static String getSystemEncoding() {
		String defaultEncoding = new InputStreamReader(
				new ByteArrayInputStream(new byte[0])).getEncoding();

		return defaultEncoding;

	}

	/**
	 * Converts a string to UTF-8 encoding.
	 * @param sStr The input string.
	 * @return A UTF-8 encoded version of the input string.
	 */
	public static String toUTF8(String sStr) {
		byte[] baBytes = sStr.getBytes();
		try {
			return new String(baBytes, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
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
	 * Calculates the product of two lists.
	 * @param oA The first list.
	 * @param oB The second list.
	 * @return The product of the elements of the two lists as a new list of lists.
	 */
	private static final List getListProduct(Object oA, Object oB) {
		// Join list of lists
		ArrayList<Object> aRes = new ArrayList<Object>();
		List<Object> lAList, lBList;

		// If unary, create unary list, else use existing list
		if (!(oA instanceof List)) {
			lAList = new ArrayList<Object>();
			lAList.add(oA);
		}
		else
			lAList = (List<Object>)oA;
		if (!(oB instanceof List)) {
			lBList = new ArrayList<Object>();
			lBList.add(oB);
		}
		else
			lBList = (List<Object>) oB;

		// For every item in A
		Iterator<Object> iA = lAList.iterator();        
		while (iA.hasNext()) {
			Object oANext = iA.next();

			// For every item in B
			Iterator<Object> iB = lBList.iterator();
			while (iB.hasNext()) {
				Object oBNext = iB.next();

				ArrayList<Object> lTemp = new ArrayList<Object>();
				if (oANext instanceof List)
					lTemp.addAll((List)oANext);
				else
					lTemp.add(oANext);
				if (oBNext instanceof List)
					lTemp.addAll((List)oBNext);
				else
					lTemp.add(oBNext);

				aRes.add(lTemp);
			}

		}

		return (List)aRes;
	}

	/**
	 * Bubble-sorts an array of comparable items.
	 * @param aArr An array of {@link Comparable} objects.
	 */
	public static final void bubbleSortArray(Comparable[] aArr) {
		boolean bChanged = true;
		Comparable a, b;
		while (bChanged) {
			bChanged = false;
			for (int iCnt = 0; iCnt < aArr.length - 1; iCnt++) {
				a = aArr[iCnt];
				b = aArr[iCnt + 1];
				if (a.compareTo(b) > 0) {
					aArr[iCnt] = b;
					aArr[iCnt + 1] = a;
					bChanged = true;
				}
			}
		}
	}

	/**
	 * Parses the command line expecting values of either
	 * `-switch` or
	 * `-key=value`
	 * and returns corresponding {@link Hashtable}, with switches as keys
	 * and `TRUE` as value, or `key` as keys and `value` as values
	 * @param sCommands The command line array of Strings.
	 * @return The described hashtable.
	 */
	public static Hashtable<String, String>
	parseCommandLineSwitches(String[] sCommands) {
		Hashtable<String, String> hRes = new Hashtable<String, String>();
		Iterator<String> iStr = Arrays.asList(sCommands).iterator();
		while (iStr.hasNext()) {
			String sToken = (String)iStr.next();
			String sType, sVal;
			if (sToken.indexOf("-")==0) {
				// Switch
				if (sToken.contains("=")) {
					// Parameter
					// Part before '=' is key, omitting dash
					sType = (sToken.split("=")[0]).substring(1);
					// Part after '=' is value
					sVal = sToken.split("=")[1];
				}
				else
				{
					// Simple switch. Omit dash
					sType = sToken.substring(1);
					sVal = "TRUE";
				}

				hRes.put(sType, sVal);
			}
		}
		return hRes;
	}

	/**
	 * Given a {@link Hashtable} and a given option string, this function returns either the 
	 * option set in the hashtable, or a given default if the option has not been set. 
	 * @param hSwitches The hashtable of switches (see also <code>parseCommandLineSwitches</code>).
	 * @param sOption The name of the option of interest.
	 * @param sDefault The default value to be used if the option has not been set.
	 * @return The value of the switch, or the default value if no value has been set.
	 */
	public static String
	getSwitch(Hashtable<String, String> hSwitches, String sOption, String sDefault) {
		Iterator<String> iIter = hSwitches.keySet().iterator();
		while (iIter.hasNext()) {
			String sCurSwitch = iIter.next();
			if (sCurSwitch.equals(sOption))
				return hSwitches.get(sCurSwitch);
		}
		return sDefault;
	}

	/**
	 * Returns the sum of a sequence of numbers in a specified range
	 * @param iStart The minimum term of the sequence
	 * @param iEnd The maximum term of the sequence
	 */
	public static int sumFromTo(int iStart, int iEnd) {
		int iRes = 0;
		for (int iCnt = iStart; iCnt <= iEnd; iRes += iCnt++);
		return iRes;
	}

	/**
	 * Gets the sum of the values in a histogram.
	 *
	 * @param the histogram to sum from
	 * @return the sum of the histogram's values.
	 */
	public double getHistogramTotal(HashMap<?, Double> hHist) {
		double dSum = 0.0;
		for (Double d: hHist.values()) {
			dSum += d.doubleValue();
		}
		return dSum;
	}    

	/**
	 * Loads the contents of a file into a string, <i>without preserving newlines</i>. 
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToString(String sFilename) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Coult not load file:" + sFilename);
			e.printStackTrace(System.err);
		}

		return sb.toString();
	}

	/**
	 * Loads the contents of a file into a string, <i>without preserving newlines</i>. 
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToString(String sFilename, int iMaxLen) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while (((line = in.readLine()) != null) && 
					(sb.length() + line.length() < iMaxLen)) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Coult not load file:" + sFilename);
			e.printStackTrace(System.err);
		}

		return sb.toString();
	}

	/**
	 * Loads the contents of a file into a string, preserving newlines. 
	 * @param sFilename The filename of the file to load.
	 * @return A String containing the contents of the given file.
	 */
	public static String loadFileToStringWithNewlines(String sFilename) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(sFilename));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Coult not load file:" + sFilename);
			e.printStackTrace(System.err);
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
		StringBuffer sbRes = new StringBuffer();
		for (String sCurFile : ssFiles) {
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
		StringBuffer sbRes = new StringBuffer();
		for (String sCurFile : ssFiles) {
			sbRes.append(lLoader.loadFile(sCurFile)).append((char)0);
		}

		return sbRes.toString();
	}

	/**
	 * Repeats a given string a specified number of times.
	 * @param sStr The string to repeat.
	 * @param iTimes The times to repeat the string.
	 * @return A string containing the given string concatenated the specified number of times.
	 */
	public static final String repeatString(String sStr, int iTimes) {
		StringBuffer sb = new StringBuffer();
		for (int iCnt=0; iCnt < iTimes; iCnt++)
			sb.append(sStr);

		return sb.toString();
	}

	/**
	 * Returns a reversed version of a given string.
	 *
	 * @param source the string to reverse
	 * @return the reversed string
	 */
	public static String reverseString(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);

		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}

	/** 
	 * Returns a reversed (by means of item index) version of a given list.
	 *
	 * @param l The list to reverse.
	 * @return The reversed list.
	 */
	public static List<?> reverseList(List<?> l) {
		LinkedList<?> lRes = new LinkedList<Object>(l);
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
		String sVowels = "aeiuoy ";
		String sConsonants = "qwrtpsdf jklhzxcvbnm ";
		StringBuffer sbRes = new StringBuffer();
		int iLen = (int)(7.0 +
				(3.0 * new Random().nextGaussian()));

		// Randomly initialize to vowel or consonant
		boolean bVowel = new Random().nextBoolean();
		for (int iCharCnt=0; iCharCnt<iLen; iCharCnt++) {
			int iStart;
			if (bVowel) {
				iStart = Math.abs(new Random().nextInt()) % (sVowels.length() - 1);
				sbRes.append(sVowels.substring(iStart, iStart + 1));
			}
			else {
				iStart = Math.abs(new Random().nextInt()) % (sConsonants.length() - 1);
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
		String sAlphabet = "aeiuoy qwrtpsdfjklhzxcvbnm1234567890!@#";
		StringBuffer sbRes = new StringBuffer();
		int iLen = (int)(12.0 +
				(11.0 * new Random().nextGaussian()));

		// Randomly generate from alphabet
		for (int iCharCnt=0; iCharCnt<iLen; iCharCnt++) {
			int iStart;
			iStart = Math.abs(new Random().nextInt()) % (sAlphabet.length() - 1);
			sbRes.append(sAlphabet.substring(iStart, iStart + 1));
		}
		return sbRes.toString();
	}
}
