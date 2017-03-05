package de.wwu.muggl.vm.initialization.strings;

/**
 * Represents a primitive char and offers methods to compare and hash it. This class is meant to be
 * used for the string cache only. To make String caching fast, hashing characters is essential. This
 * class makes sure that no two characters have the same hash code value; however, in contradiction
 * to the implementation in java.lang.Character, two HashableCharacters representing the same
 * character will have the same hash code value!<br />
 * <br />
 * As this class should only be used by {@link StringCache}, the visibility is limited to the package.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
final class HashableCharacter implements Comparable<HashableCharacter> {
	// Static field.
	private static final int CACHE_OFFSET = 0;
	private static final int CACHE_SIZE = 256;
	private static final HashableCharacter[] CACHE;

	// Instance field.
	private final char character;

	/*
	 * Cache initialization. The first 256 unicode characters will be cached. On systems using Latin
	 * signs they are by far the most used characters. Caching could be adjusted to the memory
	 * available and probably to the language of the host system or application executed. This can
	 * be done using the constants CACHE_OFFSET and CACHE_SIZE. For now, it should do perfectly well
	 * and speed up initialization.
	 */
	static {
		CACHE = new HashableCharacter[CACHE_SIZE];
		for (int a = CACHE_OFFSET; a < CACHE_OFFSET + CACHE_SIZE; a++) {
			CACHE[a] = new HashableCharacter((char) a);
		}
	}

	/**
	 * Construct the HashableCharacter with the character it will represent. Instance of
	 * HashableCharacter are immutable. The constructor is used internally only, instances should be
	 * aquired via the {@link #instanceFor(char)} method.
	 *
	 * @param character The character to represent.
	 */
	private HashableCharacter(char character) {
		this.character = character;
	}

	/**
	 * Get an instance of HashableCharacter for the specified character. This method uses caching to reduce initiation times.
	 *
	 * @param character The character to represent.
	 * @return The instance HashableCharacter.
	 */
	public static HashableCharacter instanceFor(char character) {
		int intValue = character;

		// Got a cached instance?
		if (intValue < CACHE_OFFSET + CACHE_SIZE) {
			return CACHE[intValue];
		}

		// Instantiate and return.
		return new HashableCharacter(character);
	}

	/**
	 * Getter for the represented character.
	 *
	 * @return The represented character.
	 */
	public char getCharacter() {
		return this.character;
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero,
	 * or a positive integer as this object is less than, equal to, or greater than the specified
	 * object.
	 *
	 * @param hashableCharacter The object to be compared.
	 * @return A negative integer, zero, or a positive integer as the represented character's
	 *         integer representation is less than, equal to, or greater than the specified object.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(HashableCharacter hashableCharacter) {
		return this.character - hashableCharacter.character;
	}

	/**
	 * Indicates whether some other object is equal to this one. Only HashableCharacter objects
	 * representing the same character are considered equal. Thus, comparing a
	 * {@link java.lang.Character} that wraps the same char as this will still have false returned.
	 *
	 * @param obj The object to be compared.
	 * @return true, if the specified object is a HashableCharacter representing the same character;
	 *         false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HashableCharacter) {
			if (((HashableCharacter) obj).character == this.character) return true;
		}
		return false;
	}

	/**
	 * Returns a hash code value for the hashbale character.
	 *
	 * @return A hash code value for this object which is equal to the inter representation of the
	 *         wrapped char.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.character;
	}

	/**
	 * Returns a String representation of the character represented.
	 *
	 * @return A String representation of the character represented.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(this.character);
	}
}