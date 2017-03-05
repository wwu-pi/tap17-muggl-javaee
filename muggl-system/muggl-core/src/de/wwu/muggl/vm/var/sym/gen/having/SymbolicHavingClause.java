package de.wwu.muggl.vm.var.sym.gen.having;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.orm.persister.entity.internal.EntityPersisterImpl;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmHavingClause;

import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.sym.gen.SymbolicQueryResultElementGenerator;

public class SymbolicHavingClause {
	
	protected JPAVirtualMachine vm;
	protected Map<String, Set<EntityObjectref>> entityMap;
	protected Map<String, Object> parameterMap;
	protected SymbolicQueryResultElementGenerator generator;
	protected int counter;
	protected final int MAX_LOOPS = 5;
	
	public SymbolicHavingClause(JPAVirtualMachine vm, Map<String, Set<EntityObjectref>> entityMap, Map<String, Object> parameterMap, SymbolicQueryResultElementGenerator generator) {
		this.entityMap = entityMap;
		this.vm = vm;
		this.generator = generator;
		this.parameterMap = parameterMap;
		this.counter = 0;
	}

	public void applyHaving(SqmHavingClause havingClause) {
		if(havingClause.getPredicate() instanceof RelationalSqmPredicate) {
			applyRelationalPredicate((RelationalSqmPredicate)havingClause.getPredicate());
		}
	}
	
	private void applyRelationalPredicate(RelationalSqmPredicate predicate) {
		SqmExpression leftHS = predicate.getLeftHandExpression();
		SqmExpression rightHS = predicate.getRightHandExpression();
		
		if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
			applySumFunctionRelParameter((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
		
		if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof LiteralIntegerSqmExpression) {
			applySumFunctionRelLiteral((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (LiteralIntegerSqmExpression)rightHS);
		}
		
		if(leftHS instanceof AvgFunctionSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
			applyAvgFunctionRelParameter((AvgFunctionSqmExpression)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
	}
	
	private void applySumFunctionRelParameter(SumFunctionSqmExpression leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
		if(leftHS.getArgument() instanceof SqmSingularAttributeBindingBasic) {
			SqmSingularAttributeBindingBasic sa = (SqmSingularAttributeBindingBasic)leftHS.getArgument();
			String attributeName = sa.getBoundNavigable().getAttributeName();
			if(sa.getBoundNavigable().getSource() instanceof EntityPersisterImpl) {
				EntityPersisterImpl<?> ep = (EntityPersisterImpl<?>)sa.getBoundNavigable().getSource();
				String sourceEntityName = ep.getEntityName();
				Set<EntityObjectref> eo = this.entityMap.get(sourceEntityName);
				
				Term symbolicSum = generateSymbolicSumOfAttribute(eo, attributeName);
				
				NamedParameterSqmExpression paraExpression = (NamedParameterSqmExpression)rightHS;
				String paraName = paraExpression.getName();
				Term parameter = (Term)parameterMap.get(paraName);
				
				ConstraintExpression ce = getConstraintExpression(symbolicSum, operator, parameter);
				if(ce != null) {
					int level = vm.getSolverManager().getConstraintLevel();
					vm.getSolverManager().addConstraint(ce);
					try {
						if(!vm.getSolverManager().hasSolution()) {
							this.counter++;
							vm.getSolverManager().resetConstraintLevel(level);
							if(this.counter < this.MAX_LOOPS) {
								this.generator.generateNewElementAndJoinIt(sourceEntityName);
								applySumFunctionRelParameter(leftHS, operator, rightHS);
							}
						}
					} catch (TimeoutException | SolverUnableToDecideException e) {
						throw new RuntimeException("error while checking solution", e);
					}
				}
			}
		}
	}

	private void applySumFunctionRelLiteral(SumFunctionSqmExpression leftHS, RelationalPredicateOperator operator, LiteralIntegerSqmExpression rightHS) {
		if(leftHS.getArgument() instanceof SqmSingularAttributeBindingBasic) {
			SqmSingularAttributeBindingBasic sa = (SqmSingularAttributeBindingBasic)leftHS.getArgument();
			String attributeName = sa.getBoundNavigable().getAttributeName();
			if(sa.getBoundNavigable().getSource() instanceof EntityPersisterImpl) {
				EntityPersisterImpl<?> ep = (EntityPersisterImpl<?>)sa.getBoundNavigable().getSource();
				String sourceEntityName = ep.getEntityName();
				Set<EntityObjectref> eo = this.entityMap.get(sourceEntityName);
				Term symbolicSum = generateSymbolicSumOfAttribute(eo, attributeName);
				
//				vm.getSolverManager().addConstraint();
			}
		}
	}
	
	private void applyAvgFunctionRelParameter(AvgFunctionSqmExpression leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
		if(leftHS.getArgument() instanceof SqmSingularAttributeBindingBasic) {
			SqmSingularAttributeBindingBasic sa = (SqmSingularAttributeBindingBasic)leftHS.getArgument();
			String attributeName = sa.getBoundNavigable().getAttributeName();
			if(sa.getBoundNavigable().getSource() instanceof EntityPersisterImpl) {
				EntityPersisterImpl<?> ep = (EntityPersisterImpl<?>)sa.getBoundNavigable().getSource();
				String sourceEntityName = ep.getEntityName();
				Set<EntityObjectref> eo = this.entityMap.get(sourceEntityName);
				
				Term symbolicSum = generateSymbolicAvgOfAttribute(eo, attributeName);
				
				NamedParameterSqmExpression paraExpression = (NamedParameterSqmExpression)rightHS;
				String paraName = paraExpression.getName();
				Term parameter = (Term)parameterMap.get(paraName);
				
				ConstraintExpression ce = getConstraintExpression(symbolicSum, operator, parameter);
				if(ce != null) {
					int level = vm.getSolverManager().getConstraintLevel();
					vm.getSolverManager().addConstraint(ce);
					try {
						if(!vm.getSolverManager().hasSolution()) {
							this.counter++;
							vm.getSolverManager().resetConstraintLevel(level);
							if(this.counter < this.MAX_LOOPS) {
								this.generator.generateNewElementAndJoinIt(sourceEntityName);
								applyAvgFunctionRelParameter(leftHS, operator, rightHS);
							}
						}
					} catch (TimeoutException | SolverUnableToDecideException e) {
						throw new RuntimeException("error while checking solution", e);
					}
				}
			}
		}
	}
	


	private ConstraintExpression getConstraintExpression(Object leftValueObject, RelationalPredicateOperator operator, Object rightValueObject) {
		if(leftValueObject instanceof Term && rightValueObject instanceof Term) {
			Term leftValue = (Term)leftValueObject;
			Term rightValue = (Term)rightValueObject;
			switch(operator) {
				case EQUAL : return NumericEqual.newInstance(leftValue, rightValue);
				case GREATER_THAN : return GreaterThan.newInstance(leftValue, rightValue);
				case GREATER_THAN_OR_EQUAL : return GreaterOrEqual.newInstance(leftValue, rightValue);
				case LESS_THAN : return LessThan.newInstance(leftValue, rightValue);
				case LESS_THAN_OR_EQUAL : return GreaterOrEqual.newInstance(rightValue, leftValue);
				case NOT_EQUAL : return NumericNotEqual.newInstance(rightValue, leftValue);
				default: return null;
			}
		}
		
		return null;
	}

	private Term generateSymbolicSumOfAttribute(Set<EntityObjectref> set, String attributeName) {
		
		if(set.size() == 0) {
			return null;
		}
		EntityObjectref eo = set.toArray(new EntityObjectref[1])[0];
		Field attributeField = eo.getInitializedClass().getClassFile().getFieldByName(attributeName);
		
		if(set.size() == 1) {
			return (NumericVariable)eo.getField(attributeField);
		}
		
		List<NumericVariable> nvList = new ArrayList<>();
		for(EntityObjectref e : set) {
			NumericVariable nv = (NumericVariable)e.getField(attributeField);
			nvList.add(nv);
		}
		Term symbolicSum = Sum.newInstance(nvList.get(0), nvList.get(1));
		for(int i=2; i<nvList.size(); i++) {
			symbolicSum = Sum.newInstance(symbolicSum, nvList.get(i));
		}
		return symbolicSum;
	}
	
	private Term generateSymbolicAvgOfAttribute(Set<EntityObjectref> set, String attributeName) {
		if(set.size() == 0) {
			return null;
		}
		EntityObjectref eo = set.toArray(new EntityObjectref[1])[0];
		Field attributeField = eo.getInitializedClass().getClassFile().getFieldByName(attributeName);
		
		if(set.size() == 1) {
			return (NumericVariable)eo.getField(attributeField);
		}
		
		List<NumericVariable> nvList = new ArrayList<>();
		for(EntityObjectref e : set) {
			NumericVariable nv = (NumericVariable)e.getField(attributeField);
			nvList.add(nv);
		}
		Term symbolicSum = Sum.newInstance(nvList.get(0), nvList.get(1));
		for(int i=2; i<nvList.size(); i++) {
			symbolicSum = Sum.newInstance(symbolicSum, nvList.get(i));
		}
		return symbolicSum;
	}
}
