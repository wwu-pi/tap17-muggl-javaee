package de.wwu.muggl.instructions.replaced;

import de.wwu.muggl.instructions.interfaces.Instruction;

/**
 * Interface for classes representing a java bytecode instruction that can be used to replace other
 * instructions at runtime to optimize execution speed.<br />
 * <br />
 * Some instructions require computationally expensive actions to be taken at runtime. This for
 * example includes getting elements from the constant pool. Quite a lot of these expensive
 * operations have to be done every time an instruction is encountered. Hence, it is a good idea to
 * save the result of the operation and use it the next time the instruction is encountered. The
 * results will be saved to a new instruction which is the "quick" version of the original
 * instruction. This interface marks those quick instructions.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface QuickInstruction extends Instruction {

	/**
	 * Get the {@link ReplacingInstruction} that constructed this QuickInstruction.
	 *
	 * @return The ReplacingInstruction that constructed this QuickInstruction.
	 */
	ReplacingInstruction getReplacer();

}
