package de.wwu.muggl.vm.initialization.strings;

import java.util.HashMap;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.FieldAccessError;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.PrimitiveWrappingImpossibleException;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.util.HostEnvironment;

/**
 * This class provides a cache for strings. It is required by the Java Language Specification,
 * Second Edition <a href="http://java.sun.com/docs/books/jls/second_edition/html/j.title.doc.html"
 * target="_blank">http://java.sun.com/docs/books/jls/second_edition/html/j.title.doc.html</a> that
 * string literals are "interned" and share the same internal instance (see section 3.10.5). Hence,
 * two string literals with the same values are also the same instance.<br />
 * <br />
 * More technically speaking, the string cache is used when executing ldc instructions and when
 * using the intern() method of strings. ldc and its widened version ldc_w can be used to push
 * string literals from the constant pool. They will get a value from the constant pool and use the
 * string cache to get an String object reference for it. Calls to the native method intern() in
 * class java.lang.String are caught and forwarded to the string cache either. By doing so, the
 * requirements can be met and this virtual machine implementation behaves as it is supposed to.<br />
 * <br />
 * Whenever a String object reference is requested, the cache is searched for an already existing
 * instance of the adequate String. If it is found, it will simply be returned. Otherwise, a new
 * String object reference is constructed. Before it is returned it will be cached for future usage.<br />
 * <br />
 * To speed up the access to cached object references, the character representation of strings is
 * used to build a search tree of String object reference. There is a root {@link StringCacheEntry},
 * containing the String object reference for the empty String. It has a {@link HashMap} containing
 * the first characters of cached strings as its key and linking to further StringCacheEntry
 * instances. They each have a HashMap of their own, linking to further entries.<br />
 * <br />
 * Searching for a cached String starts at the root entry. If the empty String is searched for, the
 * object reference is returned directly. Otherwise, the first character is used to check the
 * HashMap for the corresponding entry. When found, the StringCacheEntry is accessed and search
 * continues on it with the next character. This is kept on until the last character is reached. The
 * corresponding StringCacheEntry will hold a reference to the appropriate String object reference.
 * If it does not, or if at some point there was no tree entry for the currently searched character,
 * the needed entries are generated.<br />
 * <br />
 * The search tree thus has a layout like this:<br />
 *
 * <blockquote>
 *
 * <pre>
 *        a             b        c        d        ...
 *    / / | \ \     / / | \ \
 *   a b  c d ...  a b  c d ...
 *  / \
 * a   ...
 * </pre>
 *
 * </blockquote>
 *
 * Each node (StringCacheEntry) (not only leafs) may hold a reference to a String object reference.
 * The edges of the search tree are the entries of the HashMaps.<br />
 * <br />
 * In order to speed up execution even further, hashing is used. Wrapping characters as
 * {@link HashableCharacter} makes sure that each distinct character has an unique hash code value
 * and can be used in HashMap instances that provide constant-time performance for the operations
 * required.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class StringCache {
	// Reference of the virtual machine.
	private VirtualMachine vm;

	// Cached objects.
	private ClassFile stringClassFile;
	private ClassFile characterClassFile;
	private Field stringCountField;
	private Field stringHashField;
	private Field stringOffsetField;
	private Field stringValueField;

	// The root entry.
	private StringCacheEntry root;

	/**
	 * Construct the string cache. It will acquire the required class files by using the class
	 * loader.
	 *
	 * @param vm The virtual machine this frame belongs to.
	 * @throws InitializationException If either of the required classes could not be loaded.
	 */
	public StringCache(VirtualMachine vm) throws InitializationException {
		this.vm = vm;

		// Pre-cache objects.
		try {
			this.stringClassFile = vm.getClassLoader().getClassAsClassFile("java.lang.String");
		} catch (ClassFileException e) {
			throw new InitializationException(
					"Fatal problem constructing the StringCache: Cannot load class java.lang.String.");
		}
		try {
			this.characterClassFile = vm.getClassLoader()
					.getClassAsClassFile("java.lang.Character");
		} catch (ClassFileException e) {
			throw new InitializationException(
					"Fatal problem constructing the StringCache: Cannot load class java.lang.Character.");
		}
		this.stringHashField = this.stringClassFile.getFieldByNameAndDescriptor("hash", "I");
		this.stringValueField = this.stringClassFile.getFieldByNameAndDescriptor("value", "[C");
		if (HostEnvironment.getMajor() == 1 && HostEnvironment.getMinor() <= 6) {
			// These private properties are inexistent since Java SE 8.
			this.stringCountField = this.stringClassFile.getFieldByNameAndDescriptor("count", "I");
			this.stringOffsetField = this.stringClassFile.getFieldByNameAndDescriptor("offset", "I");
		}
		this.root = new StringCacheEntry(this, provideStringReference(new char[0]));
	}

	/**
	 * Construct the string cache. The required class files are specified explicitly.
	 *
	 * @param vm The virtual machine this frame belongs to.
	 * @param stringClassFile The ClassFile for java.lang.String.
	 * @param characterClassFile The ClassFile for java.lang.Character.
	 * @throws IllegalArgumentException If the specified class files do not represent the expected classes.
	 */
	public StringCache(VirtualMachine vm, ClassFile stringClassFile, ClassFile characterClassFile) {
		this.vm = vm;

		// Check the class files.
		if (stringClassFile == null || stringClassFile.getName().equals("java.lang.String"))
			throw new IllegalArgumentException("The specified class file must be java.lang.String.");
		if (characterClassFile == null || characterClassFile.getName().equals("java.lang.Character"))
			throw new IllegalArgumentException("The specified class file must be java.lang.Character.");

		// Pre-cache objects.
		this.stringClassFile = stringClassFile;
		this.characterClassFile = characterClassFile;
		this.stringHashField = this.stringClassFile.getFieldByNameAndDescriptor("hash", "I");
		this.stringValueField = this.stringClassFile.getFieldByNameAndDescriptor("value", "[C");
		if (HostEnvironment.getMajor() == 1 && HostEnvironment.getMinor() <= 6) {
			// These private properties are inexistent since Java SE 8.
			this.stringCountField = this.stringClassFile.getFieldByNameAndDescriptor("count", "I");
			this.stringOffsetField = this.stringClassFile.getFieldByNameAndDescriptor("offset", "I");
		}
		this.root = new StringCacheEntry(this, provideStringReference(new char[0]));
	}

	/**
	 * Get the String object reference for the specified String.
	 *
	 * @param string The String to get a String object reference for.
	 * @return The "interned" String object reference.
	 */
	public Objectref getStringObjectref(String string) {
		return getStringObjectref(string.toCharArray());
	}

	/**
	 * Get the String object reference for the specified array of characters.
	 *
	 * @param characters The array of characters to get the String object reference for.
	 * @return The "interned" String object reference.
	 */
	public Objectref getStringObjectref(char[] characters) {
		// Try to find the String.
		Objectref objectref = this.root.get(characters);

		// Has it been found?
		if (objectref == null) {
			// Generate and put it.
			objectref = provideStringReference(characters);
			this.root.put(characters, objectref);
		}

		// Whatever happened, at this point we have the object reference ready.
		return objectref;
	}

	/**
	 * Get the "interned" String object reference for the specified object reference.
	 *
	 * @param objectref The String object reference to get the "interned" String object reference
	 *        for.
	 * @return The "interned" String object reference.
	 * @throws IllegalArgumentException If the object reference is not for class java.lang.String.
	 */
	public Objectref getStringObjectref(Objectref objectref) {
		// Check if the object reference is a reference of java.lang.String.
		if (!objectref.getInitializedClass().getClassFile().getName().equals("java.lang.String")) {
			throw new IllegalArgumentException(
					"Can only process object references of java.lang.String.");
		}

		// Get the array of characters.
		Arrayref arrayref = (Arrayref) objectref.getField(this.stringValueField);

		// Convert it.
		boolean symbolicalMode = Options.getInst().symbolicMode;
		char[] characters = new char[arrayref.length];
		for (int a = 0; a < arrayref.length; a++) {
			if (symbolicalMode) {
				characters[a] = (char) ((IntConstant) arrayref.getElement(a)).getIntValue();
			} else {
				characters[a] = (Character) arrayref.getElement(a);
			}
		}

		// Get the String object reference.
		return getStringObjectref(characters);
	}

	/**
	 * Provide a String object reference. It automatically adjusts to normal respectively symbolic
	 * execution mode.
	 *
	 * @param charArray The array of characters representing the String a object reference shall be
	 *        generated for.
	 * @return A String object reference.
	 */
	private Objectref provideStringReference(char[] charArray) {
		boolean symbolicalMode = Options.getInst().symbolicMode;
		Objectref stringInitializedClass = this.vm.getAnObjectref(this.stringClassFile);
		ReferenceValue referenceValue = null;

		// Preparation of the array of characters.
		try {
			referenceValue = this.characterClassFile.getAPrimitiveWrapperObjectref(this.vm);
		} catch (PrimitiveWrappingImpossibleException e) {
			// This cannot happen.
		}
		Arrayref arrayref = new Arrayref(referenceValue, charArray.length);
		for (int a = 0; a < charArray.length; a++) {
			if (symbolicalMode) {
				arrayref.putElement(a, IntConstant.getInstance(charArray[a]));
			} else {
				arrayref.putElement(a, charArray[a]);
			}
		}

		// Put the fields.
		stringInitializedClass.putField(this.stringValueField, arrayref);
		if (symbolicalMode) {
			if (HostEnvironment.getMajor() == 1 && HostEnvironment.getMinor() <= 6) {
				// These private properties are inexistent since Java SE 8.
				stringInitializedClass.putField(this.stringOffsetField, IntConstant.getInstance(0));
				stringInitializedClass.putField(this.stringCountField,
						IntConstant.getInstance(charArray.length));
			}
			stringInitializedClass.putField(this.stringHashField,
					IntConstant.getInstance(getHashCode(charArray)));
		} else {
			if (HostEnvironment.getMajor() == 1 && HostEnvironment.getMinor() <= 6) {
				// These private properties are inexistent since Java SE 8.
				stringInitializedClass.putField(this.stringOffsetField, 0);
				stringInitializedClass.putField(this.stringCountField, charArray.length);
			}
			stringInitializedClass.putField(this.stringHashField, getHashCode(charArray));
		}
		return stringInitializedClass;
	}

	/**
	 * Get the hash code for an array of characters as specified by
	 * {@link java.lang.String#hashCode()}.
	 *
	 * @param characters An array of characters.
	 * @return The hash code value.
	 */
	private int getHashCode(char[] characters) {
		int length = characters.length;
		int hashCode = 0;
		for (int a = 0; a < length; a++) {
			hashCode += (int) (characters[a] * Math.pow(31, length - a - 1));
		}
		return hashCode;
	}

	/**
	 * Returns a String representation of the string cache.
	 *
	 * @return A String representation of the string cache
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "String cache";
	}

	/**
	 * Get a String value from a field of an object reference.
	 * 
	 * @param objectref The object reference to get the field from.
	 * @param fieldName The name of the field.
	 * @return The extracted String; or null, if either the field is unset or its value is null.
	 * @throws ExecutionException If field access or resolution failed.
	 * @throws IllegalArgumentException If a field with the specified name exists but its type is
	 *         not java.lang.String.
	 */
	public String getStringFieldValue(Objectref objectref, String fieldName) throws ExecutionException {
		try {
			ClassFile objectrefClassFile = objectref.getInitializedClass().getClassFile();
			Field field = objectrefClassFile.getFieldByName(fieldName, true);
			if (!field.getType().equals("java.lang.String"))
				throw new IllegalArgumentException("The field " + fieldName + " in "
						+ objectrefClassFile.getCanonicalName()
						+ " is not of Type java.lang.String.");
			Objectref stringObjectref = (Objectref) objectref.getField(field);
			if (stringObjectref == null)
				return null;
			Arrayref arrayref = (Arrayref) stringObjectref.getField(this.stringValueField);
			if (arrayref == null)
				return null;
			
			// Convert it.
			boolean symbolicalMode = Options.getInst().symbolicMode;
			char[] characters = new char[arrayref.length];
			for (int a = 0; a < arrayref.length; a++) {
				if (symbolicalMode) {
					characters[a] = (char) ((IntConstant) arrayref.getElement(a)).getIntValue();
				} else {
					characters[a] = (Character) arrayref.getElement(a);
				}
			}
			return new String(characters);
		} catch (FieldAccessError e) {
			throw new ExecutionException("Getting String value of field " + fieldName + " from "
					+ objectref.toString() + " failed.");
		} catch (FieldResolutionError e) {
			throw new ExecutionException("Getting String value of field " + fieldName + " from "
					+ objectref.toString() + " failed.");
		}
	}

}