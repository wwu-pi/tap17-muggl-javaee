//package de.wwu.muggl.vm.impl.jpa.list;
//
//import de.wwu.muggl.solvers.expressions.list.SymbolicIterator;
//import de.wwu.muggl.solvers.expressions.list.SymbolicList;
//import de.wwu.muggl.solvers.expressions.list.SymbolicListElement;
//import de.wwu.muggl.vm.classfile.ClassFile;
//import de.wwu.muggl.vm.classfile.ClassFileException;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class SymbolicIteratorImpl extends SymbolicIterator {
//
//	public SymbolicIteratorImpl(SymbolicList list) {
//		super(list);
//	}
//
//	@Override
//	protected SymbolicListElement generateListElement() {
//		try {
//			// TODO: momentan wird immer ein Object.class als type verwendet...
//			String type = ((SymbolicListObjectRef)list).getCollectionType();
//			ClassFile objectClassFile = ((SymbolicListObjectRef)list).getVM().getClassLoader().getClassAsClassFile(type);
//			Objectref objectRef = ((SymbolicListObjectRef)list).getVM().getAnObjectref(objectClassFile);
//			SymbolicListElement element = new SymbolicListElementObjectRef(objectRef, type);
//			return element;
//		} catch(ClassFileException e) {
//			e.printStackTrace();
//			System.out.println("Error while generating new symbolic list element");
//			throw new RuntimeException("Could not generate a new list element", e);
//		}
//	}
//}
