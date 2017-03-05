package de.wwu.muggl.db.sym.list;

public class SymbolicHasNext {

	protected IteratorObjectref<?> iterator;
	
	public SymbolicHasNext(IteratorObjectref<?> iteratorObjectref) {
		this.iterator = iteratorObjectref;
	}
	
	public IteratorObjectref<?> getIterator() { 
		return iterator;
	}
}
