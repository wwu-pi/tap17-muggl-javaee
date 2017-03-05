package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.instructions.typed.IntegerInstruction;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Restore;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Sum;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Implementation of the instruction <code>iinc</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-26
 */
public class Iinc extends de.wwu.muggl.instructions.general.IntegerIncremenet implements Instruction, LocalVariableAccess {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Iinc(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		// The increment is a byte value. Sign extend it.
		int increment = (byte) this.otherBytes[1];

		// Call the actual implementation of iinc.
		execute(frame, getLocalVariableIndex(), increment);
	}

	/**
	 * For widened index: execute the instruction.
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to increase.
	 * @param increment The incremantation value.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	public void execute(Frame frame, int localVariable, int increment) throws ExecutionException {
		try {
			Object[] localVariables = frame.getLocalVariables();
			// Unexpected exception: the type of the local variable does not allow incrementation.
			if (!localVariables[localVariable].getClass().getName().equals("java.lang.Integer")) {
				throw new ExecutionException("Could not load local variable #" + localVariable + ": Expected java.lang.Integer got " + localVariables[localVariable].getClass() + ".");
			}
			localVariables[localVariable] = (Integer) localVariables[localVariable] + increment;
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		// The increment is a byte value. Sign extend it.
		int increment = (byte) this.otherBytes[1];

		// Call the actual implementation of iinc.
		executeSymbolically(frame, getLocalVariableIndex(), increment);
	}

	/**
	 *
	 * For widened index: execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to increase.
	 * @param increment The incremantation value.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	public void executeSymbolically(Frame frame, int localVariable, int increment) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			// Define the local variable, if it is not defined, yet.
			if (frame.getLocalVariables()[localVariable].getClass().getName().endsWith("UndefinedValue")) {
				// Define and load the new value.
				IntegerInstruction integerInstruction = new IntegerInstruction();
				Variable variable = integerInstruction.getNewVariable(frame.getMethod(), localVariable, frame.getVm());
				frame.setLocalVariable(localVariable, variable);
				frame.getMethod().setVariable(localVariable, variable);
			}

			// Load the current value.
			Term term = null;
			try {
				term = (Term) frame.getLocalVariables()[localVariable];
			} catch (ClassCastException e) {
				throw new SymbolicExecutionException("Could not load local variable #" + localVariable + ": Expected a Term, got " + frame.getLocalVariables()[localVariable].getClass().getName() + ".");
			}

			// Is the type correct?
			if (term.getType() != Expression.INT)
				throw new SymbolicExecutionException("The expected type for iinc is int. Cannot process any other type.");

			// Save the current value, if necessary.
			if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingLocalVariableValues()) {
				Restore localVariableValue = new Restore(localVariable, term);
				((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveLocalVariableValue(localVariableValue);
			}

			// Store the new value.
			term = Sum.newInstance(term, IntConstant.getInstance(increment));
			frame.setLocalVariable(localVariable, term);
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Get the index into the local variables.
	 * @return The index into the local variables.
	 */
	public int getLocalVariableIndex() {
		return this.otherBytes[0];
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "iinc";
	}

}
