package de.wwu.muggl.db.list;

import java.util.List;
import java.util.Set;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.solvers.expressions.NumericVariable;

@Deprecated
public interface SymbolicQueryResultList {

	public NumericVariable getSymbolicLength();
	
	public List<DatabaseObject> getSymbolicElements();
	
	public String getResultEntityName();
	
	public void addElement(DatabaseObject dbObjectToAdd);
	
	public Set<MugglJPAQueryRestriction> getQueryRestrictions();
}
