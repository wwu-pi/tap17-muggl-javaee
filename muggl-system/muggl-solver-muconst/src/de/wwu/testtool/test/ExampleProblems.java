package de.wwu.testtool.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.*;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.testtool.solver.SolverManagerOld;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.Equation;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.numbers.DoubleWrapper;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * @author Marko Ernsting
 *
 */
@SuppressWarnings("unused")
public class ExampleProblems {
    
    SolverManager solverManager;

    // some numbers and variables
    private NumericConstant zero = DoubleConstant.getInstance(0);
    private NumericConstant one = DoubleConstant.getInstance(1);
    private NumericConstant two = DoubleConstant.getInstance(2);
    private NumericConstant three = DoubleConstant.getInstance(3);
    private NumericConstant four = DoubleConstant.getInstance(4);
    private NumericConstant five = DoubleConstant.getInstance(5);
    private NumericConstant six = DoubleConstant.getInstance(6);
    private NumericConstant seven = DoubleConstant.getInstance(7);
    private NumericConstant eight = DoubleConstant.getInstance(8);
    private NumericConstant nine = DoubleConstant.getInstance(9);

    private NumericConstant zero_int = IntConstant.getInstance(0);
    private NumericConstant one_int = IntConstant.getInstance(1);
    private NumericConstant two_int = IntConstant.getInstance(2);
    private NumericConstant three_int = IntConstant.getInstance(3);
    private NumericConstant four_int = IntConstant.getInstance(4);
    private NumericConstant five_int = IntConstant.getInstance(5);
    private NumericConstant six_int = IntConstant.getInstance(6);
    private NumericConstant seven_int = IntConstant.getInstance(7);
    private NumericConstant eight_int = IntConstant.getInstance(8);
    private NumericConstant nine_int = IntConstant.getInstance(9);
    
    
    NumericVariable w = new NumericVariable("w", Expression.DOUBLE);
    NumericVariable x = new NumericVariable("x", Expression.DOUBLE);
    NumericVariable y = new NumericVariable("y", Expression.DOUBLE);
    NumericVariable z = new NumericVariable("z", Expression.DOUBLE);


    NumericVariable a = new NumericVariable("a", Expression.INT);
    NumericVariable b = new NumericVariable("b", Expression.INT);
    NumericVariable c = new NumericVariable("c", Expression.INT);

    
    // logging
    private Logger texLogger;
    private Logger logger;
    private Logger console;
    
    Random generator = new Random(2);
    ArrayList<NumericVariable> doubleVariables = new ArrayList<NumericVariable>(); 
    ArrayList<NumericVariable> intVariables = new ArrayList<NumericVariable>();
    
    public ComposedConstraint toComposedConstraint(ConstraintExpression ce){
	SubstitutionTable subTable = new SubstitutionTable();
	return ce.convertToComposedConstraint(subTable);
    }
    
    public NumericVariable getDoubleVariable(int index){
	for (int i = doubleVariables.size(); i < index + 1; ++i){
	    doubleVariables.add(new NumericVariable("x_" + doubleVariables.size(), Expression.DOUBLE));
	}
	return doubleVariables.get(index);
    }
    
    public NumericVariable getIntVariable(int index){
	for (int i = intVariables.size(); i < index + 1; ++i){
	    intVariables.add(new NumericVariable("x_" + intVariables.size(), Expression.INT));
	}
	return intVariables.get(index);
    }

    public DoubleConstant getRandomDoubleConstant(){
	//return DoubleConstant.getInstance(generator.nextInt(100));
	return DoubleConstant.getInstance( (generator.nextDouble()-0.5) * 10. );
    }
    
    /**
     * Produces a (not always solvable) linear constraint with random double values
     * and double variables.
     * 
     * @param length how many variables at maximum.
     * @return
     */
    public ConstraintExpression getRandomDoubleCoeffDoubleVarLinearConstraint(int length){
	if (length < 0) throw new ArrayIndexOutOfBoundsException("Only positive length supported.");
	Term lhs = getRandomDoubleConstant();
	lhs = Sum.newInstance( lhs, Product.newInstance(getRandomDoubleConstant(), getDoubleVariable(0)) );
	for (int i = 1; i < length; ++i){
	     if (generator.nextBoolean()){
		 lhs = Sum.newInstance( lhs, Product.newInstance(getRandomDoubleConstant(), getDoubleVariable(i)) );
	     }
	}
	return LessOrEqual.newInstance(lhs, zero);
//	return LessThan.newInstance(lhs, zero);
    }    

    /**
     * Produces a (not always solvable) linear constraint with random double values
     * and integer variables.
     * 
     * @param how many variables at maximum.
     * @return
     */
    public ConstraintExpression getRandomDoubleCoeffIntVarLinearConstraint(int length){
	if (length < 0) throw new ArrayIndexOutOfBoundsException("Only positive length supported.");
	Term lhs = getRandomDoubleConstant();
	lhs = Sum.newInstance( lhs, Product.newInstance(getRandomDoubleConstant(), getIntVariable(0)) );
	for (int i = 1; i < length; ++i){
	     if (generator.nextBoolean()){
		 lhs = Sum.newInstance( lhs, Product.newInstance(getRandomDoubleConstant(), getIntVariable(i)) );
	     }
	}
//	return LessOrEqual.newInstance(lhs, zero);
	return LessOrEqual.newInstance(lhs, zero);
    }    
    
    
// ------------
    
    
    /**
     * 2a + b ≤ 4
     * a + 2b ≤ 3
     * 
     * a, b \in Z
     * 
     * @return
     */
    public SingleConstraintSet branchAndBoundTest() {
	SingleConstraintSet cs = new SingleConstraintSet();
	Term lhs;
	ConstraintExpression constraint;
	
	lhs = Sum.newInstance(Sum.newInstance(Product.newInstance(one, a), Product.newInstance(one, b)), one);
	constraint = LessOrEqual.newInstance(lhs, four);
	cs.add( (SingleConstraint) this.toComposedConstraint(constraint) );
	
	lhs = Sum.newInstance(Product.newInstance(one, a), Product.newInstance(one, b));
	constraint = GreaterOrEqual.newInstance( lhs, three );
	cs.add( (SingleConstraint) this.toComposedConstraint(constraint) );

//	lhs = Sum.newInstance(Product.newInstance(DoubleConstant.getInstance(100.).negate(), a), Product.newInstance(one.negate(), b));
//	constraint = LessOrEqual.newInstance(lhs, zero);
//	cs.add( (SingleConstraint) this.toComposedConstraint(constraint) );
//	
//	lhs = Sum.newInstance(Product.newInstance(DoubleConstant.getInstance(0.1), a), Product.newInstance(one.negate(), b));
//	constraint = LessOrEqual.newInstance(lhs, zero);
//	cs.add( (SingleConstraint) this.toComposedConstraint(constraint) );
	
	return cs;
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
    public SingleConstraintSet branchAndBoundLoopTest() {
	
	SingleConstraintSet cs = new SingleConstraintSet();
	ConstraintExpression constraint;
	
	Term lhs = Sum.newInstance(Product.newInstance(three, a), Product.newInstance(three.negate(), b));
	cs.add( (SingleConstraint) this.toComposedConstraint(GreaterOrEqual.newInstance(lhs, one)) );
	cs.add( (SingleConstraint) this.toComposedConstraint(LessOrEqual.newInstance( lhs, two )) );

	return cs;
    }
    
    public SingleConstraintSet falseTest() {
	SingleConstraintSet cs = new SingleConstraintSet();
	
	cs.add( (SingleConstraint) this.toComposedConstraint(LessOrEqual.newInstance(getDoubleVariable(0), one)) );
	cs.add( (SingleConstraint) this.toComposedConstraint(GreaterOrEqual.newInstance(getDoubleVariable(0), two)) );
	
	return cs;
    }
    
    public ConstraintExpression falseTestExpression() {
	return And.newInstance(LessThan.newInstance(x,zero), GreaterOrEqual.newInstance(x, zero));
    }

    
    
//============================= old stuff ==================================================
    
    public ConstraintExpression oneLinearConstraint() {
	Term lhs1 = Sum.newInstance( Sum.newInstance( Product.newInstance(five, x), 
	              Product.newInstance(three, y) ),
	              Product.newInstance(seven, z));

	return GreaterOrEqual.newInstance(lhs1, one);
    }
    
    public ConstraintExpression oneLinearConstraint2(){
	Term lhs2 = Sum.newInstance( Sum.newInstance( Product.newInstance(two, x),
		      Product.newInstance(two.negate(), y) ),
		      Product.newInstance(four, z));
	
	return GreaterOrEqual.newInstance(lhs2, one);
    }
    
    public ConstraintExpression oneLinearConstraint3(){
	Term lhs3 = Sum.newInstance( Sum.newInstance( Product.newInstance(seven, x),
	              Product.newInstance(five, y) ),
	              Product.newInstance(six, z));
	
	return  GreaterOrEqual.newInstance(lhs3, three);
    }
    
    
    public ConstraintExpression typeCastExpression(){
	
	Term lhs = Sum.newInstance( 
	              Product.newInstance(nine, y),
	              Product.newInstance(seven, z));
	
	Term varX = TypeCast.newInstance(lhs, Expression.DOUBLE, Expression.INT);
	
	lhs = Sum.newInstance( Product.newInstance(one, x), varX );
	ConstraintExpression expr = GreaterOrEqual.newInstance(lhs, four);
	
	Term lhs2 = Sum.newInstance( Sum.newInstance( Product.newInstance(two, x),
		      Product.newInstance(two.negate(), y) ),
		      varX);
	
	return And.newInstance(expr, GreaterOrEqual.newInstance(lhs, three)); 
    } 
    
    
    
    public ConstraintExpression typeCastExpressionTest() {
	Term lhs1 = Sum.newInstance(
			Product.newInstance(seven, z),
			Product.newInstance(nine, y));
	Term lhs2 = Sum.newInstance(
			Product.newInstance(seven.negate(),z),
			Sum.newInstance(
				Product.newInstance(nine.negate(), y),
				Sum.newInstance(
					Product.newInstance(w, one),
					one.negate())
				)
			);
		
	Term lhs3 = Sum.newInstance(
		Product.newInstance(seven,z),
		Sum.newInstance(
			Product.newInstance(nine, y),
			Sum.newInstance(
				Product.newInstance(w, one.negate()),
				zero)
			)
		);
		
	Term lhs4 = Sum.newInstance(
			Product.newInstance(x, one.negate()),
			Sum.newInstance(
				Product.newInstance(w, one.negate()),
				four
				)
		    	);
	
	ConstraintExpression expr = null;
	
	expr = LessOrEqual.newInstance(lhs1, zero);
	expr = And.newInstance(expr, LessOrEqual.newInstance(lhs2, zero));
	expr = And.newInstance(expr, LessOrEqual.newInstance(lhs3, zero));
	expr = And.newInstance(expr, LessOrEqual.newInstance(lhs4, zero));
	return expr;
    }
    
 // -------------
        
    /**
     * Provides a simple Linear Program with all DOUBLE values and variables.
     * @return {@code ConstraintExpression} containing a simple Linear Program.
     */
    public ConstraintExpression simpleLinearProgramDouble(){
	// 5x+3y+7z >= 1
	// 2x-2y+4z >= -2
	// 7x+5y+6z = 3
	// x >= 0
	// y >= 0
			
	// lhs terms
	Term lhs1 = Sum.newInstance( Sum.newInstance( Product.newInstance(five, x), 
				 	              Product.newInstance(three, y) ),
				 	              Product.newInstance(seven, z));
	Term lhs2 = Sum.newInstance( Sum.newInstance( Product.newInstance(two, x),
						      Product.newInstance(two.negate(), y) ),
						      Product.newInstance(four, z));
	Term lhs3 = Sum.newInstance( Sum.newInstance( Product.newInstance(seven, x),
					              Product.newInstance(five, y) ),
					              Product.newInstance(six, z));
	
	// constraint expressions for this linear program
	ArrayList<ConstraintExpression> constraints = new ArrayList<ConstraintExpression>(); 
	constraints.add( GreaterOrEqual.newInstance(lhs1, one) );
	constraints.add( GreaterOrEqual.newInstance(lhs2, two.negate()) );
	constraints.add( NumericEqual.newInstance(lhs3, three) );
	constraints.add( GreaterOrEqual.newInstance(x, zero) );
	constraints.add( GreaterOrEqual.newInstance(y, zero) );
	
	// conjunction of these constraints
	ConstraintExpression constraint = BooleanConstant.TRUE;
	for(ConstraintExpression c : constraints){
	    constraint = And.newInstance(constraint, c);
	}
		
	return constraint;
    }
    

    /**
     * Provides a simple Linear Program with all integer values and variables.
     * @return {@code ConstraintExpression} containing a simple Linear Program.
     */
    public ConstraintExpression simpleLinearProgramInteger(){
	// 5x+3y+7z >= 1
	// 2x-2y+4z >= -2
	// 7x+5y+6z = 3
	// x >= 0
	// y >= 0
			
	// lhs terms
	Term lhs1 = Sum.newInstance( Sum.newInstance( Product.newInstance(five_int, a), 
				 	              Product.newInstance(three_int, b) ),
				 	              Product.newInstance(seven_int, c));
	Term lhs2 = Sum.newInstance( Sum.newInstance( Product.newInstance(two_int, a),
						      Product.newInstance(two_int.negate(), b) ),
						      Product.newInstance(four_int, c));
	Term lhs3 = Sum.newInstance( Sum.newInstance( Product.newInstance(seven_int, a),
					              Product.newInstance(five_int, b) ),
					              Product.newInstance(six_int, c));
	
	// constraint expressions for this linear program
	ArrayList<ConstraintExpression> constraints = new ArrayList<ConstraintExpression>(); 
	constraints.add( GreaterOrEqual.newInstance(lhs1, one_int) );
	constraints.add( GreaterOrEqual.newInstance(lhs2, two_int.negate()) );
	constraints.add( NumericEqual.newInstance(lhs3, three_int) );
	constraints.add( GreaterOrEqual.newInstance(a, zero_int) );
	constraints.add( GreaterOrEqual.newInstance(b, zero_int) );
	
	// conjunction of these constraints
	ConstraintExpression constraint = BooleanConstant.TRUE;
	for(ConstraintExpression c : constraints){
	    constraint = And.newInstance(constraint, c);
	}
	
	console.debug("Created constraint: " + constraint);
		
	return constraint;
    }
    
    
    /**
     * Example from Wikipedia's Branch and Bound algorithm article.
     * @return
     */
    public ConstraintExpression simpleLinearProgramInteger2() {
	// 2a + b ≤ 4
	// a + 2b ≤ 3
	// a, b ≥ 0
	
	Term lhs1 = Sum.newInstance(Product.newInstance(two_int, a), b);
	Term lhs2 = Sum.newInstance(a, Product.newInstance(two_int, b));
	
	ConstraintExpression constraint = And.newInstance(LessOrEqual.newInstance(lhs1, four_int), LessOrEqual.newInstance(lhs2, three_int));
	constraint = And.newInstance(constraint,
				     And.newInstance(GreaterOrEqual.newInstance(a, zero_int), GreaterOrEqual.newInstance(b, zero_int)) 
				     ); 
	    
	return constraint;
    }
    
    /**
     * 
     */
    
    
    /**
     * 
     * @return
     */
    private ConstraintExpression simpleNonLinearProgram() {
	Term lhs1 = Sum.newInstance( Product.newInstance(x, x), 
	            		     Product.newInstance(y, y) );
	Term lhs2 = Sum.newInstance( Product.newInstance(x, one),
				     Product.newInstance(z, two));
	Term lhs3 = Product.newInstance(lhs1, lhs2);
	
	ConstraintExpression constraint = NumericEqual.newInstance(lhs3, one);
	console.debug("Created constraint: " + constraint);
	return constraint;
    }
    
    /**
     * 
     * @return
     */
    public ConstraintExpression transformationTest() {
//	ConstraintExpression ce = simpleLinearProgramDouble();
	
	// lhs terms
	Term lhs1 = Sum.newInstance( Sum.newInstance( Product.newInstance(five_int, a), 
				 	              Product.newInstance(three_int, b) ),
				 	              Product.newInstance(seven_int, c));
	Term lhs2 = Sum.newInstance( Sum.newInstance( Product.newInstance(two_int, a),
						      Product.newInstance(two_int.negate(), b) ),
						      Product.newInstance(four_int, c));
	Term lhs3 = Sum.newInstance( Sum.newInstance( Product.newInstance(seven_int, a),
					              Product.newInstance(five_int, b) ),
					              Product.newInstance(six_int, c));
	
	// constraint expressions for this linear program
	ArrayList<ConstraintExpression> constraints = new ArrayList<ConstraintExpression>(); 
	constraints.add( GreaterOrEqual.newInstance(lhs1, one_int) );
	constraints.add( GreaterOrEqual.newInstance(lhs2, two_int.negate()) );
	// constraints.add( NumericEqual.newInstance(lhs3, three_int) );
	constraints.add( GreaterOrEqual.newInstance(a, zero_int) );
	constraints.add( GreaterOrEqual.newInstance(b, zero_int) );
	
	// conjunction of these constraints
	ConstraintExpression ce = BooleanConstant.TRUE;
	for(ConstraintExpression c : constraints){
	    ce = And.newInstance(ce, c);
	}
	
	ConstraintExpression temp = NumericEqual.newInstance(y, zero); 
	ce = And.newInstance(ce, temp);
	ce = And.newInstance(BooleanConstant.TRUE, ce);
	
	temp = BooleanEqual.newInstance(new BooleanVariable("Boolean Variable"), BooleanConstant.TRUE);
	ce = And.newInstance(temp, ce);
	
	temp = NumericEqual.newInstance(lhs3, three_int);
	ce = And.newInstance(ce, temp);
	
	return ce;
    }
    
    /**
     * 
     * @return
     */
    public ConstraintExpression booleanTest(){
	BooleanVariable A = new BooleanVariable("A");
	BooleanVariable B = new BooleanVariable("B");
	BooleanVariable C = new BooleanVariable("C");
	
	ConstraintExpression or1 = Or.newInstance(A, B);
	ConstraintExpression and1 = And.newInstance(or1, C);
	ConstraintExpression and2 = And.newInstance(and1, Not.newInstance(A));
	
	ConstraintExpression xor = Xor.newInstance(and2, Not.newInstance(and1));
	
	ConstraintExpression or2 = Or.newInstance(A, B);
	ConstraintExpression and3 = And.newInstance(or2, C);
	ConstraintExpression and4 = And.newInstance(and3, Not.newInstance(A));
	
	ConstraintExpression xor2 = Xor.newInstance(and4, Not.newInstance(and3));
	
	ConstraintExpression boolEqual = BooleanEqual.newInstance(xor, xor2);
	
	return boolEqual;
    }
    
    
    // helper functions
    
    /**
     * Solves the current ConstraintStack on the SolverManager.
     */
    private void solve() {
	Solution solution = null;
	console.info("Solving:");

	try {
	    solution = solverManager.getSolution();
	    console.info("Solution = " + solution);
	} catch (SolverUnableToDecideException e) {
	    console.info("SolverUnableToDecideException\n" + e);
	} catch (TimeoutException e) {
	    console.info("TimeoutException\n" + e);
	}
    }

    
    public void solverManagerTest() {
	// set up logging and solver manager
	logger = TesttoolConfig.getLogger();
	logger.setLevel(Level.TRACE);
	texLogger = TesttoolConfig.getTexLogger();
	texLogger.setLevel(Level.DEBUG);
	console = Logger.getLogger("de.wwu.testtool.test.console");
	console.setLevel(Level.DEBUG);
	ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout("%-5p [%t:%r] %M(): %m%n"));
	consoleAppender.setName("Console Appender");
	consoleAppender.setLayout(new PatternLayout("[%r] %20.20M(): %m%n") );
	console.addAppender(consoleAppender);

	// setup some stuff
	solverManager = new SolverManagerOld();
	ConstraintExpression constraint = null;
	
	// linear program with doubles
//	constraint = this.simpleLinearProgramDouble();	
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();

	// linear program with integer
//	constraint = this.simpleLinearProgramInteger();
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();
	
	// linear program with integer
//	constraint = this.simpleLinearProgramInteger2();
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();

	// transformation tests
	constraint = this.transformationTest();
	solverManager.addConstraint(constraint);
	//solverManager.addConstraint( Not.newInstance(constraint) );
	//solverManager.addConstraint( this.simpleLinearProgramDouble() );
	this.solve();
	//solverManager.removeConstraint();
	//solverManager.removeConstraint();
	solverManager.removeConstraint();
	
	// type casts
//	constraint = this.simpleExpressionWithTypeCasts();
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();
	
	// non linear
//	constraint = this.simpleNonLinearProgram();
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();
	
	// boolean mixed with linear double
//	constraint = this.simpleLinearProgramDouble();
//	solverManager.addConstraint(Xor.newInstance(this.booleanTest(), constraint));
//	this.solve();
//	solverManager.removeConstraint();
	
	// boolean
//	solverManager.addConstraint(this.booleanTest());
//	this.solve();
//	solverManager.removeConstraint();
	
	// boolean combined with linear
//	constraint = BooleanEqual.newInstance(Not.newInstance(constraint), this.booleanTest());
//	solverManager.addConstraint(constraint);
//	this.solve();
//	solverManager.removeConstraint();
    }
    
    
    /**
     * 
     * @return {@code ConstraintExpression} containing a simple Linear Program.
     */
    public SingleConstraintSet diplomaThesisExample(){
	// 5x+3y+7z >= 1
	// 2x-2y+4z >= -2
	// 7x+5y+6z = 3
		
	// y <= x+3
	// y <= 3x-1
	// y >= -x +3
	// y >= 0.5 x
	
	// rhs terms
	Term rhs1 = Sum.newInstance( x, three);
	//Term rhs2 = Sum.newInstance( Product.newInstance(three, x), one.negate());
	Term rhs3 = Sum.newInstance( Product.newInstance(one.negate(), x) , three);
	Term rhs4 = Product.newInstance(DoubleConstant.getInstance(0.5),x);
	
	Term rhs_add = Sum.newInstance(Product.newInstance(DoubleConstant.getInstance(0.6).negate(), x), DoubleConstant.getInstance(4.4));
	
	// Term rhs_add = Sum.newInstance(Product.newInstance(DoubleConstant.getInstance(1.5).negate(), x), DoubleConstant.getInstance(1.5));
	
	SingleConstraintSet constraints = new SingleConstraintSet(); 
	
	// Running Example
//	constraints.add( (SingleConstraint) this.toComposedConstraint( LessOrEqual.newInstance(y, rhs1) ));
//	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterOrEqual.newInstance(y, rhs3) ));
//	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterOrEqual.newInstance(y, rhs4) ));
//	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterOrEqual.newInstance(y, rhs_add) ));
	
	
	// Strict Inequation Example
	constraints.add( (SingleConstraint) this.toComposedConstraint( LessOrEqual.newInstance(y, rhs1) ));
	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterThan.newInstance(y, rhs3) ));
	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterThan.newInstance(y, rhs4) ));
	constraints.add( (SingleConstraint) this.toComposedConstraint( GreaterThan.newInstance(y, rhs_add) ));

	return constraints;
    }
    
    
    public static int foo(int a, int b) {
	int c = 3*a;
	if (a < b){
	    if (c > 9) {
		c = a+b;
	    } else {
		c = 2*b;
	    }
	} else {
	    c = 2 * a;
	}
	return c;
    }
    
    
}
