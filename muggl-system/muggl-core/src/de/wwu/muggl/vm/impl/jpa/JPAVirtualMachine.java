package de.wwu.muggl.vm.impl.jpa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Level;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmSelectClause;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.VirtualObjectDatabase;
import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.constraint.StaticEntityConstraintManager;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.db.solution.PostExecutionDatabaseSolution;
import de.wwu.muggl.db.solution.PreExecutionDatabaseSolution;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Getfield;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.interfaces.Instruction;
//import de.wwu.muggl.instructions.jpa.JPAIfnonnull;
import de.wwu.muggl.instructions.jpa.JPAInvokeInterface;
import de.wwu.muggl.jpa.FindResult;
import de.wwu.muggl.jpa.criteria.ComparisonOperator;
import de.wwu.muggl.jpa.criteria.MugglJPAEntityPath;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryComparisonRestriction;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.jpa.criteria.MugglJoin;
import de.wwu.muggl.jpa.criteria.MugglRoot;
import de.wwu.muggl.jpa.criteria.predicate.MugglPredicateComparison;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.choco.ChocoSolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.AllDifferentString;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.expressions.array.IArrayref;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.symbolic.jpa.MugglEntityManager;
import de.wwu.muggl.symbolic.jpa.MugglUserTransaction;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.testCases.jpa.db.GeneratorRequiredEntityText;
import de.wwu.muggl.symbolic.testCases.jpa.db.MethodParameterTextGenerator;
import de.wwu.muggl.symbolic.testCases.jpa.db.PostDatabaseStateTextGenerator;
import de.wwu.muggl.symbolic.testCases.jpa.db.RequiredDatabaseStateTextGenerator;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.db.constraints.JPAStaticEntityConstraintManager;
import de.wwu.muggl.vm.impl.jpa.dbgen.DBObjectGenerator;
import de.wwu.muggl.vm.impl.jpa.dbgen.DBQueryResultDataGenerator;
import de.wwu.muggl.vm.impl.obj.ObjectStore;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceQueryResultArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;
import de.wwu.muggl.vm.var.sym.gen.SymbolicQueryResultElementGenerator;

public class JPAVirtualMachine extends SymbolicVirtualMachine {
	
	protected VirtualDatabase vdb;
	
	protected VirtualObjectDatabase vodb;
	
	protected MugglEntityManager mugglEntityManager;
	
	protected ObjectStore objectStore;
	
	
	protected Set<Map<QLStatement<?>,NumericVariable>> countSingleResultSet;
	
	
	
//	protected Map<String, Objectref> objectStore;
	
	// the bean constant pool
	// currently: for each bean (key=bean-name) there is exactly one instance in the pool (no need for more currently)
	protected Map<String, Objectref> beanInstancePool;

	public JPAVirtualMachine(Application application,
			MugglClassLoader classLoader, ClassFile classFile,
			Method initialMethod) throws InitializationException {
		super(application, classLoader, classFile, initialMethod);
		this.vdb = new VirtualDatabase();
		this.vodb = new VirtualObjectDatabase();
		this.mugglEntityManager = new MugglEntityManager(this);
		this.beanInstancePool = new HashMap<>();
		this.countSingleResultSet = new HashSet<>(); 
//		this.objectStore = new HashMap<>();
		this.objectStore = new ObjectStore();
	}
	
	public void addCountSingleResult(QLStatement<?> qlStatement, NumericVariable countNumber) {
		Map<QLStatement<?>,NumericVariable> map = new HashMap<>();
		map.put(qlStatement, countNumber);
		this.countSingleResultSet.add(map);
	}
	
	public ObjectStore getObjectStore() { 
		return this.objectStore;
	}
	
	public VirtualObjectDatabase getVirtualObjectDatabase() {
		return this.vodb;
	}
	
	public VirtualDatabase getVirtualDatabase() {
		return vdb;
	}
	
	public void setVirtualDatabase(VirtualDatabase virtualDatabase) {
		this.vdb = virtualDatabase;
//		DatabaseInspector.setLastDB(virtualDatabase);
	}
	
	@Override
	protected void executeFrame(boolean allowStepping) throws ExecutionException, InterruptedException,InvalidInstructionInitialisationException {
		super.executeFrame(allowStepping);
	}
	
	
	@Deprecated
	public FindResult getAFindResultObjectref(ClassFile classFile) {
		InitializedClass initializedClass = classFile.getInitializedClass();
		if (initializedClass == null) {
			initializedClass = new InitializedClass(classFile, this);
		}
		Objectref entityObjRef = initializedClass.getANewInstance();

		FindResult findResult = new FindResult(entityObjRef);
		try {
			findResult.fillInitialEntityAttributesWithVariables();
			
			for(Field field : classFile.getFields()) {
//				if(!field.isAccStatic() && field.getType().equals("java.util.Collection")) {
//					String collectionType = getCollectionType(field.getName(), classFile.getName());
//					
//					String name = classFile.getName() + UUID.randomUUID().toString();
//					Objectref newArrayList = getArrayListReference();
//					SymbolicListObjectRef symList = new SymbolicListObjectRef(newArrayList, name, collectionType, this);
//					findResult.putField(classFile.getFieldByName(field.getName()), symList);
//					
////					createArrayListReference(classFile, findResult, field);
//				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not fill fields in entity object.", e);
		}
		return findResult;
	}
	
	private String getCollectionType(String fieldName, String className) {
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			java.lang.reflect.Field clazzField = clazz.getDeclaredField(fieldName);
			java.lang.reflect.Type genericType = ((java.lang.reflect.ParameterizedType)clazzField.getGenericType()).getActualTypeArguments()[0];
			return genericType.toString().substring(6);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Objectref getArrayListReference() throws ClassFileException {
		ClassFile objectClassFile = getClassLoader().getClassAsClassFile("java.lang.Object");
		Objectref objectRef = getAnObjectref(objectClassFile);
		Arrayref arrayRef = new Arrayref(objectRef, 0);
		ClassFile collectionClassFile = getClassLoader().getClassAsClassFile("java.util.ArrayList");
		Objectref collectionRef = getAnObjectref(collectionClassFile);
		Field elementData = collectionClassFile.getFieldByName("elementData");
		collectionRef.putField(elementData, arrayRef);
		return collectionRef;
	}

	private void createArrayListReference(ClassFile classFile, FindResult findResult, Field field) throws ClassFileException {
		ClassFile objectClassFile = getClassLoader().getClassAsClassFile("java.lang.Object");
		Objectref objectRef = getAnObjectref(objectClassFile);
		Arrayref arrayRef = new Arrayref(objectRef, 0);
		
		
		ClassFile collectionClassFile = getClassLoader().getClassAsClassFile("java.util.ArrayList");
		Objectref collectionRef = getAnObjectref(collectionClassFile);
		Field elementData = collectionClassFile.getFieldByName("elementData");
//		Field emptyElementDataField = collectionClassFile.getFieldByName("EMPTY_ELEMENTDATA");
//		Object emptyElementDataValue = collectionRef.getInitializedClass().getField(emptyElementDataField);
		collectionRef.putField(elementData, arrayRef);
//		collectionRef.putField(collectionClassFile.getFieldByName("serialVersionUID"), 42);
		findResult.putField(classFile.getFieldByName(field.getName()), collectionRef);
	}
	
	@Override
	protected void executeInstruction(Instruction instruction) throws ExecutionException {
		if(instruction instanceof JPAInvokeInterface) {
//			JPAInvokeInterface jpaInvokeInterface = (JPAInvokeInterface)instruction;
			System.out.println("do the JPA");
			((JPAInvokeInterface)instruction).setSolverManager(solverManager);
		}
		
		//((HashSet<?>)this.vodb.getPreExecutionRequiredData().values().toArray()[0]).toArray();

		
		
		
//		if(instruction instanceof JPAIfnonnull) {
//			JPAIfnonnull jpaIfNonNull = (JPAIfnonnull)instruction;
//			jpaIfNonNull.setSolverManager(solverManager);
//		}
		super.executeInstruction(instruction);
	}
	
//	public void generateNewJPAEntityManagerFindChoicePoint(JPAInvokeInterface instruction, ConstraintExpression expression) throws SymbolicExecutionException {
//		this.searchAlgorithm.generateNewEntityManagerFindChoicePoint(this, instruction, expression);
//	}
	

	public void generateFindCP(JPAInvokeInterface instruction, String entityName, Variable idVariable) {
		this.searchAlgorithm.generateFindCP(this, instruction, entityName, idVariable);
	}
	
	public void generateResultListCP(JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery) {
		this.searchAlgorithm.generateResultListCP(this, instruction, criteriaQuery);
	}
	
//	public void generateResultListCP(JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery) {
//		this.searchAlgorithm.generateResultListCP(this, instruction, criteriaQuery);
//	}
	
	private void printReferenceVariable(ReferenceVariable refVar, String indent) {
		for(String d : refVar.valueMap().keySet()) {
			Object o = refVar.valueMap().get(d);
			System.out.println(indent + " > " + d + " = " + o);
			if(o instanceof ReferenceVariable) {
				printReferenceVariable((ReferenceVariable)o, indent+"   ");
			}
			else if(o instanceof SymbolicArrayref) {
				printArrayRef((SymbolicArrayref)o, indent+"   ");
			}
		}
	}
	
	private void printArrayRef(SymbolicArrayref arrRef, String indent) {
		System.out.println(indent+"  element size in this array: " + arrRef.getElements().size());
		for(Integer i : arrRef.getElements().keySet()) {
			System.out.println(indent + "  element #"+i);
			Object d = arrRef.getElements().get(i);
			if(d instanceof ReferenceVariable) {
				printReferenceVariable((ReferenceVariable)d, indent+ "     ");
			} else {
				System.out.println( indent+ "     " + d);
			}
		}
	}
	
	/**
	 * Since the execution came to an end, save the found solution.
	 */
	protected void saveSolution() {
		System.out.println("** save solution **");
		// Reset the instruction counter.
		this.instructionsExecutedSinceLastSolution = 0;

		
//		for(String entityName : this.vodb.getPreExecutionRequiredData().keySet()) {
//			System.out.println("Entity = " + entityName);
//			for(DatabaseObject dbObje : this.vodb.getPreExecutionRequiredData().get(entityName)) {
//				for(String fieldName : dbObje.valueMap().keySet()) {
//					Object data = dbObje.valueMap().get(fieldName);
//					System.out.println("  > " + fieldName + " = " + data);
//					if(data instanceof ReferenceVariable) {
//						ReferenceVariable refData = (ReferenceVariable)data;
//						printReferenceVariable(refData, "     ");
//					}
//				}
//			}
//		}
		
		// Generate an expression from the solutions.
		try {
			// Check if there would be a return value.
			Object returnValue;
			if (this.hasAReturnValue || this.threwAnUncaughtException) {
				returnValue = this.returnedObject;
			} else {
				returnValue = new UndefinedValue();
			}
						
			// before the solverManager generates a solution -> add all entity constraints
//			if(this.solverManager instanceof JaCoPSolverManager) {
//				JaCoPSolverManager jacopManager = (JaCoPSolverManager)this.solverManager;
////				jacopManager.getSolution();
//			}
//			for(String entity : this.currentFrame.getConstraintManager().getEntitiesVariableMap().keySet()) {
//				System.out.println("Entity: " + entity);
//				for(String field : this.currentFrame.getConstraintManager().getEntitiesVariableMap().get(entity).keySet()) {
//					Variable variable = this.currentFrame.getConstraintManager().getEntitiesVariableMap().get(entity).get(field);
//					System.out.println("    field=" + field + ", with var=" + variable);
//				}
//			}
			
			// add static entity constraints
			JPAStaticEntityConstraintManager.addConstraints(this, this.vdb, this.solverManager);			
			
			// test
			boolean b = false;
			if(b) {
				NumericVariable t1_ED = new NumericVariable("t1_ED", Expression.INT);
				NumericVariable t2_ED = new NumericVariable("t2_ED", Expression.INT);
				NumericVariable t3_ED = new NumericVariable("t3_ED", Expression.INT);
				NumericVariable t1 = new NumericVariable("t1", Expression.INT);
				NumericVariable t2 = new NumericVariable("t2", Expression.INT);
				NumericVariable t3 = new NumericVariable("t3", Expression.INT);
				ConstraintExpression ce1 = GreaterThan.newInstance(t1_ED, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce2 = GreaterThan.newInstance(t2_ED, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce3 = GreaterThan.newInstance(t3_ED, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce4 = GreaterThan.newInstance(t1, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce5 = GreaterThan.newInstance(t2, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce6 = GreaterThan.newInstance(t3, NumericConstant.getOne(Expression.INT));
				ConstraintExpression ce7 = NumericEqual.newInstance(t1_ED, t1);
				ConstraintExpression ce8 = NumericEqual.newInstance(t2_ED, t2);
				ConstraintExpression ce9 = NumericEqual.newInstance(t3_ED, t3);
				this.solverManager.addConstraint(ce1);
				this.solverManager.addConstraint(ce2);
				this.solverManager.addConstraint(ce3);
				this.solverManager.addConstraint(ce4);
				this.solverManager.addConstraint(ce5);
				this.solverManager.addConstraint(ce6);
				this.solverManager.addConstraint(ce7);
				this.solverManager.addConstraint(ce8);
				this.solverManager.addConstraint(ce9);
			}
			// end test
			
			
			
			
			
			
			// Add static Database Constraints
			StaticEntityConstraintManager.addStaticEntityConstraints(this.mugglEntityManager.getMetamodel(), this.solverManager, this.vodb);
						
			DBObjectGenerator dbObjGenerator = new DBObjectGenerator(this);
			
			
		
			
			
			// SYMBOLIC QUERY RESULT ----------------------------------
			for(ISymbolicResultList symbolicResult : this.vodb.getSymbolicQueryResultLists()) {
				SymbolicQueryResultList res = (SymbolicQueryResultList)symbolicResult;
				
//				this.solverManager.addConstraint(GreaterOrEqual.newInstance(res.getSymbolicLength(), NumericConstant.getInstance(2, Expression.INT)));
//				if(!this.solverManager.hasSolution()) {
//					this.solverManager.removeConstraint();
//				}				

				for(Objectref eo : res.getResultList()) {
					System.out.println(eo.getName());
					Map<String, Object> valueMap = eo.valueMap();
					for(String k :valueMap.keySet()) {
						System.out.println("    " + k +  " = " + valueMap.get(k));
					}
				}
				System.out.println(symbolicResult);
			}
			// ------------------------------------------------------------------------------------------
			
			// Add the solutions.
			Solution solution = null;
			if(this.countSingleResultSet.size() > 0) {
				int l1 = this.solverManager.getConstraintLevel();
				this.solverManager.addConstraint(GreaterThan.newInstance(new NumericVariable("a", Expression.INT), new NumericVariable("b", Expression.INT)));
				solution = this.solverManager.getSolution();
				this.solverManager.resetConstraintLevel(l1);
			}
			
			this.solverManager.hasSolution();
			solution = this.solverManager.getSolution();
			
			
			boolean changed = false;
			int levelBeforeSingleResult = this.solverManager.getConstraintLevel();
			for(Map<QLStatement<?>, NumericVariable> map : this.countSingleResultSet) {
				for(QLStatement<?> qlStatement : map.keySet()) {
					SymbolicQueryResultElementGenerator generator = new SymbolicQueryResultElementGenerator(this, qlStatement);
					NumericVariable nv = map.get(qlStatement);
					int countSize = ((IntConstant)solution.getValue(nv)).getIntValue();
					int alreadyExisting = getAlreadyExistingCount(qlStatement);
					for(int i=0; i<(countSize-alreadyExisting); i++) {
						generator.generateElements();
						changed = true;
					}
				}
			}
			
			// add uniqueness constraints
			for(String entityName : this.vodb.getPreExecutionRequiredData().keySet()) {
				Set<Object> uniqueSet = new HashSet<>();
				for(DatabaseObject dbObj : this.vodb.getPreExecutionRequiredData().get(entityName)) {
					if(dbObj instanceof EntityObjectref) {
						EntityObjectref e = (EntityObjectref)dbObj;
						ClassFile entityClassFile = e.getInitializedClass().getClassFile();
						if(!isGeneratedIdAttribute(entityClassFile)) {
							uniqueSet.add(e.getField(getIdField(entityClassFile)));
						}
					}
				}
				if(uniqueSet.size() > 1) {
					Object o = uniqueSet.iterator().next();
					if(o instanceof NumericVariable) {
						Set<Expression> uniqueSetExpression = new HashSet<>();
						for(Object x : uniqueSet) {
							uniqueSetExpression.add((Expression)x);
						}
						AllDifferent ad = new AllDifferent(uniqueSetExpression);
						this.getSolverManager().addConstraint(ad);
					}
					
					if(o instanceof EntityStringObjectref) {
						IArrayref[] stringArrays = new IArrayref[uniqueSet.size()]; 
						int i=0;
						for(Object x : uniqueSet) {
							EntityStringObjectref eso = (EntityStringObjectref)x;
							Field valueField = eso.getInitializedClass().getClassFile().getFieldByName("value");
							stringArrays[i++] = (IArrayref) eso.getField(valueField);
						}
						AllDifferentString ad = new AllDifferentString(stringArrays);
						this.getSolverManager().addConstraint(ad);
					}
				}
			}
			
			if(changed) {
				solution = this.solverManager.getSolution();
			}
						
			this.vodb.getPreExecutionRequiredData();
			this.vodb.getData();
						
						
			
			// generate database objects for the expected result queries...
//			DBQueryResultDataGenerator qryResGenerator = new DBQueryResultDataGenerator(this);
//			for(SymbolicQueryResultList symQryResList : this.vodb.getSymbolicQueryResultLists()) {
//				Map<String, List<ReferenceVariable>> requiredData = dbObjGenerator.generateEntitySelectData((ReferenceQueryResultArrayListVariable) symQryResList, solution, this.solverManager);
//				
//				if(requiredData != null) {
//					Set<String> addedObjects = new HashSet<>();
//					for(String s : requiredData.keySet()) {
//						System.out.println("Entity: " + s);
//						for(ReferenceVariable r : requiredData.get(s)) {
//							System.out.println("    -> " + r.getObjectId());
//							
//							if(!addedObjects.contains(r.getObjectId())) {
//								this.vodb.addPreExecutionRequiredData(s, r);
//								
//								// set to 'added objects set', i.e. mark that object as 'added' plus all its equal objects too
//								addedObjects.add(r.getObjectId());
//								Set<ReferenceVariable> equalObjects = dbObjGenerator.getObjectStore().getEqualObjects(r.getObjectId());
//								if(equalObjects != null) {
//									for(ReferenceVariable eq : equalObjects) {
//										addedObjects.add(eq.getObjectId());
//									}
//								}
//							}
//						}
//					}
//				}
//			}
			
			
			
			
			
			
			
			/*
			// +++++++++++++++++++++++++++
			// +++ SYMBOLIC QUERY LIST +++
			// +++++++++++++++++++++++++++
			
			EntityReferenceGenerator entityGenerator = new EntityReferenceGenerator(this);
			// add the query result list constraints...
			for(SymbolicQueryResultList symQryResList : this.vodb.getSymbolicQueryResultLists()) {
				NumericVariable symLength = symQryResList.getSymbolicLength();
				int length = ((NumericConstant)solution.getValue(symLength)).getIntValue();
				System.out.println("--> length is: " + length);
				List<DatabaseObject> symbolicElementList = symQryResList.getSymbolicElements();
				if(symbolicElementList.size() < length) {
					// some elements have not been added to the list...
					for(int i=symbolicElementList.size(); i<length; i++) {
						DatabaseObject newElement = entityGenerator.generateNewEntityReference(symQryResList.getResultEntityName());
						symQryResList.addElement(newElement);
					}
				}
				
				// at this point the symbolic list is filled with symbolic elements...
				// we can add all objects to the required data list
				for(DatabaseObject dbObj : symbolicElementList) {
					this.vodb.addPreExecutionRequiredData(dbObj.getObjectType(), dbObj);
				}
				
				// now we apply some restrictions on it...
				for(MugglJPAQueryRestriction restriction : symQryResList.getQueryRestrictions()) {
					if(restriction instanceof MugglJPAQueryComparisonRestriction) {
						// we have a comparison restriction
						// check the type of comparison
						
						MugglJPAQueryComparisonRestriction comparison = (MugglJPAQueryComparisonRestriction)restriction;
						if(comparison.getComparisonOperator().equals(ComparisonOperator.EQ)) {
							// apply an equal restriction
							
							Object left = comparison.getLeft();
							Object right = comparison.getRight();
							
							if(left instanceof MugglJPAEntityPath) {
								MugglJPAEntityPath path = (MugglJPAEntityPath)left;
								String entityName = path.getEntityClassName();
								String attributeName = path.getAttributeName();
								
								for(DatabaseObject dbObj : symbolicElementList) {
									if(dbObj.getObjectType().equals(entityName)) { 
										Object value = dbObj.valueMap().get(attributeName);
										System.out.println("-> make that value="+value + " equals " + right);
									}
								}
								
							}
						}
					}
				}
				

			}
			
			// +++++++++++++++++++++++++++++++
			// +++ SYMBOLIC QUERY LIST END +++
			// +++++++++++++++++++++++++++++++
			*/
			
			
			PreExecutionDatabaseSolution preSolution = new PreExecutionDatabaseSolution(this.vdb, solution);
			PostExecutionDatabaseSolution postSolution = new PostExecutionDatabaseSolution(this.vdb, solution);
			
			RequiredDatabaseStateTextGenerator generator = new RequiredDatabaseStateTextGenerator(this.mugglEntityManager.getMetamodel(), solution, dbObjGenerator.getObjectStore());
			StringBuilder requiredDBDataStringBuilder = new StringBuilder();
			generator.generateRequiredDatabase(requiredDBDataStringBuilder, this.vodb.getPreExecutionRequiredData());

			this.printVODB();
			
			System.out.println("*********** PRE-EXECUTION-REQUIRED *************");
			System.out.println(requiredDBDataStringBuilder.toString());
			
			PostDatabaseStateTextGenerator post_generator = new PostDatabaseStateTextGenerator(this.mugglEntityManager.getMetamodel(), solution, generator.getObjectFieldNameMap(), dbObjGenerator.getObjectStore());
			StringBuilder post_DataStringBuilder = new StringBuilder();
			post_generator.generateRequiredDatabase(post_DataStringBuilder, this.vodb.getData());
			
			System.out.println("*********** POST *************");
			System.out.println(post_DataStringBuilder.toString());
			
			
			try {
				String text = "**** SAVE SOLUTION **** class.hashCode="+this.hashCode() + "\r\n";
			    Files.write(Paths.get("C:/WORK/log/cp.txt"), text.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			

			this.solutionProcessor.addSolution(solution, returnValue, 
					requiredDBDataStringBuilder, post_DataStringBuilder,
					preSolution, postSolution, this.threwAnUncaughtException,
					this.coverage.getCFCoverageMap(), this.coverage.getDUCoverageAsBoolean());
			
			
			// generate method input parameters
			Map<String, String> x = generator.getObjectFieldNameMap();
			Object[] parameters = this.solutionProcessor.getNewestSolution().getParameters(this.getStringCache());
			MethodParameterTextGenerator methodParaTxtGen = new MethodParameterTextGenerator(parameters, solution, generator.getObjectFieldNameMap());
			StringBuilder methodParameterStringBuilder = new StringBuilder();
			methodParaTxtGen.generate(methodParameterStringBuilder);
			
			System.out.println("************** METHOD ARGUMENTS ***************");
			System.out.println(methodParameterStringBuilder);
			
			this.solutionProcessor.getNewestSolution().addMethodArgumentStringBuilder(methodParameterStringBuilder);

		
			// !!!!!!!!!!!!!!!!!!!!!! TEST !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//			this.solverManager.addConstraint(GreaterOrEqual.newInstance(new NumericVariable("AFTERSOLVE", Expression.INT), IntConstant.getInstance(42)));	
//			solution.addBinding(new NumericVariable("NEW_AFTER", Expression.INT), IntConstant.getInstance(32));
			
			if(changed) {
				this.vodb.resetDatabase(levelBeforeSingleResult);
			}
			

			// Commit the coverage of control flow edges and def-use chains.
			this.coverage.commitAllchanges();
		} catch (SolverUnableToDecideException e) {
			// This should not happen as the Constraints already were solvable.
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("Could not generate a Solution as a SolverUnableToDecideException was thrown with the message: "
								+ e.getMessage()
								+ ".\n\nThis should not happen, please check the corresponding source code.");
		} catch (TimeoutException e) {
			// Log that.
			if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
				Globals.getInst().symbolicExecLogger
						.debug("Could not generate a Solution as a TimeoutException was thrown with the message: "
								+ e.getMessage() + ".");
		}
	}

	
	private int getAlreadyExistingCount(QLStatement<?> qlStatement) {
		SqmWhereClause whereClause = qlStatement.getSqmStatement().getQuerySpec().getWhereClause();
		SqmFromClause fromClause = qlStatement.getSqmStatement().getQuerySpec().getFromClause();
		SqmEntityBinding s = (SqmEntityBinding)fromClause.getFromElementSpaces().get(0).getRoot().getBinding();
		String entityName = s.getBoundNavigable().getEntityName();
		Set<DatabaseObject> dataSet = this.vodb.getData().get(entityName);
		int count = 0;
		if(dataSet != null) {
			for(DatabaseObject dbObj : dataSet) {
				if(satsifiesQueryRestrictions(whereClause, dbObj)) {
					count++;
				}
			}
		}
		return count;
	}
	
	private boolean satsifiesQueryRestrictions(SqmWhereClause whereClause, DatabaseObject dbObj) {
		return satsifiesPredicate(whereClause.getPredicate(), dbObj);
	}
	
	public boolean satsifiesPredicate(SqmPredicate predicate, DatabaseObject dbObj) {
		if(predicate instanceof AndSqmPredicate) {
			return checkAndPredicate((AndSqmPredicate) predicate, dbObj);
		}
		if(predicate instanceof RelationalSqmPredicate) {
			return checkRelationalPredicate((RelationalSqmPredicate)predicate, dbObj);
		}
		return false;
	}
	
	private boolean checkRelationalPredicate(RelationalSqmPredicate predicate, DatabaseObject dbObj) {
		
		return true;
	}
	
	private boolean  checkAndPredicate(AndSqmPredicate predicate, DatabaseObject dbObj) {
		return satsifiesPredicate(predicate.getLeftHandPredicate(), dbObj) && satsifiesPredicate(predicate.getRightHandPredicate(), dbObj);
	}
	

	private void generateEntitySelectData(ReferenceQueryResultArrayListVariable symQryResList, Solution solution) {
		List<ReferenceVariable> elements = getSymbolicElementsFromField(symQryResList, solution);
		int length = ((NumericConstant)solution.getValue(symQryResList.getSymbolicLength())).getIntValue();
		
		if(elements.size() > length) {
			throw new RuntimeException("No solution found for query result list with more elements ("+elements.size()+" than expected ("+length+")");
		}
		
		for(int i=elements.size(); i<length; i++) {
			ReferenceVariable plainEntity = generateEntityObjectref(symQryResList.getResultEntityName());
			elements.add(plainEntity);
		}
		
		for(Field f : elements.get(0).getFields().keySet()) {
			Object v = elements.get(0).getField(f);
			System.out.println("v="+v);
		}
		
		Map<String, List<ReferenceVariable>> dataMap = new HashMap<>();
		dataMap.put(symQryResList.getResultEntityName(), elements);
		
		// FAKE:
		if(elements.size() > 0) {
			Map<String, Integer> entitiesToGenerate = new HashMap<>();
			entitiesToGenerate.put("roster.entity.Player", 2);
			entitiesToGenerate.put("roster.entity.Team", 1);
			entitiesToGenerate.put("roster.entity.League", 1);
			
			generateInitialEntities(dataMap, entitiesToGenerate);
			JoinExpression je1 = new JoinExpression("roster.entity.Player", "roster.entity.Team", "teams", 2, 1); // zwei player im selben team
			JoinExpression je2 = new JoinExpression("roster.entity.Team", "roster.entity.League", "league", 1, 1); // ein einziges team in einer einzigen league
			setJoins(dataMap, je1, je2);
		}
	}
	
	public static class JoinExpression {
		
		protected String leftEntityName;
		protected String rightEntityName;
		protected String joinAttribute;
		protected int leftCount;
		protected int rightCount;
		
		public JoinExpression(String leftEntityName, String rightEntityName, String joinAttribute, int leftCount, int rightCount) {
			this.leftEntityName = leftEntityName;
			this.rightEntityName = rightEntityName;
			this.joinAttribute = joinAttribute;
			this.leftCount = leftCount;
			this.rightCount = rightCount;
		}
	}
	
	
	private void setJoins(Map<String, List<ReferenceVariable>> dataMap, JoinExpression... joins) {
//		for(JoinExpression join : joins) {
//			// TODO: diese logic ist noch nicht ganz richtig
//			
//			List<ReferenceVariable> left = dataMap.get(join.leftEntityName);
//			List<ReferenceVariable> right = dataMap.get(join.rightEntityName);
//			for(int i=0; i<join.leftCount; i++) {
//				ReferenceVariable leftVar = left.get(i);
//				for(int j=0; j<join.rightCount; j++) {
//					ReferenceVariable rightVar = right.get(j);
//					Field joinField = leftVar.getInitializedClass().getClassFile().getFieldByName(join.joinAttribute);
//					leftVar.putField(joinField, rightVar);
//				}
//			}
//		}
//		throw new RuntimeException(" diese logic ist noch nicht ganz richtig");
	}

	private void generateInitialEntities(Map<String, List<ReferenceVariable>> dataMap, Map<String, Integer> entitiesToGenerate) {
		for(String entityName : entitiesToGenerate.keySet()) {
			Integer entityCnt = entitiesToGenerate.get(entityName);
			List<ReferenceVariable> refVarList = dataMap.get(entityName);
			if(refVarList == null) {
				refVarList = new ArrayList<>();
			}
			for(int i=refVarList.size(); i<entityCnt; i++) {
				ReferenceVariable plainEntity = generateEntityObjectref(entityName);
				refVarList.add(plainEntity);
			}
			dataMap.put(entityName, refVarList);
		}
		
		System.out.println("set joins...");
		for(String entityName : entitiesToGenerate.keySet()) {
			System.out.println("entity: " + entityName);
			for(ReferenceVariable refVar : dataMap.get(entityName)) {
				System.out.println("    " + refVar.getName());
			}
		}
	}
	
	

	private void generateQueryData(ReferenceQueryResultArrayListVariable symQryResList, Solution solution) {
		List<ReferenceVariable> elements = getSymbolicElementsFromField(symQryResList, solution);
		int length = ((NumericConstant)solution.getValue(symQryResList.getSymbolicLength())).getIntValue();
		
		if(elements.size() > length) {
			throw new RuntimeException("No solution found for query result list with more elements ("+elements.size()+" than expected ("+length+")");
		}
		
		// first, iterate over all existing objects in the symbolic query result list
		for(int i=0; i<elements.size(); i++) {
			ReferenceVariable dbObj = elements.get(i);
			System.out.println("set joins for: " + dbObj);
			setJoinedObject(dbObj, getJoinsFromQuery(symQryResList));
		}
		
		// second, generate new objects that are not yet in the symbolic query result list
		for(int i=elements.size(); i<length; i++) {
			System.out.println("generate new dbObj");
		}
	}
	
	
	protected void setJoinedObject(ReferenceVariable dbObject, List<MugglJoin<?,?>> joinList) {
		for(MugglJoin join : joinList) {
			ReferenceVariable joinedObject = null;
			if(join.getSourceEntityName().equals(dbObject.getObjectType())) {
				joinedObject = generateEntityObjectref(join.getTargetEntityName());
			} else if(join.getTargetEntityName().equals(dbObject.getObjectType())) {
				joinedObject = generateEntityObjectref(join.getSourceEntityName());
			}
			if(joinedObject != null) {
				Field joinField = dbObject.getInitializedClass().getClassFile().getFieldByName(join.getJoinAttributeName());
				dbObject.putField(joinField, joinedObject);
			}
		}
	}
	
	protected ReferenceVariable generateEntityObjectref(String entityName) {
		try {
			ClassFile entityClassFile = getClassLoader().getClassAsClassFile(entityName);
			Objectref objectRef = getAnObjectref(entityClassFile);
			return new ReferenceVariable(entityName+"."+UUID.randomUUID().toString(), objectRef, this);
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
	
	
	protected List<MugglJoin<?,?>> getJoinsFromQuery(ReferenceQueryResultArrayListVariable symbolicQueryResultList) {
		List<MugglJoin<?,?>> joinList = new ArrayList<>();
		for(MugglRoot<?> root : symbolicQueryResultList.getCriteriaQuery().getRootSet()) {
			for(MugglJoin join : root.getJoinSet()) {
				joinList.add(join);
				getJoinsFromQuery(joinList, join);
			}
		}
		return joinList;
	}
	
	protected List<MugglJoin<?,?>> getJoinsFromQuery(List<MugglJoin<?,?>> joinList, MugglJoin join) {
		for(Object o : join.getJoinSet()) {
			joinList.add((MugglJoin)o);
			getJoinsFromQuery(joinList, (MugglJoin)o);
		}
		return joinList;
	}
	
	
	
	
	
	
	private void generateRequiredQueryData(SymbolicQueryResultList symQryResList, Solution solution) {
		/*EntityReferenceGenerator entityGenerator = new EntityReferenceGenerator(this);
		
		NumericVariable symLength = symQryResList.getSymbolicLength();
		int length = ((NumericConstant)solution.getValue(symLength)).getIntValue();
		System.out.println("Generate database entries to find a result list of size: " + length);
		
		if(length > 0) {
			// some data must be created...

			List<DatabaseObject> symbolicElementList = symQryResList.getSymbolicElements(); // there might be already some elements in the symbolic list
			if(symbolicElementList.size() < length) {
				// some elements have not been added to the list, generate elements and add them...
				for(int i=symbolicElementList.size(); i<length; i++) {
					DatabaseObject newElement = entityGenerator.generateNewEntityReference(symQryResList.getResultEntityName());
					symQryResList.addElement(newElement);
				}
			}
			
			for(MugglJPAQueryRestriction restriction : symQryResList.getQueryRestrictions()) {
				if(restriction instanceof MugglJPAQueryComparisonRestriction) {
					// we have a comparison restriction
					// check the type of comparison
					
					MugglJPAQueryComparisonRestriction comparison = (MugglJPAQueryComparisonRestriction)restriction;
					if(comparison.getComparisonOperator().equals(ComparisonOperator.EQ)) {
						// apply an equal restriction
						
						Object left = comparison.getLeft();
						Object right = comparison.getRight();
						
						if(left instanceof MugglJPAEntityPath && right instanceof Variable) {
							MugglJPAEntityPath path = (MugglJPAEntityPath)left;
							String entityName = path.getEntityClassName();
							String attributeName = path.getAttributeName();
							
							for(DatabaseObject dbObj : symbolicElementList) {
								if(dbObj.getObjectType().equals(entityName)) { 
									Object value = dbObj.valueMap().get(attributeName);
									System.out.println("-> make that value="+value + " equals " + right);
								}
							}
							
						}
					}
				}
			}
		}
		
//		solution.getValue(((ReferenceVariable)((MugglPredicateComparison)symQryResList.getQueryRestrictions().toArray()[0]).getRight()));
//		
//		
//		((ReferenceVariable)((MugglPredicateComparison)symQryResList.getQueryRestrictions().toArray()[0]).getRight())
//		
//		solution.getValue(((SymbolicArrayref)((ReferenceVariable)((MugglPredicateComparison)symQryResList.getQueryRestrictions().toArray()[0]).getRight()).valueMap().get("value")).getSymbolicLength())
//		((SymbolicArrayref)((ReferenceVariable)((MugglPredicateComparison)symQryResList.getQueryRestrictions().toArray()[0]).getRight()).valueMap().get("value")).getSymbolicLength()
//		
//		this.vodb.getData().get(symQryResList.getResultEntityName()).toArray(new Objectref[1]);
//		this.vodb.getPreExecutionRequiredData().get(symQryResList.getResultEntityName());
		
		
		System.out.println("query: " + symQryResList);
		*/
	}
	

	public void generateNewFindResultChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, FindResult findResultObject, int jumpTarget) {
		this.searchAlgorithm.generateNewFindResultChoicePoint(vm, instruction, findResultObject, jumpTarget);
	}
	
	public void generateNewCollectionChoicePoint(GeneralInstructionWithOtherBytes instruction, CollectionVariable<DatabaseObject> collection) {
		this.searchAlgorithm.generateNewCollectionChoicePoint(this,instruction,collection);
	}

	
	
	// entity manager # find method -> choice point
	public void generateEntityManagerFindChoicePoint(JPAInvokeInterface instruction, String entityName, Object idReference)  throws VmRuntimeException {
		this.searchAlgorithm.generateEntityManagerFindChoicePoint(this, instruction, entityName, idReference);
	}
	
	// entity manager # persist method -> choice point
	public void generateEntityManagerPersistChoicePoint(JPAInvokeInterface instruction, Objectref entityObject) throws VmRuntimeException {
		this.searchAlgorithm.generateEntityManagerPersistChoicePoint(this, instruction, entityObject);
	}
	
	// JPA query  # getResultList
	public void generateGetQueryResultListChoicePoint(JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException {
		this.searchAlgorithm.generateGetQueryResultListChoicePoint(this, instruction, qlStatement);
	}

	// JPA query  # getSingleResult
	public void generateGetSingleResultChoicePoint(JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException {
		this.searchAlgorithm.generateGetSingleResultChoicePoint(this, instruction, qlStatement);
	}
	
	// Symbolic Iterator # has more elements
	public void generateSymbolicIterationChoicePoint(Invoke instruction, SymbolicIterator symbolicIterator) {
		this.searchAlgorithm.generateSymbolicIterationChoicePoint(instruction, symbolicIterator);
	}
	
	
	public void generateNewObjectrefIsNullChoicePoint(
			GeneralInstructionWithOtherBytes instruction,
			Objectref objectReference,
			boolean jumpToIsNull) {
		this.searchAlgorithm.generateNewObjectrefIsNullChoicePoint(this, instruction, objectReference, jumpToIsNull);
	}
	
	public void generateNewGetFieldOfNullableObjectChoicePoint(
			Getfield instruction,
			Objectref objectReference)  throws VmRuntimeException {
		this.searchAlgorithm.generateNewGetFieldOfNullableObjectChoicePoint(this, instruction, objectReference);
	}
		
	public MugglEntityManager getMugglEntityManager() {
		return this.mugglEntityManager;
	}
	
	public void generateIteratorHasNextChoicePoint(SymbolicIterator symbolicIterator, GeneralInstructionWithOtherBytes instruction) {
		this.searchAlgorithm.generateIteratorHasNextChoicePoint(this, symbolicIterator, instruction);
	}

	/**
	 * Generate an instance of Objectref for the specified ClassFile. This will invoke the static
	 * initializers, if that has not been done, yet.
	 * 
	 * @param classFile The class file to get an object reference for.
	 * @return A new instance of objectref for this ClassFile.
	 * @throws ExceptionInInitializerError If class initialization fails.
	 */
	public Objectref getAnObjectref(ClassFile classFile) {
		InitializedClass initializedClass = classFile.getInitializedClass();
		if (initializedClass == null) {
			initializedClass = new InitializedClass(classFile, this);
		}
		Objectref objectRef = initializedClass.getANewInstance();
		if(this.objectStore != null) {
			this.objectStore.addObject(objectRef);
		}
		if(initializedClass.getClassFile().getPostConstructMethod() != null) {
			Object[] methodArgs = {objectRef};
			try {
				Frame oldFrame = this.currentFrame;
				Frame frame = createFrame(null, initializedClass.getClassFile().getPostConstructMethod(), methodArgs);
				this.currentFrame = frame;
				try {
					executeFrame(false);
				} catch (InvalidInstructionInitialisationException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.currentFrame = oldFrame;
			} catch(ExecutionException e) {
				throw new RuntimeException("Could not generate an entity object reference of class file: " + classFile + " when executing its @PostConstruct method");
			}
		}
		return objectRef;
	}

	public void addObjectrefToBeanInstancePool(Objectref objectRef) {
		if(this.beanInstancePool != null && this.beanInstancePool.get(objectRef.getObjectType()) == null) {
			this.beanInstancePool.put(objectRef.getObjectType(), objectRef);
		}
	}
	
	public Objectref getBeanFromPool(String type) {
		return this.beanInstancePool.get(type);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private Map<String, ClassFile> springBeanMap = new HashMap<>();
	private Set<String> springBeanNotLoadableMap = new HashSet<>();
	
	public ClassFile getSpringBean(String classTypeName, String configurationName) {

		// try to get it from cache
		ClassFile cl = springBeanMap.get(configurationName);
		if(cl != null) {
			return cl;
		}
		
		if(this.springBeanNotLoadableMap.contains(configurationName)) {
			return null;
		}
		
		try {
			ClassFile c = this.classLoader.getClassAsClassFile(classTypeName);
			Constant[] constantPool = c.getConstantPool();
			for(Attribute attribute : c.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation annotation : attributeAnnotation.getAnnotations()) {
						String annoName = constantPool[annotation.getTypeIndex()].getStringValue();
						if (       annoName.equals("Lorg/springframework/stereotype/Repository;")
								|| annoName.equals("Lorg/springframework/stereotype/Component;")
								|| annoName.equals("Lorg/springframework/stereotype/Service;")) {
							String configName = annotation.getElementValuePairs()[0].getElementValues().getStringValue();
							if(configName.equals(configurationName)) {
								this.springBeanMap.put(configurationName, c);
								return c;
							}
						}
					}
				}
			}
		} catch(Exception e) {
			this.springBeanNotLoadableMap.add(configurationName);
			System.out.println("*** " + configurationName + " not loadable");
			return null;
		}
		
		if(!classTypeName.endsWith("Impl")) {
			ClassFile c = getSpringBean(classTypeName+"Impl", configurationName);
//			this.springBeanMap.put(configurationName, c);
			return c;
		}
		
		this.springBeanNotLoadableMap.add(configurationName);
		return null;
	}

	public MugglUserTransaction getMugglUserTransaction() {
		return new MugglUserTransaction(this);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	public void printVODB() {
		System.out.println("********* PRE-EXECUTION-REQUIRED DATA IN DATA STORE *********** ");
		for(String e : this.vodb.getPreExecutionRequiredData().keySet()) {
			System.out.println("   " + e);
			for(DatabaseObject o : this.vodb.getPreExecutionRequiredData().get(e)) {
				System.out.println("          " + o.getObjectId());
				for(String value : o.valueMap().keySet()) {
					System.out.println("                " + value +" = " + o.valueMap().get(value));
				}
			}
		}
		System.out.println("****************************************************************");
		
		System.out.println("********* APPLICATION DATA (POST-EXPECTED) IN DATA STORE *********** ");
		for(String e : this.vodb.getData().keySet()) {
			System.out.println("   " + e);
			for(DatabaseObject o : this.vodb.getData().get(e)) {
				String x = "<NONE>";
				if(o instanceof EntityObjectref) {
					EntityObjectref r = ((EntityObjectref)o).getRequiredEntity();
					if(r != null) {
						x = r.getObjectId();
					}
				}
				for(String value : o.valueMap().keySet()) {
					System.out.println("                " + value +" = " + o.valueMap().get(value));
				}
				System.out.println("          " + o.getObjectId() + " -> required object: "+ x);
			}
		}
		System.out.println("****************************************************************");
	}
	
	
	
	
	
	
	protected boolean isGeneratedIdAttribute(ClassFile classFile) {		
		// check field annotations
		Field idField = getIdField(classFile);
		for(Attribute attribute : idField.getAttributes()) {
			AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
			for(Annotation anno : attributeAnnotation.getAnnotations()) {
				if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
					return true;
				}
			}
		}
		
		// check method annotations
		Method idMethod = null;
		String idMethodName = "get"+idField.getName();
		for(Method m : classFile.getMethods()) {
			if(m.getName().toUpperCase().equals(idMethodName.toUpperCase())) {
				idMethod = m;
				break;
			}
		}
		if(idMethod != null) {
			for(Attribute attribute : idMethod.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation anno : attributeAnnotation.getAnnotations()) {
						if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	protected Field getIdField(ClassFile entityClassFile) {
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityClassFile.getName());
		return this.getField(entityClassFile, idFieldName);
	}
	
	protected Field getField(ClassFile entityClassFile, String fieldName) {
		try {
			Field field = entityClassFile.getFieldByName(fieldName, true);
			return field;
		} catch(FieldResolutionError e) {
			try {
				if(entityClassFile.getSuperClassFile() != null) {
					return getField(entityClassFile.getSuperClassFile(), fieldName);
				}
			} catch (ClassFileException e1) {
			}
		}
		throw new RuntimeException("ID field for entity class: " + entityClassFile + " not found");
	}

}
