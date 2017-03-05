package de.wwu.testtool.test;

import java.util.ArrayList;
import java.util.Random;

import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LessOrEqual;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Product;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

// not made to inner classes of DiplomaThesisProblems out of convenience 
enum Variable {INT, DOUBLE};
enum Constant {INT, DOUBLE};
enum IneqType {WEAK, STRICT}

public class DiplomaThesisProblems {
    // randomly generated constraints need to use the same variables
    private ArrayList<NumericVariable> doubleVariables = new ArrayList<NumericVariable>(); 
    private ArrayList<NumericVariable> intVariables = new ArrayList<NumericVariable>();

    /**
     *  The random number generator is initialized each time constraints are generated 
     *  in order to give reproducable results.
     */
    private long RANDOM_SEED;
    private Random generator;

    private int RANDOM_CONSTRAINT_LENGTH;
    private int RANDOM_CONSTRAINT_COUNT;
    
    // double numbers
    private NumericConstant zero = DoubleConstant.getInstance(0);
    private NumericConstant one = DoubleConstant.getInstance(1);
    private NumericConstant two = DoubleConstant.getInstance(2);
    private NumericConstant three = DoubleConstant.getInstance(3);
    private NumericConstant four = DoubleConstant.getInstance(4);
    
    // double variables
    private NumericVariable x = new NumericVariable("x", Expression.DOUBLE);
    private NumericVariable y = new NumericVariable("y", Expression.DOUBLE);
    
    // int variables
    private NumericVariable a = new NumericVariable("a", Expression.INT);
    private NumericVariable b = new NumericVariable("b", Expression.INT);
        
    public DiplomaThesisProblems(int constraintCount, int length, long randomSeed){
	    RANDOM_SEED = randomSeed;
	    generator = new Random(RANDOM_SEED);
	    RANDOM_CONSTRAINT_LENGTH = length;
	    RANDOM_CONSTRAINT_COUNT = constraintCount;
    }
    
    
    /*
     * Helper functions 
     */
    
    /**
     * Converts the given constraint expression into a single constraint by casting.
     * No type checking is done and an exception might be thrown in case of errors.
     * Especially if the ConstraintExpression contains more than a single constraint.
     */
    private SingleConstraint toSingleConstraint(ConstraintExpression ce){
	SubstitutionTable subTable = new SubstitutionTable();
	return (SingleConstraint) ce.convertToComposedConstraint(subTable);
    }
    
    /**
     * Returns the [double, int] variable with the given index.
     * Double variables are named with the scheme x_i, inv variables a_i.
     * 
     * @param varType type of the variable.
     * @param index index of the variable.
     * @return
     */
    private NumericVariable getVariable(Variable varType, int index){
	NumericVariable var;
	if (varType == Variable.DOUBLE){
	    for (int i = doubleVariables.size(); i < index + 1; ++i){
		doubleVariables.add(new NumericVariable("x_" + doubleVariables.size(), Expression.DOUBLE));
	    }
	    var = doubleVariables.get(index);
	} else {
	    for (int i = intVariables.size(); i < index + 1; ++i){
		intVariables.add(new NumericVariable("a_" + intVariables.size(), Expression.INT));
	    }
	    var = intVariables.get(index);
	}
	return var;
    }
    
    /**
     * Returns a randomly initialized [Int, Double]Constant.
     * @param constType
     * @return
     */
    private NumericConstant getRandomConstant(Constant constType){
	NumericConstant constant;
	if (constType == Constant.DOUBLE) {
	    constant = DoubleConstant.getInstance( (generator.nextDouble() * 100.) - 50.);
	} else {
	    constant = IntConstant.getInstance( generator.nextInt(100) );
	}
	return constant; 
    }
    
    /**
     * Produces a random linear constraint according to the options.
     * @param intCoeff if true produces integer coefficients, otherwise double
     * @param intVar if true produces integer variables, otherwise double
     * @param strictInequation if true produces strict inequations, otherwise weak
     * @param length the length of the linear term in the constraint
     * @return
     */
    private SingleConstraint getRandomLinearConstraint(Constant constType, Variable varType, IneqType ineqType, int length){
	if (length < 0) throw new ArrayIndexOutOfBoundsException("Only positive length supported.");
	
	Term term = getRandomConstant(constType);
	term = Sum.newInstance( term, Product.newInstance(getRandomConstant(constType), getVariable(varType, 0)) );
	for (int i = 1; i < length; ++i){
	     if (generator.nextBoolean()){
		 term = Sum.newInstance( term, Product.newInstance(getRandomConstant(constType), getVariable(varType ,i)) );
	     }
	}
	
	ConstraintExpression ce;
	if (ineqType == IneqType.STRICT) {
	    ce = LessThan.newInstance( term, zero); 
	} else if (ineqType == IneqType.WEAK) {
	    ce = LessOrEqual.newInstance( term, zero);
	} else {
	    ce = null;
	}
	
	SingleConstraint sc = toSingleConstraint(ce);
	return sc;
    }
    
    /**
     * Random Linear Arithmetic Problem.
     * @return
     */
    public SingleConstraintSet getRandomSingleConstraintSetProblem(Constant constType, Variable varType, IneqType ineqType ){
	int count = RANDOM_CONSTRAINT_COUNT;
	int length = RANDOM_CONSTRAINT_LENGTH;
	
	// reset generator to have reproducible results
	generator = new Random(RANDOM_SEED);
	
	SingleConstraintSet set = new SingleConstraintSet();
	for (int i = 0; i < count; ++i){
	    set.add( getRandomLinearConstraint(constType, varType, ineqType, length) );
	}
	return set;
    }

    /**
     * The running example.
     * y <= x+3
     * y >= -x +3
     * y >= 0.5 x
     * 
     * and constraint for adding and removal
     * 
     * y >= -0.6*x + 4.5 
     * 
     * @param ineqType
     * @return
     */
    public SingleConstraintSet runningExample(IneqType ineqType){
	// rhs terms
	Term rhs1 = Sum.newInstance( x, three);
	Term rhs2 = Sum.newInstance( Product.newInstance(one.negate(), x) , three);
	Term rhs3 = Product.newInstance(DoubleConstant.getInstance(0.5),x);
	Term rhs_add = Sum.newInstance(Product.newInstance(DoubleConstant.getInstance(0.6).negate(), x), DoubleConstant.getInstance(4.4));	
	
	SingleConstraintSet constraints = new SingleConstraintSet(); 
	
	if (ineqType == IneqType.WEAK) {
	    constraints.add( toSingleConstraint( LessOrEqual.newInstance(y, rhs1) ));
	    constraints.add( toSingleConstraint( GreaterOrEqual.newInstance(y, rhs2) ));
	    constraints.add( toSingleConstraint( GreaterOrEqual.newInstance(y, rhs3) ));
	    constraints.add( toSingleConstraint( GreaterOrEqual.newInstance(y, rhs_add) ));
	} else {
	    constraints.add( toSingleConstraint( LessThan.newInstance(y, rhs1) ) );
	    constraints.add( toSingleConstraint( GreaterThan.newInstance(y, rhs2) ) );
	    constraints.add( toSingleConstraint( GreaterThan.newInstance(y, rhs3) ) );
	    constraints.add( toSingleConstraint( GreaterThan.newInstance(y, rhs_add) ) );
	}
	
	return constraints;
    }

    /**
     * This example will loop forever, because gcd(3,3) = 3.
     * </BR></BR>
     * 3a - 3b >= 1
     * 3a - 3b <= 2
     * </BR></BR>
     * a,b \in \mathbb{Z}
     * @return
     */
    public SingleConstraintSet branchAndBoundLoopProblem() {
	SingleConstraintSet cs = new SingleConstraintSet();
	
	Term lhs = Sum.newInstance(Product.newInstance(three, a), Product.newInstance(three.negate(), b));
	cs.add( toSingleConstraint(GreaterOrEqual.newInstance(lhs, one)) );
	cs.add( toSingleConstraint(LessOrEqual.newInstance( lhs, two )) );

	return cs;
    }
    
    /**
     * 3x - 3y >= 1
     * 3x - 3y <= 2
     * a,b \in \mathbb{R}
     * </BR></BR>
     * @return
     */
    public SingleConstraintSet branchAndBoundLoopProblemRelaxed() {
	SingleConstraintSet cs = new SingleConstraintSet();
	
	Term lhs = Sum.newInstance(Product.newInstance(three, x), Product.newInstance(three.negate(), y));
	cs.add( toSingleConstraint(GreaterOrEqual.newInstance( lhs, one)) );
	cs.add( toSingleConstraint(LessOrEqual.newInstance( lhs, two )) );

	return cs;
    }

    
    /**
     * A simple constraint set that has no solution.
     * @return
     */
    public SingleConstraintSet falseTest() {
	SingleConstraintSet cs = new SingleConstraintSet();
	
	cs.add( toSingleConstraint(LessOrEqual.newInstance(getVariable(Variable.DOUBLE, 0), one)) );
	cs.add( toSingleConstraint(GreaterOrEqual.newInstance(getVariable(Variable.DOUBLE, 0), two)) );
	
	return cs;
    }

    /**
     * 2a + b ≤ 4
     * a + 2b ≤ 3
     * 
     * a, b \in Z
     * 
     * @return
     */
    public SingleConstraintSet simpleBranchAndBoundProblem() {
	SingleConstraintSet cs = new SingleConstraintSet();
	Term lhs;
	
	lhs = Sum.newInstance(Sum.newInstance(Product.newInstance(one, a), Product.newInstance(one, b)), one);
	cs.add( toSingleConstraint(LessOrEqual.newInstance(lhs, four)) );
	
	lhs = Sum.newInstance(Product.newInstance(one, a), Product.newInstance(one, b));
	cs.add( toSingleConstraint(GreaterOrEqual.newInstance( lhs, three )) );
	
	return cs;
    }

}
