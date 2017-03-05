//package de.wwu.muggl.solvers.expressions.list;
//
//public abstract class SymbolicIterator {
//	
//	protected SymbolicHasNext hasNext;
//	protected SymbolicList list;
//	protected int currentPosition;
//
//	public SymbolicIterator(SymbolicList list) {
//		this.list = list;
//		this.hasNext = new SymbolicHasNext(this);
//	}
//	
//	public SymbolicHasNext hasNext() {
//		return this.hasNext;
//	}
//
//	public int currentPosition() {
//		return currentPosition;
//	}
//
//	public SymbolicList list() {
//		return list;
//	}
//	
//	protected abstract SymbolicListElement generateListElement();
//
//	public SymbolicListElement next() {
//		SymbolicListElement element = generateListElement();
//		this.list.addElement(element);
//		this.currentPosition++;
//		return element;
//	}
//	
////	private boolean canGetNext(Store store) {
////		store.
////        Search<IntVar> search = new DepthFirstSearch<IntVar>(); 
////        SelectChoicePoint<IntVar> select = new InputOrderSelect<IntVar>(
////        		store, allVars, new IndomainMin<IntVar>()); 
////        return search.labeling(store, select); 
////	}
//}
