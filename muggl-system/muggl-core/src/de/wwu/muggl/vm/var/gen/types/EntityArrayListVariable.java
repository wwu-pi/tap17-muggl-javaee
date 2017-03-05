//package de.wwu.muggl.vm.var.gen.types;
//
//import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.var.EntityObjectref;
//import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
//
//public class EntityArrayListVariable extends ReferenceArrayListVariable {
//
//	protected EntityObjectref owner;
//	
//	protected Field field;
//	
//	/**
//	 * 
//	 * @param owner the entity object-reference owning this list
//	 * @param field the field in the entity object-reference for this list
//	 * @param name the name of this variable
//	 * @param vm the virtual machine initializing this variable
//	 * @throws SymbolicObjectGenerationException
//	 */
//	public EntityArrayListVariable(EntityObjectref owner, Field field, String name, JPAVirtualMachine vm) throws SymbolicObjectGenerationException {
//		super(name, vm);
//		this.owner = owner;
//		this.field = field;
//	}
//	
//	public EntityObjectref getEntityOwner() {
//		return this.owner;
//	}
//	
//	public Field getListField() {
//		return this.field;
//	}
//
//}
