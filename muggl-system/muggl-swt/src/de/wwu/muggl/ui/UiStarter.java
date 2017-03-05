package de.wwu.muggl.ui;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.ConfigReader;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.SWTGui;
import de.wwu.muggl.util.StaticStringFormatter;

/**
 * The UiStarter is the only class in the whole application offering a main-method. It offers the
 * possibility to start the GUI. Depending on the command line arguments it branches and loads the
 * selected GUI, passing further command line options if needed. Currently there are only two branches
 * and the system does not provide any further arguments but -nogui and -c. -nogui will result in the
 * message, that no textual UI is currently provided. By default, invocation of main will load the
 * graphical user interface of this application. The switch -c offers the possibility to provide a
 * configuration file to load. The default configuration file will not be loaded in that case.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-16
 */
public final class UiStarter {
	private static final int MAX_BUFFER = 80;
	
	/**
	 * Protected default constructor.
	 */
	private UiStarter() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Main method for the application, loading the GUI.
	 * 
	 * @param args The command line arguments as a String array.
	 */
	public static void main(String[] args) {
		// Check for arguments.
		if (args.length > 0) {
			for (int a = 0; a < args.length; a++) {
				if (args[a].toLowerCase().equals("-nogui")) {
					// Found the argument -nogui.
					System.out.println(
							Globals.APP_NAME + " " + Globals.VERSION_MAJOR + "." + Globals.VERSION_MINOR + " " + Globals.VERSION_STATUS + "\n"
								+ "Currently there is no textual user interface provided.\n\n"
							);
					byte[] yesNo = new byte[MAX_BUFFER];
					while (true) {
						System.out.println("Do you wish to start the graphical ui? (Y/N)");
						try {
							int len = System.in.read(yesNo, 0, MAX_BUFFER);
							if (len > 0) {
								if (String.valueOf((char) yesNo[0]).toUpperCase().equals("Y")) break;
								if (String.valueOf((char) yesNo[0]).toUpperCase().equals("N")) {
									System.out.println("Good bye!");
									System.exit(0);
								}
							}
						} catch (IOException e) {
							System.out.println("Did no successfully receive an input. Shutting down.");
							System.exit(0);
						}
					}
				} else if (args[a].toLowerCase().equals("-c")) {
					// Found the argument -c. Check if there is a following argument and that it is a file.
					a++;
					if (a < args.length && args[a].length() > 0 && !args[a].startsWith("-")) {
						File file = new File(args[a]);
						if (file.exists() && file.isFile()) {
							if (Globals.getInst().logger.isInfoEnabled()) Globals.getInst().logger.info("The command line switch -c was used. Supplying the specified configuration file to the parser.");
							Options.getInst().configurationFile = args[a].replace("\\", "/");
						} else {
							if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("The command line switch -c was used but the specified configuration file could not be found.");
						}
					} else {
						if (args[a].startsWith("-")) {
							// There is no file specified but the next command line argument follows. Decrease a so it will be processed.
							a--;
						}
						if (Globals.getInst().logger.isEnabledFor(Level.WARN)) Globals.getInst().logger.warn("The command line switch -c was used but there was no configuration file specified.");
					}
				}
			}
		}

		// Try to read the configuration from the configuration file.
		ConfigReader.loadConfig(false);

		if (Globals.getInst().logger.isInfoEnabled())
			Globals.getInst().logger.info("Basic initialization finished. Starting "
					+ Globals.APP_NAME + " with up to "
					+ StaticStringFormatter.formatByteValue(Runtime.getRuntime().maxMemory())
					+ " of memory available.");

		// Start the graphical user interface.
		startGui();
	}

	/**
	 * Initialize and start the GUI.
	 */
	private static void startGui() {
		SWTGui gui = new SWTGui();
		gui.launch();
	}

}
