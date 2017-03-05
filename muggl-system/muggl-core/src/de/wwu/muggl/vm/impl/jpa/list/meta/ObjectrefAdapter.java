package de.wwu.muggl.vm.impl.jpa.list.meta;

import java.util.Hashtable;

import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.threading.Monitor;

public class ObjectrefAdapter extends Objectref {
	
	protected Objectref original;
	
	public ObjectrefAdapter(Objectref original) {
		super(original.getInitializedClass(), false);
		this.original = original;
	}
	
	public Objectref getOriginal() {
		return original;
	}

	@Override
	public Object getField(Field field) {
		return original.getField(field);
	}
	
	@Override
	public Hashtable<Field, Object> getFields() {
		return original.getFields();
	}
	
	@Override
	public InitializedClass getInitializedClass() {
		return original.getInitializedClass();
	}
	
	@Override
	public long getInstantiationNumber() {
		return original.getInstantiationNumber();
	}
	
	@Override
	public Monitor getMonitor() {
		return original.getMonitor();
	}
	
	@Override
	public String getName() {
		return original.getName();
	}
	
	@Override
	public int hashCode() {
		return original.hashCode();
	}
	
	@Override
	public boolean hasValueFor(Field field) {
		return original.hasValueFor(field);
	}
	
	@Override
	public boolean isArray() {
		return original.isArray();
	}
	
	@Override
	public boolean isPrimitive() {
		return original.isPrimitive();
	}
	
	@Override
	public void putField(Field field, Object value) {
		original.putField(field, value);
	}
	
	@Override
	public void setMonitor(Monitor monitor) {
		original.setMonitor(monitor);
	}
	
	@Override
	public String toString() {
		return original.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return original.equals(obj);
	}
	
	@Override
	public boolean equals(Objectref objectref) {
		return original.equals(objectref);
	}
	
}
