package de.wwu.muggl.symbolic.flow.defUseChains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Athrow;
import de.wwu.muggl.instructions.bytecode.Putfield;
import de.wwu.muggl.instructions.bytecode.Swap;
import de.wwu.muggl.instructions.bytecode.Wide;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.general.ObjectInitialization;
import de.wwu.muggl.instructions.general.Put;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.control.JumpInvocation;
import de.wwu.muggl.instructions.interfaces.control.JumpSwitching;
import de.wwu.muggl.instructions.interfaces.data.OtherFrameStackPush;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.instructions.interfaces.data.VariableDefining;
import de.wwu.muggl.instructions.interfaces.data.VariableLoading;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.instructions.interfaces.data.VariablyStackPop;
import de.wwu.muggl.instructions.interfaces.data.VariablyStackPush;
import de.wwu.muggl.symbolic.flow.controlflow.CGUtilities;
import de.wwu.muggl.symbolic.flow.controlflow.ControlGraph;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.Def;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.DefUseVariable;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.LocalVariable;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.ObjectAttribute;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.ObjectAttributeCandidate;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.StackPosition;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.StaticAttribute;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.Use;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * The DUGenerator generates def-use chains starting with a specified method. Therefore, it analyzes
 * the methods instructions and works out the definitions and uses found in it. It also branches to
 * any methods invoked as long as this has not been disabled.<br />
 * <br />
 * The DUGenerator has package visibility only as it is only meant to be utilized by the DUCoverage
 * class.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
class DUGenerator {
	// Mapping of def-use chains to methods.
	private Map<Method, DefUseChains>	defUseChainsMap;
	// The method to start with.
	private Method						initialMethod;
	// Internal data structure.
	private TreeSet<DefUseChain>		newDefsSinceLastEncounter;
	/*
	 * The current state of generation. There are five possible states: <ul> <li>0 - Initialization
	 * of this Generator is not yet finished.</li> <li>1 - Chains are currently sought for.</li>
	 * <li>2 - Chains have been found but there could be superfluous ones.</li> <li>3 - Omitting
	 * superfluous chains is in progress.</li> <li>4 - Chains have been found and superfluous ones
	 * have been omitted.</li> </ul>
	 */
	private Integer						state;
	// The instantiation number.
	private long instantiationNumber;
	
	/**
	 * Initialize the DUGenerator with the information needed.
	 * 
	 * @param defUseChains The DefUseChains object used to store the generated chains.
	 * @param method The Method to begin finding def-use chains with.
	 * @throws NullPointerException If the DefUseChains object or the Method is null.
	 */
	public DUGenerator(DefUseChains defUseChains, Method method) {
		if (defUseChains == null)
			throw new NullPointerException("The supplied DefUseChains object must not be null.");
		if (method == null)
			throw new NullPointerException("The supplied Method must not be null.");

		this.defUseChainsMap = new HashMap<Method, DefUseChains>();
		this.defUseChainsMap.put(method, defUseChains);
		this.initialMethod = method;
		this.newDefsSinceLastEncounter = new TreeSet<DefUseChain>();
		this.state = 0;
	}

	/**
	 * Find the def-use chains and store them into the DefUseChains object.
	 * 
	 * @return The mapping of methods to def-use chains.
	 * @throws IllegalStateException If this method has at least been called once (this is NOT meant
	 *         to be thread-safe).
	 * @throws InvalidInstructionInitialisationException When parsing of the bytecode
	 *         representation to instruction objects fails.
	 * @throws DUGenerationException If generation of def-use chains failed.
	 */
	public Map<Method, DefUseChains> findChains() throws InvalidInstructionInitialisationException,
			DUGenerationException {
		// Check state.
		synchronized (this.state) {
			if (this.state != 0) {
				if (this.state == 1) {
					throw new IllegalStateException("Finding chains is already in progress.");
				}
				throw new IllegalStateException("Finding chains is already finished.");
			}
			this.state = 1;
		}
		
		// Logging.
		if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
			Globals.getInst().symbolicExecLogger
					.debug("Generating def-use chains starting with method " + this.initialMethod.getName());

		// Continue until all branches have been processed.
		Stack<DUGenerationBranch> stack = new Stack<DUGenerationBranch>();
		stack.push(new DUGenerationBranch(this.initialMethod, 0));
		long startTime = System.currentTimeMillis(); // TODO
		while (!stack.isEmpty()) {
			if (System.currentTimeMillis() - startTime > 2 * TimeSupport.MILLIS_SECOND) { // TODO
				//System.out.println("too long!" + this.defUseChainsMap.get(this.initialMethod).getDefUseChains().size());
				break;
			}
			
			DUGenerationBranch currentBranch = stack.pop();
			Method method = currentBranch.getMethod();
			Instruction[] instructions = method.getInstructionsAndOtherBytes();
			int pc = currentBranch.getPC();
			boolean branchIsFinished = false;
			
			/*
			 * Continue if pc is not out of the instruction's arrays bounds. If it is, the end of
			 * the execution has been reached and the next element on the stack can be processed.
			 */
			if (pc < instructions.length) {
				Instruction instruction = instructions[pc];			
				if (instruction instanceof Athrow) {
					// Special handling of athrow.
					handleAthrow(stack, currentBranch, method, pc);
				} else {
					// If the instruction is wide, get the instruction it offers widened access to.
					if (instruction instanceof Wide)
						instruction = ((Wide) instruction).getNextInstruction();

					// Analyze the current instruction. First look if it loads a variable.
					StackPosition[] loadedStackPositions = null;
					if (instruction instanceof StackPop || instruction instanceof VariableLoading) {
						try {
							Thread.sleep(1000);
							System.out.println("HIER MUSS ICH WEGEN EINER RACE CONDITION WARTEN :((");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						loadedStackPositions = findLoadedVariables(currentBranch, method, pc,
								instruction, loadedStackPositions);
					}
					
					// Special case of loading: object initialization.
					if (instruction instanceof ObjectInitialization) {
						currentBranch.addVariable(new ObjectAttributeCandidate(method,
								this.instantiationNumber, currentBranch.copyStackPosition()));
						this.instantiationNumber++;
					}

					// Now check if it defines a variable.
					if (instruction instanceof StackPush || instruction instanceof OtherFrameStackPush
							|| instruction instanceof VariableDefining) {
						findDefinedVariables(currentBranch, method, pc, instruction);
					}

					// Finally check if is uses a variable.
					if (instruction instanceof StackPop || instruction instanceof VariableUsing) {
						branchIsFinished = findUsedVariables(currentBranch, method, pc,
								instruction, loadedStackPositions);
					}

					// Only continue if this branch is not finished, yet.
					if (!branchIsFinished) {
						branch(stack, currentBranch, method, pc, instruction);
					}
				}
			}
		}

		// Update state.
		this.state = 2;
		
		// Return the mapping.
		return this.defUseChainsMap;
	}

	/**
	 * Branch by following the control flow.
	 *
	 * @param stack The stack of generation branches.
	 * @param currentBranch The currently processed generation branch.
	 * @param method The method currently inspected.
	 * @param pc The pc currently inspected.
	 * @param instruction The instruction currently inspected.
	 * @throws InvalidInstructionInitialisationException When parsing of the bytecode
	 *         representation to instruction objects fails.
	 * @throws DUGenerationException If generation of def-use chains failed.
	 */
	private void branch(Stack<DUGenerationBranch> stack, DUGenerationBranch currentBranch,
			Method method, int pc, Instruction instruction)
			throws InvalidInstructionInitialisationException, DUGenerationException {
		// Get branch information generated by control-flow analysis.
		Set<Integer> edges = method.getControlGraph().getControlGraphFor(pc);
		if (edges != null) {
			Set<Integer> edgesToProcess = new HashSet<Integer>();
			for (int edge : edges) {
				/*
				 * Just ignore if the method is left due to an uncaught exception or
				 * a return statement.
				 */
				if (edge != ControlGraph.CG_EXCECPTION_METHOD
						&& edge != ControlGraph.CG_RETURN_METHOD) {
					edgesToProcess.add(edge);
				}
			}
			
			if (edgesToProcess.size() == 1) {				
				// No need for a safe copy.
				int edge = edgesToProcess.iterator().next();
				currentBranch.setPC(edge);
				stack.push(currentBranch);
			} else {
				for (int edge : edgesToProcess) {
					// Create a branch for each possible jump target.
					DUGenerationBranch branch = currentBranch.getSafeCopy();
					branch.setPC(edge);
					stack.push(branch);
				}
			}
		}
		
		// Invoking another method?
		if (instruction instanceof JumpInvocation) {
			ClassFile classFile = method.getClassFile();
			Constant[] constantPool = classFile.getConstantPool();
			MugglClassLoader classLoader = classFile.getClassLoader();
			Method invokedMethod;
			try {
				invokedMethod = ((JumpInvocation) instruction).getInvokedMethod(
						constantPool, classLoader);
			} catch (ClassFileException e) {
				throw new DUGenerationException(e);
			} catch (ExecutionException e) {
				throw new DUGenerationException(e);
			}

			// Only branch if tracking is desired by the option set.
			if (branchingDesired(this.initialMethod, invokedMethod)) {
				DUGenerationBranch branch = new DUGenerationBranch(currentBranch,
						invokedMethod, 0);

				// Mark as many local variables as defined as the method has parameters.
				int numberOfParameters = method.getNumberOfParameters();
				int startat = 1;
				if (method.isAccStatic()) startat = 0;
				for (int a = startat; a < numberOfParameters; a++) {
					DefUseVariable variable = new LocalVariable(method, a);
					branch.addDef(new Def(variable, ControlGraph.CG_INVOKED_METHOD));
				}

				// Push the new branch.
				stack.push(branch);
				
				// Check whether chains have been found for this method, yet.
				if(!this.defUseChainsMap.containsKey(invokedMethod)) {
					//Instantiate the new DefUseChains object and put it.
					DefUseChains defUseChains = new DefUseChains(invokedMethod);
					this.defUseChainsMap.put(invokedMethod, defUseChains);
				}	
			} else {
				// We did not branch, but probably the method would have returned a parameter.
				if (!invokedMethod.getReturnType().equals("void")) {
					currentBranch.getStackPosition();
				}
			}
		}
	}
	
	/* old code - most likely it is obsolete since the control graph's data is used.
	// Distinguish between instructions by their jumping characteristics.
	if (instruction instanceof JumpAlways) {
		currentBranch.setPC(currentBranch.getPC()
				+ ((JumpAlways) instruction).getJumpIncrement());
		stack.push(currentBranch);
	} else if (instruction instanceof JumpConditional) {
		DUGenerationBranch branch = currentBranch.getSafeCopy();
		branch.setPC(((JumpConditional) instruction).getJumpTarget());
		stack.push(branch);
		// Also push the non-jump target.
		currentBranch.setPC(pc + instruction.getNumberOfOtherBytes() + 1);
		stack.push(currentBranch);
	} else if (instruction instanceof JumpInvocation) {
		ClassFile classFile = method.getClassFile();
		Constant[] constantPool = classFile.getConstantPool();
		MugglClassLoader classLoader = classFile.getClassLoader();
		Method invokedMethod;
		try {
			invokedMethod = ((JumpInvocation) instruction).getInvokedMethod(
					constantPool, classLoader);
		} catch (ClassFileException e) {
			throw new DUGenerationException(e);
		} catch (ExecutionException e) {
			throw new DUGenerationException(e);
		}

		// Only branch if tracking is desired by the option set.
		if (branchingDesired(this.initialMethod, invokedMethod)) {
			DUGenerationBranch branch = new DUGenerationBranch(currentBranch,
					invokedMethod, 0);

			// Mark as many local variables as defined as the method has parameters.
			int numberOfParameters = method.getNumberOfParameters();
			int startat = 1;
			if (method.isAccStatic()) startat = 0;
			for (int a = startat; a < numberOfParameters; a++) {
				DefUseVariable variable = new LocalVariable(method, a);
				branch.addDef(new Def(variable, -1));
			}

			// First push the return target.
			currentBranch.setPC(pc + instruction.getNumberOfOtherBytes() + 1);
			stack.push(currentBranch);

			// Then push the new branch.
			stack.push(branch);
		} else {
			currentBranch.setPC(pc + instruction.getNumberOfOtherBytes() + 1);
			stack.push(currentBranch);
		}
	} else if (instruction instanceof JumpSwitching) {
		// Visit all possible jumping targets.
		int[] jumpTargets = ((JumpSwitching) instruction).getJumpTargets();
		for (int target : jumpTargets) {
			DUGenerationBranch branch = currentBranch.getSafeCopy();
			branch.setPC(target);
			stack.push(branch);
		}
	} else {
		// Just increase the pc and push the branch
		currentBranch.setPC(pc + instruction.getNumberOfOtherBytes() + 1);
		stack.push(currentBranch);
	}

	// Instructions that may throw exceptions require special treatment.
	if (instruction instanceof JumpException) {
		// Get branch information generated by control-flow analysis.
		Set<Integer> edges = controlGraph.getControlGraphFor(pc);
		if (edges != null) {
			for (int edge : edges) {
				// Just ignore if the method is left due to an uncaught exception.
				if (edge != ControlGraph.CG_EXCECPTION_METHOD) {
					// Create a branch for the handled exception.
					currentBranch.getSafeCopy().setPC(edge);
					stack.push(currentBranch);
				}
			}
		}
	}*/

	/**
	 * Handle an <code>athrow</code> instruction.
	 * 
	 * Athrow is a special case since it may define an element on the current frame's operand
	 * stack. It, however, only does so, if the thrown exception is caught locally.
	 * 
	 * If an exception thrown by athrow is not caught locally, athrow will define an element of the
	 * operand stack of the frame that caught the exception. This will no be considered here,
	 * though.
	 * 
	 * @param stack The stack of generation branches.
	 * @param currentBranch The currently processed generation branch.
	 * @param method The method currently inspected.
	 * @param pc The pc currently inspected.
	 * @throws InvalidInstructionInitialisationException When parsing of the bytecode representation
	 *         to instruction objects fails.
	 */
	private void handleAthrow(Stack<DUGenerationBranch> stack, DUGenerationBranch currentBranch,
			Method method, int pc) throws InvalidInstructionInitialisationException {
		Set<Integer> edges = method.getControlGraph().getControlGraphFor(pc);
		if (edges != null) {
			for (int edge : edges) {
				// Just ignore if the method is left due to an uncaught exception.
				if (edge != ControlGraph.CG_EXCECPTION_METHOD) {
					// Clear the stack.
					while (currentBranch.getStackPosition() > 0) {
					DefUseVariable variable = new StackPosition(method, currentBranch
							.decreaseStackPosition());
					currentBranch.unloadVariable(variable);
					}
					// Push one value
					DefUseVariable variable = new StackPosition(method, 0);
					currentBranch.addVariable(variable);
					
					// Create a branch for this.
					currentBranch.getSafeCopy().setPC(edge);
					stack.push(currentBranch);
				}
			}
		}
	}
	
	/**
	 * Find variables loaded by an instruction.
	 * 
	 * @param currentBranch The currently processed generation branch.
	 * @param method The method currently inspected.
	 * @param pc The pc currently inspected.
	 * @param instruction The instruction currently inspected.
	 * @param loadedStackPositions An array to track loaded stack positions.
	 * @return The array of loaded stack positions.
	 */
	private synchronized StackPosition[] findLoadedVariables(DUGenerationBranch currentBranch, Method method,
			int pc, Instruction instruction, StackPosition[] loadedStackPositions) {
		System.out.println("Find loaded variables for method=" + method+ " at pc="+pc+" with instruction="+instruction);
		// Check both cases.
		if (instruction instanceof StackPop) {
			
			int elements = ((StackPop) instruction).getNumberOfPoppedElements();
			if (instruction instanceof JumpInvocation) {
				// Probably, more elements have to be popped.
				ClassFile classFile = method.getClassFile();
				Constant[] constantPool = classFile.getConstantPool();
				MugglClassLoader classLoader = classFile.getClassLoader();
				try {
					elements += ((JumpInvocation) instruction).getInvokedMethod(constantPool, classLoader).getNumberOfArguments();
				} catch (ClassFileException e) {
					// Ignore.
				} catch (ExecutionException e) {
					// Ignore.
				}
			}

			if (instruction instanceof VariablyStackPop) {
				VariablyStackPop variablyStackPop = (VariablyStackPop) instruction;
				byte[] types = getTypesOnStack(method, pc, variablyStackPop
						.getMaximumNumberOfPoppedElements());
				elements = variablyStackPop.getNumberOfPoppedElements(types);
			}

			loadedStackPositions = new StackPosition[elements];
			for (int a = 0; a < elements; a++) {
				StackPosition stackPosition = new StackPosition(method,
						currentBranch.decreaseStackPosition());
				currentBranch.addVariable(stackPosition);
				loadedStackPositions[a] = stackPosition;
			}
		}
		
		if (instruction instanceof VariableLoading) {
			LocalVariable loadedVariable = new LocalVariable(method, ((VariableLoading) instruction)
					.getLocalVariableIndex());
			currentBranch.addVariable(loadedVariable);
		}
		
		return loadedStackPositions;
	}
	
	/**
	 * Find definitions made by this instruction.
	 * 
	 * @param currentBranch The currently processed generation branch.
	 * @param method The method currently inspected.
	 * @param pc The pc currently inspected.
	 * @param instruction The instruction currently inspected.
	 * @param loadedStackPositions An array to track loaded stack positions.
	 * @throws InvalidInstructionInitialisationException When parsing of the bytecode
	 *         representation to instruction objects fails.
	 */
	private void findDefinedVariables(DUGenerationBranch currentBranch, Method method, int pc,
			Instruction instruction) throws InvalidInstructionInitialisationException {
		// Check all three cases.
		if (instruction instanceof StackPush) {
			int elements = ((StackPush) instruction).getNumberOfPushedElements();

			if (instruction instanceof VariablyStackPush) {
				VariablyStackPush variablyStackPush = (VariablyStackPush) instruction;
				byte[] types = getTypesOnStack(method, pc, variablyStackPush
						.getMaximumNumberOfPushedElements());
				elements = variablyStackPush.getNumberOfPushedElements(types);
			}

			for (int a = 0; a < elements; a++) {
				DefUseVariable variable = new StackPosition(this.initialMethod,
						currentBranch.getStackPosition());
				Def def = new Def(variable, pc);
				currentBranch.addDef(def);
				registerDef(def, method);
			}
		}
		if (instruction instanceof OtherFrameStackPush) {
			/*
			 *  It either is athrow or a return instruction. But athrow has been handled earlier...
			 */
			DUGenerationBranch invokedBy = currentBranch.getInvokedBy();
			if (invokedBy != null) {
				DefUseVariable variable = new StackPosition(invokedBy.getMethod(),
						invokedBy.getStackPosition());
				Def def = new Def(variable, currentBranch.getInvokedFromPc());
				invokedBy.addDef(def);
				registerDef(def, invokedBy.getMethod());
			}
		}
		if (instruction instanceof VariableDefining) {
			// Distinguish between the possibilities.
			// TODO does astore not need the VariableDefining interface?
			if (instruction instanceof Invoke) {
				ClassFile classFile = this.initialMethod.getClassFile();
				int arguments = 0;
				try {
					arguments = ((Invoke) instruction).getNumberOfArguments(classFile.getConstantPool(), classFile.getClassLoader());
				} catch (ClassFileException e) {
					// This cannot happen - the class is already loaded.
				} catch (ExecutionException e) {
					// This cannot happen - the method is already loaded.
				} 
				for (int a = 0; a < arguments; a++) {
					LocalVariable variable = new LocalVariable(method, a);									
					Def def = new Def(variable, pc);
					currentBranch.addDef(def);
					registerDef(def, method);
				}
			} else if (instruction instanceof Put) {
				DefUseVariable variable;
				Field attribute;
				ClassFile classFile = this.initialMethod.getClassFile();
				try {
					attribute = ((Put) instruction).getFieldNoExecution(classFile, classFile.getClassLoader());
				
					/*
					 * Distinguish between putting an object attribute and a static
					 * attribute.
					 */
					if (instruction instanceof Putfield) {
						ObjectAttributeCandidate candidate = currentBranch
								.getObjectrefAtCurrentPos();
						variable = new ObjectAttribute(candidate, attribute);
					} else {
						variable = new StaticAttribute(method, attribute);
					}

					Def def = new Def(variable, pc);
					currentBranch.addDef(def);
					registerDef(def, method);
				} catch (ClassFileException e) {
					// This cannot happen - the class is already loaded.
				}
			} else {
				// It has to access a local variable by a store instruction.
				LocalVariable variable = new LocalVariable(method,
						((LocalVariableAccess) instruction).getLocalVariableIndex());									
				Def def = new Def(variable, pc);
				currentBranch.addDef(def);
				registerDef(def, method);
			}
		}
	}
	
	/**
	 * Find usages made by this instruction.
	 *
	 * @param currentBranch  currently processed generation branch.
	 * @param method The method currently inspected.
	 * @param pc The pc currently inspected.
	 * @param instruction The instruction currently inspected.
	 * @param loadedStackPositions An array to track loaded stack positions.
	 * @return true, if execution in this branch can be finished; false otherwise.
	 */
	private boolean findUsedVariables(DUGenerationBranch currentBranch, Method method, int pc,
			Instruction instruction, StackPosition[] loadedStackPositions) {
		boolean branchMightBeFinished = false;
		boolean branchIsNotFinished = false;

		// Have elements been popped?
		if (instruction instanceof StackPop) { 
			if (loadedStackPositions != null) {
				for (DefUseVariable variable : loadedStackPositions) {
					Def def = currentBranch.fetchDef(variable);
					if (def != null) {
						Use use = new Use(variable, pc);
						DefUseChain chain = new DefUseChain(def, use);
						if (!addNewChain(chain, method)) {
							// Only continue if new definitions were found in the meantime.
							if (!checkAndResetChanges(chain)) {	// Changed to ! Is that correct? TODO
								branchMightBeFinished = true;
							}
						}
	
						// Unload variable.
						currentBranch.unloadVariable(variable);
					}
				}
			}
		}
		
		// Have elements been loaded?
		if (instruction instanceof VariableUsing) {
			Set<DefUseVariable> variablesToUnload = new TreeSet<DefUseVariable>();
			for (DefUseVariable variable : currentBranch.getVariable()) {
				Def def = currentBranch.fetchDef(variable);
				if (def != null) {
					Use use = new Use(variable, pc);
					DefUseChain chain = new DefUseChain(def, use);
					if (!addNewChain(chain, method)) {
						// Only continue if new definitions were found in the meantime.
						if (checkAndResetChanges(chain)) {
							branchMightBeFinished = true;
						} else {
							// We definitely have to continue.
							branchIsNotFinished = true;
						}
					} else {
						// We definitely have to continue.
						branchIsNotFinished = true;
					}
	
					// Unload variable.
					variablesToUnload.add(variable);
				}
			}
		
			// To avoid concurrent modification, unload variables now.
			if (variablesToUnload.size() > 0) {
				for (DefUseVariable variable : variablesToUnload) {
					currentBranch.unloadVariable(variable);
				}
			}
		}
		
		return branchMightBeFinished && !branchIsNotFinished;
	}
	
	/**
	 * Omit any def-use chains that are not required. This applies to any chain that will always be
	 * passed if its defining instruction is executed as both the defining and the using instruction
	 * are in the same basic block.
	 * 
	 * @throws IllegalStateException Either if finding def-use chains is not yet finished or if it
	 *         is finished and this method has at least been called once (this is NOT meant to be
	 *         thread-safe).
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instructions.
	 */
	public void omitIrrelevantChains() throws InvalidInstructionInitialisationException {
		// Check state.
		synchronized (this.state) {
			switch (this.state) {
				case 0: // Fall through. 
				case 1:
					throw new IllegalStateException("Finding chains is not yet finished.");
				case 2:
					break; // Just continue, this is the desired state.
				case 3:
					throw new IllegalStateException("Omitting chains is already in progress.");
				case 4:
					throw new IllegalStateException("Omitting chains is already finished.");
			}
			this.state = 3;
		}

		// Try to find irrelevant chains.
		for (DefUseChains chains : this.defUseChainsMap.values()) {
			// Get basic blocks for the method.
			ControlGraph controlGraph = chains.getMethod().getControlGraph();
			Map<Integer, Integer> basicBlocks = CGUtilities.getBasicBlocks(controlGraph);

			// Combine def-use chains that are within basic blocks.
			Set<DefUseChain> toDelete = new TreeSet<DefUseChain>();
			for (Entry<Integer, Integer> entry : basicBlocks.entrySet()) {
				int pcStart = entry.getKey();
				int pcEnd = entry.getValue();
				// Def def = null; TODO: this should not be required.
				// Use use = null; TODO: this should not be required.
				// Check all def-use chains.
				for (DefUseChain chain : chains.getDefUseChains()) {
					int pcDef = chain.getDef().getInstructionNumber();
					int pcUse = chain.getUse().getInstructionNumber();
					// Is it defined and used within the basic block?
					if (pcDef >= pcStart && pcUse <= pcEnd) {
						/* TODO: this should not be required.
						if (def == null) {
							def = chain.getDef();
						} else {
							// Only change if the definition is closer to pcStart.
							if (def.getInstructionNumber() > pcDef) {
								def = chain.getDef();
							}
						}
						if (use == null) {
							use = chain.getUse();
						} else {
							// Only change if the usage is closer to pcEnd.
							if (use.getInstructionNumber() < pcUse) {
								use = chain.getUse();
							}
						}*/

						/*
						 * Mark the def-use chain for deleting. We cannot do so now without getting
						 * a ConcurrentModificationException.
						 */
						toDelete.add(chain);
					}
				}

				// Delete any chains that were marked.
				for (DefUseChain chain : toDelete) {
					chains.removeDefUseChain(chain);
				}

			}
		}

		// Update state.
		this.state = 4;
	}
	
	/**
	 * Determine the types of the 1 to 4 uppermost stack entries.
	 * 
	 * @param method The Method to process.
	 * @param pc The pc to start reverted execution from.
	 * @param elements The number of elements to determine the type for.
	 * @return An array containing the stack elements types in reversed order.
	 */
	private byte[] getTypesOnStack(Method method, int pc, int elements) {
		ClassFile methodClassFile = method.getClassFile();

		/*
		 * The stack will grow at most to <code>elements</code> elements. Empty positions are marked
		 * with -1.
		 */
		byte[] types = new byte[elements];
		int pos = 0;
		for (int a = 0; a < types.length; a++) {
			types[a] = -1;
		}

		// Process the method in reverse order.
		Instruction[] instructions;
		try {
			instructions = method.getInstructionsAndOtherBytes();
		} catch (InvalidInstructionInitialisationException e) {
			// This cannot happen here, it would have happened earlier.
			return null;
		}

		List<int[]> swapAsSoonAsPossible = new ArrayList<int[]>();
		for (int a = pc - 1; a > 0; a--) {
			Instruction instruction = instructions[a];
			// Did we find an instruction?
			if (instruction != null) {
				/*
				 * If the instruction is a conditional jump or switching instruction, we give up.
				 * The actual types are likely to be only determinable completely dynamically then.
				 * A complete analysis that takes the types pushed by any preceding instructions
				 * into account and calculates possible or likely results would take too much time
				 * to execute. Results could as well be depended on input parameter.
				 */
				if (instruction instanceof JumpConditional || instruction instanceof JumpSwitching)
					break;

				/*
				 * Also give up if another instruction is encountered that pops or pushes a variable
				 * number of elements.
				 */
				if (instruction instanceof VariablyStackPop
						|| instruction instanceof VariablyStackPush) break;

				// Special treatment of instruction swap.
				if (instruction instanceof Swap) {
					int[] swap = { pc, pc + 1 };
					swapAsSoonAsPossible.add(swap);
				} else {
					// Try to modify the stack accordingly.
					int elementsToPop = 0;
					byte[] typesToPop = null;
					int elementsToPush = 0;
					byte typeToPush = -1;
					if (instruction instanceof StackPop) {
						StackPop popInstruction = (StackPop) instruction;
						elementsToPop = popInstruction.getNumberOfPoppedElements();
						typesToPop = popInstruction.getTypesPopped(methodClassFile);
					}

					if (instruction instanceof StackPush) {
						StackPush pushInstruction = (StackPush) instruction;
						elementsToPush = pushInstruction.getNumberOfPushedElements();
						typeToPush = pushInstruction.getTypePushed(methodClassFile);
					}

					/*
					 * First pop as many elements as the instructions would push. Then push as many
					 * elements as it would pop. The stack than has the state that it had prior to
					 * executing the instruction.
					 * 
					 * This is done in three steps.
					 */
					if (elementsToPush > 0) {
						/*
						 * 1. Check whether the topmost elements on the stack have undefined types.
						 * If so, define them if possible.
						 */
						if (typeToPush != -1) {
							for (int b = 0; b < elementsToPush; b++) {
								int curPos = pos - b;
								if (curPos < 0) break;
								if (types[curPos] == -1) {
									types[curPos] = typeToPush;
								}
							}

							// Check whether this is sufficient.
							if (pos >= types.length) {
								boolean missingType = false;
								for (int b = 0; b < types.length; b++) {
									if (types[b] == -1) {
										missingType = true;
										break;
									}
								}

								if (!missingType) {
									return types;
								}
							}
						}

						// 2. Pop items from the stack.
						pos -= elementsToPush;
					}

					if (elementsToPop > 0) {
						// 3. Push as many elements as would have been popped by the instruction.
						for (int b = 0; b < elementsToPop; b++) {
							if (pos < types.length && pos >= 0) {
								/*
								 * Only push an element if it is not yet defined and if it can be
								 * defined now.
								 */
								if (types[pos] == -1 && typesToPop != null && typesToPop.length > b) {
									types[pos] = typesToPop[b];
								}
							}

							pos++;
						}

						// Check if all required elements on the stack are defined by now.
						if (pos >= types.length) {
							boolean missingType = false;
							for (int b = 0; b < types.length; b++) {
								if (types[b] == -1) {
									missingType = true;
									break;
								}
							}

							if (!missingType) {
								return types;
							}
						}
					}

				}
			}
		}

		// Finished.
		return types;
	}

	/**
	 * Add a newly found def-use chain to the already found chains. It is is known already, return
	 * false.
	 * 
	 * @param chain The chain to add.
	 * @param method The method the def-use chains belongs to.
	 * @return true, if it was added; false if it was known already.
	 */
	private boolean addNewChain(DefUseChain chain, Method method) {
		// Get the DefUseChains object or create it if there is no mapping, yet.
		DefUseChains chains = this.defUseChainsMap.get(method);
		if (chains == null) {
			chains = new DefUseChains(method);
			this.defUseChainsMap.put(method, chains);
		}

		// Put the chain.
		return chains.addDefUseChain(chain);
	}

	/**
	 * Register a new definition so it can be checked whether it is a new definition found on the
	 * path between already known def-use chains.
	 * 
	 * @param def The definition just found.
	 * @param method The method it belongs to.
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instructions.
	 */
	private void registerDef(Def def, Method method)
			throws InvalidInstructionInitialisationException {
		// Preparations.
		DefUseChains chains = this.defUseChainsMap.get(method);
		int pc = def.getInstructionNumber();
		ControlGraph controlGraph = method.getControlGraph();

		// Process all def-use chains yet found.
		for (DefUseChain chain : chains.getDefUseChains()) {
			int from = chain.getDef().getInstructionNumber();
			int to = chain.getUse().getInstructionNumber();
			if (CGUtilities.isOnPath(controlGraph, from, to, pc)) {
				this.newDefsSinceLastEncounter.add(chain);
			}
		}
	}

	/**
	 * Check whether new definitions were found on the path between the specified def-use chains
	 * definitions and usage. Reset the information about it after doing so.
	 * 
	 * @param chain The def-use chain to check.
	 * @return true, if there were new definitions; false otherwise.
	 */
	private boolean checkAndResetChanges(DefUseChain chain) {
		if (this.newDefsSinceLastEncounter.contains(chain)) {
			this.newDefsSinceLastEncounter.remove(chain);	
			return true;
		}
		return false;
	}

	/**
	 * Check if branching is desired for the specified method by checking the settings for coverage
	 * tracking.
	 * 
	 * @param initialMethod The method coverage generation was started with.
	 * @param methodToInvoke The method that would be invoked.
	 * @return true, if branching is desired; false otherwise.
	 * @see Options#coverageTracking
	 */
	private boolean branchingDesired(Method initialMethod, Method methodToInvoke) {
		switch (Options.getInst().coverageTracking) {
			case 0:
				if (initialMethod == methodToInvoke) {
					return true;
				}
				break;
			case 1:
				if (initialMethod.getClassFile() == methodToInvoke.getClassFile()) {
					return true;
				}
				break;
			case 2:
				if (initialMethod.getClassFile().getPackageName().equals(
						methodToInvoke.getClassFile().getPackageName())) {
					return true;
				}
				break;
			case 3:		
				if (initialMethod.getClassFile().getTopLevelPackageName().equals(
						methodToInvoke.getClassFile().getTopLevelPackageName())) {
					return true;
				}
				break;
			case 4:
				return true;
		}
		return false;
	}

}
