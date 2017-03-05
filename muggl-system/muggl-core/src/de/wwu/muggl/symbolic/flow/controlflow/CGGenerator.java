package de.wwu.muggl.symbolic.flow.controlflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.ALoad;
import de.wwu.muggl.instructions.bytecode.AStore;
import de.wwu.muggl.instructions.bytecode.Athrow;
import de.wwu.muggl.instructions.general.ReturnWithoutOrWithoutValue;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpAlways;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.control.JumpException;
import de.wwu.muggl.instructions.interfaces.control.JumpInvocation;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.control.JumpSwitching;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.Limitations;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ExceptionTable;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This class provides the means to generate the control graph for a method. The CGGenerator is used
 * by the ControlGraph only and hence has package visibility.<br />
 * <br />
 * Any instruction but those that are unconditional jumps will have an edge to their direct
 * successor.<br />
 * <br />
 * Conditional jumps have an additional edge which connects them to their jump target. Switching
 * instructions even have a number of edges like this.<br />
 * <br />
 * There will be an edge between an invocation instruction and the following instruction. The return
 * from the invocation is equal to an edge between the invocation instruction and the following
 * instruction. This edge will not be covered if there is no normal return from the invocation but
 * an exception was thrown while executing it.<br />
 * <br />
 * Leaving the method will be marked as "-2".<br />
 * <br />
 * Exceptions need special handling. Any instruction that may throw an exception has one edge per
 * exception thrown. The edge is either connected to the handler pc if there should be one, or it
 * hints to an abortion of the method. This is marked as "-3", which means execution of the method
 * will be ended and exception handling will be continued at the calling method. The also is a list
 * of exception types that will not be caught. It can be used to augment other control graphs with
 * these information, as those exceptions may be thrown when the method is invoked.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
class CGGenerator {
	
	/**
	 * Protected default constructor.
	 */
	protected CGGenerator() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Find the edges of the control graph for the given method.
	 * 
	 * @param method The method to generate the control graph for.
	 * @param edges The data structures to save edges in.
	 * @param uncaughtExceptionTypes The set on exception types that might be thrown by this method
	 *        without being caught.
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instruction.
	 */
	public static void findEdges(Method method, Map<Integer, Set<Integer>> edges,
			Set<String> uncaughtExceptionTypes) throws InvalidInstructionInitialisationException {
		if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
			Globals.getInst().symbolicExecLogger
					.debug("Generating control-graph for method " + method.getName());
		
		// Auxiliary structures.		
		Instruction[] instructions = method.getInstructionsAndOtherBytes();
		Map<Integer, Set<String>> athrowExceptionTypeMapping = new TreeMap<Integer, Set<String>>();
		List<FinallyHandler> finallyList = new ArrayList<FinallyHandler>();
		Set<Integer> athrowList = new HashSet<Integer>();
		MugglClassLoader classLoader = method.getClassFile().getClassLoader();
		Constant[] constantPool = method.getClassFile().getConstantPool();
		ExceptionTable[] exceptionTable = method.getCodeAttribute().getExceptionTable();

		// Process all instructions.
		for (int pc = 0; pc < instructions.length; pc++) {
			// Simply skip other bytes.
			if (instructions[pc] != null) {
				// New Set for the edges.
				Set<Integer> targets = new HashSet<Integer>();
				// Insert it.
				edges.put(pc, targets);
				
				/*
				 * Now find any edges. Start with single edges...
				 * 
				 * Note: Athrow strictly speaking never jumps, but the control flows does not
				 * continue normally either.
				 */
				if (!(instructions[pc] instanceof JumpAlways)) {
					if (instructions[pc] instanceof ReturnWithoutOrWithoutValue) {
						// Leaving the method.
						targets.add(ControlGraph.CG_RETURN_METHOD);
					} else if (!(instructions[pc] instanceof Athrow)) {
						// The instruction has an edge with the directly following one.
						int jumpTarget = pc + 1 + instructions[pc].getNumberOfOtherBytes();
						if (jumpTarget >= Limitations.MAX_CODE_LENGTH)
							jumpTarget -= Limitations.MAX_CODE_LENGTH;
						targets.add(jumpTarget);
					}
				} else {
					// Always jump.
					int jumpTarget = pc + ((JumpAlways) instructions[pc]).getJumpIncrement();
					if (jumpTarget >= Limitations.MAX_CODE_LENGTH)
						jumpTarget -= Limitations.MAX_CODE_LENGTH;
					targets.add(jumpTarget);
				}

				// Any other jumps possible?
				if (!(instructions[pc] instanceof JumpNever)) {
					// Conditional jump or switching?
					if (instructions[pc] instanceof JumpConditional) {
						int jumpTarget = ((JumpConditional) instructions[pc]).getJumpTarget();
						if (jumpTarget >= Limitations.MAX_CODE_LENGTH)
							jumpTarget -= Limitations.MAX_CODE_LENGTH;
						targets.add(jumpTarget);
					} else if (instructions[pc] instanceof JumpSwitching) {
						int[] jumpTargets = ((JumpSwitching) instructions[pc]).getJumpTargets();
						for (int b = 0; b < jumpTargets.length; b++) {
							if (jumpTargets[b] >= Limitations.MAX_CODE_LENGTH)
								jumpTargets[b] -= Limitations.MAX_CODE_LENGTH;
							targets.add(jumpTargets[b]);
						}
					}

					// Special handling of exceptions.
					if (instructions[pc] instanceof JumpException) {
						// Athrow needs special handling which will be done later.
						if (instructions[pc] instanceof Athrow) {
							athrowList.add(pc);
						} else {
							// Skip return instructions if their method is not synchronized.
							if (!(instructions[pc] instanceof ReturnWithoutOrWithoutValue) || method.isAccSynchronized()) {
								// Get the potentially thrown exception types and the exception table.
								String[] exceptionTypes = ((JumpException) instructions[pc]).getThrownExceptionTypes();
	
								// Is it an invocation Instruction?
								if (instructions[pc] instanceof JumpInvocation) {
									/*
									 * In theory, exceptions might be thrown. However, it is very
									 * unlikely.
									 */
									try {
										Method invokedMethod = ((JumpInvocation) instructions[pc])
												.getInvokedMethod(constantPool, classLoader);
										String[] additionalExceptionTypes = invokedMethod.getAllExceptions();
										if (additionalExceptionTypes.length > 0) {
											// Merge the exception type arrays.
											int length1 = exceptionTypes.length;
											int length2 = additionalExceptionTypes.length;
											String[] exceptionTypesNew = new String[length1 + length2];
											System.arraycopy(exceptionTypes, 0, exceptionTypesNew, 0,
													length1);
											System.arraycopy(additionalExceptionTypes, 0,
													exceptionTypesNew, length1, length2);
											exceptionTypes = exceptionTypesNew;
										}
									} catch (ClassFileException e) {
										// Log as a warning.
										if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
											Globals.getInst().symbolicExecLogger
													.debug("Generating control-graph for method "
															+ method.getName()
															+ ": An exception occured when trying to determine exceptions "
															+ "thrown by an invoked method. The resulting graph might lack "
															+ "an edge dynamic execution will reveal.");
									} catch (ExecutionException e) {
										// Log as a warning.
										if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
											Globals.getInst().symbolicExecLogger
													.debug("Generating control-graph for method "
															+ method.getName()
															+ ": An exception occured when trying to determine exceptions "
															+ "thrown by an invoked method. The resulting graph might lack "
															+ "an edge dynamic execution will reveal.");
									} catch (NoSuchMethodError e) {
										// Log as a warning.
										if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
											Globals.getInst().symbolicExecLogger
													.debug("Generating control-graph for method "
															+ method.getName()
															+ ": An exception occured when trying to determine exceptions "
															+ "thrown by an invoked method. The resulting graph might lack "
															+ "an edge dynamic execution will reveal.");
									}
								}
	
								// Process each type by checking if there is a handler for it.
								for (int b = 0; b < exceptionTypes.length; b++) {
									handleException(exceptionTable, constantPool, exceptionTypes[b],
											classLoader, pc, targets, method, uncaughtExceptionTypes, finallyList);
								}
							} // end not returning from a not synchronized method
						} // end not athrow
					} // end Jump Exception
				} // end JumpNever
			}
		} // end for

		/*
		 * Process the finally handlers.
		 * 
		 * In general, the following can be expected: The first pc at the handler pc is astore. If
		 * it is pop, the exception cannot be thrown again. On some path, an aload instruction
		 * should be found that pushed the exception onto the stack. It should be directly
		 * followed by athrow, which throws the exception.
		 * 
		 * Theoretically, the exception could be pushed right again. This is no expected behavior
		 * for the compiler, but it is syntactically correct.
		 */
		for (FinallyHandler handler : finallyList) {
			if (instructions[handler.pc] instanceof AStore) {
				int localVariableIndex = ((AStore) instructions[handler.pc])
						.getLocalVariableIndex();
				Stack<Integer> stack = new Stack<Integer>();
				stack.push(handler.pc);
				while (!stack.isEmpty()) {
					for (Integer key : edges.get(stack.pop())) {
						// Check.
						if (instructions[key] instanceof ALoad) {
							ALoad instruction = (ALoad) instructions[key];
							if (localVariableIndex == instruction.getLocalVariableIndex()) {
								int nextPc = key + instruction.getNumberOfOtherBytes() + 1;
								if (instructions[nextPc] instanceof Athrow) {
									// We now know the thrown exception type that is thrown again.
									Set<String> athrowExceptionTypes = new HashSet<String>();
									athrowExceptionTypes.add(handler.exceptionType);
									athrowExceptionTypeMapping.put(nextPc, athrowExceptionTypes);
								}
							}
						}
						// Proceed.
						stack.push(key);
					}
				}	
			}
		}

		// Finally process the athrow instructions.
		for (int pc : athrowList) {
			Set<String> exceptionTypes = athrowExceptionTypeMapping.get(pc);
			// New Set for the edges.
			Set<Integer> targets = new HashSet<Integer>();
			// Insert it.			
			edges.put(pc, targets);
			/*
			 * Process known exception types that could be thrown. If they are unknown, they cannot
			 * be declared, either. Therefore, either a RuntimeException or an error is thrown.
			 */
			if (exceptionTypes != null && exceptionTypes.size() > 0) {
				for (String exceptionType : exceptionTypes) {
					handleException(exceptionTable, constantPool, exceptionType, classLoader, pc, targets, method, uncaughtExceptionTypes, finallyList);
				}
			} else {
				targets.add(ControlGraph.CG_EXCECPTION_METHOD);
			}
		}
	}
	
	/**
	 * Handle an exception, adding the required edges or an uncaught exceptions.
	 *
	 * @param exceptionTable The exception_table of the handled method.
	 * @param constantPool the constant_pool of the class the control graph is created for.
	 * @param exceptionType The full name of the exception to be handled. 
	 * @param classLoader A class loader.
	 * @param pc The pc control is at.
	 * @param targets The jump targets for that pc.
	 * @param method The handled method.
	 * @param uncaughtExceptionTypes The set of uncaught exception types.
	 * @param finallyList The list of finally handlers.
	 */
	private static void handleException(ExceptionTable[] exceptionTable, Constant[] constantPool,
			String exceptionType, MugglClassLoader classLoader, int pc, Set<Integer> targets,
			Method method, Set<String> uncaughtExceptionTypes, List<FinallyHandler> finallyList) {
		boolean foundAHandler = false;
	for (int c = 0; c < exceptionTable.length; c++) {
			try {
				/*
				 * The line at which the exception is thrown has to be
				 * between the handler start line and its end line (not
				 * included!). The handler is either zero (finally) or the
				 * exception type has to be matched.
				 */
				String catchingExceptionName = constantPool[exceptionTable[c].getCatchType()].toString();
				if (pc >= exceptionTable[c].getStartPc()
						&& pc < exceptionTable[c].getEndPc()
						&& (exceptionTable[c].getCatchType() == 0 || ExceptionHandler
								.checkForExceptionMatch(exceptionType, catchingExceptionName,
										classLoader))) {
					/*
					 * There is an edge between the instruction throwing the
					 * exception and the handling instruction.
					 */
					int handlerPc = exceptionTable[c].getHandlerPc();
					if (handlerPc >= Limitations.MAX_CODE_LENGTH) {
						handlerPc -= Limitations.MAX_CODE_LENGTH;
					}
					targets.add(handlerPc);

					/*
					 * Finally needs special care. There will be an athrow
					 * instruction following that throws the caught
					 * exceptions again.
					 * 
					 * Note it for later processing.
					 */
					if (exceptionTable[c].getCatchType() == 0) {
						FinallyHandler handler = new FinallyHandler(pc, exceptionType);
						finallyList.add(handler);
					}

					// Found at least one handler.
					foundAHandler = true;
					// Note: Do not break! There could be both finally and an normal handler for it.
				}
			} catch (ExecutionException e) {
				// Log as a warning.
				if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
					Globals.getInst().symbolicExecLogger
							.debug("Generating control-graph for method "
									+ method.getName()
									+ ": An exception occured when trying to process exceptions "
									+ "thrown by an invoked method. The resulting graph might lack "
									+ "an edge dynamic execution will reveal.");
			}
		}
		/*
		 * If no handler has been found, put a new edge and put the
		 * exception type into the set of uncaught types.
		 */
		if (!foundAHandler) {
			targets.add(ControlGraph.CG_EXCECPTION_METHOD);
			uncaughtExceptionTypes.add(exceptionType);
		}
	}
	
}
