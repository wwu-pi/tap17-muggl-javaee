package de.wwu.muggl.vm.initialization.strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.vm.initialization.Objectref;

/**
 * This class provides the nodes in the search tree of the string cache.<br />
 * <br />
 * As described in detail in {@link StringCache}, the string cache is organized as a tree. The
 * StringCache has a reference to the root StringCacheEntry instance, which does not have a key but
 * holds the reference to the empty String object reference. Beside that, it has a {@link HashMap}
 * containing the first character of cached strings as its key and linking to further string cache
 * entries.<br />
 * <br />
 * Each entry has a HashMap itself, representing the edges to the next level of entries. The values
 * of those nodes are the characters of the cached String object reference, whereas the depth of the
 * search tree reflects the length of the strings.<br />
 * <br />
 * Beside providing this node functionality, including the possibility to store String reference
 * values, this class also encapsulates the functionality to get and put objects from the search
 * tree. The algorithms for getting and putting the object references for strings are found in this
 * class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
final class StringCacheEntry {
	// Fields.
	private final StringCache stringCache;
	private char key;
	private Objectref stringObjectref;
	private boolean isRoot;
	private Map<HashableCharacter, StringCacheEntry> children;

	/**
	 * Construct the root entry. It does not have a key and will thus reflect the empty String.<br />
	 * <br />
	 * Do <b>not</b> use this constructor but to generate the empty String entry for the string
	 * cache.
	 *
	 * @param stringCache A reference to the StringCache this entry belongs to.
	 * @param stringObjectref The String object reference to store for this node of the string cache
	 *        search tree.
	 * @throws NullPointerException If either of the parameters is null.
	 */
	public StringCacheEntry(StringCache stringCache, Objectref stringObjectref) {
		// Check the parameters.
		if (stringCache == null)
			throw new NullPointerException("The specified StringCache must not be null.");
		if (stringObjectref == null)
			throw new NullPointerException("The specified object reference must not be null.");

		// Set the fields.
		this.stringCache = stringCache;
		this.stringObjectref = stringObjectref;
		this.isRoot = true;

		/*
		 * Make sure concurrent access will be all right. Multiple threads might attempt to put the
		 * same String at a time.
		 */
		this.children = Collections
				.synchronizedMap(new HashMap<HashableCharacter, StringCacheEntry>());
	}

	/**
	 * Construct an entry without a cached object reference.
	 *
	 * @param stringCache A reference to the StringCache this entry belongs to.
	 * @param key The value of this node of the string cache search tree, reflecting the x'th
	 *        character of a String.
	 * @throws NullPointerException If the reference to the string cache is null.
	 */
	public StringCacheEntry(StringCache stringCache, char key) {
		this(stringCache, key, null);
	}

	/**
	 * Construct an entry with a cached object reference.
	 *
	 * @param stringCache A reference to the StringCache this entry belongs to.
	 * @param key The value of this node of the string cache search tree, reflecting the x'th
	 *        character of a String.
	 * @param stringObjectref The String object reference to store for this node of the string cache
	 *        search tree.
	 * @throws NullPointerException If the reference to the string cache is null.
	 */
	public StringCacheEntry(StringCache stringCache, char key, Objectref stringObjectref) {
		// Check the parameters.
		if (stringCache == null)
			throw new NullPointerException("The specified StringCache must not be null.");

		// Set the fields.
		this.stringCache = stringCache;
		this.key = key;
		this.stringObjectref = stringObjectref;
		this.isRoot = false;

		/*
		 * Make sure concurrent access will be all right. Multiple threads might attempt to put the
		 * same String at a time.
		 */
		this.children = Collections
				.synchronizedMap(new HashMap<HashableCharacter, StringCacheEntry>());
	}

	/**
	 * Getter for the cached String object reference.
	 *
	 * @return The cached String object reference; might be null.
	 */
	public Objectref getStringObjectref() {
		return this.stringObjectref;
	}

	/**
	 * Setter for the cached String object reference. It should only be called if the String object
	 * reference has not be set at construction time and it should only be called once.
	 *
	 * @param stringObjectref The String object reference to cache.
	 * @throws IllegalStateException If overwriting the String object reference is attempted.
	 * @throws NullPointerException If the specified object reference is null.
	 */
	public void setStringObjectref(Objectref stringObjectref) {
		if (this.stringObjectref != null)
			throw new IllegalStateException("Cannot ovewrite a String object reference.");
		if (stringObjectref == null)
			throw new NullPointerException("The specified object reference must not be null.");
		this.stringObjectref = stringObjectref;
	}


	/**
	 * Get the cached String object reference for a String.
	 *
	 * @param keys The array of characters representing the string's value.
	 * @return The String object reference; or null, if no appropriate object reference could be
	 *         found (i.e. the String is interned for the first time).
	 */
	public Objectref get(char[] keys) {
		if (keys.length == 0) {
			/*
			 * It is the empty String. It is cached, since this is the root entry. Anything else is
			 * impossible if this class is used correctly.
			 */
			return this.stringObjectref;
		}

		// Search for the appropriate entry.
		return get(keys, 0);
	}

	/**
	 * Recursively get the cached String object reference for a String. This is the internal method
	 * for this purpose. Thus, it introduces the parameter {@code offset} that will show which
	 * position in the array of characters is currently looked up in the search tree.
	 *
	 * @param keys The array of characters representing the string's value.
	 * @param offset The position in the array of characters that is currently looked up in the
	 *        search tree.
	 * @return The String object reference; or null, if no appropriate object reference could be
	 *         found (i.e. the String is interned for the first time).
	 */
	private Objectref get(char[] keys, int offset) {
		// Check if there is a edge to the key.
		StringCacheEntry entry = this.children.get(HashableCharacter.instanceFor(keys[offset]));
		if (entry == null) {
			return null;
		}

		// What to do next?
		if (offset < keys.length - 1) {
			// Keep on searching.
			return entry.get(keys, offset + 1);
		} else if (offset == keys.length - 1) {
			// Found the entry! It may contain null, but returning that is all right.
			return entry.stringObjectref;
		}

		// Cannot move beyond the offset - 1.
		return null;
	}

	/**
	 * Put the cached String object reference for a String.
	 *
	 * @param keys The array of characters representing the string's value.
	 * @param stringObjectref The String object reference to cache.
	 * @throws IllegalArgumentException If the String object reference is cached already.
	 */
	public void put(char[] keys, Objectref stringObjectref) {
		put(keys, 0, stringObjectref);
	}

	/**
	 * Recursively put the cached String object reference for a String. This is the internal method
	 * for this purpose. Thus, it introduces the parameter {@code offset} that will show which
	 * position in the array of characters is currently looked up - and generated, if not met - in
	 * the search tree.
	 *
	 * @param keys The array of characters representing the string's value.
	 * @param offset The position in the array of characters that is currently looked up in the
	 *        search tree.
	 * @param stringObjectref The String object reference to cache.
	 * @throws IllegalArgumentException If the String object reference is cached already.
	 */
	private void put(char[] keys, int offset, Objectref stringObjectref) {
		// Try to find the edge to the desired entry.
		HashableCharacter hashableCharacter = HashableCharacter.instanceFor(keys[offset]);
		StringCacheEntry entry = this.children.get(hashableCharacter);
		if (entry == null) {
			// Create the edge.
			entry = new StringCacheEntry(this.stringCache, keys[offset], null);
			this.children.put(hashableCharacter, entry);
		}

		// What to do next?
		if (offset < keys.length - 1) {
			// Keep on searching (and, at need, generating).
			entry.put(keys, offset + 1, stringObjectref);
		} else if (offset == keys.length - 1) {
			// Check if the entry already exists.
			if (entry.stringObjectref != null)
				throw new IllegalArgumentException("String object reference is cached already.");
			entry.stringObjectref = stringObjectref;
		}
	}

	/**
	 * Returns a String representation of the string cache entry.
	 *
	 * @return A String representation of the string cache entry.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String hasObjectref = "";
		String children = "";
		if (this.stringObjectref != null) {
			hasObjectref = " with an String object reference";
		}

		Set<HashableCharacter> keySet = this.children.keySet();
		for (HashableCharacter key : keySet) {
			if (children.length() > 0) children += ", ";
			children += key.toString();
		}

		// Is it the root entry?
		if (this.isRoot) {
			return "Root String cache entry. Children:\n" + children;
		}
		return "String cache entry for " + String.valueOf(this.key) + hasObjectref
				+ ". Children:\n" + children;
	}

}
