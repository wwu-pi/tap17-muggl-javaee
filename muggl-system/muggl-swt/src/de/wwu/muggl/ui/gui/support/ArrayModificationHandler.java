package de.wwu.muggl.ui.gui.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.ui.gui.GUIException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * The ArrayModificationHandler is used to enable the user to edit arrays via the GUI. Not only the values
 * of entries of multidimensional arrays can be changed. This class can expand arbitrary dimensions of
 * arrays as well as delete single entries or whole dimensions.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class ArrayModificationHandler {
	// Array related fields.
	private Object[] array;
	private boolean hasAReturnValue;
	private String typeString;
	private boolean isPrimitive;
	private int[] dimensionsLength;

	/**
	 * Initialize the ArrayModificationHandler.
	 * @param array The array this handler will work on. Can be null to indicate a new array is being worked on.
	 * @param typeString A String characterizing the type stored.
	 * @param isPrimitive Indicates whether the array is a wrapper type for a primitive array.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not be supported by the GUI anyway.
	 */
	public ArrayModificationHandler(Object[] array, String typeString, boolean isPrimitive) throws GUIException {
		this.array = array;
		this.typeString = typeString;
		this.isPrimitive = isPrimitive;

		// Initialize the array if needed.
		if (this.array == null) {
			this.array = getNewArray();
		} else {
			// Build up the dimensions length.
			ArrayList<Integer> dimensionsArrayList = new ArrayList<Integer>();
			Object currentDimension = this.array;
			while (currentDimension.getClass().isArray()) {
				int length = ((Object[]) currentDimension).length;
				dimensionsArrayList.add(Integer.valueOf(length));
				if (length == 0) break;
				currentDimension = ((Object[]) currentDimension)[0];
			}
			this.dimensionsLength = new int[dimensionsArrayList.size()];
			Iterator<Integer> iterator = dimensionsArrayList.iterator();
			int a = 0;
			while (iterator.hasNext()) {
				this.dimensionsLength[a] = iterator.next();
				a++;
			}
		}

		// At the beginning, we assume that there is a return value.
		this.hasAReturnValue = true;
	}

	/**
	 * Get a part of a larger multidimensional array.
	 *
	 * @param dimensions An array that describes the "way" through the multidimensional array to reach the desired entry.
	 * @return The partial array.
	 * @throws ArrayIndexOutOfBoundsException If the array has less dimensions than found in the dimensions array or if a entry out of a dimension's bounds is requested.
	 */
	public Object[] getPartialArray(int[] dimensions) {
		if (dimensions != null && dimensions.length > 0) {
			Object[] returnedArray = this.array;
			for (int a = 0; a < dimensions.length; a++) {
				returnedArray = (Object[]) returnedArray[dimensions[a]];
			}
			return returnedArray;
		}
		return this.array;
	}

	/**
	 * Get an entry from an array.
	 *
	 * @param dimensions An array that describes the "way" through the multidimensional array to reach the desired entry's dimension.
	 * @param entryIndex The index of the desired entry.
	 * @return The entry of the array.
	 * @throws ArrayIndexOutOfBoundsException If the array has less dimensions than found in the dimensions array or if a entry out of a dimension's bounds is requested.
	 */
	public Object getArrayEntry(int[] dimensions, int entryIndex) {
		Object[] currentDimension = this.array;
		if (dimensions != null && dimensions.length > 0) {
			for (int a = 0; a < dimensions.length; a++) {
				currentDimension = (Object[]) currentDimension[dimensions[a]];
			}
		}
		return currentDimension[entryIndex];
	}

	/**
	 * Set an entry of an array.
	 * @param dimensions An array that describes the "way" through the multidimensional array to reach the desired entry's dimension.
	 * @param entryIndex The index of the desired entry.
	 * @param entry The entry to be stored in the array.
	 * @throws ArrayIndexOutOfBoundsException If the array has less dimensions than found in the dimensions array or if a entry out of a dimension's bounds is requested.
	 * @throws ArrayStoreException If the type of the entry to store does not match the type of the array.
	 */
	public void setArrayEntry(int[] dimensions, int entryIndex, Object entry) {
		Object[] currentDimension = this.array;
		if (dimensions.length > 0) {
			for (int a = 0; a < dimensions.length; a++) {
				currentDimension = (Object[]) currentDimension[dimensions[a]];
			}
		}
		currentDimension[entryIndex] = entry;
	}

	/**
	 * Create a new Array.
	 * @return The new array.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not be supported by the GUI anyway.
	 */
	private Object[] getNewArray() throws GUIException {
		int dimensions = 1;
		// Determine the number of dimensions.
		String typeString = this.typeString;
		while (typeString.endsWith("[]")) {
			dimensions++;
			typeString = typeString.substring(0, typeString.length() - 2);
		}

		// Initialize all dimensions to zero length.
		if (this.dimensionsLength == null) {
			this.dimensionsLength = new int[dimensions];
			for (int a = 0; a < dimensions - 1; a++) {
				this.dimensionsLength[a] = 1;
			}
			this.dimensionsLength[dimensions - 1] = 0;
		}

		// Create the array.
		return (Object[]) Array.newInstance(getArrayObjectForReflectionApi().getClass(), this.dimensionsLength);
	}

	/**
	 * Provide an object the Reflection API can work with to generate a new array.
	 * @return An object to be used a a parameter for Array.newInstance
	 * @throws GUIException If a type is encountered that no array can be created for as it will not be supported by the GUI anyway.
	 */
	private Object getArrayObjectForReflectionApi() throws GUIException {
		String typeString = this.typeString;
		while (typeString.endsWith("[]")) {
			typeString = typeString.substring(0, typeString.length() - 2);
		}

		if (typeString.equals("char") || typeString.equals("java.lang.Character")) {
			if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
			return Character.valueOf((char) 0);
		} else if (typeString.equals("boolean") || typeString.equals("java.lang.Boolean")) {
			if (Options.getInst().symbolicMode) return BooleanConstant.getInstance(false);
			return Boolean.FALSE;
		} else if (typeString.equals("byte") || typeString.equals("java.lang.Byte")) {
			if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
			return Byte.valueOf((byte) 0);
		} else if (typeString.equals("double") || typeString.equals("java.lang.Double")) {
			if (Options.getInst().symbolicMode) return DoubleConstant.getInstance(0D);
			return Double.valueOf(0D);
		} else if (typeString.equals("int") || typeString.equals("java.lang.Integer")) {
			if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
			return Integer.valueOf(0);
		} else if (typeString.equals("float") || typeString.equals("java.lang.Float")) {
			if (Options.getInst().symbolicMode) return FloatConstant.getInstance(0F);
			return Float.valueOf(0F);
		} else if (typeString.equals("long") || typeString.equals("java.lang.Long")) {
			if (Options.getInst().symbolicMode) return LongConstant.getInstance(0L);
			return Long.valueOf(0L);
		} else if (typeString.equals("short") || typeString.equals("java.lang.Short")) {
			if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
			return Short.valueOf((short) 0);
		} else if (typeString.equals("java.lang.String")) {
			return new String();
		} else {
			throw new GUIException("Type cannot be processed.");
		}
	}

	/**
	 * Expand one dimension of the array without inserting a value. This is meant to be used to to
	 * expand any dimension but the last of a multidimensional array. An exception will be If
	 * you try to expand the last dimension. Doing this always should be done while supplying a value.
	 * Use expandArrayInsert() for thatit
	 * The sub array(s) of the new dimension will be filled with null values, while existing values
	 * are shifted according to the dimensions array.
	 *
	 * @param dimension The dimension to expand.
	 * @param entryIndex The dimension to insert the new branch in.
	 * @throws ArrayIndexOutOfBoundsException If the array has less dimensions than found in the dimensions array or if a entry out of a dimension's bounds is accessed.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not be supported by the GUI anyway. Also If it is tryed to expand the last dimension using this method.
	 */
	public void expandArray(int dimension, int entryIndex) throws GUIException {
		if (this.dimensionsLength.length - 1 == dimension)
			throw new GUIException("Cannot expand the last dimension using this method.");
		this.dimensionsLength[dimension]++;
		Object[] newArray = getNewArray();
		copyIntoExpand(this.array, newArray, 0, dimension, entryIndex);
		this.array = newArray;
	}

	/**
	 * Expand one dimension of the array without inserting a value. This is the recursive
	 * implementation of expandArray() that will make sure any contents from the old array are
	 * copyed to the corresponding positions in the new array.
	 * 
	 * @param source The source array. Should be this.array on the first call.
	 * @param target The target array. Should be an array generated by getNewArray() on the first
	 *        call.
	 * @param currentDimension The current dimension.
	 * @param dimension The dimension to insert the new branch in.
	 * @param entryIndex The index of the new branch in the array in the target dimension.
	 * @throws ArrayIndexOutOfBoundsException If a entry out of a dimension's bounds is accessed.
	 *         Cannot be If the parameters are valid.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not
	 *         be supported by the GUI anyway.
	 */
	private void copyIntoExpand(Object[] source, Object[] target, int currentDimension,
			int dimension, int entryIndex) throws GUIException {
	// Does the source have sub dimensions?
		if (source.length > 0 && source[0] != null && source[0].getClass().isArray()) {
			// Process this branch.
			for (int a = 0; a < target.length; a++) {
				// Special care has to be taken if we have reached the target dimension.
				if (currentDimension == dimension) {
					if (a == entryIndex) {
						// This is the target index. Fill up the array with zero values.
						target[a] = fillUp(target[a]);
					} else if (a < entryIndex) {
						// Copy any entries before the index to the corresponding places.
						copyIntoExpand((Object[]) source[a], (Object[]) target[a], currentDimension + 1, dimension, entryIndex);
					} else if (a > entryIndex) {
						// Copy any entries before the index to the places shifted to the right by one..
						copyIntoExpand((Object[]) source[a - 1], (Object[]) target[a], currentDimension + 1, dimension, entryIndex);
					}
				} else {
					// Just proceed with the next dimension.
					copyIntoExpand((Object[]) source[a], (Object[]) target[a], currentDimension + 1, dimension, entryIndex);
				}
			}
		} else {
			// We have reached the last dimension and can now copy the contents.
			System.arraycopy(source, 0, target, 0, target.length);
		}
	}

	/**
	 * Expand the last dimension of the array inserting a value.
	 * 
	 * If multidimensional arrays are processed, there is a shifing option. If set to true, there
	 * will be a zero value inserted at the corresponding position of non-target branches and the
	 * other values shifted to the right by one. If set to false, no shifting will be done and a
	 * zero value will be inserted as the last value.
	 * 
	 * Example: Imagine an Array with three nested arrays of three entries each, like int[3][3]. Now
	 * we insert into the second array at the second position. Hence, the second dimension now
	 * contains four elements. Inserting one element so effectively inserts three elements (two of
	 * the are null initially). There are two possible ways to do this: 1. Insert the null values at
	 * the corresponding positions. 2. Insert the null values as the last elements of the other
	 * arrays. In other words: 1. will have [0][1] and [2][1] be null with the other elements
	 * shifted to the right by one, while 2. will have null values inserted as [0][3] and [2][3].
	 * 
	 * @param dimensions An array that describes the "way" through the multidimensional array to
	 *        reach the branch of the array that the new value gets inserted into.
	 * @param entryIndex The index to insert the new value to.
	 * @param newEntry The object to insert.
	 * @param shift If set to true, shifing is enabled.
	 * @throws ArrayIndexOutOfBoundsException If the dimensions entry does not point to the last
	 *         dimension or or if a entry out of a dimension's bounds is accessed.
	 * @throws ArrayStoreException If the type of the entry to store does not match the type of the
	 *         array.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not
	 *         be supported by the GUI anyway.
	 */
	public void expandArrayInsert(int[] dimensions, int entryIndex, Object newEntry, boolean shift)
			throws GUIException {
		if (dimensions.length < this.dimensionsLength.length - 1)
		throw new ArrayIndexOutOfBoundsException("The dimensions array does not point to the last dimension.");
		this.dimensionsLength[this.dimensionsLength.length - 1]++;
		if (entryIndex < 0 || entryIndex > this.dimensionsLength[this.dimensionsLength.length - 1] - 1)
			throw new ArrayIndexOutOfBoundsException("The entry index is out of the array's bounds.");
		Object[] newArray = getNewArray();
		copyIntoExpandInsert(this.array, newArray, 0, dimensions, entryIndex, newEntry, shift);
		this.array = newArray;
	}

	/**
	 * Expand the last dimension of the array inserting a value. This is the recursive
	 * implementation of expandArrayInsert() that will make sure any contents from the old array are
	 * copied to the corresponding positions in the new array.
	 * 
	 * @param source The source array. Should be this.array on the first call.
	 * @param target The target array. Should be an array generated by getNewArray() on the first
	 *        call.
	 * @param currentDimension The current dimension.
	 * @param dimensions An array that describes the "way" through the multidimensional array to
	 *        reach the branch of the array that the new value gets inserted into.
	 * @param entryIndex The index to insert the new value to.
	 * @param newEntry The object to insert.
	 * @param shift If set to true, shifting is enabled.
	 * @throws ArrayIndexOutOfBoundsException If a entry out of a dimension's bounds is accessed.
	 *         Cannot be If the parameters are valid.
	 * @throws ArrayStoreException If the type of the entry to store does not match the type of the
	 *         array.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not
	 *         be supported by the GUI anyway.
	 */
	private void copyIntoExpandInsert(Object[] source, Object[] target, int currentDimension,
			int[] dimensions, int entryIndex, Object newEntry, boolean shift) throws GUIException {
		// Check if the last dimension has been reached.
		if (currentDimension < this.dimensionsLength.length - 1) {
			// Process this branch.
			for (int a = 0; a < source.length; a++) {
				// If dimensions is not null, signaling this is not the target branch, check if we are on the target path.
				if (dimensions != null && a == dimensions[currentDimension]) {
					copyIntoExpandInsert((Object[]) source[a], (Object[]) target[a], currentDimension + 1, dimensions, entryIndex, newEntry, shift);
				} else {
					// There is no need to keep either the dimensions array or the new entry object.
					copyIntoExpandInsert((Object[]) source[a], (Object[]) target[a], currentDimension + 1, null, entryIndex, null, shift);
				}
			}
		} else {
			// Check if this is the target branch.
			if (dimensions != null) {
				// Copy any elements previous to the entry's index to the corresponding positions.
    			if (entryIndex > 0) System.arraycopy(source, 0, target, 0, entryIndex);
    			// Shift any elements after the entry's index by one position to the right.
    			if (entryIndex < target.length - 1) System.arraycopy(source, entryIndex, target, entryIndex + 1, target.length - 1 - entryIndex);
    			// Finally set the new entry to the entry index' position.
    			target[entryIndex] = newEntry;
			} else {
				// Distinguish between shifing and non-shifting mode.
				if (shift) {
					// Copy any elements previous to the entry's index to the corresponding positions.
	    			if (entryIndex > 0) System.arraycopy(source, 0, target, 0, entryIndex);
	    			// Shift any elements after the entry's index by one position to the right.
	    			if (entryIndex < target.length - 1) System.arraycopy(source, entryIndex, target, entryIndex + 1, target.length - 1 - entryIndex);
	    			// Finally fill the last index with a zero value.
	    			target[entryIndex] = getZeroValueObject();
				} else {
					// Simply copy all elements to the corresponding positions.
					System.arraycopy(source, 0, target, 0, source.length);
					// Fill the last index with a zero value.
					target[target.length - 1] = getZeroValueObject();
				}
			}
		}
	}

	/**
	 * Delete an entry from an array that can be multi-dimensional at wish. This method is
	 * asymmetric to the expand methods. It works both for deleting whole branches as for deleting
	 * entries on the last dimension.
	 * 
	 * If multidimensional arrays are processed, there is a shifting option. If set to true, there
	 * corresponding branches or entries will be deleted from the dimension of that truncated depth.
	 * If set to false, the target branch or entry will be deleted, while the last elements of the
	 * non-target branches of the truncated depth are deleted.
	 * 
	 * Example: Imagine an Array with three nested arrays of three entries each, like int[3][3]. Now
	 * we delete [1][1]. Hence, the second dimension now contains two elements only. Deleting one
	 * element so effectively deletes three elements. There are two possible ways to do this: 1.
	 * Delete the corresponding elements. 2. Delete the last element of the other Arrays. In other
	 * words: 1. will have [0][1] and [2][1] deleted as well, while 2. will have [0][2] and [2][2]
	 * deleted.
	 * 
	 * @param dimension The dimension to truncate.
	 * @param dimensions An array that describes the "way" through the multidimensional array to
	 *        reach the branch of the array that the new value gets inserted into.
	 * @param entryIndex The index of the value or branch to delete.
	 * @param shift If set to true, shifting is enabled.
	 * @throws ArrayIndexOutOfBoundsException If a entry out of a dimension's bounds is accessed.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not
	 *         be supported by the GUI anyway.
	 */
	public void truncateArray(int dimension, int[] dimensions, int entryIndex, boolean shift)
			throws GUIException {
		if (dimension < this.dimensionsLength.length - 1 && this.dimensionsLength[dimension] == 1)
			throw new ArrayIndexOutOfBoundsException(
					"Cannot reduce the length of any dimension but the last one to zero.");
		if (entryIndex < 0 || entryIndex > this.dimensionsLength[this.dimensionsLength.length - 1] - 1)
			throw new ArrayIndexOutOfBoundsException("The entry index is out of the array's bounds.");
		this.dimensionsLength[dimension]--;
		Object[] newArray = getNewArray();
		copyIntoTruncate(this.array, newArray, 0, dimension, dimensions, entryIndex, shift);
		this.array = newArray;
	}

	/**
	 * Delete an entry from an array that can be multi-dimensional at wish. This is the recursive
	 * implementation of truncateArray() that will make sure any contents from the old array are
	 * copyed to the corresponding positions in the new array.
	 * 
	 * @param source The source array. Should be this.array on the first call.
	 * @param target The target array. Should be an array generated by getNewArray() on the first
	 *        call.
	 * @param currentDimension The current dimension.
	 * @param targetDimension The target dimension to delete a branch or entry from.
	 * @param dimensions An array that describes the "way" through the multidimensional array to
	 *        reach the branch of the array that the new value gets inserted into.
	 * @param entryIndex The index of the value or branch to delete.
	 * @param shift If set to true, shifting is enabled.
	 * @throws ArrayIndexOutOfBoundsException If a entry out of a dimension's bounds is accessed.
	 *         Cannot be If the parameters are valid.
	 */
	private void copyIntoTruncate(Object[] source, Object[] target, int currentDimension,
			int targetDimension, int[] dimensions, int entryIndex, boolean shift) {
		// Have we reached the target dimension?
		if (currentDimension == targetDimension) {
			// Now its time to delete a branch or entry. Shifting is done if either shifting is enabled or if we are on the target path.
			if (shift || dimensions != null) {
				// Copy any elements previous to the entry's index to the corresponding positions.
				if (entryIndex > 0) System.arraycopy(source, 0, target, 0, entryIndex);
				// Shift any elements after the entry's index by one position to the left.
    			if (entryIndex < target.length) System.arraycopy(source, entryIndex + 1, target, entryIndex, target.length - entryIndex);
			} else {
				// Simply skip the last entry from the source.
				System.arraycopy(source, 0, target, 0, target.length);
			}
		} else {
			/*
			 * The last dimension would always be the target dimension if it is not encountered prior to that. So we can
			 * assume to have an array containing arrays here and directly process it.
			 */
			for (int a = 0; a < source.length; a++) {
				// If dimensions is not null, signaling this is not the target branch, check if we are on the target path.
				if (dimensions != null && a == dimensions[currentDimension]) {
					copyIntoTruncate((Object[]) source[a], (Object[]) target[a], currentDimension + 1, targetDimension, dimensions, entryIndex, shift);
				} else {
					// There is no need to keep the dimensions array.
					copyIntoTruncate((Object[]) source[a], (Object[]) target[a], currentDimension + 1, targetDimension, null, entryIndex, shift);
				}
			}
		}
	}

	/**
	 * Fill up an object with zero values using the method getZeroValueObject(). If an array
	 * is supplied, it will be processed recursively.
	 * @param target The target object or array to process.
	 * @return An object of value zero, or an array of zero values of an array was specified.
	 * @throws GUIException If a type is encountered that no array can be created for as it will not be supported by the GUI anyway.
	 */
	private Object fillUp(Object target) throws GUIException {
		if (target != null && target.getClass().isArray()) {
			Object[] targetArray = (Object[]) target;
			for (int a = 0; a < targetArray.length; a++) {
				targetArray[a] = fillUp(targetArray[a]);
			}
			return targetArray;
		}
		return getZeroValueObject();
	}

	/**
	 * Provide an object representing zero for arrays of primitive wrapper types, or null for any
	 * reference types.
	 *
	 * @return The object which represents zero, or null.
	 * @throws GUIException If a primitive wrapper type is encountered that no array can be created for as it will not be supported by the GUI anyway.
	 */
	private Object getZeroValueObject() throws GUIException {
		if (this.isPrimitive) {
			String typeString = this.typeString;
			while (typeString.endsWith("[]")) {
				typeString = typeString.substring(0, typeString.length() - 2);
			}

			// Provide appropriate wrapper types for primitive values.
			if (typeString.equals("char") || typeString.equals("java.lang.Character")) {
				if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
				return Character.valueOf((char) 0);
			} else if (typeString.equals("boolean") || typeString.equals("java.lang.Boolean")) {
				if (Options.getInst().symbolicMode) return BooleanConstant.getInstance(false);
				return Boolean.FALSE;
			} else if (typeString.equals("byte") || typeString.equals("java.lang.Byte")) {
				if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
				return Byte.valueOf((byte) 0);
			} else if (typeString.equals("double") || typeString.equals("java.lang.Double")) {
				if (Options.getInst().symbolicMode) return DoubleConstant.getInstance(0D);
				return Double.valueOf(0D);
			} else if (typeString.equals("int") || typeString.equals("java.lang.Integer")) {
				if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
				return Integer.valueOf(0);
			} else if (typeString.equals("float") || typeString.equals("java.lang.Float")) {
				if (Options.getInst().symbolicMode) return FloatConstant.getInstance(0F);
				return Float.valueOf(0F);
			} else if (typeString.equals("long") || typeString.equals("java.lang.Long")) {
				if (Options.getInst().symbolicMode) return LongConstant.getInstance(0L);
				return Long.valueOf(0L);
			} else if (typeString.equals("short") || typeString.equals("java.lang.Short")) {
				if (Options.getInst().symbolicMode) return IntConstant.getInstance(0);
				return Short.valueOf((short) 0);
			} else {
				throw new GUIException("Type cannot be processed.");
			}
		}

		// Any reference values are initialized to null.
		return null;
	}

	/**
	 * Getter for the information, if this handler stores a return value.
	 * @return true, if this handler stores a return value, false, if it does not.
	 */
	public boolean hasAReturnValue() {
		return this.hasAReturnValue;
	}

	/**
	 * Make sure this handler will not provide a return value. This is meant to be used
	 * if any fatal errors on processing the array occurs or if returning the changed
	 * array is not desired.
	 */
	public void doNotReturnThisArray() {
		this.hasAReturnValue = false;
	}

	/**
	 * Get the processed array. Do NOT use this method before calling hasAReturnValue()
	 * to check whether it has a return value.
	 * @return The processed array.
	 * @throws NullPointerException If no return value can be provided.
	 */
	public Object getFinishedArray() {
		if (this.hasAReturnValue) return this.array;
		throw new NullPointerException();
	}

	/**
	 * Get the type String representing the type of the array processed by this handler.
	 * @return The type String representing the type of the array processed by this handler.
	 */
	public String getTypeString() {
		return this.typeString;
	}

	/**
	 * Getter for the information whether this handler processes a primitive type wrapping array.
	 * @return true, if this handler processes an array that is a wrapper for a primitive type, false otherwise.
	 */
	public boolean getIsPrimitive() {
		return this.isPrimitive;
	}

	/**
	 * Debug method that prints out the array so changes can be checked.
	 * @param object Should be this.array.
	 * @param ident Should be 0.
	 * @param number Should be 0.
	 */
	@SuppressWarnings("unused")
	private void printArray(Object object, int ident, int number) {
		if (ident == 0) System.out.print("\n");
		if (object != null && object.getClass().isArray()) {
			Object[] arrayObject = (Object[]) object;
			if (ident > 0) {
				String identString = "";
				for (int a = 1; a < ident; a++) {
					identString += "\t";
				}
				System.out.println(identString + number);
			}
			ident++;
			for (int a = 0; a < arrayObject.length; a++) {
				printArray(arrayObject[a], ident, a);
			}
		} else {
			String outString = "";
			for (int a = 1; a < ident; a++) {
				outString += "\t";
			}
			outString += number + ": ";
			if (object != null) {
				outString += object.toString();
			} else {
				outString += "(null)";
			}
			System.out.println(outString);
		}
	}
}
