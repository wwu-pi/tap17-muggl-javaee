package de.wwu.muggl.ui.gui.support;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Representation for a entry in a jar file. This supporting class is needed to display jar
 * files in the directory tree and get means of expanding them.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class JarFileEntry {
	private JarFile jaFfile;
	private String fileName;
	private ArrayList<Object[]> fileList;

	/**
	 * Initialize the jar entry.
	 * @param jarFile The jar file the entry is in.
	 * @param fileName The name of the file to store in this entry.
	 * @param fileList A reference to the current fileList.
	 */
	public JarFileEntry(JarFile jarFile, String fileName, ArrayList<Object[]> fileList) {
		this.jaFfile = jarFile;
		this.fileName = fileName;
		this.fileList = fileList;
	}

	/**
	 * Expand the JarFile at the TreeItem root.
	 * @param root The TreeItem to expand.
	 * @param onlyShowDirectories If true, only directorys will be shown.
	 */
	public void expand(TreeItem root, boolean onlyShowDirectories) {
		Enumeration<JarEntry> entries = this.jaFfile.entries();
		int preLength = this.fileName.length();
		ArrayList<String> directories = new ArrayList<String>();
		Hashtable<String, TreeItem> directoryItemMapping = new Hashtable<String, TreeItem>();
		// Browse trough all entries, since jar files might be unordered and are not browsable hierarchically.
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			// match?
			if (entry.getName().length() > preLength && entry.getName().substring(0, preLength).equals(this.fileName))
			{
				// it is a directory or it might be a directory that is no explicit entry
				if (entry.isDirectory() || entry.getName().substring(preLength, entry.getName().length() - 1).contains("/"))
				{
					int slashPos = entry.getName().substring(preLength).indexOf("/") + 1;
					if (entry.isDirectory() || entry.getName().length() > preLength + slashPos)
					{
						String fileName = entry.getName().substring(preLength, preLength + slashPos);
						// check if the entry does not exist
						if (!directories.contains(fileName)) {
							// Add to the directory tree.
							directories.add(fileName);
			    			TreeItem item = new TreeItem(root, 0);
			    			item.setText(entry.getName().substring(preLength, preLength + slashPos - 1));
			    			item.setData(new JarFileEntry(this.jaFfile, entry.getName().substring(0, preLength + slashPos), this.fileList));
			    			directoryItemMapping.put(fileName, item);
						}
		    			// Does this entry contain any subdirectories or jar-files beneath it?
		    			String subDirectories = entry.getName().substring(preLength + slashPos);
						if (subDirectories.length() > 0
								&& (subDirectories.contains("/") || isArchive(subDirectories))) {
							TreeItem item = directoryItemMapping.get(fileName);
		    				if (item != null && item.getItemCount() == 0) {
		    					new TreeItem(item, 0);
		    				}
		    			}
					}
				} else if (!onlyShowDirectories && !entry.getName().substring(preLength).contains("/")) { // also display files in this directory
	    			TreeItem item = new TreeItem(root, 0);
	    			item.setText(entry.getName().substring(preLength));
	    			item.setData(new JarFileEntry(this.jaFfile, entry.getName(), this.fileList));
				}
			}
		}
	}

	/**
	 * Add the class files in the currently selected directory to the fileList.
	 * @param fileList The fileList to add the class files to.
	 */
	public void expandClassFiles(List fileList) {
		Enumeration<JarEntry> entries = this.jaFfile.entries();
		int preLength = this.fileName.length();
		// Browse trough all entries, since jar files might be unordered and are not browsable hierarchically.
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			// Find the correct directory.
			if (!entry.isDirectory() && entry.getName().length() > preLength && entry.getName().substring(0, preLength).equals(this.fileName)) {
				String remainingEntry = entry.getName().substring(preLength);
				// Only add class files.
				if (!remainingEntry.contains("/") && remainingEntry.length() >= 6 && remainingEntry.substring(remainingEntry.length() - 6).equals(".class")) {
					Object[] object = {remainingEntry, entry.getName(), this.jaFfile};
					this.fileList.add(object);
				}
			}
		}

		// Sort the new entries - first convert it into a String array.
		String[] orderedEntries = new String[this.fileList.size()];
		Iterator<Object[]> iterator = this.fileList.iterator();
		int a = 0;
		while (iterator.hasNext()) {
			orderedEntries[a] = (String) iterator.next()[0];
			a++;
		}

		// Use the standard sort functionality.
		java.util.Arrays.sort(orderedEntries);

		// And finally add the entries to the list.
		for (a = 0; a < orderedEntries.length; a++) {
			fileList.add(orderedEntries[a]);
		}
	}

	/**
	 * Getter for the jar file.
	 * @return The JarFile the entry is in.
	 */
	public JarFile getJarFile() {
		return this.jaFfile;
	}

	/**
	 * Getter for the files' name.
	 * @return The name of the file.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Checks whether a given file name represents a jar, ear or war archive.
	 * @param filename
	 * @return true if a filename corresponds to a Java archive. 
	 */
	public static boolean isArchive(String filename) {
		if (! (filename.length() > 4)) {
			return false;
		}
		
		String substring = filename.toLowerCase();
		return substring.endsWith(".ear") || substring.endsWith(".jar") || substring.endsWith(".war");
	}

}
