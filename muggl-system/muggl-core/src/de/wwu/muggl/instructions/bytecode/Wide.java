package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Load;
import de.wwu.muggl.instructions.general.Store;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction  <code>wide</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class Wide extends de.wwu.muggl.instructions.general.WideAbstract implements Instruction, LocalVariableAccess {
	private static final int OPCODE_LENGTH_THREE = 3;
	private static final int OPCODE_LENGTH_FIFE = 5;
	
	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Wide(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// If the number of additional bytes is not five, we have to distinguish between a couple of instructions.
			if (this.otherBytes.length == OPCODE_LENGTH_THREE) {
				// Distinguish between the possible instructions to wide.
				if (this.nextInstruction instanceof ALoad) {
					((Load) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof DLoad) {
					((Load) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof FLoad) {
					((Load) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof ILoad) {
					((Load) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof LLoad) {
					((Load) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof AStore) {
					((Store) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof DStore) {
					((Store) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof FStore) {
					((Store) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof IStore) {
					((Store) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof LStore) {
					((Store) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof Ret) {
					((Ret) this.nextInstruction).execute(frame, getLocalVariableIndex());
				} else {
					throw new ExecutionException("The opcode supplied to the instruction wide is not supported by it.");
				}
			} else if (this.otherBytes.length == OPCODE_LENGTH_FIFE) {
				/*
				 *  No additional cast checking is needed due to the initialization of this class - if there are five additional bytes, the next instruction field has a reference to the iinc instruction.
				 */
				((Iinc) this.nextInstruction).execute(frame, getLocalVariableIndex(), this.otherBytes[3] << ONE_BYTE | this.otherBytes[4]);
			} else {
				throw new ExecutionException("Wrong opcode count for instruction wide.");
			}
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
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			if (this.otherBytes.length == OPCODE_LENGTH_THREE) {
				if (this.nextInstruction instanceof ALoad) {
					((Load) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof DLoad) {
					((Load) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof FLoad) {
					((Load) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof ILoad) {
					((Load) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof LLoad) {
					((Load) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof AStore) {
					((Store) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof DStore) {
					((Store) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof FStore) {
					((Store) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof IStore) {
					((Store) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof LStore) {
					((Store) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else if (this.nextInstruction instanceof Ret) {
					((Ret) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex());
				} else {
					throw new SymbolicExecutionException("Opcode supplies to the instruction wide is not supported.");
				}
			} else if (this.otherBytes.length == OPCODE_LENGTH_FIFE) {
				if (this.nextInstruction instanceof Iinc) {
					((Iinc) this.nextInstruction).executeSymbolically(frame, getLocalVariableIndex(), this.otherBytes[3] << ONE_BYTE | this.otherBytes[4]);
				}
			} else {
				throw new SymbolicExecutionException("Wrong opcode count for instruction wide.");
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Construct an index into the local variables from the second and the third additional byte.
	 * @return An index into the variables.
	 */
	public int getLocalVariableIndex() {
		return this.otherBytes[1] << ONE_BYTE | this.otherBytes[2];
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "wide";
	}

}
