package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	public static boolean INITIALIZED = false;

	public static String BOOTSTRAP_ADDRESS;
	public static int BOOTSTRAP_PORT;

	public static String WORKING_DIR;
	public static String STORAGE_DIR;

	public static int WEAK_TIMEOUT;
	public static int STRONG_TIMEOUT;

	public static String TEAM;
	public static int LIMIT;

	public static int SERVENT_COUNT;
	
	public static ChordState chordState;
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 * 
	 * @param configName name of configuration file
	 * @param serventId id of the servent, as used in the configuration file
	 */
	public static void readConfig(String configName, int serventId){

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configName));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		BOOTSTRAP_ADDRESS = properties.getProperty("bs_address");
		if (BOOTSTRAP_ADDRESS == null) {
			timestampedErrorPrint("Problem reading bs_address. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs_port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bs_port. Exiting...");
			System.exit(0);
		}

		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}

		WORKING_DIR = properties.getProperty("work_dir");
		if (WORKING_DIR == null) {
			System.err.println("Problem reading work_directory property. Exiting...");
			System.exit(0);
		}

		STORAGE_DIR = properties.getProperty("storage");
		if (STORAGE_DIR == null) {
			System.err.println("Problem reading storage property. Exiting...");
			System.exit(0);
		}

		try {
			WEAK_TIMEOUT = Integer.parseInt(properties.getProperty("weak_timeout"));
		} catch (NumberFormatException e) {
			System.err.println("Problem reading weak_timeout property. Exiting...");
			System.exit(0);
		}

		try {
			STRONG_TIMEOUT = Integer.parseInt(properties.getProperty("strong_timeout"));
		} catch (NumberFormatException e) {
			System.err.println("Problem reading strong_timeout property. Exiting...");
			System.exit(0);
		}

		TEAM = properties.getProperty("team");
		if (TEAM == null) {
			System.err.println("Problem reading team property. Exiting...");
			System.exit(0);
		}

		try {
			LIMIT = Integer.parseInt(properties.getProperty("limit"));
		} catch (NumberFormatException e) {
			System.err.println("Problem reading limit property. Exiting...");
			System.exit(0);
		}
		
		try {
			int chordSize = Integer.parseInt(properties.getProperty("chord_size"));
			
			ChordState.CHORD_SIZE = chordSize;
			chordState = new ChordState();
			
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(0);
		}

		String ipAddressProperty = "servent" + serventId + ".ip_address";
		String ipAddress = properties.getProperty(ipAddressProperty);
		if (ipAddress == null) {
			timestampedErrorPrint("Problem reading ip_address property. Exiting...");
			System.exit(0);
		}

		String serventPortProperty = "servent" + serventId +".port";
		int port = -1;
		try {
			port = Integer.parseInt(properties.getProperty(serventPortProperty));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading port property. Exiting...");
			System.exit(0);
		}
		
		myServentInfo = new ServentInfo(ipAddress, port);
	}
	
}
