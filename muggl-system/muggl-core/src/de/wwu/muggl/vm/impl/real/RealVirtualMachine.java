package de.wwu.muggl.vm.impl.real;

import java.util.Stack;

import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This concrete class represents a virtual machine for the "normal" execution of java bytecode. It inherits
 * methods from the abstract implementation, providing some additional methods as well as overriding
 * others.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-10
 */
public class RealVirtualMachine extends VirtualMachine {

	/**
	 * Basic constructor. It initializes the stack to a "normal" Stack instance.
	 *
	 * @param application The application this virtual machine is used by.
	 * @param classLoader The main classLoader to use.
	 * @param classFile The classFile to start execution with.
	 * @param initialMethod The Method to start execution with. This Method has to be a method of
	 *        the supplied classFile.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	public RealVirtualMachine(Application application, MugglClassLoader classLoader,
			ClassFile classFile, Method initialMethod) throws InitializationException {
		super(application, classLoader, classFile, initialMethod);
		this.stack = new Stack<Object>();
	}

	/**
	 * This concrete method executes the given instruction.
	 * @param instruction The instruction that is to be executed.
	 * @throws ExecutionException An exception is thrown on any fatal errors during execution.
	 */
	@Override
	protected void executeInstruction(Instruction instruction) throws ExecutionException {
		super.executedInstructions++;
		instruction.execute(this.currentFrame);
	}

	/**
	 * Finalize the RealVirtualMachine.
	 */
	@Override
	public void finalize() {
		super.finalize();
	}

}
