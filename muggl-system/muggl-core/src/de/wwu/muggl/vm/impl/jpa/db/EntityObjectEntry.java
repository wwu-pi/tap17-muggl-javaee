//package de.wwu.muggl.vm.impl.jpa.db;
//
//import java.util.Hashtable;
//
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.initialization.InitializedClass;
//import de.wwu.muggl.vm.initialization.Objectref;
//import de.wwu.muggl.vm.threading.Monitor;
//
//public class EntityObjectEntry extends Objectref {
//
//	public boolean preExecutionRequired;
//	public Objectref parent;
//
//	public EntityObjectEntry(Objectref parent) {
//		super(parent.getInitializedClass(), false);
//		this.parent = parent;
//	}
//	
//	public boolean isPreExecutionRequired() {
//		return this.preExecutionRequired;
//	}
//	
//	public void setPreExecutionRequired(boolean preExecutionRequired) {
//		this.preExecutionRequired = preExecutionRequired;
//	}
//
//	@Override
//	public Object getField(Field field) {
//		return parent.getField(field);
//	}
//	
//	@Override
//	public Hashtable<Field, Object> getFields() {
//		return parent.getFields();
//	}
//	
//	@Override
//	public InitializedClass getInitializedClass() {
//		return parent.getInitializedClass();
//	}
//	
//	@Override
//	public long getInstantiationNumber() {
//		return parent.getInstantiationNumber();
//	}
//	
//	@Override
//	public Monitor getMonitor() {
//		return parent.getMonitor();
//	}
//	
//	@Override
//	public String getName() {
//		return parent.getName();
//	}
//	
//	@Override
//	public int hashCode() {
//		return parent.hashCode();
//	}
//	
//	@Override
//	public boolean hasValueFor(Field field) {
//		return parent.hasValueFor(field);
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		return parent.equals(obj);
//	}
//	
//	@Override
//	public boolean equals(Objectref objectref) {
//		return parent.equals(objectref);
//	}
//	
//	@Override
//	public boolean isPrimitive() {
//		return parent.isPrimitive();
//	}
//	
//	@Override
//	public String toString() {
//		return parent.toString();
//	}
//	
//	@Override
//	public void setMonitor(Monitor monitor) {
//		parent.setMonitor(monitor);
//	}
//	
//	@Override
//	public void putField(Field field, Object value) {
//		parent.putField(field, value);
//	}
//	
//	@Override
//	public boolean isArray() {
//		return parent.isArray();
//	}
//
//	
//	
//}
