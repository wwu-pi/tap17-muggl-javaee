package de.wwu.muggl.vm.impl.jpa.dbgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.jpa.criteria.ComparisonOperator;
import de.wwu.muggl.jpa.criteria.MugglCriteriaQuery;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryComparisonRestriction;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.jpa.criteria.MugglJoin;
import de.wwu.muggl.jpa.criteria.MugglPath;
import de.wwu.muggl.jpa.criteria.MugglRoot;
import de.wwu.muggl.jpa.criteria.metamodel.JoinType;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.choco.ChocoSolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.AllDifferentObjectReference;
import de.wwu.muggl.solvers.expressions.AndList;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.EntityConstraintExpression;
import de.wwu.muggl.solvers.expressions.EntityFieldValueEqualConstraint;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.ReferenceVariablesEqualConstraint;
import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.type.IObjectreference;
import de.wwu.muggl.symbolic.obj.SymbolicObjectEqualManager;
import de.wwu.muggl.symbolic.obj.SymbolicObjectStore;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerator;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceQueryResultArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class DBObjectGenerator {
	
	protected JPAVirtualMachine vm;
	protected SymbolicObjectEqualManager objectEqualManager;
	protected SymbolicObjectStore objectStore;
	
	protected SymbolicObjectGenerator symObjGenerator;
	
	@Deprecated
	protected Map<ReferenceVariable, NumericVariable> objectIdMap;

	
	
	public DBObjectGenerator(JPAVirtualMachine vm) {
		this.vm = vm;
		this.symObjGenerator = new SymbolicObjectGenerator();
		this.objectIdMap = new HashMap<>();
		this.objectEqualManager = new SymbolicObjectEqualManager();
		this.objectStore = new SymbolicObjectStore();
	}
	
	public SymbolicObjectStore getObjectStore() {
		return this.objectStore;
	}
	
	public Map<String, List<ReferenceVariable>> generateEntitySelectData(ReferenceQueryResultArrayListVariable symQryResList, Solution solution, SolverManager solverManager) {		
		if(solution == Solution.NOSOLUTION) {
			return null; // nothing required
		}
		
		// get the elements that are currently in the result list
		List<ReferenceVariable> resultElements = getSymbolicElementsFromField(symQryResList, solution);
		
		// get the expected length of the result list
		int length = ((NumericConstant)solution.getValue(symQryResList.getSymbolicLength())).getIntValue();
		
		// check that there are not more elements in the result list as expected
		if(resultElements.size() > length) {
			throw new RuntimeException("No solution found for query result list with more elements ("+resultElements.size()+" than expected ("+length+")");
		}
		
		// add missing elements in the result list
		for(int i=resultElements.size(); i<length; i++) {
			ReferenceVariable plainEntity = generateObjectref(symQryResList.getResultEntityName());
			resultElements.add(plainEntity);
		}
		
		// build joins
		for(MugglRoot<?> root : symQryResList.getCriteriaQuery().getRootSet()) {
			for(MugglJoin<?,?> join : root.getJoinSet()) {
				handleJoin(join);
			}
		}
		
		return doAlgorithm(resultElements, symQryResList.getCriteriaQuery(), solverManager);
	}
	
	
	protected Map<String, List<ReferenceVariable>> doAlgorithm(List<ReferenceVariable> resultElements, MugglCriteriaQuery<?> query, SolverManager solverManager) {		
		Map<String, List<ReferenceVariable>> dataMap = new HashMap<>();
		Map<String, Set<ReferenceVariable>> markedData = new HashMap<>();
		
		List<EntityConstraintExpression> entityConstraints = new ArrayList<>();
		
		for(ReferenceVariable expectedResultEntry : resultElements) {
			
			// perform the join operations
			for(MugglRoot<?> root : query.getRootSet()) {
				for(MugglJoin<?,?> join : root.getJoinSet()) {
					doAlgorithmJoin(join, dataMap, markedData);
				}
			}
			
			// add the query restrictions
			for(EntityConstraintExpression ce : getQueryRestrictionConstraints(markedData, query)) {
				entityConstraints.add(ce);
			}
			
			// add restrictions from expected result
			for(EntityConstraintExpression ce : getExpectedResultRestrictions(expectedResultEntry, markedData)) {
				entityConstraints.add(ce);
			}
			
			// check the constraints
			for(EntityConstraintExpression ce : entityConstraints) {
				if(!checkConstraint(objectEqualManager, objectStore, ce)) {
					throw new RuntimeException("Constraint " + ce +" cannot be imposed!");
				}
			}
			
			revise(objectEqualManager, objectStore, dataMap);
			
			// clear marked data
			markedData = new HashMap<>();
		}
		
		return dataMap;
	}
	
	
	private void revise(SymbolicObjectEqualManager objectEqualManager, SymbolicObjectStore objectStore, Map<String, List<ReferenceVariable>> dataMap) {
		// consolidate unique entities, i.e. entity with the same object-reference value for its ID field
		for(String entityName : dataMap.keySet()) {
			
			Map<ReferenceVariable, Object> refVarIdValueMap = new HashMap<>();
			Map<Object, ReferenceVariable> idValueRefVarMap = new HashMap<>();
			
			
			for(ReferenceVariable refVar : dataMap.get(entityName)) {
				String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
				Object idValue = refVar.valueMap().get(idFieldName);
				refVarIdValueMap.put(refVar, idValue);
				idValueRefVarMap.put(idValue, refVar);
			}
			
			Set<ReferenceVariable> equalEntityObjects = new HashSet<>();
			
			for(ReferenceVariable refVar : refVarIdValueMap.keySet()) {
				Object idValue = refVarIdValueMap.get(refVar);
				if(idValue instanceof ReferenceVariable) {
					Set<ReferenceVariable> equalObjects = objectStore.getEqualObjects(((ReferenceVariable)idValue).getObjectId());
					if(equalObjects != null) {
						for(ReferenceVariable equalIdObj : equalObjects) {
							Set<ReferenceVariable> equalIDObjects = objectStore.getEqualObjects(equalIdObj.getObjectId());
							for(ReferenceVariable o : equalIDObjects) {
								ReferenceVariable equalEntity = idValueRefVarMap.get(o);
								equalEntityObjects.add(equalEntity);
							}
						}
					}
				}
			}
			
			ReferenceVariable[] equalEntityObjectsArr = equalEntityObjects.toArray(new ReferenceVariable[equalEntityObjects.size()]);
			for(int i=0; i<equalEntityObjectsArr.length-1; i++) {
				for(int j=i+1; j<equalEntityObjectsArr.length; j++) {
					ReferenceVariable o1 = equalEntityObjectsArr[i];
					ReferenceVariable o2 = equalEntityObjectsArr[j];
					System.out.println(" -> compare: " + o1 + "   <->  " + o2);
					if(objectEqualManager.canBeEqual(o1, o2)) {
						objectStore.addObjectsEqual(o1, o2);
					} else {
						throw new IllegalArgumentException("cannot set equal entity objects");
					}
				}
			}
		}
	}

	protected boolean checkConstraint(SymbolicObjectEqualManager objectEqualManager, SymbolicObjectStore objectStore, EntityConstraintExpression constraintExpression) {
		if(constraintExpression.getConstraint() instanceof ReferenceVariablesEqualConstraint) {
			ReferenceVariablesEqualConstraint r = (ReferenceVariablesEqualConstraint)constraintExpression.getConstraint();
			IObjectreference o1 = r.getObjectReference1();
			IObjectreference o2 = r.getObjectReference2();
			if(objectEqualManager.canBeEqual(o1, o2)) {
				objectStore.addObjectsEqual((ReferenceVariable)o1, (ReferenceVariable)o2);
				return true;
			}
		}
		
		return false;
	}
	
	
	
	
	private List<EntityConstraintExpression> getExpectedResultRestrictions(ReferenceVariable expectedResultEntry, Map<String, Set<ReferenceVariable>> markedData) {
		List<EntityConstraintExpression> constraints = new ArrayList<>();
		String entityName = expectedResultEntry.getObjectType();
		for(ReferenceVariable markedEntity : markedData.get(entityName)) {
			for(String fieldName : expectedResultEntry.valueMap().keySet()) {
				Object o1 = expectedResultEntry.valueMap().get(fieldName);
				Object o2 = markedEntity.valueMap().get(fieldName);
				EntityFieldValueEqualConstraint constraint = new EntityFieldValueEqualConstraint(o1, o2);
				constraints.add(new EntityConstraintExpression(entityName, constraint));
			}
		}
		return constraints;
	}
	
	
	
	

	protected void doAlgorithm2(List<ReferenceVariable> resultElements, MugglCriteriaQuery<?> query, SolverManager solverManager) {
		ChocoSolverManager chocoManager = (ChocoSolverManager)solverManager;
		
		
		Map<String, List<ReferenceVariable>> dataMap = new HashMap<>();
		Map<String, Set<ReferenceVariable>> markedData = new HashMap<>();
		
		List<EntityConstraintExpression> entityConstraints = new ArrayList<>();
		
		for(ReferenceVariable e : resultElements) {
			
			// perform the join operations
			for(MugglRoot<?> root : query.getRootSet()) {
				for(MugglJoin<?,?> join : root.getJoinSet()) {
					doAlgorithmJoin(join, dataMap, markedData);
				}
			}		
					
			// add the query restrictions
			List<EntityConstraintExpression> restrictionConstraints = getQueryRestrictionConstraints(markedData, query);
			
			// add global restrictions
//			List<EntityConstraintExpression> globalConstraints = getGlobalConstraints(dataMap);
						
			// ----------------------------------------------------------
			// print data (TEST)
			for(String s : dataMap.keySet()) {
				System.out.println("Entity: " + s);
				for(ReferenceVariable r : dataMap.get(s)) {
					boolean marked = markedData.get(r.getObjectType()).contains(r);
					System.out.print("   " + r.getObjectId() + " -> marked=" + marked + "\t\t");
					Map<String, Object> v = r.valueMap();
					for(String f : v.keySet()) {
						System.out.print("\t\t >>"+f+"<< ="+v.get(f)+"\t");
					}
					System.out.print("\n");
				}
			}
			
			System.out.println("Query restirctions: " + restrictionConstraints.size());
			for(EntityConstraintExpression ce : restrictionConstraints) {
				System.out.println("   " + ce);
				entityConstraints.add(ce);
//				solverManager.addConstraint(ce);
			}
//			System.out.println("Global restirctions: " + globalConstraints.size());
//			for(EntityConstraintExpression ce : globalConstraints) {
//				System.out.println("   " + ce);
//				entityConstraints.add(ce);
////				solverManager.addConstraint(ce);
//			}
			// ----------------------------------------------------------
			
			
			// check satisfiability
			try {
//				boolean satisfiable = isSatisfiable(e, dataMap, markedData, query, solverManager);
				chocoManager.checkDBConstraints(entityConstraints);
				boolean satisfiable = solverManager.hasSolution();
				System.out.println("is satisfiable: " + satisfiable);
			} catch (TimeoutException | SolverUnableToDecideException e1) {
				e1.printStackTrace();
			}
			
			
			try {
				solverManager.getSolution();
			} catch (TimeoutException | SolverUnableToDecideException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			/*
			
			try {
				solverManager.getSolution();
			} catch (TimeoutException | SolverUnableToDecideException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			NumericVariable league = new NumericVariable("league", Expression.INT);
			
			NumericVariable lid1 = new NumericVariable("LID1", Expression.INT);
			NumericVariable lid2 = new NumericVariable("LID2", Expression.INT);
			ConstraintExpression lid1_EQ_league = NumericEqual.newInstance(lid1, league);
			ConstraintExpression lid2_EQ_league = NumericEqual.newInstance(lid2, league);
			try {
				solverManager.addConstraints(lid1_EQ_league, lid2_EQ_league);
				boolean satisfiable = solverManager.hasSolution();
				Solution s = solverManager.getSolution();
				System.out.println("is satisfiable: " + satisfiable);
			} catch (TimeoutException | SolverUnableToDecideException e1) {
				e1.printStackTrace();
			}
			
			Set<Expression> unique = new HashSet<>();
			unique.add(lid1);
			unique.add(lid2);
			ConstraintExpression allDifferent = new AllDifferent(unique);
			
//			ConstraintExpression[] constraints = {lid1_EQ_league, lid2_EQ_league, allDifferent};
//			solverManager.addConstraints(constraints);
			solverManager.addConstraint(allDifferent);
			try {
				boolean satisfiable = solverManager.hasSolution();
				System.out.println("is satisfiable: " + satisfiable);
				if(!satisfiable) {
					solverManager.removeConstraint();
					solverManager.removeConstraint();
					solverManager.addConstraint(allDifferent);
				}
				satisfiable = solverManager.hasSolution();
				System.out.println("is satisfiable: " + satisfiable);
				Solution s = solverManager.getSolution();
			} catch (TimeoutException | SolverUnableToDecideException e1) {
				e1.printStackTrace();
			}
			*/
			
			
			
			
			
			
			
			
			
			
			
			// clear marked data
			markedData = new HashMap<>();
		}
	}
	
	


	private List<EntityConstraintExpression> getQueryRestrictionConstraints(Map<String, Set<ReferenceVariable>> markedData,	MugglCriteriaQuery<?> query) {
		List<EntityConstraintExpression> constraints = new ArrayList<>();
		for(MugglJPAQueryRestriction restriction : query.getWhereSet()) {
			if(restriction instanceof MugglJPAQueryComparisonRestriction) {
				MugglJPAQueryComparisonRestriction comparison = (MugglJPAQueryComparisonRestriction)restriction;
				
				if(comparison.getLeft() instanceof MugglPath) {
					MugglPath<?> path = (MugglPath<?>)comparison.getLeft();
					String attribute = path.getAttributeName();
					String entityName = path.getEntityClassName();
					
					for(ReferenceVariable markedRef : markedData.get(entityName)) {
						Field comparisonField = markedRef.getInitializedClass().getClassFile().getFieldByName(attribute);
						Object comparisonValue = markedRef.getField(comparisonField);
						if(comparisonValue == null) { // if there is not a variable value in the reference, create a new one for comparison
							comparisonValue = generateFieldValue(markedRef.getName()+"."+comparisonField.getName(), comparisonField.getType());
							markedRef.putField(comparisonField, comparisonValue);
						}
						
						if(comparisonValue instanceof ReferenceVariable && comparison.getRight() instanceof ReferenceVariable) {
							ConstraintExpression referenceEqualConstraint = new ReferenceVariablesEqualConstraint((ReferenceVariable)comparisonValue, (ReferenceVariable)comparison.getRight());
							constraints.add(new EntityConstraintExpression(entityName, referenceEqualConstraint));
						} else {
							throw new RuntimeException("Currently, comparsion only supported between two reference variables");
						}
					}
				}
			}
		}
		return constraints;
	}

	private List<EntityConstraintExpression> getGlobalConstraints(Map<String, List<ReferenceVariable>> dataMap) {
		List<EntityConstraintExpression> constraints = new ArrayList<>();
		for(String entityName : dataMap.keySet()) {
			List<ReferenceVariable> data = dataMap.get(entityName);
			if(data.size() > 1) {
				// set uniqueness constraints
				Set<Expression> uniqueValues = new HashSet<>();
				String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
				Field idField = data.get(0).getInitializedClass().getClassFile().getFieldByName(idFieldName);
				for(ReferenceVariable refVar : data) {
					Object idValue = refVar.getField(idField);
					if(idValue == null) {
						idValue = generateFieldValue(refVar.getName()+"."+idField.getName(), idField.getType());
						refVar.putField(idField, idValue);
					}
					uniqueValues.add((Variable)idValue);
				}
				ConstraintExpression ce = imposeAllDifferentConstraint(uniqueValues, idField.getType());
				constraints.add( new EntityConstraintExpression(entityName, ce));
			}
		}
		return constraints;
	}
	
	private ConstraintExpression imposeAllDifferentConstraint(Set<Expression> uniqueValues, String type) {
		ConstraintExpression uniqueConstraint = null;
		if(type.equals("java.lang.Integer") || type.equals("int")) {
			uniqueConstraint = new AllDifferent(uniqueValues);
		} else if(type.equals("java.lang.String")) {
			Set<IObjectreference> objectSet = new HashSet<IObjectreference>();
			for(Expression e : uniqueValues) {
				objectSet.add((IObjectreference)e);
			}
			uniqueConstraint = new AllDifferentObjectReference(objectSet);
		}
		
		if(uniqueConstraint != null) {
			return uniqueConstraint;
		}
			
		throw new RuntimeException("Could not set unique constraint");
	}

	private Variable generateFieldValue(String name, String type) {
		if(type.equals("java.lang.Integer") || type.equals("int")) {
			return new NumericVariable(name, Expression.INT);
		} else {
			Objectref referenceValue = generateObjectref(type);
			return new ReferenceVariable(name, referenceValue, this.vm);
		}
	}
	
	

	protected boolean isSatisfiableOLD(ReferenceVariable resultElement, Map<String, List<ReferenceVariable>> dataMap, Map<String, Set<ReferenceVariable>> markedData, MugglCriteriaQuery<?> query, SolverManager solverManager) throws TimeoutException, SolverUnableToDecideException {
		if(true) {
			return true;
		}
		
		System.out.println("check satisfiability...");
		List<ConstraintExpression> constraintList = new ArrayList<>();
		
		boolean isAggregation = false;
		Set<ReferenceVariable> markedVariables = markedData.get(resultElement.getObjectType());
		if(markedVariables.size() > 1) {
			isAggregation = true;
		}
		
		// first, get the constraints based on the result element
		for(Field field : resultElement.getFields().keySet()) {
			Object fieldValue = resultElement.getField(field);
			if(fieldValue instanceof NumericVariable) {
				// TODO: get the numeric variable constraint
			} else if(fieldValue instanceof ReferenceVariable && ((ReferenceVariable) fieldValue).getInitializedClassName().equals(String.class.getName())) {
				for(ReferenceVariable markedVar : markedVariables) {
					if(markedVar.getField(field) == null) {
						markedVar.putField(field, fieldValue);
					}
				}
			} else if(fieldValue instanceof ReferenceCollectionVariable) {
				// TODO: get the reference collection variable constraint
				
//				ReferenceCollectionVariable collectionVariable = (ReferenceCollectionVariable)fieldValue;
//				ConstraintExpression lengthNotNegative = GreaterOrEqual.newInstance(collectionVariable.getSymbolicLength(), NumericConstant.getZero(Expression.INT));
//				constraintList.add(lengthNotNegative);
//				for(ReferenceVariable markedVar : markedVariables) {
//					Object o = markedVar.getField(field);
//					if(o != null && o instanceof ReferenceCollectionVariable) {
//						ReferenceCollectionVariable oRefCol = (ReferenceCollectionVariable)o;
//						Map<Integer, Object> oEle = oRefCol.getSymbolicArray().getElements();
//					}
//					System.out.println("o: " + o);
//				}
				
			} else if(fieldValue instanceof ReferenceVariable) {
				// TODO: get the string variable constraint
			}
		}
		
		// second, get the constraints based on the query
		List<ConstraintExpression> restrictionConstraints = null;
		for(MugglJPAQueryRestriction restriction : query.getWhereSet()) {
			if(restriction instanceof MugglJPAQueryComparisonRestriction) {
				restrictionConstraints = handleComparisonRestriction((MugglJPAQueryComparisonRestriction)restriction, markedData);
			}
		}
		
		if(restrictionConstraints != null) {
//			for(ConstraintExpression ce : restrictionConstraints) {
//				solverManager.addConstraint(ce);
//				boolean hasSolution = solverManager.hasSolution();
//				if(!hasSolution) {
//					throw new RuntimeException("HERE WE HAVE NO SOLUTION");
//				}
//			}
			
			// check globally, i.e. for all generated entities, e.g. for UNIQUE ID FIELDS
			
			// first, check unique values
			for(String entityName : dataMap.keySet()) {
				String idFieldName = "id";
				List<Object> uniqueValues = new ArrayList<>();
				for(ReferenceVariable refVar : dataMap.get(entityName)) {
					Object idFieldValue = refVar.valueMap().get(idFieldName);
					if(idFieldValue != null) {
						uniqueValues.add(idFieldValue);
					}
				}
				System.out.println("UNIQUES: " + uniqueValues);
				Set<Expression> allDifferentSet = new HashSet<>();
				for(Object o : uniqueValues) {
					if(o instanceof ReferenceVariable) {
						allDifferentSet.add(this.objectIdMap.get((ReferenceVariable)o));
					}
				}
				
				
				if(uniqueValues.size() > 1) {
					restrictionConstraints.add(new AllDifferent(allDifferentSet));
					AndList and = new AndList(restrictionConstraints);
					solverManager.addConstraint(and);
					boolean hasSolution = solverManager.hasSolution();
					if(!hasSolution) {
						throw new RuntimeException("HERE WE HAVE NO SOLUTION");
					}
				}
				
//				if(allDifferentSet.size() == 2) {
//					Object[] a = allDifferentSet.toArray();
//					solverManager.addConstraint(NumericNotEqual.newInstance((Term)a[0], (Term)a[1]));
//					boolean b = solverManager.hasSolution();
//					if(!b) {
//						throw new RuntimeException("HERE WE HAVE NO SOLUTION");
//					}
//				}
				
				
//				AllDifferent ad = new AllDifferent(allDifferentSet);
//				solverManager.addConstraint(ad);
//				boolean hasSolution = solverManager.hasSolution();
//				if(!hasSolution) {
//					throw new RuntimeException("HERE WE HAVE NO SOLUTION");
//				}
				
				
			}
		}
		
		System.out.println(" END ");
		
		
		
		
		
		
//		try {
//			NumericVariable nv = new NumericVariable("foobar", Expression.INT);
//			
//			solverManager.addConstraint(NumericEqual.newInstance(
//					nv, 
//					NumericConstant.getZero(Expression.INT)));
//			boolean b1 = solverManager.hasSolution();
//			System.out.println("b1 = " + b1);
//			
//			solverManager.addConstraint(NumericNotEqual.newInstance(
//					nv, 
//					NumericConstant.getZero(Expression.INT)));
//			boolean b2 = solverManager.hasSolution();
//			System.out.println("b2 = " + b2);
//		} catch (TimeoutException | SolverUnableToDecideException e) {
//			e.printStackTrace();
//		}
		
		
		
		
		
		return solverManager.hasSolution();
	}
	
	
	protected void addObjectToMap(ReferenceVariable refVar) {
//		int hash = getNextCounterNumber();
		NumericVariable nv = new NumericVariable(refVar.getName()+".GENERATEDHASH", Expression.INT);
		this.objectIdMap.put(refVar, nv);
//		return NumericEqual.newInstance(nv, NumericConstant.getInstance(hash, Expression.INT)); 
	}
	
//	private int getNextCounterNumber() {
//		return this.counter++;
//	}

	private List<ConstraintExpression> handleComparisonRestriction(MugglJPAQueryComparisonRestriction restriction, Map<String, Set<ReferenceVariable>> markedData) {
		List<ConstraintExpression> constraintList = new ArrayList<>();
		if(restriction.getLeft() instanceof MugglPath) {
			MugglPath<?> path = (MugglPath<?>)restriction.getLeft();
			String attribute = path.getAttributeName();
			String entityName = path.getEntityClassName();
			
			for(ReferenceVariable markedRef : markedData.get(entityName)) {
				Field comparisonField = markedRef.getInitializedClass().getClassFile().getFieldByName(attribute);
				Object comparisonValue = markedRef.getField(comparisonField);
				if(comparisonValue == null) { // if there is not a variable value in the reference, create a new one for comparison
					comparisonValue = generateFieldValue(markedRef.getName()+"."+comparisonField.getName(), comparisonField.getType());
					markedRef.putField(comparisonField, comparisonValue);
				}
				
				if(comparisonValue instanceof ReferenceVariable && restriction.getRight() instanceof ReferenceVariable) {
					if(this.objectIdMap.get(comparisonValue) == null) {
						addObjectToMap((ReferenceVariable)comparisonValue);
//						ConstraintExpression ce = addObjectToMap((ReferenceVariable)comparisonValue);
//						constraintList.add(ce);
					}
					if(this.objectIdMap.get(restriction.getRight()) == null) {
						addObjectToMap((ReferenceVariable)restriction.getRight());
//						ConstraintExpression ce = addObjectToMap((ReferenceVariable)restriction.getRight());
//						constraintList.add(ce);
					}
					NumericVariable nv1 = this.objectIdMap.get(comparisonValue);
					NumericVariable nv2 = this.objectIdMap.get(restriction.getRight());
					if(restriction.getComparisonOperator() == ComparisonOperator.EQ) {
						ConstraintExpression ce = NumericEqual.newInstance(nv1, nv2);
						constraintList.add(ce);
					} else if(restriction.getComparisonOperator() == ComparisonOperator.NEQ) {
						ConstraintExpression ce = NumericNotEqual.newInstance(nv1, nv2);
						constraintList.add(ce);
					} else {
						throw new RuntimeException("Cannot compare on " + restriction.getComparisonOperator() + " for reference variables..");
					}
				}
			}
		} else {
			throw new RuntimeException("This comparsion is not supported yet");
		}
		
		return constraintList;
	}



	protected boolean isSatisfiableOLD(ReferenceVariable resultElement, Map<String, List<ReferenceVariable>> dataMap, Map<String, Set<ReferenceVariable>> markedData, MugglCriteriaQuery<?> query) {
		// first, set elements of resultElement to the one in the marked data
		Set<ReferenceVariable> markedResultTypes = markedData.get(resultElement.getObjectType());
		
		for(ReferenceVariable markedRefVar : markedResultTypes) {
			for(Field field : resultElement.getFields().keySet()) {
				Object resObj = resultElement.getField(field);
				Object genObj = markedRefVar.getField(field);
				if(genObj != null && genObj instanceof ReferenceCollectionVariable) {
					ReferenceCollectionVariable refColVar = (ReferenceCollectionVariable)genObj;
					Map<Integer, Object> elements = refColVar.getSymbolicArray().getElements();
					Map<Integer, Object> elements2 = ((ReferenceCollectionVariable)resObj).getSymbolicArray().getElements();
					System.out.println("elements.value = " + elements);
					System.out.println("elements2.value = " + elements2);
					System.out.println("----");
				}
				markedRefVar.putField(field, resultElement.getField(field));
			}
		}
		
		
		
		return false;
	}
	
	protected void doAlgorithmJoin(MugglJoin<?,?> join, Map<String, List<ReferenceVariable>> dataMap, Map<String, Set<ReferenceVariable>> markedMap) {
		
		// source
		ReferenceVariable sourceJoinObject = generateJoinObject(join.getSourceEntityName(), dataMap, markedMap);
		
		// target
		ReferenceVariable targetJoinObject = generateJoinObject(join.getTargetEntityName(), dataMap, markedMap);
		
		// perform the join
		performJoin(sourceJoinObject, targetJoinObject, join);
		
		System.out.println("JOIN NOW");
		
		// next joins...
		for(MugglJoin<?,?> j : join.getJoinSet()) {
			doAlgorithmJoin(j, dataMap, markedMap);
		}
		
		
	}
	
	private void performJoin(ReferenceVariable sourceJoinObject, ReferenceVariable targetJoinObject, MugglJoin<?,?> join) {
		Field joinField = sourceJoinObject.getInitializedClass().getClassFile().getFieldByName(join.getJoinAttributeName());
		Object joinValue = sourceJoinObject.getField(joinField);
		if(join.getMugglJoinType() == JoinType.MANY_TO_MANY || join.getMugglJoinType() == JoinType.ONE_TO_MANY) {
			// x-n relationship -> join attribute is a collection
			if(joinValue == null) {
				if(joinField.getType().equals(Collection.class.getName())) {
					try {
						joinValue = symObjGenerator.getSymbolicObjectReference(this.vm, sourceJoinObject.getName(), joinField, false);
					} catch (SymbolicObjectGenerationException e) {
						throw new RuntimeException("Could not generate collection variable", e);
					}
				} else {
					throw new RuntimeException("Currently only java.lang.Collection supported in n-x relationship join");
				}
			}
			
			if(joinValue instanceof ReferenceCollectionVariable) {
				ReferenceCollectionVariable colVar = (ReferenceCollectionVariable)joinValue;
				colVar.add(targetJoinObject);
				sourceJoinObject.putField(joinField, colVar);
			} else {
				throw new RuntimeException("Currently only java.lang.Collection supported in n-x relationship join");
			}
			
		} else {
			// x-1 relationship -> join attribute is single attribute
			if(joinValue != null) {
				throw new RuntimeException("There is already a value in the join object!");
			}
			sourceJoinObject.putField(joinField, targetJoinObject);
		}
	}
	
	private ReferenceVariable generateJoinObject(
			String entityName,
			Map<String, List<ReferenceVariable>> dataMap,
			Map<String, Set<ReferenceVariable>> markedMap) {
		ReferenceVariable joinObject = null;
		List<ReferenceVariable> sourceData = dataMap.get(entityName);
		if(sourceData == null || sourceData.size() == 0) {
			// no source data available...
			
			joinObject = createNewMarkedDataObject(dataMap, markedMap, entityName);
		} else {
			// there is already data available...
			
			// check if the data is marked
			for(ReferenceVariable refVar : sourceData) {
				Set<ReferenceVariable> markedData = markedMap.get(refVar.getObjectType());
				if(markedData != null && markedData.size() > 0) {
					if(markedData.contains(refVar)) {
						// we have already a marked data for the join object
						joinObject = refVar;
						break;
					}
				} else {
					// there is no marked data, create a new object
					joinObject = createNewMarkedDataObject(dataMap, markedMap, entityName);
					break;
				}
			}
		}
		return joinObject;
	}

	private ReferenceVariable createNewMarkedDataObject(
			Map<String, List<ReferenceVariable>> dataMap,
			Map<String, Set<ReferenceVariable>> markedMap, 
			String entityName) {
		ReferenceVariable joinObject = generateObjectref(entityName); // generate new object
		List<ReferenceVariable> data = dataMap.get(entityName);
		if(data == null) {
			data = new ArrayList<>(); // create a new list to add the new object to
		}
		data.add(joinObject); // add the new object to the list
		dataMap.put(entityName, data); // add list to dataMap
		
		// mark the new source object as 'in progress'
		Set<ReferenceVariable> markedData = markedMap.get(entityName);
		if(markedData == null) {
			markedData = new HashSet<>();
		}
		markedData.add(joinObject);
		markedMap.put(entityName, markedData);
		
		return joinObject;
	}
	
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
	public void handleJoin(MugglJoin<?,?> join) {
		System.out.println("JOIN: " + join);
		for(MugglJoin<?,?> j : join.getJoinSet()) {
			handleJoin(j);
		}
	}
	

	public void generateEntitySelectDataOLD(ReferenceQueryResultArrayListVariable symQryResList, Solution solution) {
		// get the elements that are currently in the result list
		List<ReferenceVariable> resultElements = getSymbolicElementsFromField(symQryResList, solution);
		
		// get the expected length of the result list
		int length = ((NumericConstant)solution.getValue(symQryResList.getSymbolicLength())).getIntValue();
		
		// check that there are not more elements in the result list as expected
		if(resultElements.size() > length) {
			throw new RuntimeException("No solution found for query result list with more elements ("+resultElements.size()+" than expected ("+length+")");
		}
		
		// add missing elements in the result list
		for(int i=resultElements.size(); i<length; i++) {
			ReferenceVariable plainEntity = generateObjectref(symQryResList.getResultEntityName());
			resultElements.add(plainEntity);
		}
		
		// for each element in the result list: add missing collection elements
		for(ReferenceVariable refVar : resultElements) {
			fillMissingElementsInLists(refVar, solution);
		}
		
		Map<String, List<ReferenceVariable>> entityData = new HashMap<>();
		
		List<ReferenceVariable> data = null;
		
		for(ReferenceVariable refVar : resultElements) {
			data = entityData.get(refVar.getObjectType());
			if(data == null) {
				data = new ArrayList<>();
			}
			data.add(refVar);
			entityData.put(refVar.getObjectType(), data);

			Map<String, Object> values = refVar.valueMap();
			for(String fieldName : values.keySet()) {
				Object value = values.get(fieldName);
				if(value instanceof ReferenceCollectionVariable) {
					ReferenceCollectionVariable refCollVar = (ReferenceCollectionVariable)value;
					data = entityData.get(refCollVar.getCollectionType());
					if(data == null) {
						data = new ArrayList<>();
					}
					int l = ((NumericConstant)solution.getValue(refCollVar.getSymbolicArray().getSymbolicLength())).getIntValue();
					for(int i=0; i<l; i++) {
						Object element = refCollVar.getSymbolicArray().getElement(i);
						if(element instanceof ReferenceVariable) {
							data.add((ReferenceVariable)element);
						}
					}
					entityData.put(refCollVar.getCollectionType(), data);
				}
			}
		}
		
		// try to build up the joins for each element, such that the query is satisfied
		for(String entityName : entityData.keySet()) {
			System.out.println("Entity: "+ entityName);
			for(ReferenceVariable refVar : entityData.get(entityName)) {
				System.out.println("    name=" + refVar.getName() + ", type=" + refVar.getObjectType());
			}
		}
		
		System.out.println("----------------------------");
		
		for(ReferenceVariable refVar : resultElements) {
			satisfyJoins(refVar, symQryResList.getCriteriaQuery(), entityData);
		}
		
		
		
		
		
		for(MugglRoot<?> root: symQryResList.getCriteriaQuery().getRootSet()) {
			System.out.println("from : " + root);
			List<ReferenceVariable> rootData = entityData.get(root.getEntityClassName());
			for(MugglJoin<?,?> join : root.getJoinSet()) {
				List<ReferenceVariable> joinDataSource = entityData.get(join.getSourceEntityName());
				List<ReferenceVariable> joinDataTarget = entityData.get(join.getTargetEntityName());
				
				for(ReferenceVariable refVar : resultElements) {
					if(refVar.getInitializedClassName().equals(join.getSourceEntityName())) {
						System.out.println("Wait here");
						
					} else if(refVar.getInitializedClassName().equals(join.getTargetEntityName())) {
						System.out.println("Wait here");
					}
				}
				
				System.out.println("Wait here");
			}
		}
	}
	
	private void satisfyJoins(ReferenceVariable resultElement, MugglCriteriaQuery<?> query, Map<String, List<ReferenceVariable>> entityData) {
		for(MugglRoot<?> root: query.getRootSet()) {
			for(MugglJoin<?,?> join : root.getJoinSet()) {
				List<ReferenceVariable> sourceList = entityData.get(join.getSourceEntityName());
				List<ReferenceVariable> targetList = entityData.get(join.getTargetEntityName());
				
				if(join.getSourceEntityName().equals(resultElement.getObjectType())) {
					System.out.println("abc");
				}
			}
		}
	}
	
	@Deprecated
	private Set<MugglJoin<?,?>> getJoinsWithTargetOrSource(String entityName, Set<MugglJoin<?,?>> joinSet) {
		Set<MugglJoin<?,?>> joinsForEntity = new HashSet<>();
		for(MugglJoin<?,?> join : joinSet) {
			if(join.getSourceEntityName().equals(entityName)
			  || join.getTargetEntityName().equals(entityName)) {
				joinsForEntity.add(join);
			}
		}
		return joinsForEntity;
	}

	private void fillMissingElementsInLists(ReferenceVariable refVar, Solution solution) {
		for(Field field : refVar.getFields().keySet()) {
			Object value = refVar.getFields().get(field);
			if(value instanceof ReferenceCollectionVariable) {
				buildReferenceCollectionVariable((ReferenceCollectionVariable)value, solution);
			}
		}
	}

	private void buildReferenceCollectionVariable(ReferenceCollectionVariable colVar, Solution solution) {
		SymbolicArrayref symArray = colVar.getSymbolicArray();
		int length = ((NumericConstant)solution.getValue(symArray.getSymbolicLength())).getIntValue();
		for(int i=0; i<length; i++) {
			Object element = symArray.getElement(i);
			if(element == null) {
				element = generateObjectref(colVar.getCollectionType()); 
			}
			symArray.setElementAt(i, element);
		}
	}

	protected ReferenceVariable generateObjectref(String className) {
		try {
			ClassFile classFile = this.vm.getClassLoader().getClassAsClassFile(className);
			Objectref objectRef = this.vm.getAnObjectref(classFile);
			return new ReferenceVariable(className+"."+UUID.randomUUID().toString(), objectRef, this.vm);
		} catch (ClassFileException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected List<ReferenceVariable> getSymbolicElementsFromField(ReferenceQueryResultArrayListVariable symbolicQueryResultList, Solution solution) {
		SymbolicArrayref symArryData = (SymbolicArrayref)symbolicQueryResultList.getField(symbolicQueryResultList.getInitializedClass().getClassFile().getFieldByName("elementData"));
		List<ReferenceVariable> qryListData = new ArrayList<>();
		if(symArryData != null) {
			int symArrLength = ((IntConstant)solution.getValue(symArryData.getSymbolicLength())).getIntValue();
			for(int i=0; i<symArrLength; i++) {
				Object eleData = symArryData.getElement(i);
				if(eleData != null) {
					if(eleData instanceof ReferenceVariable) {
						qryListData.add((ReferenceVariable) eleData);
					}
				}
			}
		}
		return qryListData;
	}
	
	
}
