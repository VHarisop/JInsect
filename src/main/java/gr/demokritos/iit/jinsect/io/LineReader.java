package gr.demokritos.iit.jinsect.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

// exceptions to be used
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for reading files line by line.
 *
 * @author VHarisop
 */
public class LineReader {

	/**
	 * A {@link java.io.BufferedReader} object for possibly large files
	 */
	private BufferedReader br;
	
	/**
	 * Create a LineReader object with empty parameters 
	 */
	public LineReader() {} 

	/** 
	 * Create a LineReader object to read lines from a given File
	 * @param path the {@link java.io.File} to read the lines from
	 */
	public LineReader(File path) 
	throws FileNotFoundException 
	{
		br = new BufferedReader(new FileReader(path));
	}

	/**
	 * Reads a file into a list of strings, trimming leading and trailing
	 * whitespace for each string read.
	 *
	 * @return a list of strings with leading/trailing whitespace removed
	 */
	public List<String> getLineList() throws IOException {
		ArrayList<String> lines = new ArrayList<String>(); String line;
		while ((line = br.readLine()) != null) {
			line = line.trim(); // remove whitespace
			lines.add(line);
		}
		return lines;
	}

	/**
	 * Reads a file into an array of strings, trimming leading and trailing 
	 * whitespace for each string
	 *
	 * @return an array of strings with leading/trailing whitespace removed
	 */
	public String[] getLines() throws IOException {
		List<String> lines = getLineList();
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Reads a given file into an array of strings, trimming leading and 
	 * trailing whitespace for each string
	 *
	 * @param path the {@link java.io.File} to read the lines from
	 * @return an array of strings containing the lines read
	 */
	public String[] getLines(File path) throws IOException {
		br = new BufferedReader(new FileReader(path));
		return getLines();
	}

	/**
	 * Reads a given file into an array of strings, trimming leading and
	 * trailing whitespace for each string, converting the array to a list
	 * of strings, and returning the list to the caller.
	 *
	 * @param path the File to read the lines from
	 * @return a list of strings containing the lines read
	 */
	public List<String> getLineList(File path) throws IOException {
		br = new BufferedReader(new FileReader(path));
		return getLineList();
	}
}
