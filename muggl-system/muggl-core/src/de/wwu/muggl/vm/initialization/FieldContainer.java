package de.wwu.muggl.vm.initialization;

import java.util.Hashtable;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;

/**
 * This abstract class is a container for fields; it offers a Hashtable that stores
 * instances of Field as well as their corresponding values. If also offers the
 * functionality to get values from fields and to put values to fields.<br />
 * <br />
 * It is extends by the concrete classes InitializedClass and Objectref; the first
 * offers access to static fields (and is responsible for the instructions getstatic
 * and putstatic), while the second one offers access to instance fields (and is
 * responsible for the instructions getfield and putfield).<br />
 * <br />
 * For getting fields, lazy initialization is implemented. When statically initializing
 * a class only those static fields are initialized that are explicitly put in the
 * static initializer (the &lt;clinit&gt; method). The same applies for instance
 * initialization: Only those fields are initialized that are explicitly put in the
 * instance initializer (constructor, the chosen &lt;init%gt; method). As a consequence,
 * fields that have not been initialized yet might be accessed during the execution
 * of a program. In that case, the accessed value will simply be initialized to its
 * default value. This might lead to some overhead at runtime, it still is overall
 * faster and less memory-consuming than statically initializing all fields to
 * their default values.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
public abstract class FieldContainer {
	/**
	 * The assignment of fields to value i.e. object references.
	 */
	protected Hashtable<Field, Object> fields;

	/**
	 * Just initialize the Hashtable.
	 */
	public FieldContainer() {
		this.fields = new Hashtable<Field, Object>();
	}

	/**
	 * Get a value from a Field.
	 * @param field The Field to get the value from.
	 * @return The value assigned to the Field.
	 * @throws FieldAccessError If the Field does not belong to the Class represented by this InitializedClass.
	 */
	public Object getField(Field field) {
		// TODOFirst step: does this field really belong to this ClassFile?
		//if (!field.getClassFile().equals(getRepresentedClassFile())) throw new FieldAccessError("The field supplied does not belong to the class choosen.");

		// Second step: has it been initialized already?
		if (!this.fields.containsKey(field)) {
			// Initialize the field to its default value - choose it according to its type.
			Object value = StaticInitializationSupport.getInitializedPrimitiveTypeWrapper(field.getType(), false);

			// if object for field is null, but symbolic mode is enabled, add a null-reference object
			if(value == null && Options.getInst().symbolicMode) {
				
				ClassFile cf = field.getClassFile();
				String type = field.getType();
				String cn = cf.getClassName();
				String caca = cf.getCanonicalName();
//				InitializedClass initializedClass = classFile.getInitializedClass();
//				if (initializedClass == null) {
//					initializedClass = new InitializedClass(classFile, this);
//				}
				
				
//				System.out.println("Field: " + field);
//				if(type.endsWith("[]")) {
//					// array reference
//					value = new FieldArrayNullReference(field, cf.getInitializedClass(), 0);
//				} else {
//					// object reference
//					value = new FieldObjectNullReference(field, cf.getInitializedClass(), false);
//				}
				
			}

			// Set it for future use, if it is not null.
			if (value != null) this.fields.put(field, value);
		}

		// Third step: return the value;
		return this.fields.get(field);
	}

	/**
	 * Assign a value to a Field.
	 * @param field The Field to get a new value assigned.
	 * @param value The new value.
	 * @throws FieldAccessError If the Field does not belong to the Class represented by this InitializedClass.
	 */
	public void putField(Field field, Object value) {
		// TODO First step: does this field really belong to this ClassFile?
		//if (!field.getClassFile().equals(getRepresentedClassFile())) throw new FieldAccessError("The field supplied does not belong to the class choosen.");

		// Second step: Put it! This will automatically overwrite old values.
		if (value != null) this.fields.put(field, value);
	}
	
	/**
	 * Check whether there is a value stored for the specified field.
	 *
	 * @param field The field to check.
	 * @return true, if there is a value for the field, false otherwise.
	 */
	public boolean hasValueFor(Field field) {
		return this.fields.containsKey(field);
	}

}
