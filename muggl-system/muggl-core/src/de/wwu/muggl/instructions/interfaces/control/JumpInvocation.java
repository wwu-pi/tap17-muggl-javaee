package de.wwu.muggl.instructions.interfaces.control;

import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Interface that is to be implemented by any instruction that will invoke another method.
 * If this interface is implemented, the interfaces JumpAlways, JumpConditional,
 * JumpNever and JumpSwicthing must not be implemented. (JumpException may be implemented.)
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public interface JumpInvocation {

	/**
	 * Get a reference to the Method invoked by this instruction.
	 * @param constantPool The constant pool.
	 * @param classLoader The current class loader.
	 * @return The resolved Method.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ExecutionException On various method resolving problems.
	 * @throws NoSuchMethodError If the Method could not be found.
	 */
	Method getInvokedMethod(Constant[] constantPool, MugglClassLoader classLoader)
		throws ClassFileException, ExecutionException;

}
