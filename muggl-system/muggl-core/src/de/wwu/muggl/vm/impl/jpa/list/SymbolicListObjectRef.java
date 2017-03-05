//package de.wwu.muggl.vm.impl.jpa.list;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.list.SymbolicIterator;
//import de.wwu.muggl.solvers.expressions.list.SymbolicList;
//import de.wwu.muggl.solvers.expressions.list.SymbolicListElement;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.jpa.list.meta.ObjectrefAdapter;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class SymbolicListObjectRef extends ObjectrefAdapter implements SymbolicList {
//
//	protected String name;
//	
//	protected String collectionType;
//	protected Set<SymbolicListElement> elements;
//	protected NumericVariable length;
//	protected SymbolicIterator iterator;
//	
//	protected JPAVirtualMachine vm;
//		
//	public SymbolicListObjectRef(Objectref arrayListRef, String name, String collectionType, JPAVirtualMachine vm) {
//		super(arrayListRef);
//	
//		this.collectionType = collectionType;
//		this.vm = vm;
//		this.name = name;
//		this.length = new NumericVariable(name+".length", Expression.INT);
//		this.elements = new HashSet<SymbolicListElement>();
//		this.iterator = new SymbolicIteratorImpl(this);
//	}
//	
//	public String getCollectionType() {
//		return this.collectionType;
//	}
//	
//	public SymbolicIterator iterator() {
//		return this.iterator;
//	}
//	
//	public void addElement(SymbolicListElement element) {
//		this.elements.add(element);
//	}
//	
//	public SymbolicListElement[] elements() {
//		return this.elements.toArray(new SymbolicListElement[elements.size()]);
//	}
//	
//	public NumericVariable length() {
//		return this.length;
//	}
//
//	public JPAVirtualMachine getVM() {
//		return this.vm;
//	}
//}
