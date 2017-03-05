package de.wwu.muggl.vm.initialization;

/**
 * This class offers static methods to compare objects of the virtual machine provided by this
 * application. It is not meant to compare "normal" java objects - the result of any such
 * comparison will simply be false. Objects passed to any of the methods provided should either
 * implement the interface ReferenceValue or are wrapper objects for primitive types.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class ObjectComparator {

	/**
	 * Protected default constructor.
	 */
	protected ObjectComparator() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Compare two objects used as parameters or as a return value.
	 * @param value1 The first value to compare.
	 * @param value2 The second value to compare
	 * @return true, if the objects are equal, false otherwise.
	 */
	public static boolean compareObjects(Object value1, Object value2) {
		// Differentiate between wrapper types and actual objects.
		if (value1 instanceof ReferenceValue && value2 instanceof ReferenceValue)
			return compareReferenceValues((ReferenceValue) value1, (ReferenceValue) value2);
		return comparePrimitiveWrapperTypes(value1, value2);
	}

	/**
	 * Compare reference values.
	 * @param value1 The first ReferenceValue to compare.
	 * @param value2 The second ReferenceValue to compare
	 * @return true, if the objects are equal, false otherwise.
	 */
	public static boolean compareReferenceValues(ReferenceValue value1, ReferenceValue value2) {
		// Distinguish between array and object references.
		if (value1 instanceof Arrayref && value2 instanceof Arrayref) {
			// First compare te length.
			int length = ((Arrayref) value1).length;
			if (length != ((Arrayref) value2).length) return false;

			// Then compare all elements.
			for (int a = 0; a < length; a++) {
				// Recursive calls - if a single element is unequal, return false.
				if (!ObjectComparator.compareObjects(((Arrayref) value1).getElement(a), ((Arrayref) value2).getElement(a))) return false;
			}
			// Reaching this point means that the values are equal.
			return true;
		} else if (value1 instanceof Objectref && value2 instanceof Objectref) {
			return ((Objectref) value1).equals((Objectref) value2);
		}

		// Both values need to be of the same type, so comparison fails.
		return false;
	}

	/**
	 * Compare wrapper types by their primitive values.
	 * @param value1 The first value to compare.
	 * @param value2 The second value to compare
	 * @return true, if the primitive values are equal, false otherwise.
	 */
	public static boolean comparePrimitiveWrapperTypes(Object value1, Object value2) {
		if (value1 instanceof java.lang.Byte && value2 instanceof java.lang.Byte) {
			if (((Byte) value1).byteValue() != ((Byte) value2).byteValue()) return false;
		} else if (value1 instanceof java.lang.Boolean && value2 instanceof java.lang.Boolean) {
			if (((Boolean) value1).booleanValue() != ((Boolean) value2).booleanValue())
				return false;
		} else if (value1 instanceof java.lang.Character && value2 instanceof java.lang.Character) {
			if (((Character) value1).charValue() != ((Character) value2).charValue()) return false;
		} else if (value1 instanceof java.lang.Double && value2 instanceof java.lang.Double) {
			if (((Double) value1).doubleValue() != ((Double) value2).doubleValue()) return false;
		} else if (value1 instanceof java.lang.Float && value2 instanceof java.lang.Float) {
			if (((Float) value1).floatValue() != ((Float) value2).floatValue()) return false;
		} else if (value1 instanceof java.lang.Integer && value2 instanceof java.lang.Integer) {
			if (((Integer) value1).intValue() != ((Integer) value2).intValue()) return false;
		} else if (value1 instanceof java.lang.Long && value2 instanceof java.lang.Long) {
			if (((Long) value1).longValue() != ((Long) value2).longValue()) return false;
		} else {
			// Always return false if no wrapper type was supplied.
			return false;
		}
		// Reaching this point means that the values are equal.
		return true;
	}

}
