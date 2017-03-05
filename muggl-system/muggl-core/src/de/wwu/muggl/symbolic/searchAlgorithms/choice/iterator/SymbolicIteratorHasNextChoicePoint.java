package de.wwu.muggl.symbolic.searchAlgorithms.choice.iterator;

import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.EquationViolationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;

public class SymbolicIteratorHasNextChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	protected ConstraintExpression constraintExpression;
	protected boolean hasChanged;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	
	/**
	 * Flag to determine if the execution time should be measured.
	 */
	protected boolean measureExecutionTime;
	/**
	 * Temporary field for time measuring.
	 */
	protected long timeSolvingTemp;
	
	
	public SymbolicIteratorHasNextChoicePoint(
			Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent,
			SymbolicIterator iterator) throws EquationViolationException, SolvingException {
		
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		if(parent != null) {
			this.number = parent.getNumber() + 1;
		}
		this.hasChanged = false;
		
		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;
		
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();

		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		
		// hasNext true constraint: iterator.index < array.size
		NumericVariable iteratorIndex = null;// iterator.getSymbolicIndex();
		NumericVariable listSize = null;//iterator.getQueryResultList().getSymbolicLength();
		this.constraintExpression = GreaterThan.newInstance(listSize, iteratorIndex);
		
		SolverManager solverManager = this.vm.getSolverManager();
		solverManager.addConstraint(constraintExpression);		
		
		try {
			if (this.measureExecutionTime) this.timeSolvingTemp = System.nanoTime();
			if (!solverManager.hasSolution()) {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				tryTheNegatedConstraint(solverManager, constraintExpression);
			} else {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				// return true
				this.frame.getOperandStack().push(NumericConstant.getInstance(1, Expression.INT));
			}
		} catch (SolverUnableToDecideException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.hasChanged = true;
		} catch (TimeoutException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.hasChanged = true;
		}
		
		
	}
	
	private void tryTheNegatedConstraint(SolverManager solverManager, ConstraintExpression constraintExpression) throws EquationViolationException, SolvingException {
		try {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Cannot proceed with the jumping branch since this would violate the current constraint system. Trying the non-jumping branch...");
			// The expression is unsolvable. Is it at least possible to use the non-jumping branch?
			solverManager.removeConstraint();
			solverManager.addConstraint(constraintExpression.negate());
			if (this.measureExecutionTime) this.timeSolvingTemp = System.nanoTime();
			if (solverManager.hasSolution()) {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) this.frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				// Use the non-jumping branch.
				this.frame.getVm().setPC(this.pcNext);
				this.hasChanged = true;
			} else {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) this.frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Cannot proceed with the non-jumping branch either since this would violate the current constraint system. Tracking back...");
				// Throw the appropriate Exception.
				throw new EquationViolationException("Cannot continue with this choice point, since equations are violated.");
			}
		} catch (SolverUnableToDecideException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving again lead to a SolverUnableToDecideException with message: " + e.getMessage());
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		} catch (TimeoutException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving again lead to a TimeoutException with message: " + e.getMessage());
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		}
	}

	
	@Override
	public void applyStateChanges() {
		this.frame.getOperandStack().pop();
		this.frame.getOperandStack().push(NumericConstant.getZero(Expression.INT));
	}
	
	
	@Override
	public boolean hasAnotherChoice() {
		return !hasChanged;
	}

	
	@Override
	public long getNumber() {
		return this.number;
	}
	
	@Override
	public void changeToNextChoice() throws NoExceptionHandlerFoundException {
		if(!this.hasChanged) {
			this.constraintExpression = this.constraintExpression.negate();
			this.hasChanged = true;
		}
	}
	
	@Override
	public Frame getFrame() {
		return this.frame;
	}

	@Override
	public int getPc() {
		return this.pc;
	}

	@Override
	public int getPcNext() {
		return this.pcNext;
	}

	@Override
	public ChoicePoint getParent() {
		return this.parent;
	}
	
	@Override
	public boolean changesTheConstraintSystem() {
		return true;
	}

	@Override
	public ConstraintExpression getConstraintExpression() {
		return this.constraintExpression;
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
	}

	@Override
	public boolean hasTrail() {
		return true;
	}

	@Override
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	@Override
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	@Override
	public boolean enforcesStateChanges() {
		return true;
	}
	
	@Override
	public String getChoicePointType() {
		return "SymbolicIteratorHasNextChoicePoint";
	}

	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
	
	
}
