package de.wwu.muggl.vm.var.sym.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.hibernate.orm.persister.entity.internal.EntityPersisterImpl;
import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingEntity;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmFromElementSpace;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.select.SqmSelection;

import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;
import de.wwu.muggl.vm.var.sym.gen.ctr.SymbolicStaticEntityConstraints;
import de.wwu.muggl.vm.var.sym.gen.having.SymbolicHavingClause;
import de.wwu.muggl.vm.var.sym.gen.pred.SymbolicPredicateManager;

public class SymbolicQueryResultElementGenerator {
	
	private JPAVirtualMachine vm;
	private QLStatement<?> qlStatement;
	private int generatedElements;
	private SymoblicEntityFieldGenerator entityFieldGenerator;
	private SymbolicStaticEntityConstraints entityConstraints;
	
	private SymbolicPredicateManager predicateManager ;
	
	protected Map<String, Set<EntityObjectref>> generatedEntityMap;
	
	public SymbolicQueryResultElementGenerator(JPAVirtualMachine vm, QLStatement<?> qlStatement) {
		this.vm = vm;
		this.qlStatement = qlStatement;
		this.entityFieldGenerator = new SymoblicEntityFieldGenerator(vm);
		this.entityConstraints = new SymbolicStaticEntityConstraints(vm);
	}
	
	
	public Objectref[] generateElements() {
		SqmQuerySpec querySpec = this.qlStatement.getSqmStatement().getQuerySpec();
		
		// Start with FROM -> basic entity map
		this.generatedEntityMap = genearteFromElements(querySpec.getFromClause());
		
		// add static entity constraints
		for(String entityName : generatedEntityMap.keySet()) {
			for(EntityObjectref eo : generatedEntityMap.get(entityName)) {
				try {
					this.entityConstraints.addStaticConstraints(eo);
				} catch (VmRuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			if(!vm.getSolverManager().hasSolution()) {
				throw new RuntimeException("Query Result List has no solution");
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			throw new RuntimeException("Query Result List has no solution");
		}
		
		this.predicateManager = new SymbolicPredicateManager(vm, generatedEntityMap, this.qlStatement.getParameterMap(), this);

		// apply WHERE conditions
		if(querySpec.getWhereClause() != null) {
			predicateManager.applyPredicate(querySpec.getWhereClause().getPredicate());
		}

		// apply HAVING conditions
		if(querySpec.getHavingClause() != null) {
			SymbolicHavingClause havingManager = new SymbolicHavingClause(vm, generatedEntityMap, this.qlStatement.getParameterMap(), this);
			havingManager.applyHaving(querySpec.getHavingClause());
		}

		try {
			if(!vm.getSolverManager().hasSolution()) {
//				throw new RuntimeException("Query Result List has no solution");
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			throw new RuntimeException("Query Result List has no solution");
		}
		
		// apply GROUP BY conditions
		if(querySpec.getGroupByClause() != null) {
			
		}
		
		
		// at this point, the generation of entity objects is complete
		// add the entity objects to the symbolic database
		for(String entityName : generatedEntityMap.keySet()) {
			for(EntityObjectref eo : generatedEntityMap.get(entityName)) {
//				Objectref req_entityObjectref = this.vm.getAnObjectref(eo.getInitializedClass().getClassFile());
//				EntityObjectref req_entityObject = new EntityObjectref(entityFieldGenerator, "REQ-DATA#"+eo.getName(), req_entityObjectref);
//				eo.setRequiredEntity(req_entityObject);
//				
//				Map<String, Object> values = eo.valueMap();
//				for(String attribute : values.keySet()) {
//					System.err.println("*************************** IS THIS OK?");
//					System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*"); System.err.println("*");
//					System.err.println("******************************************************");
//					
//					
//					Field f = eo.getInitializedClass().getClassFile().getFieldByName(attribute, true);
//					Object vData = eo.getField(f);
//					Object vReq = req_entityObject.getField(f);
//					
//					if(vData instanceof EntityStringObjectref) {
//						SymbolicArrayref s1 = (SymbolicArrayref)((EntityStringObjectref) vData).valueMap().get("value");
//						SymbolicArrayref s2 = (SymbolicArrayref)((EntityStringObjectref) vReq).valueMap().get("value");
//						this.vm.getSolverManager().addConstraint(new SymbolicCharArrayEqual(s1, s2));
//					}
//					if(vData instanceof NumericVariable) {
//						this.vm.getSolverManager().addConstraint(NumericEqual.newInstance((NumericVariable)vReq, (NumericVariable)vData));
//					}
//					
////					req_entityObject.putField(f, values.get(attribute));
//				}
//				
				this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityName, eo);
//				this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityName, eo);
				
				this.vm.printVODB();
			}
		}
		
		
		// get the SELECT and return it
		if(querySpec.getSelectClause() != null) {
			List<SqmSelection> selectionList = querySpec.getSelectClause().getSelections();
			Objectref[] selectResult = new Objectref[selectionList.size()];
			for(int i=0; i<selectionList.size(); i++) {
				SqmSelection selection = selectionList.get(i);
				if(selection.getExpression() instanceof SqmSingularAttributeBindingEntity) {
					SqmSingularAttributeBindingEntity selectEntity = (SqmSingularAttributeBindingEntity)selection.getExpression();
					String entityName = selectEntity.getBoundNavigable().getEntityName();
					Set<EntityObjectref> result = this.generatedEntityMap.get(entityName);
					
					EntityObjectref reqObjRef = result.iterator().next();
					Objectref data_entityObjectref = this.vm.getAnObjectref(reqObjRef.getInitializedClass().getClassFile());
					EntityObjectref data_entityObject = new EntityObjectref(
							entityFieldGenerator, 
							"DATA#"+reqObjRef.getName(), 
							data_entityObjectref, 
							reqObjRef);
					
					try {
						this.entityConstraints.addStaticConstraints(data_entityObject);
					} catch (VmRuntimeException e) {
						e.printStackTrace();
					}
					
					this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityName, data_entityObject);
					
					return new EntityObjectref[]{data_entityObject};
				}
				
				if(selection.getExpression() instanceof SqmSingularAttributeBindingBasic) {
					SqmSingularAttributeBindingBasic selectAttribute = (SqmSingularAttributeBindingBasic)selection.getExpression();
					String attributeName = selectAttribute.getBoundNavigable().getAttributeName();
					String entityName = null;
					if(selectAttribute.getBoundNavigable().getSource() instanceof EntityPersisterImpl<?>) {
						EntityPersisterImpl epi = (EntityPersisterImpl)selectAttribute.getBoundNavigable().getSource();
						entityName = epi.getEntityName();
					}
					
					Set<EntityObjectref> result = this.generatedEntityMap.get(entityName);
					EntityObjectref reqObjRef = result.iterator().next();
					Object value = reqObjRef.valueMap().get(attributeName);
					
					if(value instanceof Objectref) {
						Objectref objRef = (Objectref)value;
						return new Objectref[]{objRef};
					}
					throw new RuntimeException("Oops, could not get attribute: " + attributeName + " from entity selection type: "+ entityName);
				}
				
				else if(selection.getExpression() instanceof SqmEntityBinding) {
					SqmEntityBinding selectEntity = (SqmEntityBinding)selection.getExpression();
					String entityName = selectEntity.getBoundNavigable().getEntityName();
					
					try {
						ClassFile entityClassFile = vm.getClassLoader().getClassAsClassFile(entityName);
						if(entityClassFile.isAccInterface()) {
							entityClassFile = getConcreteEntityClassForInterface(vm, entityClassFile);
							if(entityClassFile != null) {
								entityName = entityClassFile.getName();
							}
						}
					} catch (ClassFileException e) {
					}
					
					
					Set<EntityObjectref> generatedEntities = this.generatedEntityMap.get(entityName);
					if(generatedEntities.size() >= 1) {
						selectResult[i] = generatedEntities.iterator().next();
					}
				}
				else if(selection.getExpression() instanceof CountStarFunctionSqmExpression) {
					try {
						ClassFile longCF = vm.getClassLoader().getClassAsClassFile("java.lang.Long");
						Objectref countRef = vm.getAnObjectref(longCF);
						Field valueField = longCF.getFieldByName("value");
						countRef.putField(valueField, new NumericVariable(countRef.getName()+".value", Expression.INT));
						return new Objectref[]{countRef};
					} catch(Exception e) {
						throw new RuntimeException("error while generating count object reference result");
					}
				}
				
				else {
					throw new RuntimeException("expression type " + selection.getExpression() + " not implemented RETURN SELECT statement, please implement it");
				}
			}
			
			return selectResult;
		}
		
		return null;
	}
	
	
	
	public Set<EntityObjectref> generateSelectElement() {
		SqmQuerySpec querySpec = this.qlStatement.getSqmStatement().getQuerySpec();
		
		// Start with FROM -> basic entity map
		this.generatedEntityMap = genearteFromElements(querySpec.getFromClause());
		
		// add static entity constraints
		for(String entityName : generatedEntityMap.keySet()) {
			for(EntityObjectref eo : generatedEntityMap.get(entityName)) {
				try {
					this.entityConstraints.addStaticConstraints(eo);
				} catch (VmRuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			if(!vm.getSolverManager().hasSolution()) {
				throw new RuntimeException("Query Result List has no solution");
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			throw new RuntimeException("Query Result List has no solution");
		}
		
		SymbolicPredicateManager predicateManager = new SymbolicPredicateManager(vm, generatedEntityMap, this.qlStatement.getParameterMap(), this);

		// apply WHERE conditions
		if(querySpec.getWhereClause() != null) {
			predicateManager.applyPredicate(querySpec.getWhereClause().getPredicate());
		}
				
		// apply HAVING conditions
		if(querySpec.getHavingClause() != null) {
			SymbolicHavingClause havingManager = new SymbolicHavingClause(vm, generatedEntityMap, this.qlStatement.getParameterMap(), this);
			havingManager.applyHaving(querySpec.getHavingClause());
		}
		
		try {
			if(!vm.getSolverManager().hasSolution()) {
				throw new RuntimeException("Query Result List has no solution");
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			throw new RuntimeException("Query Result List has no solution");
		}
		
		// apply GROUP BY conditions
		if(querySpec.getGroupByClause() != null) {
			
		}
		
		
		// at this point, the generation of entity objects is complete
		// add the entity objects to the symbolic database
		for(String entityName : generatedEntityMap.keySet()) {
			for(EntityObjectref eo : generatedEntityMap.get(entityName)) {
				this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityName, eo);
			}
		}
		
		
		// get the SELECT and return it
		if(querySpec.getSelectClause() != null) {
			for(SqmSelection selection : querySpec.getSelectClause().getSelections()) {
				if(selection.getExpression() instanceof SqmSingularAttributeBindingEntity) {
					SqmSingularAttributeBindingEntity selectEntity = (SqmSingularAttributeBindingEntity)selection.getExpression();
					String entityName = selectEntity.getBoundNavigable().getEntityName();
					return this.generatedEntityMap.get(entityName);
				}
				if(selection.getExpression() instanceof SqmEntityBinding) {
					SqmEntityBinding selectEntity = (SqmEntityBinding)selection.getExpression();
					String entityName = selectEntity.getBoundNavigable().getEntityName();
					return this.generatedEntityMap.get(entityName);
				}
			}
		}
		
		return null;
	}
	

	private Map<String, Set<EntityObjectref>> genearteFromElements(SqmFromClause fromClause) {
		Map<String, Set<EntityObjectref>> generatedJoinObjects = new HashMap<>();
		
		for(SqmFromElementSpace fromEle : fromClause.getFromElementSpaces()) {
			generateRootFromElement(fromEle, generatedJoinObjects);
		}
		
		return generatedJoinObjects;
	}
		
		
	public Map<String, Set<EntityObjectref>> generateRootFromElement(SqmFromElementSpace from, Map<String, Set<EntityObjectref>> generatedJoinObjects) {	
		// just one root, but maybe some joins on it...
				
		System.out.println(from);
		String rootEntityName = from.getRoot().getEntityName();
		EntityObjectref rootEntity = generateEntityObjectref(rootEntityName);
		
		
		Set<EntityObjectref> rootSet = generatedJoinObjects.get(rootEntity.getObjectType());
		if(rootSet == null) {
			rootSet = new HashSet<>();
		}
		rootSet.add(rootEntity);
		generatedJoinObjects.put(rootEntity.getObjectType(), rootSet);
		
		for(SqmJoin join : from.getJoins()) {
			if(join.getBinding() instanceof SqmSingularAttributeBindingEntity) {
				SqmSingularAttributeBindingEntity sa = (SqmSingularAttributeBindingEntity)join.getBinding();
				
				String joinEntityName = sa.getBoundNavigable().getEntityName();
				EntityObjectref joinEntity = generateEntityObjectref(joinEntityName);
				Set<EntityObjectref> set = generatedJoinObjects.get(joinEntityName);
				if(set == null) {
					set = new HashSet<>();
					set.add(joinEntity);
					generatedJoinObjects.put(joinEntityName, set);
				}
				
				
				if(sa.getBoundNavigable().getSource() instanceof EntityPersisterImpl) {
					EntityPersisterImpl<?> ep = (EntityPersisterImpl<?>)sa.getBoundNavigable().getSource();
					String sourceEntityName = ep.getEntityName();
								
					EntityObjectref joinSourceObject = null;
					
					Set<EntityObjectref> joinSourceObjectSet = generatedJoinObjects.get(sourceEntityName);
					if(joinSourceObjectSet == null) {
						joinSourceObjectSet = new HashSet<>();
						joinSourceObject = generateEntityObjectref(sourceEntityName);
						joinSourceObjectSet.add(joinSourceObject);
						generatedJoinObjects.put(sourceEntityName, joinSourceObjectSet);
					} else {
						joinSourceObject = joinSourceObjectSet.iterator().next();
					}
					
					String joinAttriuteName = sa.getBoundNavigable().getAttributeName();
					Field attributeField = joinSourceObject.getInitializedClass().getClassFile().getFieldByName(joinAttriuteName);
					
					joinSourceObject.putField(attributeField, joinEntity);
					
					// TODO: auch in andere richtung vom Join auf das Entity Ojbect adden
					// z.B. orderItem.setOrder(order) und order.addOrderItem(orderItem)
				}
			}
		}
		
		return generatedJoinObjects;
	}
	
	
	public EntityObjectref generateEntityObjectref(String entityName) {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++  GENERATE ENTITY OBJECT REF: " + entityName);
		
		
		
		try {
			ClassFile entityClassFile = vm.getClassLoader().getClassAsClassFile(entityName);
			if(entityClassFile.isAccInterface()) {
				entityClassFile = getConcreteEntityClassForInterface(vm, entityClassFile);
			}
			
			Objectref entityObjectref = vm.getAnObjectref(entityClassFile);
			long number = entityObjectref.getInstantiationNumber();
			String entityObjectrefName = "SymbolicQuery."+number+".element#"+this.generatedElements;
			EntityObjectref entityObject = new EntityObjectref(entityFieldGenerator, entityObjectrefName, entityObjectref);
			entityConstraints.addStaticConstraints(entityObject);
			return entityObject;
		} catch(Exception e) {
			throw new RuntimeException("error while generating element", e);
		}
	}
	
	private ClassFile getConcreteEntityClassForInterface(JPAVirtualMachine vm, ClassFile interfaceClass) {
		try {
			for(Class<?> entityClass : vm.getMugglEntityManager().getManagedEntityClasses()) {
				for(Class<?> entityClassInterface : entityClass.getInterfaces()) {
					if(entityClassInterface.getName().equals(interfaceClass.getName())) {
						return vm.getClassLoader().getClassAsClassFile(entityClass.getName());
					}
				}
			}
		} catch(Exception e) {
		}
		return null;
	}
	
	
	public void generateNewElementAndJoinIt(String entityName) {
		// generate new element
		EntityObjectref entityObjectref = generateEntityObjectref(entityName);

		try {
			this.entityConstraints.addStaticConstraints(entityObjectref);
		} catch (VmRuntimeException e) {
			e.printStackTrace();
		}
		this.generatedEntityMap.get(entityName).add(entityObjectref);
		
		// add WHERE constraints
		if(this.qlStatement.getSqmStatement().getQuerySpec().getWhereClause() != null) {
			predicateManager.applyPredicate(this.qlStatement.getSqmStatement().getQuerySpec().getWhereClause().getPredicate());
			try {
				if(!vm.getSolverManager().hasSolution()) {
					throw new RuntimeException("Query Result List has no solution");
				}
			} catch (TimeoutException | SolverUnableToDecideException e) {
				throw new RuntimeException("Query Result List has no solution");
			}
		}
		
		// join it
		SqmFromClause from = this.qlStatement.getSqmStatement().getQuerySpec().getFromClause();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	private void applyHaving(SqmHavingClause havingClause) {
//		if(havingClause.getPredicate() instanceof RelationalSqmPredicate) {
//			applyRelationalPredicate((RelationalSqmPredicate)havingClause.getPredicate());
//		}
//	}
//	
//	
//	private void applyRelationalPredicate(RelationalSqmPredicate predicate) {
//		SqmExpression leftHS = predicate.getLeftHandExpression();
//		SqmExpression rightHS = predicate.getRightHandExpression();
//		
//		if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
//			applySumFunctionRelParameter((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (NamedParameterSqmExpression)rightHS);
//		}
//		
//		if(leftHS instanceof SumFunctionSqmExpression && rightHS instanceof LiteralIntegerSqmExpression) {
//			applySumFunctionRelLiteral((SumFunctionSqmExpression)leftHS, predicate.getOperator(), (LiteralIntegerSqmExpression)rightHS);
//		}
//	}
//	
//	private void applySumFunctionRelParameter(SumFunctionSqmExpression leftHS, RelationalPredicateOperator operator, NamedParameterSqmExpression rightHS) {
//		System.out.println("x");
//	}
//
//	private void applySumFunctionRelLiteral(SumFunctionSqmExpression leftHS, RelationalPredicateOperator operator, LiteralIntegerSqmExpression rightHS) {
//		if(leftHS.getArgument() instanceof SqmSingularAttributeBindingBasic) {
//			SqmSingularAttributeBindingBasic sa = (SqmSingularAttributeBindingBasic)leftHS.getArgument();
//			String attributeName = sa.getBoundNavigable().getAttributeName();
//			if(sa.getBoundNavigable().getSource() instanceof EntityPersisterImpl) {
//				EntityPersisterImpl<?> ep = (EntityPersisterImpl<?>)sa.getBoundNavigable().getSource();
//				String sourceEntityName = ep.getEntityName();
//				
//			}
//		}
//		System.out.println("x");
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	






	public Set<EntityObjectref> generateSelectElementOLD() {		
		SqmQuerySpec querySpec = this.qlStatement.getSqmStatement().getQuerySpec();
		List<SqmSelection> selectionList = querySpec.getSelectClause().getSelections();
		
		Set<EntityObjectref> selectionEntities = new HashSet<>();
		for(SqmSelection selection : selectionList) {
			EntityObjectref se = generateSelectionEntity(selection.getExpression());
			selectionEntities.add(se);
			
			
			
//			if(selection.getExpression() instanceof SqmSingularAttributeBindingEntity) {
//				handleSingularAttributeBindingEntity((SqmSingularAttributeBindingEntity) selection.getExpression());
//			}
		}
		
		this.generatedElements++;
		return selectionEntities;
	}
	
	
	private EntityObjectref generateSelectionEntity(SqmExpression expression) {
		if(expression instanceof SqmSingularAttributeBindingEntity) {
			SqmSingularAttributeBindingEntity attributeBinding = (SqmSingularAttributeBindingEntity)expression;
			String attributeName = attributeBinding.getPropertyPath().getLocalPath();
			EntityObjectref entityObjectref = generateSelectionEntity(attributeBinding.getSourceBinding().getExpressionType());
			
			Field attributeField = entityObjectref.getInitializedClass().getClassFile().getFieldByName(attributeName);
			Object attributeValue = entityObjectref.getField(attributeField);
			
		}
		
		if(expression instanceof EntityPersisterImpl) {
			String entityName = ((EntityPersisterImpl<?>)expression).getName();
			EntityObjectref entityObjectref = generateEntityObjectref(entityName);
			return entityObjectref;
		}
		
		throw new RuntimeException("not supported selection type: " + expression);
	}
	
	
	private EntityObjectref generateSelectionEntity(SqmExpressableType expression) {
		if(expression instanceof SqmSingularAttributeBindingEntity) {
			SqmSingularAttributeBindingEntity attributeBinding = (SqmSingularAttributeBindingEntity)expression;
			String attributeName = attributeBinding.getPropertyPath().getLocalPath();
			EntityObjectref entityObjectref = generateSelectionEntity(attributeBinding.getSourceBinding().getExpressionType());
			
			Field attributeField = entityObjectref.getInitializedClass().getClassFile().getFieldByName(attributeName);
			Object attributeValue = entityObjectref.getField(attributeField);
			
		}
		
		if(expression instanceof EntityPersisterImpl) {
			String entityName = ((EntityPersisterImpl<?>)expression).getName();
			EntityObjectref entityObjectref = generateEntityObjectref(entityName);
			return entityObjectref;
		}
		
		throw new RuntimeException("not supported selection type: " + expression);
	}



	


//	private void handleSingularAttributeBindingEntity(SqmSingularAttributeBindingEntity expression) {
//		String attribute = expression.getPropertyPath().getLocalPath();
//		expression.getSourceBinding().getExpressionType()
//		expression.getSourceBinding().getSourceBinding().getExpressionType()
//	}
}
