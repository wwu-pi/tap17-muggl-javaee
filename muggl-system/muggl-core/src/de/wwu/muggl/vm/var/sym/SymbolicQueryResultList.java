package de.wwu.muggl.vm.var.sym;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.orm.persister.common.internal.OrmSingularAttributeBasic;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.sqm.parser.hql.internal.antlr.HqlParser.SelectionListContext;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.domain.SqmAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingBasic;
import org.hibernate.sqm.query.expression.domain.SqmSingularAttributeBindingEntity;
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.gen.SymbolicQueryResultElementGenerator;
import de.wwu.pi.app.entities.Customer;

public class SymbolicQueryResultList extends ReferenceArrayListVariable implements ISymbolicResultList, ISymbolicList {

	protected List<Objectref> resultList;
	protected QLStatement<?> qlStmt;
	
	protected SymbolicQueryResultListArrayref[] symbolicElementData;
	
	protected SymbolicQueryResultElementGenerator generator;
	
	protected JPAVirtualMachine vm;
	
	public SymbolicQueryResultList(String name, JPAVirtualMachine vm, QLStatement<?> qlStmt) throws SymbolicObjectGenerationException {
		super(name, vm);
		this.qlStmt = qlStmt;
		this.resultList = new ArrayList<>();
		this.generator = new SymbolicQueryResultElementGenerator(vm, this.getQLStatement());
		this.vm = vm;
		
		try {
			List<SqmSelection> selectionList = qlStmt.getSqmStatement().getQuerySpec().getSelectClause().getSelections();
			
			this.symbolicElementData = generatedSymbolicSelectionArrays(vm, selectionList);
			
			if(this.symbolicElementData.length > 1) {
				ClassFile classFile = vm.getClassLoader().getClassAsClassFile("java.lang.Object");
				
				InitializedClass initializedClass = classFile.getInitializedClass();
				if(initializedClass == null) {
					initializedClass = new InitializedClass(classFile, vm);
				}
				
				SymbolicQueryResultListArrayref arrayRef = new SymbolicQueryResultListArrayref(initializedClass, name+".elementData", this);
				
				for(int i=0;i<this.symbolicElementData.length;i++) {
					arrayRef.setElementAt(i, this.symbolicElementData[i]);
					vm.getSolverManager().addConstraint(NumericEqual.newInstance(this.symbolicLength, this.symbolicElementData[i].getSymbolicLength()));
				}
				
				Field elementDataField = this.getInitializedClass().getClassFile().getFieldByName("elementData");
				this.putField(elementDataField, arrayRef);
				
			} else if(this.symbolicElementData.length == 1){
				Field elementDataField = this.getInitializedClass().getClassFile().getFieldByName("elementData");
				this.putField(elementDataField, this.symbolicElementData[0]);
				
				vm.getSolverManager().addConstraint(NumericEqual.newInstance(this.symbolicLength, this.symbolicElementData[0].getSymbolicLength()));
			}
			
		} catch(Exception e) {
			throw new SymbolicObjectGenerationException("Error while generating symbolic result array", e);
		}
		
		vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(symbolicLength, NumericConstant.getZero(Expression.INT)));
		
		try {
			if(!vm.getSolverManager().hasSolution()) {
				throw new RuntimeException("Query Result List has no solution");
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			throw new RuntimeException("Query Result List has no solution");
		}
	}
	
	@Override
	public void addElement(Objectref element) {
		this.resultList.add(element);
	}
	
	@Override
	public Objectref generateNewElement() {
		Objectref[] newElement = generateNewQueryResult(vm, 1);
		if(newElement != null && newElement.length == 1) {
			return newElement[0];
		}
		return null;
	}	
	
	public Objectref[] generateNewQueryResult(JPAVirtualMachine vm, int index) {
		Objectref[] elements = generator.generateElements();
		for(int i=0; i<this.symbolicElementData.length; i++) {
			SymbolicQueryResultListArrayref array = this.symbolicElementData[i];
			array.setElementAt(index, elements[i]);
		}
		return elements;
	}
	
	
	private SymbolicQueryResultListArrayref[] generatedSymbolicSelectionArrays(JPAVirtualMachine vm, List<SqmSelection> selectionList) throws ClassFileException, SymbolicObjectGenerationException {
		SymbolicQueryResultListArrayref[] symbolicArrays = new SymbolicQueryResultListArrayref[selectionList.size()];
		for(int i=0; i<selectionList.size(); i++) {
			SqmSelection selection = selectionList.get(i);
			String typeClassName = null;
			
			if(selection.getExpression() instanceof SqmAttributeBinding) {
				SqmAttributeBinding attBind = (SqmAttributeBinding)selection.getExpression();
				if(attBind instanceof SqmSingularAttributeBindingBasic) {
					SqmSingularAttributeBindingBasic b = (SqmSingularAttributeBindingBasic)attBind;
					if(b.getBoundNavigable() instanceof OrmSingularAttributeBasic) {
						OrmSingularAttributeBasic o = (OrmSingularAttributeBasic)b.getBoundNavigable();
						typeClassName = o.getType().getJavaType().getName();
					}
				}
			}
			
			if(typeClassName == null && selection.getExpression() instanceof SqmSingularAttributeBindingEntity) {
				SqmSingularAttributeBindingEntity attBind = (SqmSingularAttributeBindingEntity)selection.getExpression();
				typeClassName = attBind.getBoundNavigable().getEntityName();
			}
			
			if(typeClassName == null && selection.getExpression() instanceof SqmEntityBinding) {
				SqmEntityBinding attBind = (SqmEntityBinding)selection.getExpression();
				typeClassName = attBind.getBoundNavigable().getEntityName();
			}
			
			if(typeClassName != null) {
				ClassFile classFile = vm.getClassLoader().getClassAsClassFile(typeClassName);
				if(classFile.isAccInterface()) {
					classFile = getConcreteEntityClassForInterface(vm, classFile);
				}
				
				InitializedClass initializedClass = classFile.getInitializedClass();
				if(initializedClass == null) {
					initializedClass = new InitializedClass(classFile, vm);
				}
				
				SymbolicQueryResultListArrayref arrayRef = new SymbolicQueryResultListArrayref(initializedClass, name+"#idx-"+i+".elementData", this);
				symbolicArrays[i] = arrayRef;
			} else {
				throw new SymbolicObjectGenerationException("Expression type: " + selection.getExpression() + " not supported yet, please implement it");
			}
		}
		return symbolicArrays;
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
	
	public String getId() {
		return this.getObjectId();
	}
	
	public QLStatement<?> getQLStatement() {
		return this.qlStmt;
	}
	
	public List<Objectref> getResultList() {
		return this.resultList;
	}
	
	public SymbolicIterator iterator() {
		SymbolicIterator iterator = new SymbolicIterator(this);
		return iterator;
	}

	
	
	@Deprecated
	public Set<EntityObjectref> generateAndAddNewElement(JPAVirtualMachine vm) {
		Set<EntityObjectref> elements = generator.generateSelectElement();
		// TODO: currently just one element added to list
		this.resultList.add(elements.iterator().next());
		return elements;
	}
	
	
	@Deprecated
	public EntityObjectref generateAndAddNewElement(JPAVirtualMachine vm, int index) {
		SqmPredicate whereRestriction = qlStmt.getSqmStatement().getQuerySpec().getWhereClause().getPredicate();
		List<SqmSelection> selectionList = qlStmt.getSqmStatement().getQuerySpec().getSelectClause().getSelections();
		if(selectionList.size() == 1 && selectionList.get(0).getExpression() instanceof SqmEntityBinding) {
			
			try {
				SqmEntityBinding entityBinding = (SqmEntityBinding)selectionList.get(0).getExpression();
		
				String entityName = entityBinding.getExpressionType().getEntityName();
				
				SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(vm);
				
				
				ClassFile entityClassFile = vm.getClassLoader().getClassAsClassFile(entityName);
				
				// generate a new entity object reference for required data
				Objectref req_entityObjectref = vm.getAnObjectref(entityClassFile);
				long req_number = req_entityObjectref.getInstantiationNumber();
				String req_entityObjectrefName = "RESLIST#REQ#ELE#"+index+"#entity-object#"+entityClassFile.getClassName()+req_number;
				EntityObjectref req_entityObject = new EntityObjectref(entityFieldGenerator, req_entityObjectrefName, req_entityObjectref);
				
				// generate a new entity object reference for operating data
				Objectref data_entityObjectref = vm.getAnObjectref(entityClassFile);
				long data_number = data_entityObjectref.getInstantiationNumber();
				String data_entityObjectrefName = "RESLIST#DATA#ELE#"+index+"#entity-object#"+entityClassFile.getClassName()+data_number;
				EntityObjectref data_entityObject = new EntityObjectref(entityFieldGenerator, data_entityObjectrefName, data_entityObjectref, req_entityObject);
				
				// add singular restrictions
				addWhereRestrictionToEntityObjectref(vm, whereRestriction, req_entityObject);
				
				// add static constraints, e.g., @Min(value=x) constraint in Entity class
				addStaticConstraints(vm, req_entityObject);
				addStaticConstraints(vm, data_entityObject);
				
				vm.getVirtualObjectDatabase().addPreExecutionRequiredData(vm.getSolverManager(), entityName, req_entityObject);
				vm.getVirtualObjectDatabase().addEntityData(vm.getSolverManager(), entityName, data_entityObject);

				if(resultList.size() < (index+1)) {
					resultList.add(data_entityObject);
				} else {
					resultList.set(index, data_entityObject);
				}
				return data_entityObject;
			} catch(Exception e) {
				throw new RuntimeException("Error while generating query result entry:" + e.getMessage(), e);
			}
		}
		return null;
	}

	
	private void addStaticConstraints(JPAVirtualMachine vm, EntityObjectref entityObject) {
		ClassFile classFile = entityObject.getInitializedClass().getClassFile();
		for(Field field : classFile.getFields()) {
			for(Attribute attribute : field.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations runAtt = (AttributeRuntimeVisibleAnnotations)attribute;
					for(Annotation anno : runAtt.getAnnotations()) {
						Constant c = classFile.getConstantPool()[anno.getTypeIndex()];
						if(c.getStringValue().equals("Ljavax/validation/constraints/NotNull;")) {
							applyNotNullConstraint(vm, entityObject, field.getName());
						} else if(c.getStringValue().equals("Ljavax/validation/constraints/Min;")) {
							String minimumValue = anno.getElementValuePairs()[0].getElementValues().getStringValue();
							applyMinConstraint(vm, entityObject, field.getName(), Integer.parseInt(minimumValue));
						}
					}
				}
			}
		}
	}
	
	private void applyMinConstraint(JPAVirtualMachine vm, EntityObjectref entityObject, String attributeName, int minimumValue) {
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName);
		Object fieldValue = entityObject.getField(field);
		NumericVariable nv = (NumericVariable)fieldValue;
		vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(nv, NumericConstant.getInstance(minimumValue, nv.getType())));
	}
	
	private void applyNotNullConstraint(JPAVirtualMachine vm, EntityObjectref entityObject, String attributeName) {
		Field field = entityObject.getInitializedClass().getClassFile().getFieldByName(attributeName);
		Object fieldValue = entityObject.getField(field);
		if(field.getType().equals(String.class.getName())) {
			Objectref stringObjectref = (Objectref)fieldValue;
			Field stringValueField = stringObjectref.getInitializedClass().getClassFile().getFieldByName("value");
			SymbolicArrayref symValueArrayref = (SymbolicArrayref) stringObjectref.getField(stringValueField);
			vm.getSolverManager().addConstraint(GreaterThan.newInstance(symValueArrayref.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
//		} else if(field.getType().equals(Integer.class.getName()) || field.getType().equals("int")) {
//			NumericVariable nv = (NumericVariable)fieldValue;
//			vm.getSolverManager().addConstraint(GreaterThan.newInstance(symValueArrayref.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
//		} else {
//			System.out.println("*** could not set NOT NULL to field: " + field);
		}
	}

	private void addWhereRestrictionToEntityObjectref(JPAVirtualMachine vm, SqmPredicate whereRestriction, EntityObjectref entityObjectref) {
		if(whereRestriction instanceof RelationalSqmPredicate) {
			addRelationPrediate(vm, (RelationalSqmPredicate)whereRestriction, entityObjectref);
		} else {
			System.out.println("****** PREDICATE : " + whereRestriction + "  NOT HANDLED YET");
		}
	}

	private void addRelationPrediate(JPAVirtualMachine vm, RelationalSqmPredicate whereRestriction, EntityObjectref entityObjectref) {
		
		SqmExpression leftHS = whereRestriction.getLeftHandExpression();
		SqmExpression rightHS = whereRestriction.getRightHandExpression();
		if(leftHS instanceof SqmSingularAttributeBinding
			&& ((SqmSingularAttributeBinding)leftHS).getSourceBinding().getExpressionType() instanceof EntityPersister
			&& rightHS instanceof NamedParameterSqmExpression) {
			SqmSingularAttributeBinding attributeBinding = ((SqmSingularAttributeBinding)leftHS);
			String entityName = ((EntityPersister<?>)attributeBinding.getSourceBinding().getExpressionType()).getName();
			String attributeName = attributeBinding.getPropertyPath().getLocalPath();
			
			NamedParameterSqmExpression paraExpression = (NamedParameterSqmExpression)rightHS;
			String paraName = paraExpression.getName();
			Object parameter = qlStmt.getParameterMap().get(paraName);
			
			Field attributeField = entityObjectref.getInitializedClass().getClassFile().getFieldByName(attributeName);
			Object attributeValue = entityObjectref.getField(attributeField);
			
			ConstraintExpression ce = getConstraintExpression(attributeValue, whereRestriction.getOperator(), parameter);
			if(ce != null) {
				vm.getSolverManager().addConstraint(ce);
			}
		}
		
		if(whereRestriction.getOperator() == RelationalPredicateOperator.GREATER_THAN_OR_EQUAL) {
			if(leftHS instanceof CollectionSizeSqmExpression && rightHS instanceof NamedParameterSqmExpression) {
				CollectionSizeSqmExpression sizeExpression = (CollectionSizeSqmExpression)leftHS;
				String attributeName = sizeExpression.getPluralAttributeBinding().getPropertyPath().getLocalPath();
				
				NamedParameterSqmExpression paraExpression = (NamedParameterSqmExpression)rightHS;
				String paraName = paraExpression.getName();
				Term parameter = (Term)qlStmt.getParameterMap().get(paraName);
				
				Field attributeField = entityObjectref.getInitializedClass().getClassFile().getFieldByName(attributeName);
				ReferenceCollectionVariable refCol = (ReferenceCollectionVariable)entityObjectref.getField(attributeField);
				
				vm.getSolverManager().addConstraint(NumericEqual.newInstance(parameter, refCol.getSymbolicLength()));
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

	
	
	
	
	
	public void checkForExistingData() {
		SqmSelectClause selectClause = qlStmt.getSqmStatement().getQuerySpec().getSelectClause();
		SqmWhereClause whereClause = qlStmt.getSqmStatement().getQuerySpec().getWhereClause();
		if(selectClause != null) {
			// get the entity in selection:
			String entityName = Customer.class.getName(); // TODO
			Set<DatabaseObject> existingData = this.vm.getVirtualObjectDatabase().getData(entityName);
			if(existingData != null) {
				for(DatabaseObject dbObj : existingData) {
					if(whereClause == null) {
						// there is no WHERE restriction, add all elements
						this.resultList.add((Objectref)dbObj);
					} else {
						// check WHERE
						if(satisfyWhereRestrictions(dbObj, whereClause)) {
							this.resultList.add((Objectref)dbObj);
							
						}
					}
				}
				
			}
		}
		// add constraint on length...
		this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(this.symbolicLength, NumericConstant.getInstance(this.resultList.size(), Expression.INT)));
	}
	
	private boolean satisfyWhereRestrictions(DatabaseObject dbObject, SqmWhereClause whereClause) {
		if(whereClause.getPredicate() instanceof RelationalSqmPredicate) {
			return satisfyRelationWhere((RelationalSqmPredicate)whereClause.getPredicate(), dbObject);
		}
		return false;
	}

	private boolean satisfyRelationWhere(RelationalSqmPredicate predicate, DatabaseObject dbObject) {
		if(predicate.getLeftHandExpression() instanceof SqmSingularAttributeBindingBasic) {
			SqmSingularAttributeBindingBasic s = (SqmSingularAttributeBindingBasic)predicate.getLeftHandExpression();
			if(s.getSourceBinding() instanceof SqmEntityBinding) {
				String entityName = ((SqmEntityBinding)s.getSourceBinding()).getBoundNavigable().getEntityName();
				String attributeName = s.getBoundNavigable().getAttributeName();
				if(dbObject.getObjectType().equals(entityName)) {
					Object value = dbObject.valueMap().get(attributeName);
					if(value != null) {
						return checkRelationalSatisfiability(predicate.getRightHandExpression(), predicate.getOperator(),  value);
					} else {
						// TODO: generate new value and put it on dbObject and add constraint to it from this WHERE clause
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private boolean checkRelationalSatisfiability(SqmExpression sqmExpression, RelationalPredicateOperator operator, Object value) {
		if(sqmExpression instanceof LiteralIntegerSqmExpression) {
			LiteralIntegerSqmExpression e = (LiteralIntegerSqmExpression)sqmExpression;
			NumericConstant nc = NumericConstant.getInstance(e.getLiteralValue(), Expression.INT);
			int level = this.vm.getSolverManager().getConstraintLevel();
			ConstraintExpression ce = getConstraintExpression(value, operator, nc);
			if(ce instanceof BooleanConstant) {
				return ((BooleanConstant)ce).getValue();
			}
			
			this.vm.getSolverManager().addConstraint(ce);
			try {
				if(!this.vm.getSolverManager().hasSolution()) {
					this.vm.getSolverManager().resetConstraintLevel(level);
					return false;
				}
			} catch (Exception ex) {
				this.vm.getSolverManager().resetConstraintLevel(level);
				return false;
			}
			return true;
		}
		return false;
	}

}
