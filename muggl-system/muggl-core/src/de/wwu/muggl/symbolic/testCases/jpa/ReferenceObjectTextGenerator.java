package de.wwu.muggl.symbolic.testCases.jpa;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.solvers.expressions.CharVariable;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.testCases.TestCaseSolution;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;

public class ReferenceObjectTextGenerator {
	
	// map of object-id and corresponding generated field names
	protected Map<String, String> objectFieldNameMap;
	
	public ReferenceObjectTextGenerator() {
		this.objectFieldNameMap = new HashMap<>();
	}

	public void generateObjectInitializationText(TestCaseSolution solution, StringBuilder sb, String varName, ReferenceVariable referenceVariable, boolean isClassField) {
		String concreteClass = getConcreteClass(referenceVariable);
		if(isClassField) {
			sb.append("\r\n\t\t//generate input data for variable: ");
			sb.append(varName);
			sb.append("\r\n");
			sb.append("\t\tthis.");
		} else {
			sb.append("\t\t");
			sb.append(concreteClass);
			sb.append(" ");
		}
		sb.append(varName);
		sb.append(" = new ");
		if(concreteClass.equals("java.lang.String")) {
			generateString(solution, sb, referenceVariable);
		} else {
			sb.append(concreteClass);
			sb.append("();\r\n");
			addSpecialFields(solution, sb, varName, referenceVariable);
		}
	}
	
	private void generateString(TestCaseSolution solution, StringBuilder sb, ReferenceVariable referenceVariable) {
		sb.append("String(");
		try {
			Field field = referenceVariable.getInitializedClass().getClassFile().getFieldByName("value");
			Object value = referenceVariable.getField(field);
			if(value instanceof SymbolicArrayref) {
				SymbolicArrayref symArray = (SymbolicArrayref)value;
				int size = 0;
				Constant constantSize = solution.getSolution().getValue(symArray.getSymbolicLength());
				if(constantSize != null && constantSize instanceof NumericConstant) {
					size = ((NumericConstant)constantSize).getIntValue();
				}
				sb.append("\"");
				for(int i=0; i<size; i++) {
					Object elementValue = symArray.getElement(i);
					if(elementValue != null) {
						if(elementValue instanceof NumericConstant) {
							int intValue = ((NumericConstant)elementValue).getIntValue();
							char charValue = (char)intValue;
							sb.append(charValue);
						} else if(elementValue instanceof ReferenceVariable) {
							ReferenceVariable refVarElement = (ReferenceVariable)elementValue;
							Constant constant = solution.getSolution().getValue(refVarElement);
							if(constant == null) {
								sb.append("#");
							} else {
								int intValue = ((NumericConstant)constant).getIntValue();
								char charValue = (char)intValue;
								sb.append(charValue);
							}
						} else if(elementValue instanceof NumericVariable) {
							NumericVariable nv = (NumericVariable)elementValue;
							NumericConstant nc = (NumericConstant)solution.getSolution().getValue(nv);
							int intValue = nc.getIntValue();
							char charValue = (char)intValue;
							sb.append(charValue);
						}
					} else {
						sb.append('#');
					}
				}
				sb.append("\"");
			} else {
				// no field "value" set in array, so no special string is required here
				sb.append("\"\"");
			}
		} catch(FieldResolutionError fre) {
			
		}
		
		sb.append(");\r\n");
	}

	private void addSpecialFields(TestCaseSolution solution, StringBuilder sb, String varName, ReferenceVariable referenceVariable) {
		for(Field field : referenceVariable.getFields().keySet()) {
			String fieldReflectionName = varName + "Field_" + field.getName();
			this.objectFieldNameMap.put(referenceVariable.getObjectId(), fieldReflectionName);
			
			Object value = referenceVariable.getField(field);
			
			if(value instanceof SymbolicArrayref) {
				SymbolicArrayref symArray = (SymbolicArrayref)value;
				String arrayVarName = symArray.getName().replaceAll("\\.", "_");
				sb.append("\t\t");
				sb.append(symArray.getInitializedClass().getClassFile().getName());
				sb.append("[] ");
				sb.append(arrayVarName);
				sb.append(" = new ");
				sb.append(symArray.getInitializedClass().getClassFile().getName());
				sb.append("[");
				int size = 0;
				Constant constantSize = solution.getSolution().getValue(symArray.getSymbolicLength());
				if(constantSize != null && constantSize instanceof NumericConstant) {
					size = ((NumericConstant)constantSize).getIntValue();
				}
				sb.append(size);
				sb.append("];\r\n");
				for(int i=0; i<size; i++) {
					Object elementValue = symArray.getElement(i);
					
					if(elementValue instanceof ReferenceVariable) {
						ReferenceVariable elementReferenceValue = (ReferenceVariable)elementValue;
						String elementFieldName = elementReferenceValue.getName().replaceAll("\\.", "_");
						// generate object via recursion
						generateObjectInitializationText(solution, sb, elementFieldName, elementReferenceValue, false);
						sb.append("\t\t");
						sb.append(arrayVarName);
						sb.append("[");
						sb.append(i);
						sb.append("] = ");
						sb.append(elementFieldName);
						sb.append(";\r\n");
					} else {
						sb.append("\t\t");
						sb.append(arrayVarName);
						sb.append("[");
						sb.append(i);
						sb.append("] = ");
						sb.append(getElementValueText(solution, elementValue, symArray.getInitializedClass().getClassFile().getName()));
						sb.append(";\r\n");
					}
				}
//				sb.append("\t\t//TODO: add it with java.lang.reflect to the values field of the array\r\n");
				
				sb.append("\t\tjava.lang.reflect.Field ");
				sb.append(fieldReflectionName);
				sb.append(" = ");
				sb.append(field.getClassFile().getName());
				sb.append(".class.getDeclaredField(\"");
				sb.append(field.getName());
				sb.append("\");\r\n");
				
				sb.append("\t\t");
				sb.append(fieldReflectionName);
				sb.append(".setAccessible(true);\r\n");
				
				sb.append("\t\t");
				sb.append(fieldReflectionName);
				sb.append(".set(");
				sb.append(varName);
				sb.append(", ");
				sb.append(arrayVarName);
				sb.append(");\r\n");
				
			} else if(value instanceof ReferenceVariable) {
				
				generateObjectInitializationText(solution, sb,  fieldReflectionName, (ReferenceVariable)value, false);
				sb.append("\t\t");
				sb.append(varName);
				sb.append(".set");
				sb.append(field.getName().substring(0, 1).toUpperCase());
				sb.append(field.getName().substring(1));
				sb.append("(");
				sb.append(fieldReflectionName);
				sb.append(");\r\n");
				
			} else if(value instanceof NumericVariable) {
				NumericVariable nv = (NumericVariable)value;
				NumericConstant nc = (NumericConstant)solution.getSolution().getValue(nv);
				String numberToDisplay = "-50";
				if(nc != null) {
					numberToDisplay = Integer.toString(nc.getIntValue());
				}
				sb.append("\t\t");
				sb.append(varName);
				sb.append(".set");
				sb.append(field.getName().substring(0, 1).toUpperCase());
				sb.append(field.getName().substring(1));
				sb.append("(");
				sb.append(numberToDisplay);
				sb.append(");\r\n");
			} else if(value instanceof EntityStringObjectref) {
				EntityStringObjectref e = (EntityStringObjectref)value;
				SymbolicArrayref o = (SymbolicArrayref)e.valueMap().get("value");
				NumericConstant nc = solution.getSolution().getNumericValue(o.getSymbolicLength());
				int size = 0;
				if(nc != null) {
					size = nc.getIntValue();
				}
				sb.append("\t\t");
				sb.append(varName);
				sb.append(".set");
				sb.append(field.getName().substring(0, 1).toUpperCase());
				sb.append(field.getName().substring(1));
				sb.append("(");
				sb.append("\"");
				for(int i=0; i<size; i++) {
					CharVariable cv = (CharVariable)o.getElement(i);
					if(cv == null) {
						sb.append("(?)");
					} else {
						IntConstant ic = (IntConstant)solution.getSolution().getValue(cv);
						sb.append((char)ic.getIntValue());
					}
				}
				sb.append("\"");
				sb.append(");\r\n");
			} else {
				sb.append("\t\t// for field=" + field.getName() + " the variable of type=" + value.getClass().getName() + " is not handled yet\r\n");
			}
			
//			sb.append("\t\t// generate data for field: " + field.getName() + " with value = " + value + "\r\n");
		}
	}

	private String getElementValueText(TestCaseSolution solution, Object elementValue, String arrayType) {
		if(elementValue instanceof Variable) {
			Constant constant = solution.getSolution().getValue((Variable)elementValue);
			return getConstantValue(constant, arrayType);
		}
		
		if(elementValue instanceof Constant) {
			return getConstantValue((Constant)elementValue, arrayType);
		}
		
		return null;	
	}
	
	protected String getConstantValue(Constant constant, String type) {
		if(constant instanceof NumericConstant && type.equals("java.lang.Integer")) {
			return Integer.toString(((NumericConstant)constant).getIntValue());
		}
		if(constant instanceof NumericConstant && type.equals("java.lang.Character")) {
			int intValue = ((NumericConstant)constant).getIntValue();
			char charValue = (char)intValue;
			return "\'"+charValue+"'";
		}
		return null;
	}

	private String getConcreteClass(ReferenceVariable referenceVariable) {
		// TODO: checken, ob der object type abstract class oder intface ist, dann muss man ein subtype davon zurueckgeben hier...
		return referenceVariable.getObjectType();
	}

	
}
