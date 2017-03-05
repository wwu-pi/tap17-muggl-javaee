//package de.wwu.muggl.solvers.expressions;
//
//import de.wwu.muggl.solvers.Solution;
//import de.wwu.muggl.solvers.solver.constraints.Assignment;
//import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
//import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;
//
//public class CollectionSize extends ConstraintExpression implements HasLeftAndRightTerms {
//
//    /**
//     * The left hand side of the inequation.
//     */
//    protected Term left;
//
//    /**
//     * The right hand side of the inequation.
//     */
//    protected Term right;
//    
//    private CollectionSize(Term left, Term right) {
//    	this.left = left;
//    	this.right = right;
//    }
//    
//    public static CollectionSize newInstance(Term left, Term right) {
//    	return new CollectionSize(left, right);
//    }
//    
//    public void checkTypes() throws TypeCheckException {
//    	if(!Term.isNumericType(right.getType())) {
//    	    throw new TypeCheckException(right.toString() + " is not of a numeric type");
//    	}
//    	if(!(left instanceof CollectionVariable)) {
//    	    throw new TypeCheckException(left.toString() + " is not of a collection variable type");
//    	}
//    }
//    
//	@Override
//	public ConstraintExpression insertAssignment(Assignment assignment) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ConstraintExpression insert(Solution solution, boolean produceNumericSolution) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ConstraintExpression negate() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//    
//    @Override
//    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable){
//    	// TODO: HOW DO YOU DO THAT?
//    	System.out.println("++++++++++++++++++++++++++++++ HOW???? ++++++++++++++++++++++++++++++");
//    	return null;
//    }
//
//	@Override
//	public boolean isConstant() {
//		return this.left.isConstant() && this.right.isConstant();
//	}
//	
//	@Override
//	public String toString() {
//		return this.left.toString() + ".size = " + this.right.toString();
//	}
//
//	@Override
//	public String toString(boolean useInternalVariableNames) {
//		return this.toString();
//	}
//
//	@Override
//	public byte getType() {
//		return -1;
//	}
//
//	@Override
//	public Term getLeft() {
//		return this.left;
//	}
//
//	@Override
//	public Term getRight() {
//		return this.right;
//	}
//	
//	@Override
//	public String toTexString(boolean useInternalVariableNames) {
//		return null;
//	}
//
//	@Override
//	public String toHaskellString() {
//		return null;
//	}
//	
//    
//}
