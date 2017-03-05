package de.wwu.muggl.instructions.jpa;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Checkcast;
import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

public class JPACheckcast extends Checkcast {

	public JPACheckcast(AttributeCode code)	throws InvalidInstructionInitialisationException {
		super(code);
	}
	
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		// check if it is a JPA special type -> nothing to cast here
		if(frame.getOperandStack().peek() instanceof JPASpecialType) {
			return;
		}
		super.executeSymbolically(frame);
	}

}
