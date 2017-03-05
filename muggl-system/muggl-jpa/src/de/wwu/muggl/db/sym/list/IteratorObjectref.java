package de.wwu.muggl.db.sym.list;

import java.util.Iterator;

import de.wwu.muggl.db.entry.DatabaseObject;

public class IteratorObjectref<T extends DatabaseObject> implements Iterator<DatabaseObject> {
	
	protected CollectionVariable<DatabaseObject> collection;
	protected int counter;
	
	public IteratorObjectref(CollectionVariable<DatabaseObject> collection) {
		this.collection = collection;
		this.counter = 0;
	}
	
	public CollectionVariable<DatabaseObject> getCollectionVariable() {
		return this.collection;
	}
	
	public DatabaseObject getCollectionParent() {
		return this.collection.getParent();
	}
	
	public SymbolicHasNext symbolicHasNext() {
		return new SymbolicHasNext(this);
	}

	public boolean hasNext() {
		Iterator<DatabaseObject> iterator = collection.getRealCollection().iterator();
		for(int i=0; i<this.counter; i++) {
			iterator.next();
		}
		return iterator.hasNext();
	}

	public DatabaseObject next() {
		Iterator<DatabaseObject> iterator = collection.getRealCollection().iterator();
		for(int i=0; i<this.counter; i++) {
			iterator.next();
		}
		this.counter++;
		return iterator.next();
	}

	@Override
	public void remove() {
		Iterator<DatabaseObject> iterator = collection.getRealCollection().iterator();
		for(int i=0; i<this.counter; i++) {
			iterator.next();
		}
		iterator.remove();
	}

	public int getCounter() {
		return this.counter;
	}

}
