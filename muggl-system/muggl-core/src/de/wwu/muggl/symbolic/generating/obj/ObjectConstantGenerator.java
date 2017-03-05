package de.wwu.muggl.symbolic.generating.obj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.cfg.NotYetImplementedException;

import de.wwu.muggl.solvers.expressions.ObjectConstant;
import de.wwu.muggl.vm.var.ReferenceVariable;

public class ObjectConstantGenerator {

	/**
	 * A cache to remember all generated object constants.
	 */
	protected Map<String, ObjectConstant> objectCache;
	
	public ObjectConstantGenerator() {
		this.objectCache = new HashMap<>();
	}
	
	public ObjectConstant generateNewObjectConstant(ReferenceVariable referenceVariable) throws ObjectConstantGenerationException {
		// get the object reference class type from the class loader
		String objectType = referenceVariable.getObjectType();
		Class<?> objectClass = null;
		try {
			objectClass = ClassLoader.getSystemClassLoader().loadClass(objectType);
		} catch(ClassNotFoundException cnfe) {
			throw new ObjectConstantGenerationException("Could not find class: " + objectType, cnfe); 
		}
		
		// get an initial object of that class
		Object object = getObjectInstance(objectClass);
		
		// create an object constant
		ObjectConstant objectConstant = new ObjectConstant(object);
		
		// add it to the cache
		objectCache.put(referenceVariable.getObjectId(), objectConstant);
		
		// add now all required fields for the given object to generate...
		fillObjectFieldsWithConcreteValues(referenceVariable);
		
		return objectCache.get(referenceVariable.getObjectId());
	}
	
	protected void fillObjectFieldsWithConcreteValues(ReferenceVariable referenceVariable) {
		
	}

	protected Object getObjectInstance(Class<?> objectClass) throws ObjectConstantGenerationException {
		if(Modifier.isAbstract(objectClass.getModifiers())) {
			// get an sub-type of the given class
			throw new NotYetImplementedException("Generation of objects from abstract classes not implemented yet");
		}
		
		if(Modifier.isInterface(objectClass.getModifiers())) {
			// get an implementation class of this interface
			throw new NotYetImplementedException("Generation of objects from interfaces not implemented yet");
		}
		
		// if it is not an abstract class nor an interface, we can get an instance of the given class
		// first, check if there is an empty constructor available
		try {
			Constructor<?> constructor = objectClass.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException e) {
			// ok, there is no empty constructor, we have to get a constructor with parameters...
			throw new NotYetImplementedException("Generation of objects from class with an _non_empty_ constructor not implemented yet");
		} catch (Exception e) {
			throw new ObjectConstantGenerationException("Could not generate an object of type: " + objectClass, e);
		}
	}
}
