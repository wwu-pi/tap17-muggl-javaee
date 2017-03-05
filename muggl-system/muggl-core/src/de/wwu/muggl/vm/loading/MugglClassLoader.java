package de.wwu.muggl.vm.loading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * The MuggleClassLoader replaces the standard java ClassLoader and loads any classes needed
 * within the virtual machine. It also caches.<br />
 * <br />
 * There are four steps when loading a class:
 * <ol>
 * <li>Try to fetch the class file from the already loaded classes. Loading a class requires file
 * system access, so its always worth searching through the list of loaded classes.</li>
 * <li>Try to find the class in the projects path. The projects path is stored in the first
 * entry of the class path. Any sub-directories are searched recursively.</li>
 * <li>Try to find the class in the java environment libraries. For this, all jar-files in the
 * /lib sub-directory of the java-home directory are opened and the desired class searched in
 * them. Sub-directories of /lib are not opened.</li>
 * <li>Search in the class path entries gradually. Class path entries may be both directories
 * as well as jar-files. If they are directories, the sub-directories of them are also opened
 * recursively.</li>
 * </ol>
 * <br />
 * If the fourth step has been finished without a class being loaded, a ClassNotFoundException
 * is thrown. When executing a project thats class path has been set up correctly and has a
 * appropriate java-home directory specified, this should not happen if the project has been
 * compiled correctly.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class MugglClassLoader extends ClassLoader {
	// Fields.
	/**
	 * The mapping of class names to loaded class files.
	 */
	protected Map<String, ClassFile> loadedClasses;
	private String[] classPathEntries;
	private volatile long classesLoaded;
	private volatile long classesInstantiated;
	private boolean unlimitedCache;

	// Optionally used fields.
	private TreeMap<Long, ClassFile> classesAccessTime;
	private long totalBytesUsedByClassFiles;
	private long maximumClassLoaderCacheEntries;
	private long maximumClassLoaderCacheByteSize;

	/**
	 * Basic constructor.
	 * 
	 * @param classPathEntries A String array of class path entries.
	 * @throws IllegalArgumentException If classPathEntries is null or empty.
	 */
	public MugglClassLoader(String[] classPathEntries) {
		if (classPathEntries == null || classPathEntries.length == 0) {
			throw new IllegalArgumentException("At least one class path entry has to be set.");
		}
		
		this.classPathEntries = classPathEntries;
		this.loadedClasses = new HashMap<String, ClassFile>();
		this.classesLoaded = 0;
		this.classesInstantiated = 0;

		if (Options.getInst().maximumClassLoaderCacheEntries > -1
				|| Options.getInst().maximumClassLoaderCacheBytes != 0) {
			this.unlimitedCache = false;
			this.classesAccessTime = new TreeMap<Long, ClassFile>();
			this.totalBytesUsedByClassFiles = 0L;
			this.maximumClassLoaderCacheEntries = Options.getInst().maximumClassLoaderCacheEntries;
			this.maximumClassLoaderCacheByteSize = Options.getInst().maximumClassLoaderCacheBytes;
		} else {
			this.unlimitedCache = true;
		}
	}

	/**
	 * Overrides the methods provided by the java ClassLoader. It retrieves a ClassFile, using the method
	 * getClassAsClassFile() implemented in this class and takes the bytes read to generate an instance of
	 * Class.
	 * @param name The full name of the desired class, including the package (e.g. java.lang.String).
	 * @return An instance of Class.
	 * @throws ClassNotFoundException Thrown when a class could not be found on the application and system class path, or when a ClassFileException or an IOException occurs.
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			ClassFile classFile = getClassAsClassFile(name);
			// Is it a java system class?
			if (classFile.getName().length() > 4 && classFile.getName().substring(0, 5).equals("java.")) {
				// Using findSystemClass().
				return findSystemClass(classFile.getName());
			}

			// Generate the Class instance.
			return classFile.getInstanceOfClass();
		} catch (ClassFileException e) {
			throw new ClassNotFoundException(
					"Class loading failed due to a ClassFileException with message: "
							+ e.getMessage() + ".");
		}
	}

	/**
	 * Public wrapper for defineClass(). It should only be used by ClassFile.getInstanceOfClass().
	 * It will not accept an offset but expect the bytes array to contain the ful data of the class
	 * and nothing else.
	 * @param name The name of the class.
	 * @param b The bytes of the class.
	 * @return An instance of Class.
	 */
	public Class<?> defineClassFromClassFile(String name, byte[] b)  {
		try {
			Class<?> loadedClass = loadClass(name);
			if (loadedClass != null) return loadedClass;
		} catch (ClassNotFoundException e) {
			// Do nothing.
		}
		return defineClass(name, b, 0, b.length);
	}

	/**
	 * This method is used for the retrieval of any class.
	 * @param javaClass A java Class instance.
	 * @return The loaded but not yet initialized ClassFile.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 */
	public ClassFile getClassAsClassFile(Class<?> javaClass) throws ClassFileException {
		// Just call the method with the second parameter and set it to false.
		return getClassAsClassFile(javaClass.getName(), false);
	}

	/**
	 * This method is used for the retrieval of any class.
	 * @param name The full name of the desired class, including the package (e.g. java.lang.String).
	 * @return The loaded but not yet initialized ClassFile.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 */
	public ClassFile getClassAsClassFile(String name) throws ClassFileException {
		// Just call the method with the second parameter and set it to false.
		return getClassAsClassFile(name, false);
	}

	/**
	 * This method is used for the retrieval of any class. It has an additional parameter, refresh,
	 * which can be set to true to ensure a class will not be reloaded from the cache but read and
	 * parsed. If it already existed in the cache, the old copy will be overwritten.
	 *
	 * @param name The full name of the desired class, including the package (e.g. java.lang.String).
	 * @param refresh If set to true, the class file will be read and parsed even if it is cached.
	 * @return The loaded but not yet initialized ClassFile.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 */
	public ClassFile getClassAsClassFile(String name, boolean refresh) throws ClassFileException {
		name = prepareClassName(name);

		try {
			// First attempt: already loaded?
			ClassFile classFile = findAlreadyLoadedClasses(name);
			if (classFile != null) {
				if (refresh) {
					if (Globals.getInst().logger.isTraceEnabled())
						Globals.getInst().logger.trace("The already cached class " + name
								+ " will not be used and cleared from the cache.");
					this.loadedClasses.remove(classFile.getName());
				} else {
					// Refresh the access setting if needed.
					if (!this.unlimitedCache) {
						this.classesAccessTime.put(System.nanoTime(), classFile);
					}

					// Do not add it - we already have it! It just has to be returned.
					if (Globals.getInst().logger.isTraceEnabled())
						Globals.getInst().logger.trace("The already cached class " + name + " has been loaded.");
					return classFile;
				}
			}

			if (Globals.getInst().logger.isTraceEnabled())
				Globals.getInst().logger.trace("The class loader is trying to load class " + name + ".");

			// Splitting up the name...
			String[] elements = name.replace(".", "/").split("/");
			String[] path;
			if (elements.length > 1) {
				path = new String[elements.length - 1];
				for (int a = 0; a < path.length; a++) {
					path[a] = elements[a];
				}
			} else {
				path = new String[0];
			}
			String className = elements[elements.length - 1] + ".class";

			// Second attempt: find in the projects path
			classFile = getClassFromProjectPath(name, path, className);
			if (classFile != null) {
				addToClassCache(classFile);
				return classFile;
			}

			// Third attempt: find in the java environments libraries.
			classFile = getJavaEnvironmentClass(name.replace(".", "/") + ".class");
			if (classFile != null) {
				addToClassCache(classFile);
				return classFile;
			}

			// Fourth attempt: find in the class path.
			classFile = getClassFromClasspath(name, path, className);
			if (classFile != null) {
				addToClassCache(classFile);
				return classFile;
			}

			// Fifth attempt: find in Muggl's class path.
			classFile = getClassFromMugglClasspath(name, path, className);
			if (classFile != null) {
				addToClassCache(classFile);
				return classFile;
			}

			// Sixth attempt: Find in lib/solving.jar.
			try {
				File file = new File(Globals.BASE_DIRECTORY + "/lib/solving.jar");
				JarFile jarFile = new JarFile(file);
				String nameForJarFileSearch = name.replace(".", "/") + ".class";
				classFile = getFromJarFile(jarFile, nameForJarFileSearch);
				if (classFile != null) {
					addToClassCache(classFile);
					return classFile;
				}
			} catch (FileNotFoundException e) {
				throw new ClassFileException(
						"Loading of class " + name + " failed due to an Error on resolving "
						+ "the required library solving.jar from the /lib directory. The root cause is: "
						+ e.getMessage());
			}
		} catch (ClassFileException e) {
			throw new ClassFileException("Loading of class " + name
					+ " failed due to an Error while parsing the file. The root cause is: "
					+ e.getMessage());
		} catch (IOException e) {
			 // IOExceptions are thrown on fatal problems reading or writing to the file system.
			throw new ClassFileException("Loading of class " + name
					+ " failed due to an I/O Error. The root cause is an IOException: "
					+ e.getMessage());
		}

		// Loading failed!
		if (Globals.getInst().logger.isTraceEnabled())
			Globals.getInst().logger.trace("Loading of class " + name + " failed.");
		throw new ClassFileException("The class " + name + " could not be found.");
	}

	/**
	 * Prepare the class name so it can be processed.
	 *
	 * @param name The (probably badly formed) name of a class.
	 * @return The well formed name of a class.
	 */
	private String prepareClassName(String name) {
		// If the name contains "/", it most likely is in the format java/lang/String. Convert this!
		if (name.contains("/")) {
			name = name.replace("/", ".");
		}

		// Probably the class name is taken from an array. In this case, these elements will be dropped.
		if (name.startsWith("[")) {
			// Drop all dimension identifiers
			while (name.startsWith("[")) {
				name = name.substring(1);
			}
			// Drop the "L"
			if (name.startsWith("L")) name = name.substring(1);
			// Drop the ";" at the end.
			if (name.endsWith(";")) name = name.substring(0, name.length() - 1);
		} else if (name.endsWith("...")) {
			name = name.substring(0, name.length() - 3);
		}

		// If the length is only one, it is a primitive type. Get the appropriate wrapper class.
		if (name.length() == 1) {
			if (name.equals("B")) {
				name = "java.lang.Byte";
			} else if (name.equals("C")) {
				name = "java.lang.Character";
			} else if (name.equals("D")) {
				name = "java.lang.Double";
			} else if (name.equals("I")) {
				name = "java.lang.Integer";
			} else if (name.equals("F")) {
				name = "java.lang.Float";
			} else if (name.equals("J")) {
				name = "java.lang.Long";
			} else if (name.equals("S")) {
				name = "java.lang.Short";
			} else if (name.equals("Z")) {
				name = "java.lang.Boolean";
			}
		}

		// If name ends with .class, this is dropped.
		if (name.length() > 6 && name.substring(name.length() - 6).equals(".class")) {
			name = name.substring(0, name.length() - 6);
		}

		return name;
	}

	/**
	 * Check if a class has already been loaded by this class loader.
	 *
	 * @param name The full name of the desired class, including the package (e.g. java.lang.String).
	 * @return true, if the class is already loaded (and cached); false otherwise.
	 */
	public boolean isClassLoaded(String name) {
		name = prepareClassName(name);
		if (findAlreadyLoadedClasses(name) != null)
			return true;
		return false;
	}

	/**
	 * Private method for the first attempt: finding an already loaded class.
	 *
	 * @param name The full name of the class to find.
	 * @return A ClassFile in case of success, null otherwise.
	 */
	private ClassFile findAlreadyLoadedClasses(String name) {
		return this.loadedClasses.get(name);
	}

	/**
	 * Private method for the second attempt: finding a class in the project path.
	 * @param name The full name of the class to find.
	 * @param path String array holding the ordered names of the packages the class belongs to.
	 * @param className The name of the class without any package information, but with a trailing ".class".
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile getClassFromProjectPath(String name, String[] path, String className)
			throws ClassFileException, IOException {
		if (path.length == 0) {
			path = new String[]{""};
		}
		
		if (isJarFile(this.classPathEntries[0])) {
			return getFromJarFile(new JarFile(new File(this.classPathEntries[0])),
					name.replace(".", "/") + ".class");
		}
		return searchClassInDirectory(new File(this.classPathEntries[0]), 0, path, className);
	}

	/**
	 * Private method for the third attempt: finding a class in the libraries of the java environment.
	 * @param name The full name of the class to find.
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile getJavaEnvironmentClass(String name) throws ClassFileException, IOException {
		// list of all environment directories
		ArrayList<File> javaHomeDirectories = new ArrayList<File>();
		
		// add Windows directory
		javaHomeDirectories.add(new File(Options.getInst().javaHome + "/lib"));
		// add OS X directory
		javaHomeDirectories.add(new File(Options.getInst().javaHome + "/../Classes"));
		// TODO Linux? - find better solution for this
				
		ClassFile classFile = null;
		File[] files;
		
		// try each directory
		for (File javaHomeDirectory : javaHomeDirectories){
			// try each file in directory
			if (javaHomeDirectory.exists() && javaHomeDirectory.isDirectory()) {
				files = javaHomeDirectory.listFiles();
				for (int a = 0; a < files.length; a++) {
					if (files[a].isFile()) {
						// Skip alternative libraries
						if (isJarFile(files[a].getName()) && !files[a].getName().startsWith("alt-")) {
							classFile = getFromJarFile(new JarFile(files[a]), name);
							if (classFile != null) break; 
						}
					}
				}
			}
		}
		
		return classFile;
	}

	/**
	 * Private method for the fourth attempt: finding a class in the class path.
	 *
	 * @param name The full name of the class to find.
	 * @param path String array holding the ordered names of the packages the class belongs to.
	 * @param className The name of the class without any package information, but with a trailing
	 *        ".class".
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile getClassFromClasspath(String name, String[] path, String className)
			throws ClassFileException, IOException {
		String nameForJarFileSearch = name.replace(".", "/") + ".class";
		if (path.length == 0) {
			path = new String[]{""};
		}
		for (int a = 1; a < this.classPathEntries.length; a++) {
			if (isJarFile(this.classPathEntries[a])) {
				ClassFile classFile = getFromJarFile(
						new JarFile(new File(this.classPathEntries[a])), nameForJarFileSearch);
				if (classFile != null) return classFile;
			} else {
				ClassFile classFile = searchClassInDirectory(new File(this.classPathEntries[a]), 0,
						path, className);
				if (classFile != null) return classFile;
			}
		}
		return null;
	}

	/**
	 * Private method for the fifth attempt: finding a class in Muggl's running class path.
	 * Especially relevant for executing Newarray symbolically, as it tries to resolve "de.wwu.muggl.solvers.expressions.Term",
	 * which is part of the `solvers` subproject.
	 *
	 * @param name The full name of the class to find.
	 * @param path String array holding the ordered names of the packages the class belongs to.
	 * @param className The name of the class without any package information, but with a trailing
	 *        ".class".
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile getClassFromMugglClasspath(String name, String[] path, String className)
			throws ClassFileException, IOException {
		String nameForJarFileSearch = name.replace(".", "/") + ".class";
		
		String[] mugglClassPath = System.getProperty("java.class.path").split(File.pathSeparator);
		
		for (String entry : mugglClassPath) {
			if (isJarFile(entry)) {
				ClassFile classFile = getFromJarFile(
						new JarFile(new File(entry)), nameForJarFileSearch);
				if (classFile != null) return classFile;
			} else {
				ClassFile classFile = searchClassInDirectory(new File(entry), 0,
						path, className);
				if (classFile != null) return classFile;
			}
		}
		return null;
	}

	/**
	 * Browse a jar file for the desired class.
	 * @param jarFile The jar file to browse.
	 * @param name The full name of the class to find.
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile getFromJarFile(JarFile jarFile, String name) throws ClassFileException, IOException {
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			/*
			 * Just browse trough all entries, they probably will not be shown hierarchically or in
			 * a sorted order.
			 */
			JarEntry entry = entries.nextElement();
			if (entry != null && entry.getName().equals(name)) {
				// Construct the full path to the class file.
				String fullPath = jarFile.getName() + "|" + entry.getName();

				// Return the ClassFile.
				try {
					return new ClassFile(this, jarFile.getInputStream(entry), jarFile
							.getInputStream(entry), entry.getSize(), fullPath);
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Checks whether a file appears to be a jar file.
	 * @param filename The file to check.
	 * @return true, if the file appears to be a jar file, false otherwise.
	 */
	private boolean isJarFile(String filename) {
		if (!(filename.length() > 4 )) {
			return false;
		}
		String fn = filename.toLowerCase();
		return fn.endsWith(".ear") || fn.endsWith(".jar") || fn.endsWith(".war");
	}

	/**
	 * Browse the specified directory for a class file.
	 * @param directory The directory to browse.
	 * @param pathPos The package that has to be matched in this attempt. Must be at least 0 and less than path.length.
	 * @param path String array holding the ordered names of the packages the class belongs to.
	 * @param className The name of the class without ay package information, but with a trailing ".class".
	 * @return A ClassFile in case of success, null otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 * @throws IOException Thrown on fatal problems reading or writing to the file system.
	 */
	private ClassFile searchClassInDirectory(File directory, int pathPos, String[] path,
			String className) throws ClassFileException, IOException {
		File[] files = directory.listFiles();
		for (int a = 0; a < files.length; a++) {
			// No more deepening, we either get the file in this directory, or it is not there.
			if (pathPos == path.length || path[pathPos].equals("")) {
				if (files[a].isFile() && files[a].getName().equals(className)) return new ClassFile(this, files[a]);
			} else if (files[a].isDirectory() && files[a].getName().equals(path[pathPos])) {
				// recursive deepening
				return searchClassInDirectory(files[a], pathPos + 1, path, className);
			}
		}
		return null;
	}

	/**
	 * Add a ClassFile to the cache of classes.
	 *
	 * @param classFile The ClassFile to add.
	 */
	private void addToClassCache(ClassFile classFile) {
		if (this.unlimitedCache) {
			this.loadedClasses.put(classFile.getName(), classFile);
		} else {
			// Proceed at all?
			if (this.maximumClassLoaderCacheEntries != 0) {
				this.loadedClasses.put(classFile.getName(), classFile);

				// Add to the access time cache.
				this.classesAccessTime.put(System.nanoTime(), classFile);

				// Count up the now used bytes.
				this.totalBytesUsedByClassFiles += classFile.getByteLength();

				/*
				 * Check if the cache is full as too many bytes are used or the number of cached classes
				 * is too high. Remove as many classes as needed to free it.
				 */
				while ((
						(this.maximumClassLoaderCacheByteSize > 0 && this.totalBytesUsedByClassFiles > this.maximumClassLoaderCacheByteSize)
						|| (this.maximumClassLoaderCacheEntries > -1 && this.loadedClasses.size() > this.maximumClassLoaderCacheEntries)
						)
						&& this.loadedClasses.size() > 0) {
					Long key = this.classesAccessTime.firstKey();
					ClassFile classFileToRemove = this.classesAccessTime.get(key);
					this.loadedClasses.remove(classFileToRemove.getName());
					this.classesAccessTime.remove(key);
					this.totalBytesUsedByClassFiles -= classFileToRemove.getByteLength();
					if (Globals.getInst().logger.isTraceEnabled())
						Globals.getInst().logger.trace("The cached class "
								+ classFileToRemove.getName()
								+ " has been unloaded due to caching restrictions.");
				}
			}
		}
	}

	/**
	 * Method to update the class path.
	 * 
	 * @param classPathEntries The new String array of class path entries.
	 * @param discardLoadedClassesIfUnequal If set to true, any cached entries will be discarded if
	 *        the class path was not just appended.
	 * @throws IllegalArgumentException If classPathEntries is null or empty.
	 */
	public void updateClassPath(String[] classPathEntries, boolean discardLoadedClassesIfUnequal) {
		if (classPathEntries == null || classPathEntries.length == 0) {
			throw new IllegalArgumentException("At least one class path entry has to be set.");
		}
		if (discardLoadedClassesIfUnequal) {
			boolean missmatch = false;
			/*
			 * Check if anything changed. If the new array has less elements, no check is needed and
			 * the cache can be emptied.
			 */
			if (classPathEntries.length == this.classPathEntries.length
					|| classPathEntries.length > this.classPathEntries.length) {
				// Check entry by entry.
				for (int a = 0; a < this.classPathEntries.length; a++) {
					if (!classPathEntries[a].equals(this.classPathEntries[a])) {
						missmatch = true;
						break;
					}
				}
				// No mismatch and same number of entries?
				if (!missmatch && classPathEntries.length == this.classPathEntries.length) {
					// Everything is equal - no need to drop loaded classes!
					return;
				}
			} else {
				missmatch = true;
			}

			// At this point, we either have a mismatch or no mismatch but an appended class paths.
			if (missmatch) {
				// Clear already loaded classes.
				this.loadedClasses.clear();
			}
		}

		// Set new classPathEntries.
		this.classPathEntries = classPathEntries;
	}

	/**
	 * Clear the cache of initialized classes.
	 */
	public void resetInitializedClassFileCache() {
		Set<Entry<String, ClassFile>> entrySet = this.loadedClasses.entrySet();
		for (Entry<String, ClassFile> entry : entrySet) {
			entry.getValue().unloadInitializedClass();
		}
	}

	/**
	 * Undo any optimizations done at the level of class file structured, especially by replacing
	 * byte code with optimized code. This operation is working in an own thread, yet it is blocking
	 * the class cache. For a large number of classes loaded, it might take a while to complete.
	 */
	public void undoOptimizations() {
		// Create the new Runnable...
		Runnable runner = new Runnable() {
			public void run() {
				synchronized (MugglClassLoader.this.loadedClasses) {
					for (ClassFile classFile : MugglClassLoader.this.loadedClasses.values()) {
						for (Method method : classFile.getMethods()) {
							method.resetReplacedInstructions();
						}
					}
				}
			}
		};

		// Run it.
		Thread thread = new Thread(runner);
		thread.start();
	}

	/**
	 * Unload any cached instructions from methods of class files. This should be done any time byte
	 * code level optimization settings are changed. This operation is working in an own thread, yet
	 * it is blocking the class cache. For a large number of classes loaded, it might take a while
	 * to complete.
	 */
	public void unloadAllInstructions() {
		// Create the new Runnable...
		Runnable runner = new Runnable() {
			public void run() {
				synchronized (MugglClassLoader.this.loadedClasses) {
					for (ClassFile classFile : MugglClassLoader.this.loadedClasses.values()) {
						for (Method method : classFile.getMethods()) {
							method.unloadInstructions();
						}
					}
				}
			}
		};

		// Run it.
		Thread thread = new Thread(runner);
		thread.start();
	}

	/**
	 * Get the next loading number and increase it by one. This method
	 * is only to be used by initializing ClassFiles that want to receive
	 * their loading number.
	 *
	 * @return The next loading number.
	 */
	public synchronized long getNextLoadingNumber() {
		this.classesLoaded++;
		return this.classesLoaded - 1;
	}

	/**
	 * Get the next instantiation number and increase it by one. This method is only to be used by
	 * initializing ReferenceValues that want to receive their instantiation number. Each reference
	 * value that is supposed to be used within the virtual machine has to get an instantiation
	 * number. It is an unique identifier for it.
	 *
	 * @return The next instantiation number.
	 */
	public synchronized long getNextInstantiationNumber() {
		this.classesInstantiated++;
		return this.classesInstantiated - 1;
	}

	/**
	 * Reset the number of instantiated classes to zero. This should only be done when starting
	 * execution in a new virtual machine. Otherwise, two distinct objects might have the same
	 * instantiation number and thus the same hash code.
	 */
	public synchronized void resetInstantiationNumber() {
		this.classesInstantiated = 0L;
	}

	/**
	 * Getter for the number of classes loaded.
	 * @return The number of classes loaded.
	 */
	public long getClassesLoaded() {
		return this.classesLoaded;
	}

	/**
	 * Getter for the number of classes instantiated.
	 * @return The number of classes instantiated.
	 */
	public long getClassesInstantiated() {
		return this.classesInstantiated;
	}

	/**
	 * Insert a manually loaded class file into this class loaders cache of loaded class files. This method
	 * is meant to be used if for any reasons a ClassFile is instantiated directly. However, class files
	 * can only be inserted if their class loader has been specified as this.
	 *
	 * @param classFile The ClassFile to be inserted.
	 * @throws IllegalArgumentException If the class loader of the class file is inappropriate or if it already in the classes' cache.
	 * @throws NullPointerException If classFile is null.
	 */
	public synchronized void insertManuallyLoadedClassfile(ClassFile classFile) {
		if (classFile == null)
			throw new NullPointerException("You have to supply a class file.");
		if (classFile.getClassLoader() != this)
			throw new IllegalArgumentException(
					"Only class files thats classloader is this can be added to the classes' cache.");
		if (this.loadedClasses.containsKey(classFile.getName()))
			throw new IllegalArgumentException(
					"The supplied class file has been loaded by this classloader. It cannot be overwritten.");
		addToClassCache(classFile);
	}

}
