package de.wwu.muggl.db.sym.list;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;

public class CollectionVariable<T extends DatabaseObject> implements Variable, Collection<DatabaseObject> {

	protected String name;
	protected Collection<DatabaseObject> collection;
	protected DatabaseObject parent;
	protected String parentFieldName;
	protected String collectionTypeName;
	
	protected SymbolicSize symbolicSize;
	
	public CollectionVariable(DatabaseObject parent, String name, String parentFieldName, String collectionTypeName) {
		this.name = name;
		this.collection = new ArrayList<DatabaseObject>();
		this.parent = parent;
		this.parentFieldName = parentFieldName;
		this.collectionTypeName = collectionTypeName;
	}
	
	@Override
	public String toString() {
		String me = "[";
		for(DatabaseObject ele : collection) {
			me+=ele.getObjectId() + ", ";
		}
		if(me.length() > 1) {
			me = me.substring(0, me.length()-2);
		}
		return me + "]";
	}
	
	
	void setSymbolicSize(SymbolicSize symbolicSize) {
		this.symbolicSize = symbolicSize;
	}
	
	public SymbolicSize getSymbolicSize() {
		return this.symbolicSize;
	}

//	@Override
//	public Variable getClone() {
//		CollectionVariable cv = new CollectionVariable(parent, name);
//		for(DatabaseObject ele : this.collection) {
//			DatabaseObject newEle = ele.getClone();
//			cv.collection.add(newEle);
//		}
//		return cv;
//	}
	
	
	public DatabaseObject getParent() {
		return this.parent;
	}
	
	public void setParent(DatabaseObject parent) {
		this.parent = parent;
	}
	
	public int size() {
		return collection.size();
	}

	public boolean isEmpty() {
		return collection.isEmpty();
	}

	public boolean contains(Object o) {
		return collection.contains(o);
	}

	public IteratorObjectref<DatabaseObject> iterator() {
		return new IteratorObjectref(this);
	}

	public Object[] toArray() {
		return collection.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return collection.toArray(a);
	}

	public boolean add(DatabaseObject e) {
		return collection.add(e);
	}

	public boolean remove(Object o) {
		return collection.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return collection.containsAll(c);
	}

	public boolean addAll(Collection<? extends DatabaseObject> c) {
		return collection.addAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return collection.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return collection.retainAll(c);
	}

	public void clear() {
		collection.clear();
	}
	
	@Override
	public CollectionVariable<DatabaseObject> clone() {
		CollectionVariable<DatabaseObject> c = new CollectionVariable<DatabaseObject>(this.parent, this.name, this.parentFieldName, this.collectionTypeName);
		c.collection = (Collection<DatabaseObject>)((ArrayList<?>)this.collection).clone();
		return c;
	}

	public Collection<DatabaseObject> getRealCollection() {
		return this.collection;
	}

	@Override
	public void checkTypes() throws TypeCheckException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Expression insert(Solution solution, boolean produceNumericSolution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression insertAssignment(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toTexString(boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toHaskellString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInternalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeToLog(PrintStream logStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInternalVariable() {
		// TODO Auto-generated method stub
		return false;
	}


	public String getParentFieldName() {
		return this.parentFieldName;
	}


	public String getCollectionType() {
		return this.collectionTypeName;
	}

}
