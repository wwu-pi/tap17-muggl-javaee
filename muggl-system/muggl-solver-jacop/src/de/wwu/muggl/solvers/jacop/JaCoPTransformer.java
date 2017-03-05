package de.wwu.muggl.solvers.jacop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacop.constraints.Alldifferent;
import org.jacop.constraints.And;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.IfThen;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.SumWeight;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.XgtC;
import org.jacop.constraints.XgtY;
import org.jacop.constraints.XgteqC;
import org.jacop.constraints.XgteqY;
import org.jacop.constraints.XltC;
import org.jacop.constraints.XltY;
import org.jacop.constraints.XlteqC;
import org.jacop.constraints.XlteqY;
import org.jacop.constraints.XmodYeqZ;
import org.jacop.constraints.XmulCeqZ;
import org.jacop.constraints.XmulYeqZ;
import org.jacop.constraints.XneqC;
import org.jacop.constraints.XneqY;
import org.jacop.core.BoundDomain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Var;
import org.jacop.floats.constraints.PeqC;
import org.jacop.floats.constraints.PeqQ;
import org.jacop.floats.constraints.PgtC;
import org.jacop.floats.constraints.PgtQ;
import org.jacop.floats.constraints.PgteqC;
import org.jacop.floats.constraints.PgteqQ;
import org.jacop.floats.constraints.PltC;
import org.jacop.floats.constraints.PltQ;
import org.jacop.floats.constraints.PlteqC;
import org.jacop.floats.constraints.PlteqQ;
import org.jacop.floats.constraints.PminusCeqR;
import org.jacop.floats.constraints.PminusQeqR;
import org.jacop.floats.constraints.PmulCeqR;
import org.jacop.floats.constraints.PmulQeqR;
import org.jacop.floats.constraints.PneqC;
import org.jacop.floats.constraints.PneqQ;
import org.jacop.floats.constraints.PplusCeqR;
import org.jacop.floats.constraints.PplusQeqR;
import org.jacop.floats.constraints.XeqP;
import org.jacop.floats.core.FloatDomain;
import org.jacop.floats.core.FloatIntervalDomain;
import org.jacop.floats.core.FloatVar;

import de.wwu.muggl.solvers.expressions.AllDifferentObjectReference;
import de.wwu.muggl.solvers.expressions.ReferenceVariablesEqualConstraint;
import de.wwu.muggl.solvers.expressions.TypeCast;
import de.wwu.muggl.solvers.expressions.AllDifferent;
import de.wwu.muggl.solvers.expressions.AllDifferentString;
import de.wwu.muggl.solvers.expressions.AndList;
import de.wwu.muggl.solvers.expressions.BinaryOperation;
import de.wwu.muggl.solvers.expressions.CharVariable;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Difference;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.HasLeftAndRightTerms;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LessOrEqual;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.Modulo;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericNotEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Or;
import de.wwu.muggl.solvers.expressions.Product;
import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.expressions.array.IArrayref;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayNotEqual;
import de.wwu.muggl.solvers.expressions.objgen.ObjectReferenceEqual;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectGenerationConstraint;
import de.wwu.muggl.solvers.expressions.objgen.meta.ObjectReferenceComparisonConstraint;
import de.wwu.muggl.solvers.jacop.obj.JacopObjectGenerator;
import de.wwu.muggl.solvers.type.IObjectreference;

/**
 * JaCoPTransformer
 * 
 * Provides static transformation routines,
 * manipulating the state of a JaCoPMugglStore.
 * Entrypoint: {@link JaCoPTransformer#transformAndImpose(ConstraintExpression, JacopMugglStore)}.
 * 
 * @author Jan C. Dageförde, 2015.
 *
 */
public class JaCoPTransformer {
	private static final int DOMAIN_DEPRECIATION =
			1000000;
	
	private static final IntDomain DOMAIN_INTEGER = 
			new BoundDomain(IntDomain.MinInt/DOMAIN_DEPRECIATION,
					IntDomain.MaxInt/DOMAIN_DEPRECIATION);

	private static final FloatDomain DOMAIN_FLOAT = 
			new FloatIntervalDomain(-50, 50);
	
	public static void transformAndImpose(ConstraintExpression ce, JacopMugglStore store) {
		
		if (ce instanceof AndList) {
			imposeAnd((AndList)ce, store);
		} else if (ce instanceof SymbolicCharArrayEqual) {
			imposeCharArrayRefEqual((SymbolicCharArrayEqual) ce, store);
		} else if (ce instanceof SymbolicCharArrayNotEqual) {
			imposeCharArrayRefNotEqual((SymbolicCharArrayNotEqual) ce, store);
		} else if (ce instanceof HasLeftAndRightTerms) {
			imposeEquation((HasLeftAndRightTerms) ce, store);
		} else if (ce instanceof Or) {
			imposeDisjunction((Or)ce, store);
		} else if (ce instanceof AllDifferent) {
			imposeAllDifferent((AllDifferent)ce, store);
		} else if (ce instanceof AllDifferentString) {
			imposeAllDifferentString((AllDifferentString)ce, store);
		} else if (ce instanceof AllDifferentObjectReference) {
			imposeAllDifferentObjectReference((AllDifferentObjectReference)ce, store);
		} else if (ce instanceof ReferenceVariablesEqualConstraint) {
			imposeObjectReferenceEqualConstraint((ReferenceVariablesEqualConstraint)ce, store);
		} else if (ce instanceof ObjectReferenceComparisonConstraint) {
			imposeObjectReferenceComparisonConstraint((ObjectReferenceComparisonConstraint)ce, store);
		} else {
			throw new IllegalArgumentException("Unknown constraint type " + ce.getClass().getName());
		}
	}
	
	private static void imposeCharArrayRefNotEqual(SymbolicCharArrayNotEqual ce, JacopMugglStore store) {
		NumericVariable length1 = ce.getSymbolicArrayref1().getSymbolicLength();
		NumericVariable length2 = ce.getSymbolicArrayref2().getSymbolicLength();
		
		store.impose(new XgteqC((IntVar)store.getVariable(length1), 0));
		store.impose(new XgteqC((IntVar)store.getVariable(length2), 0));
		
		store.impose(new XneqY((IntVar)store.getVariable(length1), (IntVar)store.getVariable(length2)));
	}
	
	
	private static void imposeCharArrayRefEqual(SymbolicCharArrayEqual ce, JacopMugglStore store) {
		NumericVariable length1 = ce.getSymbolicArrayref1().getSymbolicLength();
		NumericVariable length2 = ce.getSymbolicArrayref2().getSymbolicLength();
		
		store.impose(new XgteqC((IntVar)store.getVariable(length1), 0));
		store.impose(new XgteqC((IntVar)store.getVariable(length2), 0));
		store.impose(new XeqY((IntVar)store.getVariable(length1), (IntVar)store.getVariable(length2)));
		
		Set<Integer> inspectedIndexes = new HashSet<>();
		
		Map<Integer,Object> elements1 = ce.getSymbolicArrayref1().getElements();
		Map<Integer,Object> elements2 = ce.getSymbolicArrayref2().getElements();
		
		for(Integer i : elements1.keySet()) {
			CharVariable nv1 = (CharVariable)elements1.get(i);
			CharVariable nv2 = (CharVariable)elements2.get(i);
			if(nv2 == null) {
				nv2 = new CharVariable(ce.getSymbolicArrayref2()+".element"+i, Expression.INT);
				IntVar var = new IntVar(store, -50, 50);
				store.addVariable(nv2, var);
			}
			store.impose(new XeqY((IntVar)store.getVariable(nv1), (IntVar)store.getVariable(nv2)));
			inspectedIndexes.add(i);
		}
		
		for(Integer i : elements2.keySet()) {
			if(inspectedIndexes.contains(i)) continue;
			
			CharVariable nv1 = (CharVariable)elements1.get(i);
			CharVariable nv2 = (CharVariable)elements2.get(i);
			if(nv1 == null) {
				nv1 = new CharVariable(ce.getSymbolicArrayref1()+".element"+i, Expression.INT);
				IntVar var = new IntVar(store, -50, 50);
				store.addVariable(nv1, var);
			}
			store.impose(new XeqY((IntVar)store.getVariable(nv1), (IntVar)store.getVariable(nv2)));
			inspectedIndexes.add(i);
		}
	}

	private static void imposeObjectReferenceEqualConstraint(ReferenceVariablesEqualConstraint ce, JacopMugglStore store) {
		IntVar var1 = store.getObjectConstraintManager().getInternalVariable(ce.getObjectReference1());
		IntVar var2 = store.getObjectConstraintManager().getInternalVariable(ce.getObjectReference2());
		Constraint c = new XeqY(var1, var2);
		store.impose(c);
	}

	private static void imposeAllDifferentObjectReference(AllDifferentObjectReference ce, JacopMugglStore store) {
		Set<IntVar> uniqueVariables = new HashSet<IntVar>();
		for(IObjectreference var : ce.getVariables() ) {
			IntVar variable = store.getObjectConstraintManager().getInternalVariable(var);
			uniqueVariables.add(variable);
		}
		Constraint c = new Alldifferent(uniqueVariables.toArray(new IntVar[uniqueVariables.size()]));
		store.impose(c);
	}

	private static void imposeAnd(AndList a, JacopMugglStore store) {
		for(ConstraintExpression ce : a.getConstraintExpressions()) {
			transformAndImpose(ce, store);
		}
	}
	
	private static void imposeObjectReferenceComparisonConstraint(ObjectReferenceComparisonConstraint objGenConstraint, JacopMugglStore store) {
		JacopObjectGenerator.imposeObjectReferenceConstraint(objGenConstraint, store);
	}
	
	
	private static void imposeAllDifferentString(AllDifferentString allDifferentString, JacopMugglStore store) {
		IArrayref[] arrayReferences = allDifferentString.getDifferentSymbolicArrays();
		if(arrayReferences != null && arrayReferences.length > 1) {
			for(int i=0; i<arrayReferences.length-1; i++) {
				for(int j=i+1; j<arrayReferences.length; j++) {
					
					
					
					if(arrayReferences[i] instanceof ISymbolicArrayref
					  && arrayReferences[j] instanceof ISymbolicArrayref) {
						// both strings are symbolic
						IntVar length1 = (IntVar) store.getVariable((Variable)((ISymbolicArrayref)arrayReferences[i]).getSymbolicLength());
						IntVar length2 = (IntVar) store.getVariable((Variable)((ISymbolicArrayref)arrayReferences[j]).getSymbolicLength());
						XeqY ifLengthEqualConstraint = new XeqY(length1, length2);
						
//						throw new RuntimeException("Not implemented yet here");
						
						ConstraintExpression ce = NumericNotEqual.newInstance(
								((ISymbolicArrayref)arrayReferences[i]).getSymbolicLength(),
								((ISymbolicArrayref)arrayReferences[j]).getSymbolicLength());
						transformAndImpose(ce, store);
					}
					
					else if(arrayReferences[i] instanceof ISymbolicArrayref
							&& !(arrayReferences[j] instanceof ISymbolicArrayref)) {
						// first string is symbolic, second string is 'concrete'
						imposeSymbolicArrayrefAndNormalArrayrefDifferent((ISymbolicArrayref)arrayReferences[i], (IArrayref)arrayReferences[j], store);
						
//						IArrayref arrayRef = (IArrayref)arrayReferences[j];
//						
//						IntVar symLength = (IntVar) store.getVariable((Variable)((ISymbolicArrayref)arrayReferences[i]).getSymbolicLength());
//						XeqC ifLengthEqualConstraint = new XeqC(symLength, arrayRef.getLength());
//						
//						
//						ISymbolicArrayref symArray = (ISymbolicArrayref)arrayReferences[i];
//						ArrayList<PrimitiveConstraint> orConstraints = new ArrayList<>();
//						for(int index=0; index<arrayRef.getLength(); index++) {
//							NumericVariable eleVar = (NumericVariable)symArray.getElement(index);
//							if(eleVar == null) {
//								// element does not exist in symbolic array, create a new integer variable here
//								// the element is potentially not used, i.e. if the length of sym array is lower than the current index...
//								eleVar = new NumericVariable(symArray.getName()+"."+index, Expression.INT);
//								symArray.setElementAt(index, eleVar);
//								IntVar jv = new IntVar(store, 34, 127);
//								store.addVariable(eleVar, jv);
//							}
//							IntVar jacopVar = (IntVar)store.getVariable(eleVar);
//							int arrayValue = ((IntConstant)arrayRef.getElement(index)).getIntValue();
//							orConstraints.add(new XneqC(jacopVar, arrayValue));
//						}
//						
//						PrimitiveConstraint thenConstraint = new org.jacop.constraints.Or(orConstraints);						
//						IfThen ifThen = new IfThen(ifLengthEqualConstraint, thenConstraint);
//						store.impose(ifThen);
					}
					
					else if(!(arrayReferences[i] instanceof ISymbolicArrayref)
							&& arrayReferences[j] instanceof ISymbolicArrayref) {
						// first string is 'concrete', second string is symoblic
						
						imposeSymbolicArrayrefAndNormalArrayrefDifferent((ISymbolicArrayref)arrayReferences[j], (IArrayref)arrayReferences[i], store);
						
						
						
//						IArrayref arrayRef = (IArrayref)arrayReferences[i];						
//						
//						IntVar symLength = (IntVar) store.getVariable((Variable)((ISymbolicArrayref)arrayReferences[j]).getSymbolicLength());
//						XeqC ifLengthEqualConstraint = new XeqC(symLength, arrayRef.getLength());
//						
//						throw new RuntimeException("Not implemented yet here");
					}
					
					else if(!(arrayReferences[i] instanceof ISymbolicArrayref)
							&& !(arrayReferences[j] instanceof ISymbolicArrayref)) {
						// both strings are 'concrete'
						IArrayref arrayRef1 = (IArrayref)arrayReferences[i];
						IArrayref arrayRef2 = (IArrayref)arrayReferences[j];
						
						int length1 = arrayRef1.getLength();
						int length2 = arrayRef2.getLength();
						
						boolean notEqual = true;
						if(length1 == length2) {
							notEqual = false; // if both have the same length, they are initially 'not equal'
							// they become 'not equal', if at least one of their value elements is 'not equal'
							for(int index=0; index<length1; index++) {
								IntConstant value1 = (IntConstant)arrayRef1.getElement(index);
								IntConstant value2 = (IntConstant)arrayRef2.getElement(index);
								
								if(value1.getIntValue() != value2.getIntValue()) {
									notEqual = true;
								}
							}
						}
						if(!notEqual) {
							throw new RuntimeException("BOTH STRINGS ARE THE SAME");
						}
					}
					
				}
			}
		}
	}
	
	
	private static void imposeSymbolicArrayrefAndNormalArrayrefDifferent(ISymbolicArrayref symArray, IArrayref arrayRef, JacopMugglStore store) {
		IntVar symLength = (IntVar) store.getVariable((Variable)symArray.getSymbolicLength());
		XeqC ifLengthEqualConstraint = new XeqC(symLength, arrayRef.getLength());

		ArrayList<PrimitiveConstraint> orConstraints = new ArrayList<>();
		for(int index=0; index<arrayRef.getLength(); index++) {
			NumericVariable eleVar = (NumericVariable)symArray.getElement(index);
			if(eleVar == null) {
				// element does not exist in symbolic array, create a new integer variable here
				// the element is potentially not used, i.e. if the length of sym array is lower than the current index...
				eleVar = new NumericVariable(symArray.getName()+"."+index, Expression.INT);
				symArray.setElementAt(index, eleVar);
				IntVar jv = new IntVar(store, 34, 127);
				store.addVariable(eleVar, jv);
			}
			IntVar jacopVar = (IntVar)store.getVariable(eleVar);
			int arrayValue = ((IntConstant)arrayRef.getElement(index)).getIntValue();
			orConstraints.add(new XneqC(jacopVar, arrayValue));
		}
		
		PrimitiveConstraint thenConstraint = new org.jacop.constraints.Or(orConstraints);						
		IfThen ifThen = new IfThen(ifLengthEqualConstraint, thenConstraint);
		store.impose(ifThen);
	}
	

	private static void imposeAllDifferent(AllDifferent allDifferent, JacopMugglStore store) {
		List<IntVar> jacopAllDiffVarList = new ArrayList<>();
		Set<StringVariable> allDiffStringList = new HashSet<>();
		for(Expression ex : allDifferent.getVariables()) {
			if(ex instanceof StringVariable) {
				StringVariable mugglStringVar = (StringVariable)ex;
				allDiffStringList.add(mugglStringVar);
			}
			else if(ex instanceof Variable) {
				Variable mugglVar = (Variable)ex;
				Var jacopVar = store.getVariable(mugglVar);
				if(jacopVar == null) {
					jacopVar = new IntVar(store, 0, 50); // TODO: namen?
					store.addVariable(mugglVar, jacopVar);
				}
				if(jacopVar instanceof IntVar) {
					jacopAllDiffVarList.add((IntVar)jacopVar);
				}
			}
			else if(ex instanceof IntConstant) {
				IntConstant constant = (IntConstant)ex;
				IntVar jacopConstant = new IntVar(store, 0, 50); // TODO: namen?
				jacopAllDiffVarList.add(jacopConstant);
				
				Constraint equalConstraint = new XeqC(jacopConstant, constant.getIntValue());
				store.impose(equalConstraint);
			}			
		}
		if(jacopAllDiffVarList.size() > 0) {
			Alldifferent jacopAllDiff = new Alldifferent(jacopAllDiffVarList.toArray(new IntVar[jacopAllDiffVarList.size()]));
			store.impose(jacopAllDiff);
		}
		if(allDiffStringList.size() > 0) {
			
			// asci zeichen ab 32 sinnvoll mehr oder weniger
			// fixe lenge von 5 pro string
			Set<IntVar> lengthVariables = new HashSet<>();
			for(StringVariable stringVar : allDiffStringList) {
				NumericVariable nvLength = stringVar.getLength();
				Var jacopVar = store.getVariable(nvLength);
				if(jacopVar == null) {
					jacopVar = new IntVar(store, 1, 50);
					store.addVariable(nvLength, jacopVar);
				}
				if(jacopVar instanceof IntVar) {
					lengthVariables.add((IntVar)jacopVar);
				}
			}
			
			Alldifferent jacopAllDiff = new Alldifferent(lengthVariables.toArray(new IntVar[lengthVariables.size()]));
			store.impose(jacopAllDiff);
		}
	}

	private static void imposeDisjunction(Or ce, JacopMugglStore store) {
		ConstraintExpression e1 = ce.getE1();
		ConstraintExpression e2 = ce.getE1();

		PrimitiveConstraint consE1 = generateEquationConstraint((HasLeftAndRightTerms) e1, store);
		PrimitiveConstraint consE2 = generateEquationConstraint((HasLeftAndRightTerms) e2, store);
		
		org.jacop.constraints.Or resultingConstraint = new org.jacop.constraints.Or(consE1, consE2);
		store.impose(resultingConstraint);
	}

	private static void imposeEquation(HasLeftAndRightTerms ce, JacopMugglStore store) {
		//System.out.println("Transforming equation " + ce);
		Term left = ce.getLeft();
		Term right = ce.getRight();
		
		if (left.isConstant() && right.isConstant()) { // sanity check (AND no JaCoP constraint would exist for this)
			throw new IllegalStateException("Both terms of the equation " + ce + " are constant. Why was this constraint added in the first place?");
		}

		Constraint resultingConstraint = generateEquationConstraint(ce, store);
		
		store.impose(resultingConstraint);
		
	}

	/**
	 * @param ce
	 * @param store
	 * @param left
	 * @param right
	 * @param resultingConstraint
	 * @return
	 */
	private static PrimitiveConstraint generateEquationConstraint(
			HasLeftAndRightTerms ce, JacopMugglStore store) {
		Term left = ce.getLeft();
		Term right = ce.getRight();
		
		boolean hasConstant = left.isConstant() || right.isConstant();
		boolean isInteger = isInteger(left) && isInteger(right);
		
		if (isInteger) {
			if (hasConstant) {
				// {{ Integer and constant
				int constantValue = left.isConstant() ? ((NumericConstant)left).getIntValue() : ((NumericConstant)right).getIntValue();
				Term variableTerm = left.isConstant() ? right : left;
				
				// Decide whether sides will need to be switched, as constants may only appear as the right term in JaCoP.
				// This implies mirroring the comparison for lt(e)/gt(e)
				boolean switchSides = left.isConstant();

				IntVar termVar = normaliseIntegerTerm(variableTerm, store);
				// assert: termVar == variableTerm
				
				if (ce instanceof LessOrEqual) {
					if (switchSides) { 
						return new XgteqC(termVar, constantValue);
					} else {
						return new XlteqC(termVar, constantValue);
					}
				} else if (ce instanceof LessThan) {
					if (switchSides) { 
						return new XgtC(termVar, constantValue);
					} else {
						return new XltC(termVar, constantValue);
					}
				} else if (ce instanceof GreaterOrEqual) {
					if (switchSides) { 
						return new XlteqC(termVar, constantValue);
					} else {
						return new XgteqC(termVar, constantValue);
					}
				} else if (ce instanceof GreaterThan) {
					if (switchSides) { 
						return new XltC(termVar, constantValue);
					} else {
						return new XgtC(termVar, constantValue);
					}
				} else if (ce instanceof NumericEqual) {
					return new XeqC(termVar, constantValue);
				} else if (ce instanceof NumericNotEqual) {
					return new XneqC(termVar, constantValue);
				} else {
					// other type of equation with constant
					throw new IllegalArgumentException("Unknown (with-constant, all-int) constraint type " + ce.getClass().getName());
				}
				// }}
			} else {
				// {{ no constant, all int
				
				IntVar leftTermVar = normaliseIntegerTerm(ce.getLeft(), store);
				IntVar rightTermVar = normaliseIntegerTerm(ce.getRight(), store);
				
				if (ce instanceof NumericEqual) {
					return new XeqY(leftTermVar, rightTermVar);
				} else if (ce instanceof NumericNotEqual) {
					return new XneqY(leftTermVar, rightTermVar);
				} else if (ce instanceof LessThan) {
					return new XltY(leftTermVar, rightTermVar);
				} else if (ce instanceof LessOrEqual) {
					return new XlteqY(leftTermVar, rightTermVar);
				} else if (ce instanceof GreaterThan) {
					return new XgtY(leftTermVar, rightTermVar);
				} else if (ce instanceof GreaterOrEqual) {
					return new XgteqY(leftTermVar, rightTermVar);
				} else {
					throw new IllegalArgumentException("Unknown (non-constant, all-int) constraint type " + ce.getClass().getName());
				}
				// }}
			}
			
		} else {
			// At least one variable is a float variable.
			if (hasConstant) {
				// {{ Float and constant
				double constantValue = left.isConstant() ? ((NumericConstant)left).getDoubleValue() : ((NumericConstant)right).getDoubleValue();
				Term variableTerm = left.isConstant() ? right : left;
				
				// Decide whether sides will need to be switched, as constants may only appear as the right term in JaCoP.
				// This implies mirroring the comparison for lt(e)/gt(e)
				boolean switchSides = left.isConstant();

				FloatVar termVar;
				if (isInteger(variableTerm)) {
					IntVar termInt = normaliseIntegerTerm(variableTerm, store);
					termVar = new FloatVar(store, DOMAIN_FLOAT);
					store.impose( new XeqP(termInt, termVar) );
				} else {
					termVar = normaliseFloatTerm(variableTerm, store);
				}
				
				if (ce instanceof LessOrEqual) {
					if (switchSides) { 
						return new PgteqC(termVar, constantValue);
					} else {
						return new PlteqC(termVar, constantValue);
					}
				} else if (ce instanceof LessThan) {
					if (switchSides) { 
						return new PgtC(termVar, constantValue);
					} else {
						return new PltC(termVar, constantValue);
					}
				} else if (ce instanceof GreaterOrEqual) {
					if (switchSides) { 
						return new PlteqC(termVar, constantValue);
					} else {
						return new PgteqC(termVar, constantValue);
					}
				} else if (ce instanceof GreaterThan) {
					if (switchSides) { 
						return new PltC(termVar, constantValue);
					} else {
						return new PgtC(termVar, constantValue);
					}
				} else if (ce instanceof NumericEqual) {
					return new PeqC(termVar, constantValue);
				} else if (ce instanceof NumericNotEqual) {
					return new PneqC(termVar, constantValue);
				} else {
					// other type of equation with constant
					throw new IllegalArgumentException("Unknown (with-constant, floating-point) constraint type " + ce.getClass().getName());
				}
				// }}
			} else {
				// {{ no constant, with float

				FloatVar leftTermVar;
				FloatVar rightTermVar; 
				if (isInteger(ce.getLeft())) {
					IntVar leftTermInt = normaliseIntegerTerm(ce.getLeft(), store);
					leftTermVar = new FloatVar(store, DOMAIN_FLOAT);
					store.impose( new XeqP(leftTermInt, leftTermVar) );
				} else {
					leftTermVar = normaliseFloatTerm(ce.getLeft(), store);
				}
				if (isInteger(ce.getRight())) {
					IntVar rightTermInt = normaliseIntegerTerm(ce.getRight(), store);
					rightTermVar = new FloatVar(store, DOMAIN_FLOAT);
					store.impose( new XeqP(rightTermInt, rightTermVar) );
				} else {
					rightTermVar = normaliseFloatTerm(ce.getRight(), store);
				}
				
				if (ce instanceof NumericEqual) {
					return new PeqQ(leftTermVar, rightTermVar);
				} else if (ce instanceof NumericNotEqual) {
					return new PneqQ(leftTermVar, rightTermVar);
				} else if (ce instanceof LessThan) {
					return new PltQ(leftTermVar, rightTermVar);
				} else if (ce instanceof LessOrEqual) {
					return new PlteqQ(leftTermVar, rightTermVar);
				} else if (ce instanceof GreaterThan) {
					return new PgtQ(leftTermVar, rightTermVar);
				} else if (ce instanceof GreaterOrEqual) {
					return new PgteqQ(leftTermVar, rightTermVar);
				} else {
					throw new IllegalArgumentException("Unknown (non-constant, floating-point) constraint type " + ce.getClass().getName());
				}
				// }}
			}
		}
	}

	private static FloatVar normaliseFloatTerm(Term floatTerm,
			JacopMugglStore store) {
		if (floatTerm instanceof NumericConstant) {
			// for a constant, just add a variable with a very restricted domain. Should save one constraint.
			double constantValue = ((NumericConstant)floatTerm).getDoubleValue();
			return new FloatVar(store, constantValue, constantValue);
		} else if (floatTerm instanceof NumericVariable) {
			FloatVar floatVar = (FloatVar) store.getVariable((Variable)floatTerm);
			
			if (floatVar == null) {
				floatVar = new FloatVar(store, 
					((NumericVariable)floatTerm).getInternalName(), 
					DOMAIN_FLOAT);
				// add to cache
				store.addVariable((Variable)floatTerm, floatVar);
			}
			return floatVar;
		} else if (floatTerm instanceof TypeCast) {
			TypeCast cast = (TypeCast)floatTerm;
			// implicit: this casts from integer to float
			
			IntVar original = normaliseIntegerTerm(cast.getInternalTerm(), store);
			FloatVar casted = new FloatVar(store, DOMAIN_FLOAT);
			store.impose(new XeqP(original, casted));
			return casted;
		} else if (floatTerm instanceof Sum) {
			Sum floatSum = (Sum) floatTerm;
			if (floatSum.getLeft().isConstant() || floatSum.getRight().isConstant()) {
				// one term is constant
				double constantValue = floatSum.getLeft().isConstant() ? ((NumericConstant)floatSum.getLeft()).getDoubleValue() : ((NumericConstant)floatSum.getRight()).getDoubleValue();
				Term variableTerm = floatSum.getLeft().isConstant() ? floatSum.getRight() : floatSum.getLeft();
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar variableJaCoP = normaliseFloatTerm(variableTerm, store);
				store.impose(
						new PplusCeqR(variableJaCoP, constantValue, 
								floatVar)
						);
				return floatVar;
				
			} else {
				// no term is constant
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar lhs = normaliseFloatTerm(floatSum.getLeft(), store);
				FloatVar rhs = normaliseFloatTerm(floatSum.getRight(), store);
				store.impose(
						new PplusQeqR(lhs, rhs, 
								floatVar)
						);
				return floatVar;
			}
		} else if (floatTerm instanceof Difference) {
			Difference floatDifference = (Difference) floatTerm;

			if (floatDifference.getRight().isConstant()) {
				// right term is constant
				// Note: Only right may be constant, as there is only P-C=R -- no C-P=R or T+P=C!
				// For achieving T-C=-P (only valid form of the equation that has a constant at second place),
				// an additional constraint would be required to obtain -P.
				// Consequently, this is not treated individually in order to avoid an additional special case.
				double rhs = ((NumericConstant)floatDifference.getRight()).getDoubleValue();
				Term lhs = floatDifference.getLeft();
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar variableJaCoP = normaliseFloatTerm(lhs, store);
				store.impose(
						new PminusCeqR(variableJaCoP, rhs, 
								floatVar)
						);
				return floatVar;
				
			} else {
				// no term or left term is constant
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar lhs = normaliseFloatTerm(floatDifference.getLeft(), store);
				FloatVar rhs = normaliseFloatTerm(floatDifference.getRight(), store);
				store.impose(
						new PminusQeqR(lhs, rhs, 
								floatVar)
						);
				return floatVar;
			}
		} else if (floatTerm instanceof Product) {
			Product floatProduct = (Product) floatTerm;
			if (floatProduct.getLeft().isConstant() || floatProduct.getRight().isConstant()) {
				// one term is constant
				double constantValue = floatProduct.getLeft().isConstant() ? ((NumericConstant)floatProduct.getLeft()).getDoubleValue() : ((NumericConstant)floatProduct.getRight()).getDoubleValue();
				Term variableTerm = floatProduct.getLeft().isConstant() ? floatProduct.getRight() : floatProduct.getLeft();
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar variableJaCoP = normaliseFloatTerm(variableTerm, store);
				store.impose(
						new PmulCeqR(variableJaCoP, constantValue, 
								floatVar)
						);
				return floatVar;
				
			} else {
				// no term is constant
				FloatVar floatVar = new FloatVar(store, DOMAIN_FLOAT);
				FloatVar lhs = normaliseFloatTerm(floatProduct.getLeft(), store);
				FloatVar rhs = normaliseFloatTerm(floatProduct.getRight(), store);
				store.impose(
						new PmulQeqR(lhs, rhs, 
								floatVar)
						);
				return floatVar;
			}
		}
		throw new IllegalArgumentException("Unknown float term type " + floatTerm.getClass().getName());
	}

	private static IntVar normaliseIntegerTerm(Term integerTerm, JacopMugglStore store) {
		if (integerTerm instanceof NumericConstant) {
			// for a constant, just add a variable with a very restricted domain. Should save one constraint.
			int constantValue = ((NumericConstant)integerTerm).getIntValue();
			return new IntVar(store, constantValue, constantValue);
		} else if (integerTerm instanceof CharVariable) {
			IntVar intVar = (IntVar) store.getVariable((Variable)integerTerm);
			if (intVar == null) {
				intVar = new IntVar(store, ((NumericVariable)integerTerm).getInternalName(), 35, 127);
				store.addVariable((Variable)integerTerm, intVar);
			}
			return intVar;
		} else if (integerTerm instanceof NumericVariable) {
			if ( !((NumericVariable)integerTerm).isInteger() ) {
				throw new IllegalStateException("Trying to normalise an integer term that contains a non-integer Variable"); 
			}
			
			IntVar intVar = (IntVar) store.getVariable((Variable)integerTerm);
			
			if (intVar == null) {
				intVar = new IntVar(store, 
					((NumericVariable)integerTerm).getInternalName(), 
					DOMAIN_INTEGER);
				// add to cache
				store.addVariable((Variable)integerTerm, intVar);
			}
			return intVar;
		} else if (integerTerm instanceof TypeCast) {
			TypeCast cast = (TypeCast)integerTerm;
			// implicit: this casts from float to integer
			
			FloatVar original = normaliseFloatTerm(cast.getInternalTerm(), store);
			IntVar casted = new IntVar(store, DOMAIN_INTEGER);
			store.impose(new XeqP(casted, original));
			return casted;
		} else if (integerTerm instanceof Sum || integerTerm instanceof Difference) {
			return normaliseIntegerSumOrDifference((BinaryOperation)integerTerm, store);
		} else if (integerTerm instanceof Product) {
			return normaliseIntegerProduct((Product)integerTerm, store);
		} else if (integerTerm instanceof Modulo) {
			IntVar lhs = normaliseIntegerTerm(((Modulo)integerTerm).getLeft(), store);
			IntVar rhs = normaliseIntegerTerm(((Modulo)integerTerm).getRight(), store);
			IntVar result = new IntVar(store, DOMAIN_INTEGER);
			store.impose(new XmodYeqZ(lhs, rhs, result));
			return result;
		} else {
			throw new IllegalArgumentException("Unknown integer term type " + integerTerm.getClass().getName());
		}
		
	}

	/**
	 * @param integerProduct
	 * @param store
	 * @return
	 */
	private static IntVar normaliseIntegerProduct(Product integerProduct,
			JacopMugglStore store) {
		// Returns an intermediate variable, and impose a constraint that this intermediate
		// variable be the product of its two factors. Note that a term may be anything (e.g. a Product),
		// so that this function may work recursively until reaching a constant or variable 
		IntVar intermediateVariable = new IntVar(store, DOMAIN_INTEGER);
		
		Constraint productConstraint;
		
		
		if (integerProduct.getLeft().isConstant() || integerProduct.getRight().isConstant()) {
			int constantValue = integerProduct.getLeft().isConstant() ? ((IntConstant)integerProduct.getLeft()).getValue() : ((IntConstant)integerProduct.getRight()).getValue();
			Term variableTerm = integerProduct.getLeft().isConstant() ? integerProduct.getRight() : integerProduct.getLeft();
			
			productConstraint = new XmulCeqZ(
					normaliseIntegerTerm(variableTerm, store),
					constantValue, 
					intermediateVariable);
		} else {
			productConstraint = new XmulYeqZ(
					normaliseIntegerTerm(integerProduct.getLeft(), store),
					normaliseIntegerTerm(integerProduct.getRight(), store), 
					intermediateVariable);
		}
		store.impose(productConstraint);
		return intermediateVariable;
	}

	/**
	 * Returns an intermediate variable, and impose a constraint that this intermediate
	 * variable be the sum of its two terms. In case one or both terms are again a Sum,
	 * the resulting constraint is composed from all comprised Sums.		
	 * @param integerSum Term that is a sum
	 * @param store JaCoPStore used for storing the resulting constraint
	 * @return One IntVar that is constrained to be equal to the Sum
	 */
	private static IntVar normaliseIntegerSumOrDifference(BinaryOperation integerSum,
			JacopMugglStore store) {
		
		ArrayList<IntVar> termList = new ArrayList<IntVar>();
		ArrayList<Integer> weightList = new ArrayList<Integer>();
		boolean allWeightsOne = true;
		
		allWeightsOne = normaliseIntegerSumRecursive(integerSum, store, termList,
				weightList, false); // false, since the entire term is treated as positive for now.

		// Compose and impose Sum constraint
		IntVar intermediateVariable = new IntVar(store, DOMAIN_INTEGER);
		Constraint sumConstraint;
		if (allWeightsOne) {
			sumConstraint = new SumInt(store, termList, "==", intermediateVariable);
		} else {
			sumConstraint = new SumWeight(termList, weightList, intermediateVariable);
		}

		store.impose(sumConstraint);
		store.consistency(); // do a consistency check here, dont know if this is really best to do here, but it works (somehow)
		return intermediateVariable;
		
	}

	/**
	 * @param integerSum
	 * @param store
	 * @param termList
	 * @param weightList
	 * @param negate True if the entire term is to be treated as negative (e.g. right hand side of a difference) 
	 * @return
	 */
	private static boolean normaliseIntegerSumRecursive(BinaryOperation integerSum,
			JacopMugglStore store, ArrayList<IntVar> termList,
			ArrayList<Integer> weightList, boolean negate) {
		boolean leftAllWeightsEqOne;
		boolean rightAllWeightsEqOne;
		
		if (integerSum.getLeft() instanceof Sum ||
				integerSum.getLeft() instanceof Difference) {
			leftAllWeightsEqOne = normaliseIntegerSumRecursive((BinaryOperation) integerSum.getLeft(), store, termList, weightList, negate);
		} else {
			leftAllWeightsEqOne = processIntegerTermOrSimpleProductAndTestWeightEqOne(store, 
					termList, weightList, integerSum.getLeft(), negate);
		}
		
		// Right hand side must be negative if EITHER entire term is negated XOR current term is a difference.
		boolean negateRHS = negate ^ (integerSum instanceof Difference);
		
		if (integerSum.getRight() instanceof Sum ||
				integerSum.getRight() instanceof Difference) {
			rightAllWeightsEqOne = normaliseIntegerSumRecursive((BinaryOperation) integerSum.getRight(), store, termList, weightList, negateRHS);
		} else {
			rightAllWeightsEqOne = processIntegerTermOrSimpleProductAndTestWeightEqOne(store, 
					termList, weightList, integerSum.getRight(), negateRHS);
		}
		
		return leftAllWeightsEqOne && rightAllWeightsEqOne;
	}

	private static boolean processIntegerTermOrSimpleProductAndTestWeightEqOne(JacopMugglStore store,
			ArrayList<IntVar> termList, ArrayList<Integer> weightList, Term term, boolean negate) {
		
		int weight;
		
		if (isSimpleProduct(term)) {
			Product p = (Product)term;
			
			Term constantTerm;
			Term variableTerm;
			
			if (p.getLeft().isConstant()) {
				constantTerm = p.getLeft();
				variableTerm = p.getRight();
			} else {
				constantTerm = p.getRight();
				variableTerm = p.getLeft();
			}
			
			termList.add( normaliseIntegerTerm(variableTerm, store) );
			
			weight = ((IntConstant)constantTerm).getIntValue();
			if (negate) { 
				weight = -weight; 
			}
			weightList.add( weight );
		} else {
			termList.add( normaliseIntegerTerm(term, store) );
			
			weight = negate ? -1 : 1;
			weightList.add( weight );
		}
		
		return weight == 1;
	}

	private static boolean isSimpleProduct(Term term) {
		if (!(term instanceof Product)) {
			return false;
		}
				
		Product p = (Product)term;
		
		boolean leftIsConstant = p.getLeft().isConstant();
		boolean rightIsConstant = p.getRight().isConstant();
		
		if (!leftIsConstant && !rightIsConstant) {
			return false;
		}
		
		return true;
	}

	private static boolean isInteger(Term term) {
		if (term instanceof IntConstant || term instanceof LongConstant) {
			return true;
		} else if (term instanceof DoubleConstant || term instanceof FloatConstant) {
			return false;
		} else if (term instanceof NumericVariable) {
			return ((NumericVariable)term).isInteger();
		} else if (term instanceof BinaryOperation) {
			return isInteger( ((BinaryOperation)term).getLeft() ) && 
					isInteger( ((BinaryOperation)term).getRight() );
		} else if (term instanceof TypeCast) {
			return Term.isIntegerType(((TypeCast)term).getType());
		}
		throw new IllegalArgumentException("Unknown term type " + term.getClass().getName());
	}
	
	/**
	 * DEBUGGING HELPER METHOD.
	 * Gets ID of current JaCoP DFS instance. Used for conditional breakpoints.
	 * Necessary, because ID value is protected. Therefore, this method uses
	 * reflection to make the ID value visible if needed.
	 * @return ID of current JaCoP DFS instance
	 */
	@SuppressWarnings("unused")
	private static int checkDFSid() {
		java.lang.reflect.Field f;
		try {
			f = org.jacop.search.DepthFirstSearch.class.getDeclaredField("no");
			f.setAccessible(true);
			return f.getInt(null);
		} catch (SecurityException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		}
	}
	
}
