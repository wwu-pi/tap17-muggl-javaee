package de.wwu.muggl.solvers.jacop.obj;

import org.jacop.constraints.XeqC;
import org.jacop.constraints.XgtC;
import org.jacop.constraints.XneqC;
import org.jacop.core.IntVar;
import org.jacop.core.Var;

import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceEqual;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceNotEqual;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectReferenceComparisonConstraint;
import de.wwu.muggl.solvers.jacop.JacopMugglStore;
import de.wwu.muggl.solvers.type.IObjectreference;

public class JacopObjectGenerator {
	
	public static void imposeObjectReferenceConstraint(ObjectReferenceComparisonConstraint referenceConstraint, JacopMugglStore store) {
		boolean isImposed = false;
		if(referenceConstraint.getObjectToCompare() instanceof Constant) {
			Constant constantEqualToReference = (Constant)referenceConstraint.getObjectToCompare();
			if(constantEqualToReference instanceof IntConstant) {
				IntConstant intConstant = (IntConstant)constantEqualToReference;
				if(referenceConstraint.getObjectReference() instanceof Variable) {
					Variable mugglVariable = (Variable)referenceConstraint.getObjectReference();
					
					IntVar jacopVariable = (IntVar)store.getVariable(mugglVariable);
					
					if(jacopVariable == null) {
						if(referenceConstraint.getObjectReference().getObjectType().equals("java.lang.Character")) {
							jacopVariable = new IntVar(store, 33, 127);
						} else {
							jacopVariable = new IntVar(store, -50, 50);
						}
					}
					
					store.addVariable(mugglVariable, jacopVariable);
					if(referenceConstraint instanceof ObjectReferenceEqual) {
						store.impose(new XeqC(jacopVariable, intConstant.getIntValue()));
						isImposed = true;
					} else if(referenceConstraint instanceof ObjectReferenceNotEqual) {
						store.impose(new XneqC(jacopVariable, intConstant.getIntValue()));
						isImposed = true;
					}
				}
			}
		}
		
		if(!isImposed) {
			// WORKAROUND: if equals constraint, we impose a NOT WORKING constraint
			if(referenceConstraint instanceof ObjectReferenceEqual) {
//				IntVar neverTrue = new IntVar(store, 0, 3);
//				XgtC thisNeverIsTrue = new XgtC(neverTrue, 5);
//				store.impose(thisNeverIsTrue);
			}
			
			System.out.println("******* NOT IMPLEMENTED YET ***********");
//			throw new RuntimeException("Reference comparison not implemented yet..");
		}
	}
	
	
	
	
	private ConstraintExpression imposeObjectReferenceEqualsOLD(ObjectReferenceEqual equalConstraint, JacopMugglStore store) {
		IObjectreference objectReference = equalConstraint.getObjectReference();
		if(equalConstraint.getObjectToCompare() instanceof Constant) {
			Constant constantEqualToReference = (Constant)equalConstraint.getObjectToCompare();
			if(constantEqualToReference instanceof IntConstant) {
				IntConstant intConstant = (IntConstant)constantEqualToReference;
				// TODO: get varaible from objectReference
				// TODO: or generate new variable for objectReference
				// and set this new variable to the value of intConstant
//				IntVar jacopVariable = (IntVar)store.getGeneratedObjectVariable(objectReference.getObjectId());
//				IntVar jacopVariable = (IntVar)objectReference.getVariable();
				
				/*
				IntVar jacopVariable = (IntVar)store.getVariable(objectReference);
				
				NumericVariable mugglVariable = null;
				if(store.getVariable(jacopVariable) != null) {
					mugglVariable = (NumericVariable)store.getVariable(jacopVariable);
				}
				
				if(mugglVariable == null) {
					mugglVariable = new NumericVariable(objectReference.getObjectId(), Expression.INT);
				}
				
				
				
				if(jacopVariable == null) {
					if(objectReference.getObjectType().equals("java.lang.Character")) {
						jacopVariable = new IntVar(store, 0, 127);
					} else {
						jacopVariable = new IntVar(store, -50, 50);
					}
					mugglVariable = new NumericVariable(objectReference.getObjectId(), Expression.INT);
					store.addVariable(mugglVariable, jacopVariable);
					objectReference.setVariable(jacopVariable);
//					store.addGeneratedObjectVariable(objectReference.getObjectId(), jacopVariable);
				}
				
				if(objectReferenceConstraint instanceof ObjectReferenceEqual) {
					return NumericEqual.newInstance(mugglVariable, intConstant);
				} else if(objectReferenceConstraint instanceof ObjectReferenceNotEqual) {
					return NumericNotEqual.newInstance(mugglVariable, intConstant);
				}
				*/
			}
		} else {
			System.out.println("*** CONSTRAINT NOT CONSIDERED YET, IMPLEMENT IT: \n " + equalConstraint.toString()  + "\n *************");
		}
		return null;
	}
}
