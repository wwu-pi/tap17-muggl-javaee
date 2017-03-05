package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

//import org.hibernate.jpa.criteria.expression.LiteralExpression;
//import org.hibernate.jpa.criteria.path.SingularAttributePath;
//import org.hibernate.jpa.criteria.predicate.BetweenPredicate;
//import org.hibernate.jpa.criteria.predicate.ComparisonPredicate;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.ana.EntityAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.qry.SymbolicComparisonPredicate;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.symbolic.generating.jpa.JPAConstraintManager;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class EMResultListCP implements ChoicePoint {

	protected boolean alreadyVisitedNonJumpingBranch = false;
	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected CriteriaQuery<?> criteriaQuery;
	protected int constraintLevel;
	
	protected JPAVirtualMachine vm;
	protected VirtualDatabase database;
	
	public EMResultListCP(Frame frame, int pc, int pcNext, ChoicePoint parent, CriteriaQuery<?> criteriaQuery) {
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.criteriaQuery = criteriaQuery;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		this.database = this.vm.getVirtualDatabase().getClone();
				
		try {
			createSingleEntryDatabase();
//			createChoicePoint();
		} catch (Exception e) {
			System.out.println("Could not create choice point: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	protected void createChoicePointNEW() {
		Class<?> collectionType = this.criteriaQuery.getResultType();
//		SymbolicResultList<?> symList = new SymbolicResultList<>(collectionType, criteriaQuery);
		
//		try {
//			createDatabaseForSingleEntry();
//		} catch (Exception e) {
//			System.out.println("Could not create required database state");
//			e.printStackTrace();
//		}
		
//		frame.getOperandStack().push(symList);
	}
	
	private void createDatabaseForSingleEntry() throws Exception {
		VirtualDatabase newVDB = this.database.getClone();
		
		for(Root<?> root : this.criteriaQuery.getRoots()) {
			EntityEntry rootEntityEntry = EntityAnalyzer.getInitialEntityEntry(root.getJavaType().getName());
			
			ClassFile entityClassFile = frame.getVm().getClassLoader().getClassAsClassFile(root.getJavaType().getName());
			Objectref rootObjRef = frame.getVm().getAnObjectref(entityClassFile);
			this.database.addRequired(rootObjRef.getObjectId(), rootEntityEntry);
			newVDB.addData(rootObjRef);
		}
		
		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
	}
	
	
	
	protected void createSingleEntryDatabase() throws Exception {
		VirtualDatabase newVDB = this.database.getClone();
		Map<String, Objectref> createdObjectEntityMap = new HashMap<>();
		for(Root<?> root : this.criteriaQuery.getRoots()) {
			String rootClassName = root.getJavaType().getName();
			EntityEntry rootEntityEntry = EntityAnalyzer.getInitialEntityEntry(rootClassName);
			Objectref rootObjectref = getObjectrefByEntityEntry(rootClassName, rootEntityEntry);
			createdObjectEntityMap.put(rootClassName, rootObjectref);
			newVDB.addRequired(rootObjectref.getObjectId(), rootEntityEntry);
			newVDB.addData(rootObjectref);
			for(Join<?,?> join : root.getJoins()) {
				handleJoin(newVDB, createdObjectEntityMap, join, rootObjectref);
			}
		}
		
		String result = criteriaQuery.getResultType().getName();
		Objectref singleObjectElement = createdObjectEntityMap.get(result);
		
		Objectref singleResultListObjRef = getArrayListObjectref();
		addObjectrefToArrayList(singleResultListObjRef, singleObjectElement);
		
		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();

//		imposeQueryRestrictionAsConstraints(createdObjectEntityMap, solverManager);
		
		frame.getOperandStack().push(singleResultListObjRef);
		
		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
	}
	
	protected void handleJoin(VirtualDatabase newVDB, Map<String, Objectref> createdObjectEntityMap, Join<?,?> join, Objectref joiningObject) throws Exception {
		String joinClassName = join.getJavaType().getName();
		EntityEntry joinEntityEntry = EntityAnalyzer.getInitialEntityEntry(joinClassName);
		Objectref joinObjectref = getObjectrefByEntityEntry(joinClassName, joinEntityEntry);
		newVDB.addRequired(joinObjectref.getObjectId(), joinEntityEntry);
		newVDB.addData(joinObjectref);
		addJoiningValue(newVDB, joiningObject, join.getAttribute().getName(), joinObjectref, join.getAttribute().getPersistentAttributeType());
		createdObjectEntityMap.put(joinClassName, joinObjectref);
		for(Join<?,?> nextJoin : join.getJoins()) {
			handleJoin(newVDB, createdObjectEntityMap, nextJoin, joinObjectref);
		}
	}
	
	protected void addJoiningValue(VirtualDatabase newVDB, Objectref joiningObject, String joiningField, Objectref joinedObject, PersistentAttributeType persistentType) {
		Object joinValue = joiningObject.valueMap().get(joiningField);
		if(persistentType == PersistentAttributeType.MANY_TO_MANY
			|| persistentType == PersistentAttributeType.ONE_TO_MANY) {
			// add to a list / collection
			CollectionVariable<DatabaseObject> colVar = (CollectionVariable<DatabaseObject>)joinValue;
			colVar.add(joinedObject);
			EntityEntry entityEntry = newVDB.getRequiredData().get(joiningObject.getObjectId());
			entityEntry.addValue(joiningField, colVar);
			System.out.println("Add to list: "+ joinValue);
		}
		else if(persistentType == PersistentAttributeType.MANY_TO_ONE
				|| persistentType == PersistentAttributeType.ONE_TO_ONE) {
			// add a a singular attribute
			System.out.println("Add as singlular attribute: "+ joinValue);
			for(Field field : joiningObject.getFields().keySet()) {
				if(field.getName().equals(joiningField)) {
					joiningObject.putField(field, joinedObject);
					EntityEntry entityEntry = newVDB.getRequiredData().get(joiningObject.getObjectId());
					entityEntry.addValue(joiningField, joinedObject);
					break;
				}
			}
		}
		else {
			throw new RuntimeException("Could handle the correct persistent type attribute: " + persistentType.name());
		}
	}
	
	
	
	
//	protected void createChoicePoint() throws Exception {
//		VirtualDatabase newVDB = this.database.getClone();
//		Set<Objectref> resultListObjectRefs = new HashSet<Objectref>();
//		for(Root<?> root : this.criteriaQuery.getRoots()) {
//			
//			String rootClassName = root.getJavaType().getName();
//			EntityEntry rootEntityEntry = EntityAnalyzer.getInitialEntityEntry(rootClassName);
//			Objectref rootObjectref = getObjectrefByEntityEntry(rootClassName, rootEntityEntry);
//			
//			newVDB.addRequired(rootObjectref.getObjectId(), rootEntityEntry);
//			newVDB.addData(rootObjectref);
//			resultListObjectRefs.add(rootObjectref);
//			
//			for(Join<?,?> join : root.getJoins()) {
//				String joinClassName = join.getJavaType().getName();				
//				EntityEntry joinEntityEntry = EntityAnalyzer.getInitialEntityEntry(joinClassName);
//				Objectref joinObjectref = getObjectrefByEntityEntry(joinClassName, joinEntityEntry); 
//				
//				newVDB.addRequired(joinObjectref.getObjectId(), joinEntityEntry);
//				newVDB.addData(joinObjectref);
//				resultListObjectRefs.add(joinObjectref);
//				
//				for(Field field : rootObjectref.getFields().keySet()) {
//					if(field.getName().equals(join.getAttribute().getName())) {
//						Object value = rootObjectref.getField(field);
//						if(value instanceof CollectionVariable) {
//							CollectionVariable colVar = (CollectionVariable)value;
//							colVar.add(joinObjectref);
//						}
//					}
//				}
//			}
//		}
//		
//		Objectref resultListObjRef = getArrayListObjectref();
//		for(Objectref element : resultListObjectRefs) {
//			addObjectrefToArrayList(resultListObjRef, element);
//		}
//		
//		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();
//		imposeQueryRestrictionAsConstraints(resultListObjectRefs, solverManager);
//		
//		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
//		
//		frame.getOperandStack().push(resultListObjRef);
//	}
	
//	protected void imposeQueryRestrictionAsConstraints(Map<String, Objectref> resultListObjectRefs, SolverManager solverManager) {
//		Predicate restriction = this.criteriaQuery.getRestriction();
//		if(restriction instanceof SymbolicComparisonPredicate) {
//			imposeSymbolicComparisonRestriction((SymbolicComparisonPredicate)restriction, resultListObjectRefs, solverManager);
//		} 
//		else if(restriction instanceof BetweenPredicate) {
//			imposeBetweenRestriction((BetweenPredicate<?>)restriction, resultListObjectRefs, solverManager);
//		}
//		else if(restriction instanceof ComparisonPredicate) {
//			imposeComparisonRestriction((ComparisonPredicate)restriction, resultListObjectRefs, solverManager);
//		}
//		else {
//			throw new RuntimeException("restriction not handled yet");
//		}
//	}
//	
//	protected void imposeBetweenRestriction(BetweenPredicate<?> restriction, Map<String, Objectref> resultListObjectRefs, SolverManager solverManager) {
//		Expression<?> lower = restriction.getLowerBound();
//		Expression<?> upper = restriction.getUpperBound();
//		Expression<?> expression = restriction.getExpression();
//		if(expression instanceof SingularAttributePath) {
//			SingularAttributePath<?> path = (SingularAttributePath<?>)expression;
//			String attributeName = path.getAttribute().getName();
//			String entityClassName = path.getAttribute().getDeclaringType().getJavaType().getName();
//			Objectref objRef = resultListObjectRefs.get(entityClassName);
//			if(objRef.getInitializedClassName().equals(entityClassName)) {
//				for(Field field : objRef.getFields().keySet()) {
//					if(field.getName().equals(attributeName)) {
//						Object entityValue = objRef.getField(field);
//						
//						if(lower instanceof LiteralExpression && upper instanceof LiteralExpression) {
//							Object lowerObject = ((LiteralExpression<?>)lower).getLiteral();
//							Object upperObject = ((LiteralExpression<?>)upper).getLiteral();
//							if(entityValue instanceof NumericVariable
//								&& lowerObject instanceof NumericVariable
//								&& upperObject instanceof NumericVariable) {
//								ConstraintExpression c1 = GreaterOrEqual.newInstance((NumericVariable)entityValue, (NumericVariable)lowerObject);
//								ConstraintExpression c2 = GreaterOrEqual.newInstance((NumericVariable)upperObject, (NumericVariable)entityValue);
//								solverManager.addConstraint(c1);
//								solverManager.addConstraint(c2);
//							}
//						} else {
//							throw new RuntimeException("object value could not be handled yet to create a restriction");
//						}
//					}
//				}
//			}
//		}
//		
//	}
//	
//	protected void imposeComparisonRestriction(ComparisonPredicate restriction,	Map<String, Objectref> resultListObjectRefs, SolverManager solverManager) {
//		Expression<?> leftExpression = restriction.getLeftHandOperand();
//		Expression<?> rightExpression = restriction.getRightHandOperand();
//		if(leftExpression instanceof SingularAttributePath && rightExpression instanceof LiteralExpression) {
//			SingularAttributePath<?> path = (SingularAttributePath<?>)leftExpression;
//			String attributeName = path.getAttribute().getName();
//			String entityClassName = path.getAttribute().getDeclaringType().getJavaType().getName();
//			Objectref objRef = resultListObjectRefs.get(entityClassName);
//			if(objRef.getInitializedClassName().equals(entityClassName)) {
//				for(Field field : objRef.getFields().keySet()) {
//					if(field.getName().equals(attributeName)) {
//						Object entityValue = objRef.getField(field);
//						LiteralExpression literalExpression = (LiteralExpression)rightExpression;
//						if(entityValue instanceof NumericVariable && literalExpression.getLiteral() instanceof Integer) {
//							int literalValue = (int)literalExpression.getLiteral();
//							NumericConstant constant = NumericConstant.getInstance(literalValue, de.wwu.muggl.solvers.expressions.Expression.INT);
//							ConstraintExpression constraintExpression = NumericEqual.newInstance((NumericVariable)entityValue, constant);
//							solverManager.addConstraint(constraintExpression);
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	protected void imposeSymbolicComparisonRestriction(SymbolicComparisonPredicate restriction, Map<String, Objectref> resultListObjectRefs, SolverManager solverManager) {
//		Expression<?> leftExpression = restriction.getLeftHandOperand();
//		Variable mugglVar = restriction.getMugglVariable();
//		if(leftExpression instanceof SingularAttributePath) {
//			SingularAttributePath<?> path = (SingularAttributePath<?>)leftExpression;
//			String attributeName = path.getAttribute().getName();
//			String entityClassName = path.getAttribute().getDeclaringType().getJavaType().getName();
//			Objectref objRef = resultListObjectRefs.get(entityClassName);
//			if(objRef.getInitializedClassName().equals(entityClassName)) {
//				for(Field field : objRef.getFields().keySet()) {
//					if(field.getName().equals(attributeName)) {
//						Object entityValue = objRef.getField(field);
//						if(entityValue instanceof NumericVariable && mugglVar instanceof NumericVariable) {
//							ConstraintExpression constraintExpression = NumericEqual.newInstance((NumericVariable)mugglVar, (NumericVariable)entityValue);
//							solverManager.addConstraint(constraintExpression);
//						} else {
//							throw new RuntimeException("object value could not be handled yet to create a restriction");
//						}
//					}
//				}
//			}
//		}
//	}
	
	
	
	protected Objectref getObjectrefByEntityEntry(String className, EntityEntry entityEntry) {
		try {
			ClassFile classFile = frame.getVm().getClassLoader().getClassAsClassFile(className);
			Objectref objRef = frame.getVm().getAnObjectref(classFile);
			
			for(Object value : entityEntry.valueMap().values()) {
				if(value instanceof CollectionVariable) {
					CollectionVariable cv = (CollectionVariable)value;
					cv.setParent(objRef);
				}
			}
			
			for(String fieldName : entityEntry.valueMap().keySet()) {
				Object value = entityEntry.valueMap().get(fieldName);
				if(value instanceof Variable) {
					value = (Variable) value;//TODO ((Variable) value).getClone();
				}
				de.wwu.muggl.vm.classfile.structures.Field field = classFile.getFieldByName(fieldName);
				objRef.putField(field, value);
			}
			
			return objRef;
		} catch (ClassFileException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	
	
	
	
	protected void createChoicePointakldflaskdjfas() {
		try {
//			ClassFile classFile = frame.getVm().getClassLoader().getClassAsClassFile("java.util.ArrayList");
//			Objectref objRef = frame.getVm().getAnObjectref(classFile);
//		
//			// create dummy data array
//			String entityClassName = Player.class.getName(); // TODO add class name
//			ClassFile entityClassFile = frame.getVm().getClassLoader().getClassAsClassFile(entityClassName);
//			Objectref entityObjRef1 = frame.getVm().getAnObjectref(entityClassFile);
//			Arrayref elementsArray = new Arrayref(entityObjRef1, 1);
//			elementsArray.putElement(0, entityObjRef1);
//
//			// add dummy data array as filed to the collection
//			objRef.putField(classFile.getFieldByName("elementData"), elementsArray);

			
			
			
			Objectref arrayListObjRef = getArrayListObjectref();
			
			// add dummy data...
//			String entityClassName = PlayerInt.class.getName(); // TODO add class name
//			ClassFile entityClassFile = frame.getVm().getClassLoader().getClassAsClassFile(entityClassName);
			
//			addObjectrefToArrayList(arrayListObjRef, dummyData1);
//			addObjectrefToArrayList(arrayListObjRef, dummyData2);
			
			
			
//			EntityEntry entityObject1 = EntityAnalyzer.getInitialEntityEntry(entityClassName);
//			Objectref dummyData1 = frame.getVm().getAnObjectref(entityClassFile);
//			for(String fieldName : entityObject1.valueMap().keySet()) {
//				Object value = entityObject1.valueMap().get(fieldName);
//				if(value instanceof Variable) {
//					value = (Variable) value;//TODO ((Variable) value).getClone();
//				}
//				de.wwu.muggl.vm.classfile.structures.Field field = entityClassFile.getFieldByName(fieldName);
//				dummyData1.putField(field, value);
//			}
//			
//			EntityEntry entityObject2 = EntityAnalyzer.getInitialEntityEntry(entityClassName);
//			Objectref dummyData2 = frame.getVm().getAnObjectref(entityClassFile);
//			for(String fieldName : entityObject2.valueMap().keySet()) {
//				Object value = entityObject2.valueMap().get(fieldName);
//				if(value instanceof Variable) {
//					value = (Variable) value;//TODO ((Variable) value).getClone();
//				}
//				de.wwu.muggl.vm.classfile.structures.Field field = entityClassFile.getFieldByName(fieldName);
//				dummyData2.putField(field, value);
//			}
//			
//			VirtualDatabase newVDB = this.database.getClone();
//			newVDB.addRequired(dummyData1.getObjectId(), entityObject1);
//			newVDB.addData(dummyData1);
//			newVDB.addRequired(dummyData2.getObjectId(), entityObject2);
//			newVDB.addData(dummyData2);
//			
//			((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
//			
//			frame.getOperandStack().push(arrayListObjRef);
		} catch(Exception e) {
			System.out.println("oops could not generate choice point for getting result list for criteria query");
		}
	}

	



	protected Objectref getArrayListObjectref() {
		try {
			ClassFile classFile = frame.getVm().getClassLoader().getClassAsClassFile("java.util.ArrayList");
			Objectref objRef = frame.getVm().getAnObjectref(classFile);
			return objRef;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("oops could not generate choice point for getting result list for criteria query");
		}
		return null;
	}
	
	protected void addObjectrefToArrayList(Objectref arrayListObjRef, Objectref newElementObjRef) {
		try {
			ClassFile classFile = frame.getVm().getClassLoader().getClassAsClassFile("java.util.ArrayList");
			Object existingArrayObj = arrayListObjRef.getField(classFile.getFieldByName("elementData"));
			Arrayref arrayRef = null;
			if(existingArrayObj == null) {
				arrayRef = new Arrayref(newElementObjRef, 1);
			} else {
				Arrayref exisingArray = (Arrayref)existingArrayObj;
				arrayRef = new Arrayref(newElementObjRef, exisingArray.length + 1);
				for(int i=0; i<exisingArray.length; i++) {
					arrayRef.putElement(i, exisingArray.getElement(i));
				}
			}
			int index = arrayRef.length - 1;
			arrayRef.putElement(index, newElementObjRef);
			arrayListObjRef.putField(classFile.getFieldByName("elementData"), arrayRef);
			arrayListObjRef.putField(classFile.getFieldByName("size"), NumericConstant.getInstance(arrayRef.length, de.wwu.muggl.solvers.expressions.Expression.INT));
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("oops could not generate choice point for getting result list for criteria query");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
//	protected void createChoicePoint() {
//		System.out.println("******************");
//		System.out.println(" -> Choice Point: a query with a result should have entries");
//		System.out.println("******************");
//		
//		NewVirtualDatabase newVDB = this.database.getClone();
//		
//		foobar(newVDB);
//		
//		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
//		
//		List<Player> resultList = new ArrayList<Player>();
//		resultList.add(new Player(12,24,5,209));		
//		
//		try {
//			Objectref objectref1 = frame.getVm().getAnObjectref(frame.getVm().getClassLoader().getClassAsClassFile("de.wwu.muggl.solvers.expressions.Term"));
//			Objectref objectref2 = frame.getVm().getAnObjectref(frame.getVm().getClassLoader().getClassAsClassFile("de.wwu.pi.entity.Player"));
//			System.out.println("o1="+objectref1 + ", o2="+objectref2);
//		} catch (ClassFileException e) {
//			e.printStackTrace();
//		}
//		
//		// TODO: push a Objectref of type List<?>...
//		// mit objectref auch fuer die elemente
//		// dann kann man naemlich auch variablen als werte uebergeben
//		// z.b: Player(salary=VAR1) usw...
//		frame.getOperandStack().push(resultList);
//	}
	
	
//	private void handleBetweenRestriction(BetweenPredicate<?> between, NewVirtualDatabase newVDB, Objectref objectref) {
//		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();
//
//		javax.persistence.criteria.Expression<?> expression = between.getExpression();
//		javax.persistence.criteria.Expression<?> lower = between.getLowerBound();
//		javax.persistence.criteria.Expression<?> upper = between.getUpperBound();
//		
//		Class<?> entityClass = null;
//		String name = null;
//		String type = null;
//		
//		if(expression instanceof SingularAttributePath<?>) {
//			SingularAttributePath<?> sap = (SingularAttributePath<?>)expression;
//			entityClass = sap.getPathSource().getJavaType();
//			name = sap.getAttribute().getName();
//			type = sap.getJavaType().toString();
//		}
//		
//		Term lowerTerm = getTermFromLiteralExpression((LiteralExpression<?>)lower);
//		Term upperTerm = getTermFromLiteralExpression((LiteralExpression<?>)upper);
//		
//		NumericVariable nv = new NumericVariable("btwn", type);
//		solverManager.addConstraint(GreaterOrEqual.newInstance(nv, lowerTerm));
//		solverManager.addConstraint(GreaterOrEqual.newInstance(upperTerm, nv));
//		
//		DynamicExpressionMatrix matrix = newVDB.getExpressionMatrix(entityClass.getName(), true);
//		matrix.newEntryStart();
//		int x = matrix.getCurrentPosition();
//		int y = matrix.getColumnPosition(name);
//		matrix.set(x, y, nv);
//		matrix.setRequiredData(x);
//	
//		
//		// set objectref entity
//		Field field = objectref.getInitializedClass().getClassFile().getFieldByName(name);
//		objectref.putField(field, nv);
//	}
//	
//	private Term getTermFromLiteralExpression(LiteralExpression<?> le) {
//		Object o = le.getLiteral();
//		if(o instanceof Integer) {
//			return IntConstant.getInstance((Integer)o);
//		}
//		if(o instanceof NumericVariable) {
//			return (NumericVariable)o;
//		}
//		
//		return null;
//	}
	
	
//	private void foobar(NewVirtualDatabase newVDB) {
//		Predicate restriction = this.criteriaQuery.getRestriction();
//		
//		// add to the db
//		String entityName = this.criteriaQuery.getResultType().getName();
//		JPAConstraintManager manager = new JPAConstraintManager((JPAVirtualMachine)frame.getVm());
//		Objectref entityObject = manager.generateInitialEntity(entityName);
//		
//		newVDB.addEntityObject(entityName, entityObject);
//
//		if(restriction instanceof BetweenPredicate<?>) {
//			handleBetweenRestriction((BetweenPredicate<?>)restriction, newVDB, entityObject);
//			
//			
////			BetweenPredicate<?> between = (BetweenPredicate<?>)restriction;
////			javax.persistence.criteria.Expression<?> expression = between.getExpression();
////			javax.persistence.criteria.Expression<?> lower = between.getLowerBound();
////			javax.persistence.criteria.Expression<?> upper = between.getUpperBound();
////			
////			Class<?> entityClass = null;
////			String name = null;
////			String type = null;
////			
////			if(expression instanceof SingularAttributePath<?>) {
////				SingularAttributePath<?> sap = (SingularAttributePath<?>)expression;
////				entityClass = sap.getPathSource().getJavaType();
////				name = sap.getAttribute().getName();
////				type = sap.getJavaType().toString();
////			}
////			
////			if(lower instanceof LiteralExpression<?>) {
////				LiteralExpression<?> le = (LiteralExpression<?>)lower;
////				Object literal = le.getLiteral();
////				if(literal instanceof Integer) {
////					
////				}
////			}
////			
////			
////						
////			System.out.println("expression=" + expression + " must be between lower="+lower + " and upper=" + upper);
//		}
//		
//	}
	
	
	
	
	
	
	
	
	@Override
	public long getNumber() {
		return this.number;
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
	}
	
	@Override
	public void changeToNextChoice() {
		System.out.println("******************");
		System.out.println(" -> Change the coice point: reset db to state before, and now:");
		System.out.println(" -> Choice Point: a query should have NO results...");
		System.out.println("******************");
	
		this.alreadyVisitedNonJumpingBranch = true;
	}
	
	@Override
	public Frame getFrame() {
		return this.frame;
	}

	@Override
	public int getPc() {
		return this.pc;
	}

	@Override
	public int getPcNext() {
		return this.pcNext;
	}

	@Override
	public ChoicePoint getParent() {
		return this.parent;
	}

	@Override
	public boolean changesTheConstraintSystem() {
		return false;
	}
	@Override
	public ConstraintExpression getConstraintExpression() {
		return null;
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
	}

	@Override
	public boolean hasTrail() {
		return true;
	}

	@Override
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	@Override
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	@Override
	public boolean enforcesStateChanges() {
		return true;
	}
	
	@Override
	public void applyStateChanges() {
		
//		List<Player> resultList = new ArrayList<Player>();
		
		// TODO: push a Objectref of type List<?>...
		frame.getOperandStack().pop(); // pop old element..
		frame.getOperandStack().push(getArrayListObjectref());
//		frame.getOperandStack().push(new SymbolicResultList<>(criteriaQuery.getResultType(), criteriaQuery));
		
		
		
		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(this.database);
		System.out.println("*************************");
		System.out.println(" -> BEFORE changeing the choice point, reset DB");
		System.out.println("*************************");
	}

	@Override
	public String getChoicePointType() {
		return "JPA TypedQuery#getResultList Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
