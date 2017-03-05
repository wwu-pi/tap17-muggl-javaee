//package de.wwu.muggl.solvers.expressions.list;
//
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.solvers.expressions.Expression;
//import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
//import de.wwu.muggl.solvers.expressions.GreaterThan;
//import de.wwu.muggl.solvers.expressions.NumericConstant;
//import de.wwu.muggl.solvers.expressions.Term;
//
//public class SymbolicHasNext {
//	
//	protected SymbolicIterator iterator;
//	
//	public SymbolicHasNext(SymbolicIterator iterator) {
//		this.iterator = iterator;
//	}
//
//	public ConstraintExpression generateTrueConstraint() {
//		NumericConstant curPosConstant = NumericConstant.getInstance(
//				iterator.currentPosition(), Expression.INT);
//		return GreaterThan.newInstance(iterator.list().length(), curPosConstant);
//	}
//	
//	public ConstraintExpression generateFalseConstraint() {
//		NumericConstant curPosConstant = NumericConstant.getInstance(
//				iterator.currentPosition(), Expression.INT);
//		return GreaterOrEqual.newInstance(curPosConstant, iterator.list().length());
//	}
//}
