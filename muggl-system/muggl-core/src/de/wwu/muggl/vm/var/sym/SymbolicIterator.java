package de.wwu.muggl.vm.var.sym;

import java.util.Iterator;

import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;

public class SymbolicIterator {

	protected ISymbolicList qryResList;
//	protected Iterator<?> iterator;
	protected int counter;
	
	public SymbolicIterator(ISymbolicList qryResList) {
		this.qryResList = qryResList; 
//		this.iterator = qryResList.getResultList().iterator();
		this.counter = 0;
	}
	
	public Object next() {
		return this.qryResList.getResultList().get(counter++);
	}
	
	public ISymbolicList getSymbolicList() {
		return qryResList;
	}
	
	public int getCounter() {
		return this.counter;
	}
	
//	public NumericConstant hasNext(Invoke instruction) {
//		if(this.qryResList.getResultList().size() > this.counter) {
////		if(iterator.hasNext()) {
//			return NumericConstant.getInstance(1, Expression.INT);
//		}
//		
//		// create a choice point, which tries to set another element to list and iterates over it...
//		// i.e. it has more elements...
//		try {
//			this.qryResList.getVM().generateSymbolicIterationChoicePoint(instruction, this);
//			
//			
////			ConstraintExpression ce = GreaterOrEqual.newInstance(this.qryResList.getSymbolicLength(), NumericConstant.getInstance((counter+1), Expression.INT));
////			this.qryResList.getVM().getSolverManager().addConstraint(ce);
////			boolean hasSolution = this.qryResList.getVM().getSolverManager().hasSolution();
////			if(!hasSolution) {
////				this.qryResList.getVM().getSolverManager().removeConstraint();
////			} else {
////				this.qryResList.getVM().generateSymbolicIterationChoicePoint(instruction, this);
////				System.out.println("- generate new choice point -");
////			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		return NumericConstant.getInstance(0, Expression.INT);
//	}
//	
////	public void refresh() {
////		this.iterator = qryResList.getResultList().iterator();
////	}
}
