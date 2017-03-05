package de.wwu.muggl.vm.initialization;

/**
 * This ModifieableArrayref inherits the complete functionality of an arrayref and does
 * only modify the put(int index, Object element)-Method. It however provides the method reset(int length) which
 * will delete any elements from the Arrayref and alter the number of elements it
 * can store. It also offers the method disableTypeChecking() which disables any form of
 * type checking.<br />
 * <br />
 * WARNING: This is no intended jvm behavior. Use with caution.<br />
 * <br />
 * The main usage for this class is the symbolic execution. It is handy to be able to
 * reset an array's length there. Furthermore, storing variables without the need of
 * using reference values is very helpful.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class ModifieableArrayref extends Arrayref {
	private boolean typeCheckingDisabled;
	private String representedType;
	private boolean representedTypeIsAPrimitiveWrapper;

	/**
	 * Initialize the arrayref. It must be of a have a type of ReferenceValue and
	 * a fixed length.
	 * @param referenceValue The desired ReferenceValue.
	 * @param length The desired length.
	 */
	public ModifieableArrayref(ReferenceValue referenceValue, int length) {
		super(referenceValue, length);
		this.representedType = null;
		this.representedTypeIsAPrimitiveWrapper = false;
	}

	/**
	 * Recursively generate a multidimensional array reference.
	 * @param referenceValue The reference value for the array.
	 * @param dimensionCount The length of the arrays' dimensions.
	 */
	public ModifieableArrayref(ReferenceValue referenceValue, int[] dimensionCount) {
		super(referenceValue, dimensionCount);
		this.typeCheckingDisabled = false;
		this.representedTypeIsAPrimitiveWrapper = false;
	}

	/**
	 * Delete any elements from this Arrayref and reset its length.
	 * @param length The new length for this Arrayref.
	 */
	public void reset(int length) {
		this.length = length;
		if (this.referenceValue.isPrimitive()) {
			// Primitive types will be represented in the java.lang wrapper classes.
			this.elements = new Object[this.length];
		} else {
			this.elements = new ReferenceValue[this.length];
		}
		this.typeCheckingDisabled = false;
	}

	/**
	 * Replace the internal storage array with an array of Objects. the
	 * put(int index, Object element)-Method  will then take any value and store
	 * it without typechecking. Please note that this method has to be reinvoked
	 * if the arrayref has been reseted.
	 */
	public void disableTypeChecking() {
		this.typeCheckingDisabled = true;
		this.elements = new Object[this.length];
	}

	/**
	 * Put an element to this arrayref. No type checking will be done if it has been disabled.
	 * @param index The index to put the element to.
	 * @param element The element to be put.
	 * @throws ArrayStoreException If the element to store is not assignment compatible with the arrayref.
	 * @throws IndexOutOfBoundsException If the index is out of the array reference's bounds.
	 */
	@Override
	public void putElement(int index, Object element) {
		if (this.typeCheckingDisabled) {
			// Proceed without type checking.
			if (index < 0 || index >= this.length) throw new IndexOutOfBoundsException("Array index out of bounds");
			this.elements[index] = element;
		} else {
			// Call the super implementation.
			super.putElement(index, element);
		}
	}

	/**
	 * Clone this array reference. This will generate a new Arrayref and copy the fields
	 * into it. If the ReferenceValue of this Arrayref is an Arrayref itself i.e. this
	 * arrayref contains nested array references, these will be cloned recursively.<br />
	 * <br />
	 * Please note than Cloning should only be done with care. Cloning of large objects might
	 * be slow and memory consuming. If elements are not primitive but reference values,
	 * they need to be cloned manually or changing them in the cloned Arrayref will have them
	 * changed in the original Arrayref (as it is one object with two references on it then).
	 * 
	 * @return The cloned array reference.
	 */
	@Override
	public Arrayref clone() {
		ModifieableArrayref arrayref = new ModifieableArrayref(this.referenceValue, this.length);
		if (this.typeCheckingDisabled) arrayref.disableTypeChecking();
		arrayref.representedType = this.representedType;
		for (int a = 0; a < this.length; a++) {
			Object element = this.elements[a];
			if (this.referenceValue.isArray())
				element = ((Arrayref) element).clone();
			arrayref.putElement(a, element);
		}
		return arrayref;
	}

	/**
	 * Get the fully qualified name of the type that is represented by the array reference.
	 *
	 * @return The fully qualified name of the type; or null, if none has been set.
	 */
	public String getRepresentedType() {
		return this.representedType;
	}

	/**
	 * Set the fully qualified name of the type that is represented by the array reference. This is
	 * used to bypass type checking in supporting implementations that require type checking. If the
	 * implementations get an ModifieableArrayref they will check against the represented type and
	 * not against the actual type.
	 *
	 * An example for a fully qualified name: java.lang.Integer
	 *
	 * @param type The fully qualified name of the type.
	 */
	public void setRepresentedType(String type) {
		this.representedType = type;
	}

	/**
	 * Set the fully qualified name of the type that is represented by the array reference and marks
	 * that it is used to wrap a primitive type. This is used to bypass type checking in supporting
	 * implementations that require type checking. If the implementations get an ModifieableArrayref
	 * they will check against the represented type and not against the actual type.
	 *
	 * An example for a fully qualified name: java.lang.Integer
	 *
	 * @param type The fully qualified name of the type.
	 */
	public void setRepresentedTypeAsAPrimitiveWrapper(String type) {
		this.representedType = type;
		this.representedTypeIsAPrimitiveWrapper = true;
	}

	/**
	 * Get the information whether the represented type is a wrapper for a primitive type.
	 *
	 * @return true, he represented type is a wrapper for a primitive type; false otherwise.
	 */
	public boolean isRepresentedTypeIsAPrimitiveWrapper() {
		return this.representedTypeIsAPrimitiveWrapper;
	}

}
