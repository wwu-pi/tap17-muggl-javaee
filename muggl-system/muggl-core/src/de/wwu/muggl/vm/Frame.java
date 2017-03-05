package de.wwu.muggl.vm;

import java.util.Stack;

//import de.wwu.muggl.symbolic.generating.jpa.JPAConstraintManager;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.threading.Monitor;

/**
 * A frame is the element used in virtual machines to represent a methods during execution. It therefore
 * holds a reference to the specific methods, as well as runtime specific information as the local
 * variables, the pc etc.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-1^5
 */
public class Frame {
	/**
	 * The frame this frame was invoked by.
	 */
	protected Frame invokedBy;
	/**
	 * The vm this frame runs in.
	 */
	protected VirtualMachine vm;
	/**
	 * The method represented by this frame.
	 */
	protected Method method;
	/**
	 * The operand stack of this frame.
	 */
	protected Stack<Object> operandStack;
	/**
	 * A reference to the constant_pool of the class the method belong to.
	 */
	private Constant[] constantPool;
	/**
	 * The local variable table.
	 */
	private Object[] localVariables;
	/**
	 * Indicator whether this frame is currently active i.e. executed.
	 */
	protected boolean active;
	// Private fields.
	private int pc;
	private Monitor monitor;
	
	
	
//	private JPAConstraintManager constraintManager;
//	
//	public JPAConstraintManager getConstraintManager() {
//		if(this.constraintManager == null) {
//			this.constraintManager = new JPAConstraintManager((JPAVirtualMachine)this.vm);
//		}
//		return this.constraintManager;
//	}
	

	/**
	 * Basic constructor.
	 *
	 * @param invokedBy The frame this frame was invoked by. Might be null.
	 * @param vm The virtual machine this frame can be executed on.
	 * @param method The Method this frame represents.
	 * @param constantPool A reference to the constant pool of the methods class.
	 * @param arguments The predefined arguments for this method.
	 * @throws ExecutionException Thrown on any fatal error that happens during execution and is not coped by one of the other Exceptions.
	 */
	public Frame(Frame invokedBy, VirtualMachine vm, Method method, Constant[] constantPool, Object[] arguments) throws ExecutionException {
		this.invokedBy = invokedBy;
		this.vm = vm;
		this.method = method;
		this.operandStack = new Stack<Object>();
		this.constantPool = constantPool;
		this.localVariables = new Object[method.getCodeAttribute().getMaxLocals()];
		/*
		 * Any non filled local variables should not be null, as this would be a value worth looking
		 * at. Instead it gets a UndefinedValue reference.
		 */
		UndefinedValue undefinedValue = new UndefinedValue();
		for (int a = 0; a < this.localVariables.length; a++) {
			this.localVariables[a] = undefinedValue;
		}

		// Store arguments as local variables.
		if (arguments != null) {
			int b = 0;
			int c = 0;
			if (!this.method.isAccStatic()) c--;
			if (arguments.length > method.getCodeAttribute().getMaxLocals())
				throw new ExecutionException(
						"Fatal error in the virtual machine: More arguments were supplied for method "
								+ method.getName() + " than its max_locals allows.");
			for (int a = 0; a < arguments.length; a++) {
				this.localVariables[b] = arguments[a];
				if (c >= 0 && this.method.getTakeASecondSlot()[c]) b++;
				b++;
				c++;
			}
		}
		this.pc = 0;
	}

	/**
	 * Getter for the operand stack.
	 * @return The operand stack.
	 */
	public Stack<Object> getOperandStack() {
		return this.operandStack;
	}

	/**
	 * Setter for the operand stack.
	 * @param operandStack The new operand stack.
	 */
	public void setOperandStack(Stack<Object> operandStack) {
		this.operandStack = operandStack;
	}

	/**
	 * Getter for the local variables.
	 * @return The local variables.
	 */
	public Object[] getLocalVariables() {
		return this.localVariables;
	}

	/**
	 * Setter for the local variables.
	 * @param localVariables The local variables.
	 */
	public void setLocalVariables(Object[] localVariables) {
		this.localVariables = localVariables;
	}

	/**
	 * Set an entry of the local variables to a new value.
	 * @param index The index in the local variables.
	 * @param value The new object to be set.
	 */
	public void setLocalVariable(int index, Object value) {
		if (index > this.localVariables.length || index < 0)
			throw new IndexOutOfBoundsException("There is no such entry in the local variables table. index must be >= 0 and less than " + this.localVariables.length + ".");
		this.localVariables[index] = value;
	}

	/**
	 * Getter for the constant pool.
	 * @return The constant pool.
	 */
	public Constant[] getConstantPool() {
		return this.constantPool;
	}

	/**
	 * Getter for the method.
	 * @return The method.
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Getter for the virtual machine.
	 * @return The virtual machine.
	 */
	public VirtualMachine getVm() {
		return this.vm;
	}

	/**
	 * Getter for the field active.
	 * @return true, if this frame is active, false otherwise.
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Setter for the field active.
	 * @param active A boolean value.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Getter for the field invokedBy.
	 * @return The frame this frame was invoked by.
	 */
	public Frame getInvokedBy() {
		return this.invokedBy;
	}

	/**
	 * Getter for the pc.
	 * @return The current pc.
	 */
	public int getPc() {
		return this.pc;
	}

	/**
	 * Setter for the pc.
	 * @param pc The new pc value as an int.
	 */
	public void setPc(int pc) {
		this.pc = pc;
	}

	/**
	 * Clears the operand stack and sets this frame inactive. This will result in control
	 * given back to the invoking frame (or there execution to end, if there was no invoking
	 * frame).
	 */
	public void returnFromMethod() {
		while (!this.operandStack.isEmpty()) {
			this.operandStack.pop();
		}
		this.active = false;
	}

	/**
	 * Pushes a returned object onto the stack of the invoking frame, or if there was none onto the
	 * virtual machines stack and sets the frame inactive. Clears this frames' operand stack
	 * afterwards.
	 * @param value The value returned from the method.
	 */
	public void returnFromMethod(Object value) {
		if (this.invokedBy != null) {
			this.invokedBy.getOperandStack().push(value);
		} else {
			this.vm.getStack().push(value);
		}
		while (!this.operandStack.isEmpty()) {
			this.operandStack.pop();
		}
		this.active = false;
	}

	/**
	 * Getter for the Monitor.
	 * @return The Monitor associated with this frame. Might by null if there is no current monitor.
	 */
	public Monitor getMonitor() {
		return this.monitor;
	}

	/**
	 * Setter for the monitor.
	 * @param monitor The Monitor to be associated with this frame.
	 */
	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Get a suitable String representation of this Frame.
	 *
	 * @return A suitable String representation of this Frame.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Frame for " + this.method.getFullNameWithParameterTypesAndNames() + " at pc "
				+ this.pc + ".";
	}

}
