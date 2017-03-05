package de.wwu.muggl.ui.gui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import de.wwu.muggl.configuration.ConfigReader;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.components.FileSelectionComposite;
import de.wwu.muggl.ui.gui.support.ImageRepository;
import de.wwu.muggl.ui.gui.support.StaticGuiSupport;
import de.wwu.muggl.ui.gui.windows.LogWindow;
import de.wwu.muggl.ui.gui.windows.OptionsWindow;

/**
 * The main window of the application, responsible for loading any other GUI elements.
 * It is started in an own Thread.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-18
 */
public class SWTGui {
	// Fields.
	private Shell shell = null;
	/**
	 * The display reference.
	 */
	protected Display display = null;
	/**
	 * The composite used to select files.
	 */
	protected FileSelectionComposite fileSelectionComposite;
	private boolean isClosing = false;
	private MenuItem recentFileMenuItem;
	private Menu recentFileMenu;

	/**
	 * Launch the Window and exit the application after it is closed.
	 */
	public void launch() {
		int exitCode = 0;
		try {
			this.display = Display.getDefault();
			createShell();
			this.shell.open();

			while (!this.shell.isDisposed()) {
				if (!this.display.readAndDispatch())
					this.display.sleep();
				}
			if (!this.display.isDisposed()) this.display.dispose();
		} catch (GUIException e) {
			String message = "Fatal Error in the GUI main window: " + e.getMessage() + "\n\n Halting and shutting down.";
			StaticGuiSupport.showMessageBox(getShell(), "Fatal error", message, SWT.OK | SWT.ICON_ERROR);
			exitCode = 1;
			if (Globals.getInst().guiLogger.isEnabledFor(Level.FATAL)) Globals.getInst().guiLogger.fatal(message);
		} catch (Exception e) {
			String message = Globals.APP_NAME + " crashed with an unexpected Exception. There is no chance to recover, so it is halting. The root cause is: " + e.getMessage();
			StaticGuiSupport.showMessageBox(getShell(), "Fatal error", message, SWT.OK | SWT.ICON_ERROR);
			exitCode = 1;
			if (Globals.getInst().guiLogger.isEnabledFor(Level.FATAL)) Globals.getInst().guiLogger.fatal(message);
		} catch (Error e) {
			String message = Globals.APP_NAME + " crashed with an Error. There is no chance to recover, so it is halting. The root cause is " + e.getClass().getName() + " with message: " + e.getMessage() + "\n\n" + Globals.OUTOFMEMORYERROR_MESSAGE;
			StaticGuiSupport.showMessageBox(getShell(), "Fatal error", message, SWT.OK | SWT.ICON_ERROR);
			exitCode = 1;
			if (Globals.getInst().guiLogger.isEnabledFor(Level.FATAL)) Globals.getInst().guiLogger.fatal(message);
		} catch (Throwable t) {
			String message = Globals.APP_NAME + " crashed with an unexpected Throwable. There is no chance to recover, so it is halting. The root cause is " + t.getClass().getName() + " with message: " + t.getMessage();
			StaticGuiSupport.showMessageBox(getShell(), "Fatal error", message, SWT.OK | SWT.ICON_ERROR);
			if (Globals.getInst().guiLogger.isEnabledFor(Level.FATAL)) Globals.getInst().guiLogger.fatal(message);
			exitCode = 1;
		} finally {
			if (Globals.getInst().logger.isInfoEnabled()) Globals.getInst().logger.info(Globals.APP_NAME + " is now shut down. Good bye.");
			System.exit(exitCode);
		}
	}

	/**
	 * Create the Shell, setting up any elements that are not set up by the main Composite.
	 * @throws GUIException On fatal initialization errors.
	 */
	private void createShell() throws GUIException {
		// Basic initialization.
		this.shell = new Shell(this.display, SWT.BORDER | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		this.shell.setText(Globals.APP_NAME + " " + Globals.VERSION_MAJOR + "." + Globals.VERSION_MINOR + " " + Globals.VERSION_RELEASE);
		this.shell.setLayout(new FillLayout(SWT.VERTICAL));

		final Image small = new Image(shell.getDisplay(),
		        SWTGui.class.getResourceAsStream("/images/tray_small.png"));
		final Image large = new Image(shell.getDisplay(),
				SWTGui.class.getResourceAsStream("/images/tray_large.png"));
		this.shell.setImages(new Image[] { small, large });
		
		
		// Get the image repository.
		ImageRepository repos = ImageRepository.getInst();
		
		// Build the menu.
		Menu menu = new Menu(this.shell, SWT.BAR);

		// Level 0 menu entries.
		MenuItem fileMenuItem = new MenuItem(menu, SWT.CASCADE);
		fileMenuItem.setText("&File");
		MenuItem loggingMenuItem = new MenuItem(menu, SWT.CASCADE);
		loggingMenuItem.setText("&Logging");
		MenuItem jpaMenuItem = new MenuItem(menu, SWT.CASCADE);
		jpaMenuItem.setText("&JPA");
		MenuItem questionmarkMenuItem = new MenuItem(menu, SWT.CASCADE);
		questionmarkMenuItem.setText("?");

		// Level 1: add entries to the file menu.
		Menu fileMenu = new Menu(menu);
	    fileMenuItem.setMenu(fileMenu);

		MenuItem openFileMenuItem = new MenuItem(fileMenu, SWT.NONE);
		openFileMenuItem.setText(" &Open");
		openFileMenuItem.setImage(repos.folderImage);
		this.recentFileMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		this.recentFileMenuItem.setText(" &Recently opened...");
		this.recentFileMenuItem.setImage(repos.folderImage);
		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem optionsFileMenuItem = new MenuItem(fileMenu, SWT.NONE);
		optionsFileMenuItem.setText(" O&ptions");
		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem exitFileMenuItem = new MenuItem(fileMenu, SWT.NONE);
		exitFileMenuItem.setText(" E&xit");
		exitFileMenuItem.setImage(repos.moonImage);

		// Level 1: add entries to the logging menu
		Menu loggingMenu = new Menu(menu);
		loggingMenuItem.setMenu(loggingMenu);

		MenuItem loggingLevelLoggingMenuItem = new MenuItem(loggingMenu, SWT.CASCADE);
		loggingLevelLoggingMenuItem.setText(" &Logging level");
		loggingLevelLoggingMenuItem.setImage(repos.checkImage);
		new MenuItem(loggingMenu, SWT.SEPARATOR);
		MenuItem showLogLoggingMenuItem = new MenuItem(loggingMenu, SWT.NONE);
		showLogLoggingMenuItem.setText(" &Show log");
		showLogLoggingMenuItem.setImage(repos.logfileImage);

		// Level 2: add entries to the logging level menu.
		Menu loggingLevelMenu = new Menu(loggingMenu);
		loggingLevelLoggingMenuItem.setMenu(loggingLevelMenu);

		MenuItem allLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		allLoggingLevelMenuItem.setText("&All");
		MenuItem traceLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		traceLoggingLevelMenuItem.setText("T&race");
		MenuItem debugLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		debugLoggingLevelMenuItem.setText("&Debug");
		MenuItem infoLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		infoLoggingLevelMenuItem.setText("&Info");
		MenuItem warnLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		warnLoggingLevelMenuItem.setText("&Warn");
		MenuItem errorLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		errorLoggingLevelMenuItem.setText("&Error");
		MenuItem fatalLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		fatalLoggingLevelMenuItem .setText("&Fatal");
		MenuItem offLoggingLevelMenuItem = new MenuItem(loggingLevelMenu, SWT.RADIO);
		offLoggingLevelMenuItem .setText("&Off");

		// Determine default logging level.
		Level level = Globals.getInst().getLoggingLevel();
		if (level.toInt() == Priority.ALL_INT) {
			allLoggingLevelMenuItem.setSelection(true);
			allLoggingLevelMenuItem.setText(allLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Level.TRACE_INT) {
			traceLoggingLevelMenuItem.setSelection(true);
			traceLoggingLevelMenuItem.setText(traceLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.DEBUG_INT) {
			debugLoggingLevelMenuItem.setSelection(true);
			debugLoggingLevelMenuItem.setText(debugLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.INFO_INT) {
			infoLoggingLevelMenuItem.setSelection(true);
			infoLoggingLevelMenuItem.setText(infoLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.WARN_INT) {
			warnLoggingLevelMenuItem.setSelection(true);
			warnLoggingLevelMenuItem.setText(warnLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.ERROR_INT) {
			errorLoggingLevelMenuItem.setSelection(true);
			errorLoggingLevelMenuItem.setText(errorLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.FATAL_INT) {
			fatalLoggingLevelMenuItem.setSelection(true);
			fatalLoggingLevelMenuItem.setText(fatalLoggingLevelMenuItem.getText() + " (default)");
		} else if (level.toInt() == Priority.OFF_INT) {
			offLoggingLevelMenuItem.setSelection(true);
			offLoggingLevelMenuItem.setText(offLoggingLevelMenuItem.getText() + " (default)");
		}

		// Level 1: add entries to the question mark menu.
		Menu questionmarkMenu = new Menu(menu);
		questionmarkMenuItem.setMenu(questionmarkMenu);

		MenuItem helpQuestionmarkMenuItem = new MenuItem(questionmarkMenu, SWT.NONE);
		helpQuestionmarkMenuItem.setText(" &Help");
		helpQuestionmarkMenuItem.setImage(repos.infoImage);
		new MenuItem(questionmarkMenu, SWT.SEPARATOR);
		MenuItem aboutQuestionmarkMenuItem = new MenuItem(questionmarkMenu, SWT.NONE);
		aboutQuestionmarkMenuItem.setText(" &About");
		aboutQuestionmarkMenuItem.setImage(repos.helpImage);

		// Set the menu bar.
		this.shell.setMenuBar(menu);

		// Set the main composite, holding almost all widgets.
		this.fileSelectionComposite = new FileSelectionComposite(this, this.shell, this.display, SWT.NONE);

		/*
		 * If the "X" at the right top of the window is clicked, close the window.
		 */
		this.shell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			@Override
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
					e.doit = doExit();
			}
		});

		/*
		 * Refresh the menu for the recently opened files when the fileMenu is shown. This saves doing so
		 * whenever the list of recently opened files is changed.
		 */
		fileMenu.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent arg0) { }

			public void menuShown(MenuEvent arg0) {
				loadRecentlyOpenedFileMenu();
			}
		});

		/*
		 * Show the open file dialog if the corresponding menu element is clicked.
		 */
		openFileMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				openFileDirectly();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Open the Options window. Disable this Shell for the meantime.
		 */
		optionsFileMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	    		getShell().setEnabled(false);
	    		try {
		    		OptionsWindow optionsWindow = new OptionsWindow();
		    		optionsWindow.show(getShell(), SWTGui.this.fileSelectionComposite.getClassLoader());
	    			} catch (Throwable t) {
    				StaticGuiSupport.processGuiError(t, "Execution options", getShell());
    			}
	    		getShell().setEnabled(true);
	    		getShell().setFocus();
	    		getShell().setActive();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	    });

		/*
		 * Close the window if the corresponding menu element is clicked.
		 */
		exitFileMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	          doExit();
	         SWTGui.this.display.dispose();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to ALL.
		 */
		allLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.ALL)
					constants.changeLogLevel(Level.ALL);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to TRACE.
		 */
		traceLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.TRACE)
					constants.changeLogLevel(Level.TRACE);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to DEBUG.
		 */
		debugLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.DEBUG)
					constants.changeLogLevel(Level.DEBUG);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to INFO.
		 */
		infoLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.INFO)
					constants.changeLogLevel(Level.INFO);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to WARN.
		 */
		warnLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.WARN)
					constants.changeLogLevel(Level.WARN);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to ERROR.
		 */
		errorLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.ERROR)
					constants.changeLogLevel(Level.ERROR);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to FATAL.
		 */
		fatalLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.FATAL)
					constants.changeLogLevel(Level.FATAL);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Change the logging level to OFF.
		 */
		offLoggingLevelMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				Globals constants = Globals.getInst();
				if (constants.getLoggingLevel() != Level.OFF)
					constants.changeLogLevel(Level.OFF);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Open the logWindow.
		 */
		showLogLoggingMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
	    		LogWindow logWindow = new LogWindow(getShell());
	    		logWindow.show();
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Display the message, that there currently is no help functionality.
		 */
		helpQuestionmarkMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				StaticGuiSupport.showMessageBox(getShell(), "Sorry, help has not been added, yet.");
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		/*
		 * Display some information about the application in a message box.
		 */
		aboutQuestionmarkMenuItem.addSelectionListener(new SelectionListener() {
	        public void widgetSelected(SelectionEvent e) {
				String text = Globals.APP_NAME + " " + Globals.VERSION_MAJOR + "."
					+ Globals.VERSION_MINOR + " " + Globals.VERSION_RELEASE + "\n"
					+ "This version is " + Globals.VERSION_STATUS + ".\n\n"
					+ "Copyrights 2007-09 by Tim Alexander Majchrzak.\n"
					+ "Developed at the chair for practical computer science at the institute for information systems, WWU M�nster, Germany.\n"
					+ "(Praktische Informatik, Institut f�r Wirtschaftsinformatik, WWU M�nster)\n\n"
					+ "This work has been inspired by it predecessor, GlassTT. It also uses some of the constraint solving techniques developed for GlassTT at the WWU M�nster. "
					+ "The author would like to thank Christoph Lembeck and Roger A. M�ller for their effort.\n\n"
					+ "Further information about " + Globals.APP_NAME + " "
					+ "can be found at http://www.wi.uni-muenster.de/pi/personal/majchrzak.php";
				StaticGuiSupport.showMessageBox(getShell(), text);
	        }

	        public void widgetDefaultSelected(SelectionEvent e) { }
	      });

		// Compute and set the needed size.
		Point point = this.shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		point.x += 2;
		point.y += 2;
		int[] posXY = StaticGuiSupport.getCenteredPosition(point.x, point.y, this.display);
		this.shell.setBounds(posXY[0], posXY[1], point.x, point.y);
	}

	/**
	 * Close the current window.
	 * @return true, if the closing was successfull, false otherwise.
	 */
	protected boolean doExit() {
		if (!this.isClosing) {
			// Set the closing flag.
			this.isClosing = true;

			// Save the options.
			if (!this.fileSelectionComposite.isDisposed()) {
				Options options = Options.getInst();
				options.methodListHideInitClinit = this.fileSelectionComposite.getHideInitCheckButton().getSelection();
				options.methodListShowMainMethodOnly = this.fileSelectionComposite.getMainOnlyCheckButton().getSelection();
				options.executionModeSingleSteps = this.fileSelectionComposite.getStepByStepCheckButton().getSelection();
				ConfigReader.saveCurrentConfig();
			}

			// Close the shell.
			this.shell.close();
			this.shell.dispose();
			return true;
		}
		return false;
	}

	/**
	 * Open the file opening dialog in the main composite.
	 */
	protected void openFileDirectly() {
		this.fileSelectionComposite.openFileDirectly();
	}

	/**
	 * Getter for the Shell.
	 * @return The current Shell.
	 */
	protected Shell getShell() {
		return this.shell;
	}

	/**
	 * Getter for the main Composite of this window.
	 * @return The FileSelectionComposite.
	 */
	protected FileSelectionComposite getChild() {
		return this.fileSelectionComposite;
	}

	/**
	 * The methods loads the entries for the recently loaded files. It then removes any entries from
	 * the corresponding menu and adds the entries it loaded.
	 */
	public void loadRecentlyOpenedFileMenu() {
		// Dispose the menu and create a new one.
		if (this.recentFileMenu != null && !this.recentFileMenu.isDisposed()) this.recentFileMenu.dispose();
		this.recentFileMenu = new Menu(this.recentFileMenuItem);
		this.recentFileMenuItem.setMenu(this.recentFileMenu);

		if (Options.getInst().recentFilesPaths.size() <= 0) {
			// Cannot add any items.
			MenuItem recentFileMenuItem = new MenuItem(this.recentFileMenu, SWT.NONE);
			recentFileMenuItem.setText("(There are no recent entries)");
			recentFileMenuItem.setEnabled(false);
		} else {
			// Add the new items.
			Iterator<String> iterator = Options.getInst().recentFilesPaths.iterator();
			while (iterator.hasNext()) {
				final String fullPath = iterator.next();
				//  Build the menu entry's text.
				int maxLength = 75;
				String menuText;
				if (fullPath.length() > maxLength) {
					int lastSlash = fullPath.lastIndexOf("/");
					int lastBackSlash = fullPath.lastIndexOf("\\");
					int lastBar = fullPath.lastIndexOf("|");
					int lastSeparator = Math.max(Math.max(lastSlash, lastBackSlash), lastBar);
					String path = fullPath.substring(0, lastSeparator + 1);
					String className = fullPath.substring(lastSeparator + 1);
					if (className.length() > maxLength) {
						menuText = "[...]" + className;
					} else {
						int remainingSpace = maxLength - className.length();
						if (remainingSpace % 2 != 0) remainingSpace--;
						menuText = path.substring(0, remainingSpace / 2) + "[...]" + path.substring(path.length() - (remainingSpace / 2)) + className;
					}
				} else {
					menuText = fullPath;
				}

				// Add the entry.
				MenuItem recentFileMenuItem = new MenuItem(this.recentFileMenu, SWT.NONE);
				recentFileMenuItem.setText(menuText);

				/*
				 * Add the listener that will open the specified file.
				 */
				recentFileMenuItem.addSelectionListener(new SelectionListener() {
			        public void widgetSelected(SelectionEvent e) {
						// Check if the file exists.
						String pathToFile = fullPath;
						String pathInJar = null;
						if (fullPath.contains("|")) {
							pathToFile = fullPath.substring(0, fullPath.indexOf("|"));
							pathInJar = fullPath.substring(fullPath.indexOf("|") + 1);
						}
						try {
							File file = new File(pathToFile);
							if (!file.exists() || !file.isFile()) {
								// The file could not be found. Remove it.
								Options.getInst().recentFilesPaths.remove(fullPath);
								// Tell this to the user.
								StaticGuiSupport.showMessageBox(getShell(), "Error", "The file " + pathToFile + " could not be opened. It has probably been removed or is located on a volume currently not available.", SWT.OK | SWT.ICON_ERROR);
								// Do not continue.
								return;
							}
							// If we have to find a file within a jar-archive, check if it is available.
							if (pathInJar != null) {
								JarFile jarFile = new JarFile(pathToFile);
								JarEntry entry = jarFile.getJarEntry(pathInJar);
								if (entry == null) {
									// The file could not be found. Remove it.
									Options.getInst().recentFilesPaths.remove(fullPath);
									// Tell this to the user.
									StaticGuiSupport.showMessageBox(getShell(), "Error", "The jar-archive " + pathToFile + " was opened. However, the entry " + pathInJar + " could not be found. The archive probably was modified.", SWT.OK | SWT.ICON_ERROR);
									// Do not continue.
									jarFile.close();
									return;
								}
								jarFile.close();
							}

							// Everything went fine. Browse to the file.
							if (!getChild().browseTroughTheDirectoryTree(fullPath, null)) {
								// Browsing failed. Remove the file and inform the user.
								Options.getInst().recentFilesPaths.remove(fullPath);
								StaticGuiSupport.showMessageBox(getShell(), "Error", "The file " + pathToFile + " seems to be valid. However, browsing to it failed.", SWT.OK | SWT.ICON_ERROR);
							}
						} catch (IOException exc) {
							// Remove the file.
							Options.getInst().recentFilesPaths.remove(fullPath);
							// Tell this to the user.
							StaticGuiSupport.showMessageBox(getShell(), "Error", "The file " + pathToFile + " could not be opened due to an I/O error. It probably is locked or cannot be processed by this application.", SWT.OK | SWT.ICON_ERROR);
						}
			        }

			        public void widgetDefaultSelected(SelectionEvent e) { }
			      });
			}
		}
	}

}
