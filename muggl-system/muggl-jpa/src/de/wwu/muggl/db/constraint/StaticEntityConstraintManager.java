package de.wwu.muggl.db.constraint;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import de.wwu.muggl.db.VirtualObjectDatabase;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.type.IObjectreference;

public class StaticEntityConstraintManager {

	public static void addStaticEntityConstraints(Metamodel metamodel, SolverManager solver, VirtualObjectDatabase vodb) {
		addIdFieldConstraints(metamodel, solver, vodb);
	}
	
	protected static void addIdFieldConstraints(Metamodel metamodel, SolverManager solver, VirtualObjectDatabase vodb) {
		Map<String, Set<DatabaseObject>> reqData = vodb.getPreExecutionRequiredData();
		for(String entityName : reqData.keySet()) {
			String idName = EntityConstraintAnalyzer.getIdFieldName(entityName);
			Set<Expression> uniqueNumericObjects = new HashSet<>();
			for(DatabaseObject dbObj : reqData.get(entityName)) {
				Object o = dbObj.valueMap().get(idName);
				if(o != null && o instanceof NumericVariable) {
					uniqueNumericObjects.add((NumericVariable)o);
//					solver.addConstraint(GreaterOrEqual.newInstance((NumericVariable)o, NumericConstant.getInstance(0, Expression.INT)));
				}
				else if(o != null && o instanceof IObjectreference && ((IObjectreference)o).getObjectType().equals("java.lang.String")) {
					Object value = ((IObjectreference)o).valueMap().get("value");
					if(value instanceof ISymbolicArrayref) {
						solver.addConstraint(GreaterThan.newInstance(((ISymbolicArrayref)value).getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
					}
				}
			}
			if(uniqueNumericObjects.size() > 1) {
				solver.addConstraint(new AllDifferent(uniqueNumericObjects));
			}
		}
	}

}
