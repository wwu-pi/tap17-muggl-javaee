package de.wwu.muggl.vm.impl.jpa.db.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.jpa.JPAFieldConstraints;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

@Deprecated
public class JPAStaticEntityConstraintManager {

	protected static JPAStaticEntityConstraintGenerator constraintGen = new JPAStaticEntityConstraintGenerator();
	
	@Deprecated
	public static void addConstraints(JPAVirtualMachine vm, VirtualDatabase database, SolverManager solverManager) {
		
		// key=entityName, value=Map[key=fieldName, value=uniqueExpression(e.g., constant or variable)]
		Map<String, Map<String, Set<Expression>>> uniqueEntityMap = new HashMap<>();
		
//		addRequiredDataAsConstraints(database, solverManager, uniqueEntityMap);
		
		Set<String> usedDatabaseObjects = new HashSet<>();
		
		for(DatabaseObject databaseObject : database.getData()) {
			usedDatabaseObjects.add(databaseObject.getObjectId());

			String entityClassName = databaseObject.getInitializedClassName();
			
			addConstraints(vm, solverManager, uniqueEntityMap, entityClassName, databaseObject.valueMap());
		}
		
		for(String objectId : database.getRequiredData().keySet()) {
//			if(usedDatabaseObjects.contains(objectId)) {
//				continue;
//			}
			EntityEntry entityEntry = database.getRequiredData().get(objectId);
			
			addConstraints(vm, solverManager, uniqueEntityMap, entityEntry.getEntityClassName(), entityEntry.valueMap());
		}
		
//			JPAStaticEntityConstraint constraints = constraintGen.generateEntityConstraints(entityClassName);
//			
//			Map<String, Set<Expression>> uniqueFields = uniqueEntityMap.get(entityClassName);
//			if(uniqueFields == null) {
//				uniqueFields = new HashMap<>();
//			}
//
//			for(String fieldName : databaseObject.valueMap().keySet()) {
//				JPAFieldConstraints fieldConstraint = constraints.getFieldConstraints().get(fieldName);
//				
//				Object fieldValue = databaseObject.valueMap().get(fieldName);
//				
//				if(fieldValue instanceof Term) {
//					Term term = (Term)fieldValue;
//					
//					handleMinConstraint(vm, solverManager, fieldConstraint,	term);
//					
//					addUniqueVariables(uniqueFields, fieldName, fieldConstraint, term);
//				}
//				
//				if(fieldValue instanceof IntConstant) {
//					addUniqueVariables(uniqueFields, fieldName, fieldConstraint, (IntConstant)fieldValue);
//				}
//			}
//			
//			uniqueEntityMap.put(entityClassName, uniqueFields);
		
		

		
		// set unique values
		for(String entityName : uniqueEntityMap.keySet()) {
			for(String fieldName : uniqueEntityMap.get(entityName).keySet()) {
				solverManager.addConstraint(new AllDifferent(uniqueEntityMap.get(entityName).get(fieldName)));
			}
		}
	}
	
	

//	private static void addRequiredDataAsConstraints(JPAVirtualMachine vm, VirtualDatabase database, SolverManager solverManager, Map<String, Map<String, Set<Expression>>> uniqueEntityMap) {
//		for(EntityEntry entityEntry : database.getRequiredData().values()) {
//			foobar(vm, solverManager, uniqueEntityMap, entityEntry);
//		}
//	}



	private static void addConstraints(JPAVirtualMachine vm,
			SolverManager solverManager,
			Map<String, Map<String, Set<Expression>>> uniqueEntityMap,
			String entityClassName,
			Map<String, Object> valueMap) {
		JPAStaticEntityConstraint constraints = constraintGen.generateEntityConstraints(entityClassName);
		
		Map<String, Set<Expression>> uniqueFields = uniqueEntityMap.get(entityClassName);
		if(uniqueFields == null) {
			uniqueFields = new HashMap<>();
		}

		for(String fieldName : valueMap.keySet()) {
			checkForFieldConstraints(vm, solverManager, valueMap, constraints, uniqueFields, fieldName);
		}
		
		uniqueEntityMap.put(entityClassName, uniqueFields);
	}



	private static void checkForFieldConstraints(JPAVirtualMachine vm,
			SolverManager solverManager, Map<String, Object> valueMap,
			JPAStaticEntityConstraint constraints,
			Map<String, Set<Expression>> uniqueFields, String fieldName) {
		JPAFieldConstraints fieldConstraint = constraints.getFieldConstraints().get(fieldName);
		
		Object fieldValue = valueMap.get(fieldName);
		
		if(fieldValue instanceof Term) {
			Term term = (Term)fieldValue;
			
			handleMinConstraint(vm, solverManager, fieldConstraint,	term);
			
			checkAndAddUniqueVariables(uniqueFields, fieldName, fieldConstraint, term);
		}
		
		if(fieldValue instanceof ReferenceVariable) {
			ReferenceVariable refVar = (ReferenceVariable)fieldValue;
			if(refVar.getInitializedClassName().equals("java.lang.String")) {
				Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArray = (SymbolicArrayref)refVar.getField(valueField);
				
				if(fieldConstraint.isId()) {
					solverManager.addConstraint(GreaterThan.newInstance(symArray.getSymbolicLength(), NumericConstant.getZero(Expression.INT)));
				}
				checkAndAddUniqueVariables(uniqueFields, fieldName, fieldConstraint, symArray.getSymbolicLength());
				
				
//				NumericVariable symbolicLength = new NumericVariable(refVar.getName()+".length", Expression.INT);
//				Map<Integer, NumericVariable> symbolicElements = new HashMap<>();
//				
//				if(stringValue != null) {
//					SymbolicArrayref arrayRef = (SymbolicArrayref)stringValue;
//					symbolicLength = arrayRef.getSymbolicLength();
//					for(Integer index : symbolicElements.keySet()) {
//						Object ele = symbolicElements.get(index);
//						if(ele instanceof NumericVariable) {
//							symbolicElements.put(index, (NumericVariable)ele);
//						} else if(ele instanceof NumericConstant) {
//							NumericConstant constant = (NumericConstant)ele;
//							NumericVariable nv = new NumericVariable(refVar.getName()+".element."+index, Expression.INT);
//							solverManager.addConstraint(NumericEqual.newInstance(nv, constant));
//							symbolicElements.put(index, nv);
//						} else {
//							throw new RuntimeException("Could not set element that is not a numeric variable or constant to string varialbe");
//						}
//					}
//				}
//							
//				StringVariable stringVar = new StringVariable(refVar.getName(), symbolicLength, symbolicElements);
//				checkAndAddUniqueVariables(uniqueFields, fieldName, fieldConstraint, stringVar);
			}
		}
		
		if(fieldValue instanceof IntConstant) {
			checkAndAddUniqueVariables(uniqueFields, fieldName, fieldConstraint, (IntConstant)fieldValue);
		}
	}

	private static void checkAndAddUniqueVariables(Map<String, Set<Expression>> uniqueFields, String fieldName, JPAFieldConstraints fieldConstraint, Expression variableOrConstant) {
		if(fieldConstraint.isId() || fieldConstraint.isUnique()) {
			Set<Expression> uniqueVariables = uniqueFields.get(fieldName);
			if(uniqueVariables == null) {
				uniqueVariables = new HashSet<>();
			}
			uniqueVariables.add(variableOrConstant);
			uniqueFields.put(fieldName, uniqueVariables);
		}
	}

	private static void handleMinConstraint(JPAVirtualMachine vm, SolverManager solverManager, JPAFieldConstraints fieldConstraint, Term term) {
		if(fieldConstraint.getMinSize() != null) {
			ConstraintExpression minConstraint = GreaterOrEqual.newInstance((Term)term, IntConstant.getInstance(fieldConstraint.getMinSize()));
			if(minConstraint instanceof BooleanConstant) {
				 if(((BooleanConstant)minConstraint).getValue()) {
					 // expression is already true, nothing to do here
				 } else {
					// minimum constraint failed
					// throw persistence exception
					// throw new VmRuntimeException(vm.generateExc("javax.persistence.PersistenceException"));
					 // TODO
				 }
			} else {
				solverManager.addConstraint(minConstraint);
			}
		}
	}
	
	
}
