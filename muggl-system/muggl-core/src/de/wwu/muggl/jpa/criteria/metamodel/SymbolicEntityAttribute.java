package de.wwu.muggl.jpa.criteria.metamodel;

import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;

public class SymbolicEntityAttribute {

	protected String entityName;
	protected ClassFile entityClassFile;
	protected Field field;
	protected JoinType joinType;
	
	public SymbolicEntityAttribute(String entityName, Field field, VirtualMachine vm) {
		this.entityName = entityName;
		this.field = field; // meta-model field
		try {
			this.entityClassFile = vm.getClassLoader().getClassAsClassFile(entityName);
		} catch (ClassFileException e) {
			e.printStackTrace();
		}
	}
	
	public ClassFile getEntityClassFile() {
		return this.entityClassFile;
	}

	public String getEntityName() {
		return this.entityName;
	}
	
	public Field getField() {
		return this.field;
	}

	public String getFieldName() {
		return this.field.getName();
	}
	
}
