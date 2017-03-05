package de.wwu.muggl.symbolic.jpa.var;

import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class SymbolicCriteriaBuilder implements JPASpecialType {

	public SymbolicCriteriaQuery createQuery(Objectref classRef) {
		SymbolicCriteriaQuery query = new SymbolicCriteriaQuery();
		String name = getName(classRef);
		query.setEntityClassName(name);
		return query;
	}

	
	protected String getName(Objectref classRef) {
		Field field = classRef.getInitializedClass().getClassFile().getFieldByName("name");  // getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
		
		Objectref stringValueObject = (Objectref)classRef.getField(field);
		
		Field nameField = stringValueObject.getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
		Object nameValue = stringValueObject.getField(nameField);
		
		String result = "";
		if(nameValue instanceof Arrayref) {
			Arrayref stringValueObjectRef = (Arrayref)nameValue;
			for(int c=0; c<stringValueObjectRef.length; c++) {
				Object o = stringValueObjectRef.getElement(c);
				if(o instanceof IntConstant) {
					int asciiStringValue = ((IntConstant)o).getValue();
					String asciiString = Character.toString((char)asciiStringValue);
					result += asciiString;
				}
			}
		}
		
		return result;
	}
}
