package de.wwu.muggl.db.sym.list;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;

public class SymbolicSize extends NumericVariable {

	protected CollectionVariable<DatabaseObject> collection;
	
	public SymbolicSize(CollectionVariable<DatabaseObject> collection, String name) {
		super(name, Expression.INT);
		this.collection = collection;
		this.collection.setSymbolicSize(this);
	}

	public CollectionVariable<DatabaseObject> getCollection() {
		return this.collection;
	}
}
