package gr.demokritos.iit.jinsect;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * A utility class for logging used across
 * the JInsect project.
 * @author vharisop
 *
 */
public class Logging {
	/**
	 * A logger in for in-class usage.
	 */
	private static final Logger privLogger =
			Logger.getLogger(Logging.class.getName());

	/**
	 * Returns a new logger to the caller, using a specified name.
	 * @param loggerName a name for the new logger
	 * @return a new {@link Logger}
	 */
	public static Logger getLogger(String loggerName) {
		return Logger.getLogger(loggerName);
	}

	/**
	 * Returns a new logger that outputs its log to a specified file
	 * using a given name. If the file cannot be written to, falls back
	 * to console logging after informing the user.
	 * @param loggerName a name for the new logger
	 * @param fileName the path of the log file
	 * @return a new {@link Logger}
	 */
	public static Logger getFileLogger(String loggerName, String fileName) {
		final Logger logger = Logger.getLogger(fileName);
		try {
			final FileHandler fh = new FileHandler();
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
		} catch (SecurityException | IOException e) {
			privLogger.warning("Filehandler could not be created for "
				+ fileName + ". Using console logging instead...");
		}
		return logger;
	}
}
