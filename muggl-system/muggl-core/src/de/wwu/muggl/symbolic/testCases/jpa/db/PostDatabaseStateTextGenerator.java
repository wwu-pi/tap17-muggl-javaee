package de.wwu.muggl.symbolic.testCases.jpa.db;

import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.AutoGeneratedGetterConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.Difference;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.obj.SymbolicObjectStore;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValueClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceCollectionVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;

public class PostDatabaseStateTextGenerator extends RequiredDatabaseStateTextGenerator {
	
	protected Solution solution;
	protected Map<String, String> objectFieldNameMap;
	protected Metamodel metamodel;

	public PostDatabaseStateTextGenerator(Metamodel metamodel, Solution solution, Map<String, String> objectFieldNameMap, SymbolicObjectStore objectStore) {
		super(metamodel, solution, objectStore);
		this.solution = solution;
		this.objectFieldNameMap = objectFieldNameMap;
		this.metamodel = metamodel;
	}
	
	public void generateRequiredDatabase(StringBuilder sb, Map<String, Set<DatabaseObject>> dataMap)  {
		sb.append("\t\t// check post-execution database state \r\n");
		for(String entityName : dataMap.keySet()) {
			for(DatabaseObject dbObj : dataMap.get(entityName)) {
				generateObjectCheck(sb, dbObj);
			}
		}
	}

	private void generateObjectCheck(StringBuilder sb, DatabaseObject dbObj) {
//		EntityType<?> entityType = null;
//		for(EntityType<?> et : this.metamodel.getEntities()) {
//			if(et.getJavaType().getName().equals(dbObj.getObjectType())) {
//				entityType = et;
//				break;
//			}
//		}
//		
//		if(entityType  == null) {
//			sb.append("\t\t// ERR: no ID type found in meta model for: " + dbObj.getObjectType());
//			return;
//		}
		
//		String entityIdFieldName = generateEntityId(sb, entityType, dbObj);
		
		String entityIdFieldName = EntityConstraintAnalyzer.getIdFieldName(dbObj.getObjectType());
		
		Object idValue = dbObj.valueMap().get(entityIdFieldName);
		
		EntityObjectref reqEntity = ((EntityObjectref)dbObj).getRequiredEntity();
		
		String entityIdFieldValue = "null";
		if(reqEntity != null) {
			String reqEntityName = this.objectFieldNameMap.get(reqEntity.getObjectId());
			if(reqEntityName != null) {
				entityIdFieldValue = reqEntityName+".get"+entityIdFieldName.toUpperCase().substring(0,1)+entityIdFieldName.substring(1)+"()";
			}			
		}
		
		if(entityIdFieldValue.equals("null") && idValue != null) {
			if(idValue instanceof Objectref && ((Objectref)idValue).getObjectType().equals("java.lang.String")) {
				entityIdFieldValue = getStringValue((Objectref)idValue).toString();
			} else if(idValue instanceof Variable) {
				de.wwu.muggl.solvers.expressions.Constant c = solution.getValue((Variable)idValue);
				if(c != null) {
					entityIdFieldValue = c.toString();
				}
			}
		}
		
		if(entityIdFieldValue.equals("null") && idValue == null) {
			sb.append("\t\t// no post-execution entity check for entity: ["+dbObj.getObjectId()+"]. Reason: generated values inside method-under-test cannot be checked yet\r\n");
			return;
		}
		
		
		String reqEntityFieldName = this.objectFieldNameMap.get(dbObj.getObjectId());
		if(reqEntityFieldName == null) {
			reqEntityFieldName = "entity"+dbObj.getInstantiationNumber();
		}
		String dbEntityFieldName = reqEntityFieldName + "_db";
		
		sb.append("\t\t");
		sb.append(dbObj.getObjectType());
		sb.append(" ");
		sb.append(dbEntityFieldName);
		sb.append(" = ");
		sb.append("(");
		sb.append(dbObj.getObjectType());
		sb.append(")em.find(");
		sb.append(dbObj.getObjectType());
		sb.append(".class, ");
		sb.append(entityIdFieldValue);
		sb.append(");\r\n");
		
		// assert that entity is not null
		sb.append("\t\tassertNotNull(");
		sb.append(dbEntityFieldName);
		sb.append(");\r\n");
		
		Map<String, Object> valueMap = dbObj.valueMap(); 
		for(String fieldName : valueMap.keySet()) {
			generateAssertFieldEquals(sb, fieldName, valueMap.get(fieldName), reqEntityFieldName, dbEntityFieldName);
		}
	}
	
	private void generateAssertFieldEquals(StringBuilder sb, String fieldName, Object value, String leftObjectName, String rightObjectName) {
		boolean ok = (value instanceof Objectref && ((Objectref)value).getObjectType().equals("java.lang.String"))
				      || (value instanceof NumericVariable
				      || (value instanceof NumericConstant)
				      || (value instanceof Sum)
				      || (value instanceof Difference));
		
		if(!ok) return;
		
		String valueString = getValueString(value);
		
		String getterName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
		sb.append("\t\tassertEquals(");
		sb.append(valueString);
		sb.append(", ");
		sb.append(rightObjectName);
		sb.append(".");
		sb.append(getterName);
		sb.append("());\r\n");
	}
	
	private String generateEntityId(StringBuilder sb, EntityType<?> entityType, DatabaseObject dbObj) {
		String reqFieldName = this.objectFieldNameMap.get(dbObj.getObjectId());
		String idFieldName =  "key" + dbObj.getInstantiationNumber();
		if(reqFieldName != null) {
			idFieldName =  reqFieldName+"_key";
		}
		
		try {
			Set<?> idAttributes = entityType.getIdClassAttributes();
			String idClassName = getIdClassName(dbObj);
			sb.append("\t\t");
			sb.append(idClassName);
			sb.append(" ");
			sb.append(idFieldName);
			sb.append(" = new ");
			sb.append(idClassName);
			sb.append("();\r\n");
			for(Object o : idAttributes) {
				if(o instanceof SingularAttribute) {
					SingularAttribute sa = (SingularAttribute)o;
					String attributeName = sa.getName();
					generateSetter(sb, idFieldName, attributeName, dbObj.valueMap().get(attributeName));
				}
			}
		} catch(IllegalArgumentException e) {
			// its a single ID attribute
			SingularAttribute<?, ?> sa = entityType.getId(entityType.getIdType().getJavaType());
			String attributeName = sa.getName();
			generateSetter(sb, idFieldName, attributeName, dbObj.valueMap().get(attributeName));
		}
		
		return idFieldName;
	}
	
	
	
	
	private void generateSimpleSetter(StringBuilder sb, String fieldName, String setterName, String setterValue) {		
		sb.append("\t\t");
		sb.append(fieldName);
		sb.append(".");
		sb.append(setterName);
		sb.append("(");
		sb.append(setterValue);
		sb.append(");\r\n");
	}
	
	private String getValueString(Object value) {
		if(value instanceof Objectref && ((Objectref)value).getObjectType().equals("java.lang.String")) {
			return getStringValue((Objectref)value).toString();
		}
		if(value instanceof NumericConstant) {
			return ""+getNumericValue((NumericConstant)value);
		}
		if(value instanceof NumericVariable) {
			Constant c = solution.getValue((NumericVariable)value);
			if(c instanceof AutoGeneratedGetterConstant) {
				return ((AutoGeneratedGetterConstant)c).getFullGetterName();
			}
			
			NumericConstant constant = solution.getNumericValue((NumericVariable)value);
			if(constant == null) {
				return "0";
			}
			return ""+getNumericValue(constant);
		}
		if(value instanceof Sum) {
			return ""+getSumValue((Sum)value);
		}
		if(value instanceof Difference) {
			return ""+getDifferenceValue((Difference)value);
		}
		return "<TYPE_NOT_SUPPORTED>";
	}
	
	private int getDifferenceValue(Difference difference) {
		Term left = difference.getLeft();
		Term right = difference.getRight();
		
		return getIntValue(left) - getIntValue(right);
	}

	private int getSumValue(Sum sum) {
		Term left = sum.getLeft();
		Term right = sum.getRight();
		
		return getIntValue(left) + getIntValue(right);
	}
	
	private int getIntValue(Term term) {
		if(term instanceof NumericConstant) {
			NumericConstant nc = (NumericConstant)term;
			return nc.getIntValue();
		} 
		if(term instanceof NumericVariable) {
			return getNumericVariableValue((NumericVariable)term);
		}
		if(term instanceof Sum) {
			return getSumValue((Sum)term);
		}
		if(term instanceof Difference) {
			return getDifferenceValue((Difference)term);
		}
		throw new RuntimeException("Sum contains other elements than NUMERIC_CONSTANT, NUMERIC_VARIABLE or SUM");
	}
	
	private int getNumericVariableValue(NumericVariable nv) {
		NumericConstant constant = solution.getNumericValue(nv);
		if(constant == null) {
			return 0;
		}
		return constant.getIntValue();
	}
	
	private String getIdClassName(DatabaseObject dbObj) {
		if(dbObj instanceof EntityObjectref) {
			EntityObjectref entityObject = (EntityObjectref)dbObj;
			ClassFile classFile = entityObject.getInitializedClass().getClassFile();
			for(Attribute attribute : classFile.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation anno : attributeAnnotation.getAnnotations()) {
						if (classFile.getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/IdClass;")) {
							int classInfoIndex = ((ElementValueClass)anno.getElementValuePairs()[0].getElementValues()).getClassInfoIndex();
							de.wwu.muggl.vm.classfile.structures.Constant c = classFile.getConstantPool()[classInfoIndex];
							return c.getStringValue().substring(1, c.getStringValue().length()-1).replace("/", ".");
						}
					}
				}
			}
		}
		return null;
	}

}
