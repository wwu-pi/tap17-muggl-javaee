//package de.wwu.muggl.vm.impl.jpa.db;
//
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import de.wwu.muggl.solvers.expressions.BooleanConstant;
//import de.wwu.muggl.solvers.expressions.Constant;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
//import de.wwu.muggl.solvers.expressions.IntConstant;
//import de.wwu.muggl.solvers.expressions.NumericConstant;
//import de.wwu.muggl.solvers.expressions.NumericNotEqual;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Term;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.symbolic.jpa.JPAEntityConstraint;
//import de.wwu.muggl.symbolic.jpa.JPAFieldConstraints;
//import de.wwu.muggl.vm.classfile.structures.Field;
//import de.wwu.muggl.vm.initialization.Arrayref;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class VirtualDatabase {
//
//	// Map<EntityClassName ,  Map < ColumnName,  ColumnExpressionValues(e.g., Variable, or Constant) > >
//	protected Map<String, Map<String, List<Expression>>> entityVariablesMap;
//	
//	// Map<EntityClassName ,  Map < ColumnName,  JPA constraints on the column/field > >
//	protected Map<String, Map<String, JPAFieldConstraints>> entityFieldConstraints;
//	
//	public VirtualDatabase() {
//		this.entityVariablesMap = new HashMap<>();
//		this.entityFieldConstraints = new HashMap<>();
//	}
//	
//	public void persist(Objectref objectRef) {
//		String className = objectRef.getName();
//		
//		Map<String, List<Expression>> expressionsPerColumnMap = entityVariablesMap.get(className);
//		
//		if(expressionsPerColumnMap == null) {
//			expressionsPerColumnMap = new HashMap<>();
//		}
//		
//		for(Field field : objectRef.getFields().keySet()) {
//			// assume field name equals column name
//			// TODO: check annotation at field for alternative names
//			String fieldName = field.getName();
//			List<Expression> expressions = expressionsPerColumnMap.get(fieldName);
//			if(expressions == null) {
//				expressions = new ArrayList<>();
//			}
//			
//			Object objectValue = objectRef.getFields().get(field);
//			Expression expressionValue = getExpressionObjectValue(objectValue);
//			
//			if(expressionValue == null) {
//				throw new RuntimeException("Could not handle value: " + objectValue+ ". It is not a expression! Please modify VirtualDatabase#getExpressionObjectValue method to return a expression for this kind of value...");
//			}
//			
//			expressions.add(expressionValue);
//			expressionsPerColumnMap.put(fieldName, expressions);
//		}
//		
//		entityVariablesMap.put(className, expressionsPerColumnMap);
//	}
//	
//	protected Expression getExpressionObjectValue(Object value) {
//		if(value instanceof Expression) {
//			return (Expression)value;
//		}
//		
//		return null;
//	}
//		
//	public List<ConstraintExpression> generateDBConstraints() {
//		List<ConstraintExpression> dbConstraints = new ArrayList<>();
//		for(String entityClass : this.entityFieldConstraints.keySet()) {
//			Map<String, JPAFieldConstraints> fieldConstraints = entityFieldConstraints.get(entityClass);
//			for(String field : fieldConstraints.keySet()) {
//				JPAFieldConstraints constraint = fieldConstraints.get(field);
//				
//				// integer minimum value constraint
//				if(constraint.getMinSize() != null) {
//					Map<String, List<Expression>> columnVariables = entityVariablesMap.get(entityClass);
//					for(Expression variable : columnVariables.get(field)) {
//						if(variable instanceof NumericVariable) {
//							ConstraintExpression newConstraint = GreaterOrEqual.newInstance((NumericVariable)variable, IntConstant.getInstance(constraint.getMinSize()));
//							dbConstraints.add(newConstraint);
//						} else if(variable instanceof NumericConstant) {
//							// validate if value is minimum value
//							int value = ((NumericConstant)variable).getIntValue();
//							if(value < constraint.getMinSize()) {
//								throw new RuntimeException("Value of existing data is not the required miminum value: existing=" + value + " < constraint=" + constraint.getMinSize());
//							}
//						} else {
//							throw new RuntimeException("Only NumericVariable is supported for min values in entities, see @Min annotation");
//						}
//					}
//				}
//				
//				// integer all different (=uniqueness or ID) constraint
//				if(constraint.isUnique() || constraint.isId()) {
//					Map<String, List<Expression>> columnVariables = entityVariablesMap.get(entityClass);
//					List<Expression> expressions = columnVariables.get(field);
//					for(int i=0; i<expressions.size(); i++) {
//						for(int j=i+1; j<expressions.size(); j++) {
//							Term term1 = (Term)expressions.get(i);
//							Term term2 = (Term)expressions.get(j);
//							ConstraintExpression newConstraint = NumericNotEqual.newInstance(term1, term2);
//							if(newConstraint instanceof BooleanConstant && ((BooleanConstant)newConstraint).getValue()) {
//								continue;
//							}
//							dbConstraints.add(newConstraint);
//						}
//					}
////					for(Expression var1 : columnVariables.get(field)) {
////						for(Expression var2 : columnVariables.get(field)) {
////							if(!(var1 instanceof Term) || !(var2 instanceof Term)) {
////								throw new RuntimeException("Expression must be a Muggl.Term");
////							}
////							if(var1 != var2) {
////								ConstraintExpression newConstraint = NumericNotEqual.newInstance((Term)var1, (Term)var2);
////								dbConstraints.add(newConstraint);
////							}
////						}
////					}
//				}
//			}
//		}
//		return dbConstraints;
//	}
//	
//	public void addVariables(JPAEntityConstraint entityConstraint) {
//		String entityClassName = entityConstraint.getEntityClass().getName();
//		
//		this.entityFieldConstraints.put(entityClassName, entityConstraint.getFieldConstraints());
//		
//		Map<String, List<Expression>> entityVariables = this.entityVariablesMap.get(entityClassName);
//		
//		if(entityVariables == null) {
//			entityVariables = new HashMap<>();
//		}
//		for(String field : entityConstraint.getFields()) {
//			List<Expression> vars = entityVariables.get(field);
//			if(vars == null) {
//				vars = new ArrayList<>();
//			}
//			vars.add(entityConstraint.getVariable(field));
//			entityVariables.put(field, vars);
//		}
//		this.entityVariablesMap.put(entityClassName, entityVariables);
//	}
//}
