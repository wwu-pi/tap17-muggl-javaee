package de.wwu.muggl.symbolic.flow.coverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.symbolic.flow.controlflow.ControlGraph;
import de.wwu.muggl.symbolic.flow.defUseChains.DefUseChainsInitial;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;

/**
 * This Class is the topmost controller for any control-flow and data-flow generation and coverage
 * tracking.<br />
 * <br />
 * While it is possible to use some of the structures independently, this controller should be used
 * to access them. It will generate full control-flow and data-flow structures starting with an
 * initial method and it offers auxiliary functions to keep track of the coverage.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class CoverageController {
	// General fields.
	private SymbolicVirtualMachine vm;
	private Method initialMethod;
	private ClassFile initialMethodClassFile;

	// Field for the mapping.
	private Map<Method, CGCoverage> cGCoverageMap;
	// Field for the def-use chains coverage.
	private DUCoverage dUCoverage;

	// Field to store coverage information.
	private String whatIsCovered;

	/**
	 * Construct this controller.
	 *
	 * @param vm The symbolic virtual machine this controller belongs to.
	 * @throws NullPointerException If any parameter is null.
	 * @throws InitializationException In cases of non-recoverable initialization exceptions.
	 */
	public CoverageController(SymbolicVirtualMachine vm) throws InitializationException {
		// Checks.
		if (vm == null) throw new NullPointerException("vm must not be null.");

		// Basic initialization.
		this.vm = vm;
		this.initialMethod = vm.getInitialMethod();
		this.initialMethodClassFile = this.initialMethod.getClassFile();
		this.cGCoverageMap = new HashMap<Method, CGCoverage>();
		Options options = Options.getInst();
		this.whatIsCovered = null;

		// Generate the control graph for the initial method and the def-use chains.
		if (options.useCFCoverage) {
			this.cGCoverageMap.put(this.initialMethod, new CGCoverage(vm, this.initialMethod));
		}
		if (options.useDUCoverage) {
			this.dUCoverage = new DUCoverage(vm, new DefUseChainsInitial(this.initialMethod));
		} else {
			this.dUCoverage = null;
		}
	}

	/**
	 * Update the coverage according to the instruction number.<br />
	 *
	 * @param method The method currently executed.
	 * @param pc The current pc of the virtual machine.
	 * @throws InitializationException In cases of non-recoverable initialization exceptions.
	 */
	public void updateCoverage(Method method, int pc) throws InitializationException {
		CGCoverage cGCoverage = getCGCoverage(method);
		if (cGCoverage != null)
			cGCoverage.updateCoverage(pc);
		if (this.dUCoverage != null)
			this.dUCoverage.updateCoverage(method, pc);
	}

	/**
	 * Update the control graph is execution of a method was finished normally by setting -2.
	 * Control graph edges from exceptions are covered by the exception handler and do not need to
	 * be treated by this method. If finished, return the information whether full coverage of the
	 * method is now reached.
	 *
	 * @param method The method currently executed.
	 * @return true, if full control flow coverage is now reached for the method; false otherwise.
	 */
	public boolean markCFInvocationFinished(Method method) {
		try {
			CGCoverage cGCoverage = getCGCoverage(method);
			if (cGCoverage != null) {
				cGCoverage.updateCoverage(ControlGraph.CG_RETURN_METHOD);
				return cGCoverage.isEverythingCovered();
			}
		} catch (InitializationException e) {
			/*
			 * If this method is used in a reasonable way, this cannot happen. If it should happen,
			 * e.g. if this method is called before actually invoking a method, simply
			 * ignore it.
			 */
		}
		return false;
	}

	/**
	 * Check whether execution should be stopped with regard to the fulfillment of the coverage criteria.
	 *
	 * @param method The method currently executed.
	 * @return true, if executed should be stopped.
	 */
	public boolean shallStopExecution(Method method) {
		// Variables.
		Options options = Options.getInst();
		boolean defUseIsCovered = false;
		boolean controlFlowIsCovered = false;
		boolean abort = false;

		// Get coverage data.
		try {
			CGCoverage cGCoverage = getCGCoverage(method);
			if (cGCoverage != null)
				defUseIsCovered = cGCoverage.isEverythingCovered();
		} catch (InitializationException e) {
			/*
			 * If this method is used in a reasonable way, this cannot happen. If it should happen,
			 * e.g. if this method is called before actually tracking coverage of a method, simply
			 * ignore it.
			 */
		}
		if (this.dUCoverage != null)
			controlFlowIsCovered = this.dUCoverage.isEverythingCovered();

		// Make a decision.
		switch(options.coverageAbortionCriteria) {
			// Abort on def-use coverage.
			case 1:
				if (defUseIsCovered) {
					if (options.coverageTracking != 0) {
						this.whatIsCovered = "def-use chains in all tracked methods";
					} else {
						this.whatIsCovered = "def-use chains in the initial method";
					}
					abort = true;
				}
				break;
			// Abort on control flow coverage.
			case 2:
				if (controlFlowIsCovered) {
					/*
					 * If coverage is needed in other but the initial method, make sure any tracked
					 * method is fully covered.
					 */
					if (options.coverageTracking != 0) {
						abort = true;
						for (Entry<Method, CGCoverage> entry : this.cGCoverageMap.entrySet()) {
							if (!entry.getValue().isEverythingCovered()) {
								abort = false;
								break;
							}
						}
						if (abort) {
							this.whatIsCovered = "control flow in all tracked methods";
						}
					} else {
						abort = true;
						this.whatIsCovered = "control flow in the initial method";
					}
				}
				break;
			// Abort coverage if both are met.
			case 3:
				if (defUseIsCovered && controlFlowIsCovered) {
					/*
					 * If coverage is needed in other but the initial method, make sure any tracked
					 * method is fully covered.
					 */
					if (options.coverageTracking != 0) {
						// Everything covered so far?
						if (defUseIsCovered) {
							abort = true;
							for (Entry<Method, CGCoverage> entry : this.cGCoverageMap.entrySet()) {
								if (!entry.getValue().isEverythingCovered()) {
									abort = false;
									break;
								}
							}
							if (abort) {
								this.whatIsCovered = "def-use chains and control flow in all tracked methods";
							}
						}
					} else {
						abort = true;
						this.whatIsCovered = "def-use chains and control flow in the initial method";
					}
				}
				break;
		}

		return abort;
	}

	/**
	 * Return a String representation of what has been covered. This information is set when
	 * {@link #shallStopExecution(Method)} is executed and returns <code>true</code>.<br />
	 * <br />
	 * Watch out: This method is not thread-safe. If #shallStopExecution(Method) is accessed again
	 * before execution of this method is finished, the returned result will be arbitrary.
	 *
	 * @return Return a String representation of what has been covered; or null, if no such
	 *         information has been set, yet.
	 */
	public String getWhatIsCovered() {
		return this.whatIsCovered;
	}

	/**
	 * Check if every every control graph edge and def-use chain is covered. Control flow coverage
	 * is tracked on a per-method level.
	 *
	 * @param method The method currently executed.
	 * @return a boolean array with two dimensions; the first ones shows control graph coverage, the
	 *         second one shows def-use coverage.
	 */
	public boolean[] isEverythingCovered(Method method) {
		boolean[] isCovered = {false, false};
		try {
			CGCoverage cGCoverage = getCGCoverage(method);
			if (cGCoverage != null)
				isCovered[0] = cGCoverage.isEverythingCovered();
		} catch (InitializationException e) {
			/*
			 * If this method is used in a reasonable way, this cannot happen. If it should happen,
			 * e.g. if this method is called before actually tracking coverage of a method, simply
			 * ignore it.
			 */
		}
		if (this.dUCoverage != null)
			isCovered[1] = this.dUCoverage.isEverythingCovered();
		return isCovered;
	}

	/**
	 * Commit the coverage of control-flow edges and def-use chains as a new solution was found.
	 */
	public void commitAllchanges() {
		for (CGCoverage cGCoverage : this.cGCoverageMap.values()) {
			cGCoverage.commit();
		}
		if (this.dUCoverage != null)
			this.dUCoverage.commit();
	}

	/**
	 * Get the mapping of methods to control flow coverage. Coverage is represented by an array of
	 * boolean values indicating which control graph edges are currently covered. A value of true
	 * indicates that the corresponding edge is covered.
	 *
	 * @return A mapping of methods to control flow coverage.
	 */
	public Map<Method, boolean[]> getCFCoverageMap() {
		Map<Method, boolean[]> cFCoverageMap = new HashMap<Method, boolean[]>();
		for (Entry<Method, CGCoverage> entry : this.cGCoverageMap.entrySet()) {
			cFCoverageMap.put(entry.getKey(), entry.getValue().getCurrentCoverage());
		}
		return cFCoverageMap;
	}

	/**
	 * Get the map of {@link Method} to {@link CGCoverage} instances.
	 *
	 * @return The mapping of methods to control flow coverage objects.
	 */
	public Map<Method, CGCoverage> getCGCoverageMap() {
		return this.cGCoverageMap;
	}
	
	/**
	 * Get the {@link DUCoverage} instance of the coverage controller.
	 *
	 * @return The def-use chains coverage object.
	 */
	public DUCoverage getDUCoverage() {
		return this.dUCoverage;
	}
	
	/**
	 * Get an array of boolean values indicating which def-use chains have been covered. A value of
	 * true indicates that the corresponding def-use chain is covered.
	 *
	 * @return An array of boolean values indicating which def-use chains have been covered.
	 */
	public boolean[] getDUCoverageAsBoolean() {
		if (this.dUCoverage == null) {
			return new boolean[0];
		}
		
		return this.dUCoverage.getCoverage();
	}

	/**
	 * Report a frame change. This is important for the control flow coverage since the method of
	 * the frame has to be covered (if it is newly entered). The value of the last pc of the covered method will be reverted
	 * to -1 indicating that the next step is the execution of the method's first instruction.
	 *
	 * @param frame The frame that will now be executed in the virtual machine.
	 */
	public void reportFrameChange(Frame frame) {
		// Check if the frame is at pc 0.
		if (frame.getPc() == 0) {
			CGCoverage cGCoverage = this.cGCoverageMap.get(frame.getMethod());
			if (cGCoverage != null) {
				cGCoverage.revertLastPc(-1);
			}
		}
	}

	/**
	 * Revert the pc for the specified method to the specified value.<br />
	 * <br />
	 * 
	 * In general, this method is to be used to report a return from an invoked method. This
	 * requires the last pc value of the invoking method to be reverted in order to reflect the
	 * transition that will happen when execution continues. Alternatively, recovering a state in a
	 * depth first search requires the pc to be reverted.
	 * 
	 * @param method The method currently executed.
	 * @param pc The pc to revert the last pc value to.
	 */
	public void revertPcTo(Method method, int pc) {
		CGCoverage cGCoverage = this.cGCoverageMap.get(method);
		if (cGCoverage != null) {
			cGCoverage.revertLastPc(pc);
		}
	}

	/**
	 * Report that handling of an exception failed in a method. This will yield a transition of the
	 * last pc value and -3 which indicates abortion of method invocation due to an exception.
	 *
	 * @param method The method currently executed.
	 */
	public void reportFailedHandling(Method method) {
		CGCoverage cGCoverage = this.cGCoverageMap.get(method);
		if (cGCoverage != null) {
			cGCoverage.updateCoverage(ControlGraph.CG_EXCECPTION_METHOD);
		}
	}

	/**
	 * Get the {@link CGCoverage} for the the given Method. Check if control flow coverage
	 * has to be initialized and do so if required.<br />
	 * <br />
	 * This method makes sure that there is only one ControlGraph per Method, even if multiple frames
	 * are generated. However, it only generates the ControlGraph object if this frame is a frame for
	 * a Method that is supposed to be tracked in accordance with the options.
	 *
	 * @param method The method to get the control flow coverage for.
	 * @return an instance of {@link CGCoverage} or null, if this method should not be covered.
	 * @throws InitializationException In cases of non-recoverable initialization exceptions.
	 */
	private CGCoverage getCGCoverage(Method method) throws InitializationException {
		CGCoverage cGCoverage = null;
		Options options = Options.getInst();

		cGCoverage = this.cGCoverageMap.get(method);
		if (cGCoverage == null && options.useCFCoverage) {
			// Generate the ControlGraph if that is desired.
			boolean tracking = false;
			switch (options.coverageTracking) {
				case 1:
					if (method.getClassFile().getName().equals(this.initialMethodClassFile.getName()))
						tracking = true;
					break;
				case 2:
					String package1 = method.getClassFile().getPackageName();
					String package2 = this.initialMethodClassFile.getPackageName();
					if (package1.equals(package2))
						tracking = true;
					break;
				case 3:
					String package3 = method.getClassFile().getPackageName();
					String package4 = this.initialMethodClassFile.getPackageName();
					// Drop anything after the first dot.
					int position = package3.indexOf(".");
					if (position != -1)
						package3 = package3.substring(0, position);
					position = package4.indexOf(".");
					if (position != -1)
						package4 = package4.substring(0, position);
					// Compare.
					if (package3.equals(package4))
						tracking = true;
					break;
				case 4:
					tracking = true;
					break;
				default:
					if (method == this.initialMethod) tracking = true;
					break;
			}

			// Generate it.
			if (tracking) {
				cGCoverage = new CGCoverage(this.vm, method);
				this.cGCoverageMap.put(method, cGCoverage);
			}
		}

		return cGCoverage;
	}

}
