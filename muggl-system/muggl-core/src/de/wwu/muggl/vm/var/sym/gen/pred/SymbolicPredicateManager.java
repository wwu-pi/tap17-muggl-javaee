package de.wwu.muggl.vm.var.sym.gen.pred;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.orm.persister.common.internal.OrmSingularAttributeEntity;
import org.hibernate.orm.persister.common.spi.OrmSingularAttribute;
import org.hibernate.orm.persister.entity.internal.EntityPersisterImpl;
import org.hibernate.orm.persister.entity.internal.IdentifierDescriptorSimpleImpl;
import org.hibernate.sqm.query.expression.AbstractLiteralSqmExpressionImpl;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.domain.SqmEntityIdentifierBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmNavigableSourceBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingEntity;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;

import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.AndList;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.CharVariable;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.gen.SymbolicQueryResultElementGenerator;

public class SymbolicPredicateManager {

	protected JPAVirtualMachine vm;
	protected Map<String, Set<EntityObjectref>> entityMap;
	protected Map<String, Object> parameterMap;
	protected SymbolicQueryResultElementGenerator generator;
	protected int counter;
	protected final int MAX_LOOPS = 5;
	
	public SymbolicPredicateManager(JPAVirtualMachine vm, Map<String, Set<EntityObjectref>> entityMap, Map<String, Object> parameterMap, SymbolicQueryResultElementGenerator generator) {
		this.entityMap = entityMap;
		this.vm = vm;
		this.generator = generator;
		this.parameterMap = parameterMap;
		this.counter = 0;
	}
	
	public void applyPredicate(SqmPredicate predicate) {
		if(predicate instanceof RelationalSqmPredicate) {
			applyRelationalPredicate((RelationalSqmPredicate)predicate);
		} else if(predicate instanceof GroupedSqmPredicate) {
			applyGroupedPredicate((GroupedSqmPredicate)predicate);
		} else if(predicate instanceof AndSqmPredicate) {
			applyAndPredicate((AndSqmPredicate)predicate);
		}
	}

	private void applyAndPredicate(AndSqmPredicate predicate) {
		applyPredicate(predicate.getLeftHandPredicate());
		applyPredicate(predicate.getRightHandPredicate());
	}
	
	private void applyGroupedPredicate(GroupedSqmPredicate predicate) {
		if(predicate.getSubPredicate() instanceof RelationalSqmPredicate) {
			applyRelationalPredicate((RelationalSqmPredicate)predicate.getSubPredicate());
		}
	}

	private void applyRelationalPredicate(RelationalSqmPredicate predicate) {
		SqmExpression leftHS = predicate.getLeftHandExpression();
		SqmExpression rightHS = predicate.getRightHandExpression();
		
		if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
			applySumFunctionRelParameter((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
		
		else if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof LiteralIntegerSqmExpression) {
			applySumFunctionRelLiteral((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (LiteralIntegerSqmExpression)rightHS);
		}
		
		else if(leftHS instanceof SqmSingularAttributeBindingBasic && rightHS instanceof NamedParameterSqmExpression) {
			applyAttributeRelParameter((SqmSingularAttributeBindingBasic)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
		
		else if(leftHS instanceof SqmSingularAttributeBindingBasic && rightHS instanceof AbstractLiteralSqmExpressionImpl) {
			applyAttributeRelLiteral((SqmSingularAttributeBindingBasic)leftHS, predicate.getOperator(), (AbstractLiteralSqmExpressionImpl<?>)rightHS);
		}
		
		else if(leftHS instanceof SqmEntityIdentifierBindingBasic && rightHS instanceof NamedParameterSqmExpression) {
			applyEntityIdentifierRelParameter((SqmEntityIdentifierBindingBasic)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
		
		else if(leftHS instanceof CollectionSizeSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
			applyCollectionSizeRelParameter((CollectionSizeSqmExpression)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
		}
		
		else if(leftHS instanceof SqmSingularAttributeBindingEntity && rightHS instanceof NamedParameterSqmExpression) {
			if(predicate.getOperator().equals(RelationalPredicateOperator.EQUAL)) {
				applyEntityBindingParameter((SqmSingularAttributeBindingEntity)leftHS, (NamedParameterSqmExpression)rightHS);
			}
		}
		
		else if(leftHS instanceof SqmSingularAttributeBindingBasic && rightHS instanceof SqmSingularAttributeBindingBasic) {
			applyAttributeOnBothHSRelation((SqmSingularAttributeBindingBasic)leftHS, predicate.getOperator(), (SqmSingularAttributeBindingBasic)rightHS);
		}
	}
	
	private void applyAttributeOnBothHSRelation(SqmSingularAttributeBindingBasic leftHS, RelationalPredicateOperator operator, SqmSingularAttributeBindingBasic rightHS) {
		if(leftHS.getSourceBinding() instanceof SqmEntityBinding
			&& rightHS.getSourceBinding() instanceof SqmEntityBinding) {
			String entity1 = ((SqmEntityBinding)leftHS.getSourceBinding()).getBoundNavigable().getEntityName();
			String entity2 = ((SqmEntityBinding)rightHS.getSourceBinding()).getBoundNavigable().getEntityName();
			
			String attribute1 = leftHS.getBoundNavigable().getAttributeName();
			String attribute2 = rightHS.getBoundNavigable().getAttributeName();
			
			Set<EntityObjectref> entitySet1 = this.entityMap.get(entity1);
			Set<EntityObjectref> entitySet2 = this.entityMap.get(entity2);
			
			Iterator<EntityObjectref> it = entitySet1.iterator();
			EntityObjectref entityObjecref1 = it.next();
			
			Iterator<EntityObjectref> it2 = entitySet2.iterator();
			EntityObjectref next = it2.next();
			while(next != null) {
				if(next.hashCode() != entityObjecref1.hashCode()) {
					break;
				}
				next = it2.next();
			}
			EntityObjectref entityObjecref2 = next;
			
			Field field1 = entityObjecref1.getInitializedClass().getClassFile().getFieldByName(attribute1);
			Field field2 = entityObjecref2.getInitializedClass().getClassFile().getFieldByName(attribute2);
			Object value1 = entityObjecref1.getField(field1);
			Object value2 = entityObjecref2.getField(field2);
			
			ConstraintExpression ce = getAttributeRelConstraint(value1, operator, value2);
			if(ce != null) {
				this.vm.getSolverManager().addConstraint(ce);
			}
			return;
		}
		

//		String x = leftHS.getBoundNavigable().getNavigableName(); leftHS.getSourceBinding()
//		String y = leftHS.getBoundNavigable().getAttributeName(); rightHS.getPropertyPath()
		throw new RuntimeException("Not supported yet");
	}



	private void applyEntityBindingParameter(SqmSingularAttributeBindingEntity leftHS, NamedParameterSqmExpression paraExpression) {
		String entityName = ((EntityPersisterImpl<?>)leftHS.getSourceBinding().getBoundNavigable()).getEntityName();
		String attributeName = leftHS.getBoundNavigable().getAttributeName();
		
		String paraName = paraExpression.getName();
		Object parameter = parameterMap.get(paraName);
		
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				eo.putField(attributeField, parameter);
			}
		}
	}

	private void applyEntityIdentifierRelParameter(SqmEntityIdentifierBindingBasic leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
		if(leftHS.getInferableType() instanceof IdentifierDescriptorSimpleImpl) {
			IdentifierDescriptorSimpleImpl idType = (IdentifierDescriptorSimpleImpl)leftHS.getInferableType();
			String attributeName = idType.getIdAttribute().getAttributeName();
			String entityName = idType.getSource().getEntityName();
			applyEntityAttributeRelation(attributeName, entityName, operator, rightHS);
		}
	}

	private void applyCollectionSizeRelParameter(CollectionSizeSqmExpression leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
		System.out.println("** here**");
	}

	private void applyEntityAttributeRelation(String attributeName, String entityName, RelationalPredicateOperator operator, Object rightHS) {	
		if(rightHS instanceof NamedParameterSqmExpression) {
			applyEntityAttributeRelationParameter(attributeName, entityName, operator, (NamedParameterSqmExpression)rightHS);
		} else if(rightHS instanceof ReferenceVariable) {
			applyEntityAttributeRelationRefVar(attributeName, entityName, operator, (ReferenceVariable)rightHS);
		} else if(rightHS instanceof Objectref) {
			applyEntityAttributeRelationObjVar(attributeName, entityName, operator, (Objectref)rightHS);
		} else if(rightHS instanceof Constant) {
			applyEntityAttributeRelationContant(attributeName, entityName, operator, (Constant)rightHS);
		} else if(rightHS instanceof NumericVariable) {
			applyEntityAttributeRelationNumericVar(attributeName, entityName, operator, (NumericVariable)rightHS);
		}
	}
	
	private void applyEntityAttributeRelationContant(String attributeName, String entityName, RelationalPredicateOperator operator, Constant constant) {
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				Object fieldValue = eo.getField(attributeField);
				ConstraintExpression ce = getAttributeRelConstraint(fieldValue, operator, constant);
				if(ce != null) {
					this.vm.getSolverManager().addConstraint(ce);
				}
			}
		}
	}
	
	private void applyEntityAttributeRelationNumericVar(String attributeName, String entityName, RelationalPredicateOperator operator, NumericVariable nv) {
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				Object fieldValue = eo.getField(attributeField);
				ConstraintExpression ce = getAttributeRelConstraint(fieldValue, operator, nv);
				if(ce != null) {
					this.vm.getSolverManager().addConstraint(ce);
				}
			}
		}
	}
	
	private void applyEntityAttributeRelationParameter(String attributeName, String entityName, RelationalPredicateOperator operator, NamedParameterSqmExpression paraExpression) {		
		String paraName = paraExpression.getName();
		Object parameter = parameterMap.get(paraName);
		
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				Object fieldValue = eo.getField(attributeField);
				ConstraintExpression ce = getAttributeRelConstraint(fieldValue, operator, parameter);
				if(ce != null) {
					this.vm.getSolverManager().addConstraint(ce);
				}
			}
		}
	}
	
	private void applyEntityAttributeRelationObjVar(String attributeName, String entityName, RelationalPredicateOperator operator, Objectref refVar) {
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				Object fieldValue = eo.getField(attributeField);
				ConstraintExpression ce = getAttributeRelConstraint(fieldValue, operator, refVar);
				if(ce != null) {
					this.vm.getSolverManager().addConstraint(ce);
				}
			}
		}
	}
	
	private void applyEntityAttributeRelationRefVar(String attributeName, String entityName, RelationalPredicateOperator operator, ReferenceVariable refVar) {
		Set<EntityObjectref> eSet = this.entityMap.get(entityName);
		
		if(eSet.size() >= 1) {
			// all entities are of same type
			Field attributeField = eSet.iterator().next().getInitializedClass().getClassFile().getFieldByName(attributeName);
			for(EntityObjectref eo : eSet) {
				Object fieldValue = eo.getField(attributeField);
				ConstraintExpression ce = getAttributeRelConstraint(fieldValue, operator, refVar);
				if(ce != null) {
					this.vm.getSolverManager().addConstraint(ce);
				}
			}
		}
	}
	
	private void applyAttributeRelLiteral(SqmSingularAttributeBindingBasic leftHS, RelationalPredicateOperator operator, AbstractLiteralSqmExpressionImpl<?> rightHS) {
		
		String entityName = null;
		String attributeName = null;
		
		if(((SqmSingularAttributeBindingBasic)leftHS).getSourceBinding().getBoundNavigable() instanceof EntityPersisterImpl<?>) {
			EntityPersisterImpl<?> ormEntity = (EntityPersisterImpl<?>)leftHS.getSourceBinding().getBoundNavigable();
			entityName = ormEntity.getEntityName();
			attributeName = leftHS.getBoundNavigable().getAttributeName();
		} else if(((SqmSingularAttributeBindingBasic)leftHS).getSourceBinding().getBoundNavigable() instanceof OrmSingularAttributeEntity) {
			OrmSingularAttributeEntity ormEntity = (OrmSingularAttributeEntity)leftHS.getSourceBinding().getBoundNavigable();
			entityName = ormEntity.getEntityName();
			attributeName = leftHS.getBoundNavigable().getAttributeName();
		}
		
		Object literal = rightHS.getLiteralValue();
		
		if(literal instanceof Integer) {
			NumericConstant nc = NumericConstant.getInstance((Integer)literal, Expression.INT);
			applyEntityAttributeRelation(attributeName, entityName, operator, nc);
		} else if(literal instanceof String) {
			ClassFile classFile = null;
			try {
				classFile = this.vm.getClassLoader().getClassAsClassFile("java.lang.String");
			} catch (ClassFileException e) {
			}
			
			Arrayref constantRef = new Arrayref(this.vm.getAnObjectref(classFile), ((String) literal).length());
			for(int i=0; i<((String) literal).length(); i++) {
				
				char c = ((String) literal).charAt(i);
				NumericConstant ic = IntConstant.getInstance((int)c, Expression.INT);
				constantRef.putElement(i, ic);
			}
			
			
			applyEntityAttributeRelation(attributeName, entityName, operator, constantRef);
			for(EntityObjectref ob : this.entityMap.get(entityName)) {
				Field stringField = ob.getInitializedClass().getClassFile().getFieldByName(attributeName);
				Object v = ob.getField(stringField);
				if(v instanceof EntityStringObjectref) {
					
					Field stringValueField = ((EntityStringObjectref)v).getInitializedClass().getClassFile().getFieldByName("value");
					Object vv = ((EntityStringObjectref)v).getField(stringValueField);
					if(vv instanceof SymbolicArrayref) {
						SymbolicArrayref symbolicRef = (SymbolicArrayref)vv;
						ConstraintExpression ce = imposeEqualityConstraints(symbolicRef, constantRef);
						if(ce != null) {
							this.vm.getSolverManager().addConstraint(ce);
						}
						
						System.out.println("s");
					}
					
				}
			}
			
		} else if(literal instanceof Boolean) {
			NumericConstant nc = null;
			if((Boolean)literal) {
				nc = NumericConstant.getInstance(1, Expression.INT);
			} else {
				nc = NumericConstant.getInstance(0, Expression.INT);
			}
			applyEntityAttributeRelation(attributeName, entityName, operator, nc);
		}
	}

	private void applyAttributeRelParameter(SqmSingularAttributeBindingBasic leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
		NamedParameterSqmExpression paraExpression = (NamedParameterSqmExpression)rightHS;
		String paraName = paraExpression.getName();
		Object parameter = parameterMap.get(paraName);
		
		String attributeName = leftHS.getBoundNavigable().getAttributeName();
		
		String entityName = null;
		if(leftHS.getSourceBinding().getBoundNavigable() instanceof OrmSingularAttributeEntity) {
			OrmSingularAttributeEntity ormEntity = (OrmSingularAttributeEntity)leftHS.getSourceBinding().getBoundNavigable();
			entityName = ormEntity.getEntityName();
		} else if(leftHS.getSourceBinding().getBoundNavigable() instanceof EntityPersisterImpl<?>) { 
			EntityPersisterImpl<?> epi = (EntityPersisterImpl<?>)leftHS.getSourceBinding().getBoundNavigable();
			entityName = epi.getEntityName();
		}
		
		if(parameter instanceof ReferenceVariable) {
			System.out.println("stop here");
			
		}
		
		applyEntityAttributeRelation(attributeName, entityName, operator, parameter);
	}
	
	private ConstraintExpression getAttributeRelConstraint(Object fieldValue, RelationalPredicateOperator operator, Object parameter) {
		ConstraintExpression ce = null;
		if(parameter instanceof Term && fieldValue instanceof Term) {
			ce = getConstraintExpression(fieldValue, operator, parameter);
		} else if (parameter instanceof ReferenceVariable) {
			if(((ReferenceVariable)parameter).getObjectType().equals("java.util.Date")) {
				Field fastTimeField = ((ReferenceVariable)parameter).getInitializedClass().getClassFile().getFieldByName("fastTime");
				Term fastTime1 = (NumericVariable)((Objectref)fieldValue).getField(fastTimeField);
				Term fastTime2 = (NumericVariable)((Objectref)parameter).getField(fastTimeField);
				ce = getConstraintExpression(fastTime1, operator, fastTime2);
			} else if(((ReferenceVariable)parameter).getObjectType().equals("java.lang.String")) {
				if(operator.equals(RelationalPredicateOperator.EQUAL)) {
					ce = getStringEqualityConstraint((Objectref)fieldValue, (Objectref)parameter);
				} else if(operator.equals(RelationalPredicateOperator.NOT_EQUAL)) {
					ce = getStringEqualityConstraint((Objectref)fieldValue, (Objectref)parameter).negate();
				}
			} else if(((ReferenceVariable)parameter).getObjectType().equals("java.lang.Long") && fieldValue instanceof NumericVariable) {
				ReferenceVariable refVar = (ReferenceVariable)parameter;
				Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
				NumericVariable value = (NumericVariable)refVar.getField(valueField);
				if(value == null) {
					value = new NumericVariable(refVar.getName()+".value", Expression.LONG);
				}
				ce = getConstraintExpression(fieldValue, operator, value);
			}
		} else if (parameter instanceof Objectref) {
			if(((Objectref)parameter).getObjectType().equals("java.lang.String")) {
				if(operator.equals(RelationalPredicateOperator.EQUAL)) {
					ce = getStringEqualityConstraint((Objectref)fieldValue, (Objectref)parameter);
				} else if(operator.equals(RelationalPredicateOperator.NOT_EQUAL)) {
					ce = getStringEqualityConstraint((Objectref)fieldValue, (Objectref)parameter).negate();
				}
			}
		}
		return ce;
	}	
	
	private ConstraintExpression getStringEqualityConstraint(Objectref reference1, Objectref reference2) {
		Field valueField = ((Objectref)reference1).getInitializedClass().getClassFile().getFieldByName("value");
		Object valueArray1 = reference1.getField(valueField);
		Object valueArray2 = reference2.getField(valueField);
		
		if(valueArray1 instanceof SymbolicArrayref && valueArray2 instanceof SymbolicArrayref) {
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueArray1).getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueArray2).getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			return new SymbolicCharArrayEqual((SymbolicArrayref)valueArray1, (SymbolicArrayref)valueArray2);
		}
		
		if(valueArray1 instanceof Arrayref && valueArray2 instanceof SymbolicArrayref) {
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueArray2).getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			return imposeEqualityConstraints((SymbolicArrayref)valueArray2, (Arrayref)valueArray1);
		}
		
		if(valueArray1 instanceof SymbolicArrayref && valueArray2 instanceof Arrayref) {
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueArray1).getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			return imposeEqualityConstraints((SymbolicArrayref)valueArray1, (Arrayref)valueArray2);
		}
		
		return null;
	}
	
	protected ConstraintExpression imposeEqualityConstraints(SymbolicArrayref symbolicValueArray, Arrayref objectArray) {
		List<ConstraintExpression> list = new ArrayList<>();
		list.add(NumericEqual.newInstance(symbolicValueArray.getSymbolicLength(), NumericConstant.getInstance(objectArray.length, Expression.INT)));
		for(int i=0; i<objectArray.length; i++) {
			CharVariable element = (CharVariable)symbolicValueArray.getElement(i);
			if(element == null) {
				element = new CharVariable(symbolicValueArray.getName()+".element."+i, Expression.INT);
				symbolicValueArray.setElementAt(i, element);
			}
			list.add(NumericEqual.newInstance(element, (IntConstant)objectArray.getElement(i)));
		}
		return new AndList(list);
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
				
				System.out.println("foobar");
//				vm.getSolverManager().addConstraint();
			}
		}
	}
	
	public ConstraintExpression getConstraintExpression(Object leftValueObject, RelationalPredicateOperator operator, Object rightValueObject) {
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
}
