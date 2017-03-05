package de.wwu.muggl.instructions.general;

import java.util.Set;
import java.util.Stack;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultListArrayref;

/**
 * Abstract instruction with some concrete methods for loading elements from an array.
 * Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class Aload extends GeneralInstruction implements JumpException, StackPop, StackPush {
	/**
	 * The type the inheriting class will take.
	 */
	protected TypedInstruction typedInstruction;

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			int index = (Integer) stack.pop();
			Object arrayrefObject = stack.pop();

			// Runtime exception: arrayref is null
			if (arrayrefObject == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

			// Unexpected exception: Arrayref does not point to an array.
			if (!(arrayrefObject instanceof Arrayref)) {
				throw new ExecutionException("Could not " + getName() + " array entry #" + index + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref = (Arrayref) arrayrefObject;

			// Runtime exception array index out of bounds.
			if (arrayref.length < index || index < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayIndexOutOfBoundsException", "Array index is out of bounds"));
			}

			// Unexpected exception: The object at the index is not of one of the required types.
			Object value = arrayref.getElement(index);
			value = this.typedInstruction.validateAndExtendValue(value);

			// Push the value.
			stack.push(value);
		} catch (VmRuntimeException e) {
			ExceptionHandler handler = new ExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailed(e2);
			}
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic
	 *         execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			// Preparations.
			Stack<Object> stack = frame.getOperandStack();
			int index = ((IntConstant) stack.pop()).getValue();
			Object arrayrefObject  = stack.pop();

			// Runtime exception: arrayref is null
			if (arrayrefObject == null)
				throw new VmRuntimeException(frame.getVm().generateExc("java.lang.NullPointerException"));

			if (arrayrefObject instanceof SymbolicQueryResultListArrayref) {

				Objectref[] result = ((SymbolicQueryResultListArrayref)arrayrefObject).getSymbolicResultList().generateNewQueryResult((JPAVirtualMachine) frame.getVm(), index);
				
				if(result.length == 1) {
					stack.push(result[0]);
					return;
				}
				else if(result.length > 1) {
					try {
						InitializedClass objectRef = frame.getVm().getClassLoader().getClassAsClassFile("java.lang.Object").getInitializedClass();
						Arrayref array = new Arrayref(objectRef.getANewInstance(), result.length);
						stack.push(array);
						return;
					} catch(Exception e) {
					}
				} else {
					throw new RuntimeException("Ooops, generated result is empty...");
				}
				
				
//				EntityObjectref element = ((SymbolicQueryResultListArrayref)arrayrefObject).getSymbolicResultList().generateAndAddNewElement((JPAVirtualMachine) frame.getVm(), index);
//				stack.push(element);
//				return;
			} else if (arrayrefObject instanceof SymbolicArrayref) {
				SymbolicArrayref symbolicArrayref = (SymbolicArrayref)arrayrefObject;
				Object element = symbolicArrayref.getElement(index);
				if(element == null) {
					// the element does not exist in the array, create a new symbolic element
					ClassFile classFile = symbolicArrayref.getInitializedClass().getClassFile();
					Class<?> entityClass = getEntityClass(frame, classFile);
					if(entityClass != null) {
						
						EntityReferenceGenerator entityRefGenerator = new EntityReferenceGenerator((JPAVirtualMachine) frame.getVm());
						ReferenceVariable entityReference = entityRefGenerator.generateNewEntityReference(classFile.getName());
						String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityReference.getInitializedClassName());
						Object idReference = entityReference.valueMap().get(idFieldName);
						if(idReference instanceof ReferenceVariable && ((ReferenceVariable)idReference).getInitializedClassName().equals("java.lang.String")) {
							// for a string: the id must have at least one character in value array
							Field valueString = ((ReferenceVariable)idReference).getInitializedClass().getClassFile().getFieldByName("value");
							SymbolicArrayref symArray = (SymbolicArrayref)((ReferenceVariable)idReference).getField(valueString);
							ConstraintExpression staticConstraint = GreaterOrEqual.newInstance(symArray.getSymbolicLength(), NumericConstant.getOne(Expression.INT));
							((SymbolicVirtualMachine)frame.getVm()).getSolverManager().addStaticConstraint(staticConstraint);
							((SymbolicVirtualMachine)frame.getVm()).getSolverManager().addConstraint(staticConstraint);
						}
						
						element = entityReference;
					} else {
						ReferenceValue referenceValue = frame.getVm().getAnObjectref(classFile);
						String elementName = symbolicArrayref.getName() + ".element." + index;
						System.out.println("Generate element: " + elementName);
						element = new ReferenceVariable(elementName, referenceValue, (JPAVirtualMachine)frame.getVm());
					}
					symbolicArrayref.setElementAt(index, element);
				}
				stack.push(element);
				return;
			}
			
			// Unexpected exception: arrayref does not point to an array.
			if (!(arrayrefObject instanceof Arrayref)) {
				throw new SymbolicExecutionException("Could not " + getName() + " array entry #" + index + ": Expected an array, but did not get one.");
			}
			Arrayref arrayref = (Arrayref) arrayrefObject;

			// Runtime exception array index out of bounds.
			if (arrayref.length <= index || index < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayIndexOutOfBoundsException", "Array index is out of bounds"));
			}

			Object value = arrayref.getElement(index);

			// Push the value.
			stack.push(value);
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	private Class<?> getEntityClass(Frame frame, ClassFile classFile) {
		try {
			Set<Class<?>> entityClasses = ((JPAVirtualMachine)frame.getVm()).getMugglEntityManager().getManagedEntityClasses();
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(classFile.getName());
			for(Class<?> ec : entityClasses) {
				if(isInstance(ec, clazz)) {
					return ec;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	
	public static boolean isInstance(Class<?> c1, Class<?> c2) {
		try {
			if(c1==null || c2==null) {
				return false;
			}
			
			if(c1.getName().equals(c2.getName())) {
				return true;
			}
			
			if(c2.isInterface()) { 
				for(Class<?> i : c1.getInterfaces()) {
					if(i.getName().equals(c2.getName())) {
						return true;
					}
				}
			}
			
			if(c1.getSuperclass() != null) {
				if(c1.getSuperclass().getName().equals(c2.getName())) {
					return true;
				}
				return isInstance(c1.getSuperclass(), c2);
			}
		} catch(Exception e) {
		}
		
		return false;
	}
	
	

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 0;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "aload";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.ArrayIndexOutOfBoundsException",
									"java.lang.NullPointerException"};
		return exceptionTypes;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 1;
	}

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 1;
	}

	/**
	 * Get the types of elements this instruction will pop from the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The types this instruction pops. The length of the arrays reflects the number of
	 *         elements pushed in the order they are pushed. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the popped type cannot be determined statically.
	 */
	public byte[] getTypesPopped(ClassFile methodClassFile) {
		byte[] types = {ClassFile.T_INT, 0};
		return types;
	}

}
