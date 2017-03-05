package de.wwu.muggl.symbolic.flow.coverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.symbolic.flow.controlflow.ControlGraph;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;

/**
 * Provides the functionality to keep track of the coverage of the edges of the {@link ControlGraph}.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class CGCoverage {
	// Reference fields.
	private SymbolicVirtualMachine vm;
	private ControlGraph controlGraph;
	
	/*
	 * Field for nodes and edges. The first boolean dimension is for the current, the second for the
	 * total coverage.
	 */
	private Map<Integer, Map<Integer, boolean[]>> coveredEdges;

	// Cache for the number of edges.
	private long numberOfEdges;
	private long expectedNumberOfEdges;
	
	// Field to store the last visited pc
	private int lastPc;

	// Counter to keep track of which total coverages are committed (i.e. lead to a solution).
	private long nextTrailElement;
	private long committedTill;
	
	/**
	 * Initialize the control graph coverage object.
	 * 
	 * @param vm The SymbolicalVirtualMachine execution currently runs in.
	 * @param method The Method to track coverage for.
	 * @throws NullPointerException If the supplied SymbolicalVirtualMachine or Method is null.
	 * @throws InitializationException In cases of non-recoverable initialization exceptions.
	 */
	public CGCoverage(SymbolicVirtualMachine vm, Method method) throws InitializationException  {
		if (vm == null) throw new NullPointerException("The SymbolicalVirtualMachine supplied  must not be null.");
		if (method == null) throw new NullPointerException("The supplied Method must not be null.");
		this.vm = vm;
		try {
			this.controlGraph = method.getControlGraph();
			this.coveredEdges = new HashMap<Integer, Map<Integer, boolean[]>>();
			Map<Integer, Set<Integer>> edges = this.controlGraph.getControlGraph();
			for (Integer key : edges.keySet()) {
				Map<Integer, boolean[]> coveredNodes = new HashMap<Integer, boolean[]>();
				for (Integer node : edges.get(key)) {
					boolean[] value = {false, false};
					coveredNodes.put(node, value);
				}
				this.coveredEdges.put(key, coveredNodes);
			}
		} catch (InvalidInstructionInitialisationException e) {
			/*
			 * There is hardly any reason why this should happen. The Method should have been parsed
			 * earlier by the executing virtual machine.
			 */
			throw new InitializationException(
					"Unexpected exception when initializing the coverage controller.");
		}
		this.numberOfEdges = -1L;
		
		// Initialize to -1, meaning "before the execution started with the first instruction".
		this.lastPc = -1;

		// Initialize the counters.
		this.nextTrailElement = 0L;
		this.committedTill = -1L;
	}

	/**
	 * Update the coverage according to the instruction number.<br />
	 * <br />
	 * This process it simple: The coverage of the edge between the last executed pc and the current
	 * pc is set to true if it is not true already. Then the current pc becomes the last executed
	 * pc.<br />
	 * <br />
	 * To reset this when backtracking, a new CGCoverageTrailElement is generated.<br />
	 * <br />
	 * As special values, -2 and -3 can be supplied. -2 means that the last instruction executed was
	 * followed by the end of the method (it was a return instruction). -3 means that the last
	 * instruction executed lead to an exception being thrown that was uncaught by the method.<br />
	 * <br />
	 * Covering method invocation and returning from invocation is taken into account. Since for
	 * coverage always the ControlGraph of the currently executed method is updated, the pc on the
	 * instruction following the invocation will be supplied to the {@link CoverageController} once
	 * invocation is finished and control is returned to the calling frame. The lastPc will be that
	 * of the invocation instruction.
	 * 
	 * @param pc The current pc of the virtual machine.
	 * @throws IllegalArgumentException If the pc does not point to a node or if there cannot be an
	 *         edge between the last pc and the current pc.
	 */
	public void updateCoverage(int pc) {
		// Do not continue if this is the first call.
		if (this.lastPc == -1) {
			this.lastPc = pc;
			return;
		}
		if (this.lastPc == -3) {
			this.lastPc = pc;
			return;
		}

		/*
		 * Do not continue if we returned from backtracking. As the choice point is
		 * generated after marking the control graph edge to the choice point generating
		 * instruction to be covered, it is not reverted. Hence, the last pc is equal to
		 * the pc supplied after backtracking. As this cannot happen under any other
		 * circumstances, just ignore it.
		 */
		if (this.lastPc == pc)
			return;

		// Logging?
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger.trace(
					"Covering control flow graph edge between " + this.lastPc + " and " + pc + "."
					);

		// Get the potential edges.
		Map<Integer, boolean[]> potentialEdges = this.coveredEdges.get(this.lastPc);
		boolean skip = false;
		if (potentialEdges == null || skip)
			throw new IllegalArgumentException(
					"The last pc (" + this.lastPc + ") did not point to a node in the control graph."
					);

		// Check if the edge exists.
		boolean[] covered = potentialEdges.get(pc);
		if (covered == null)
			throw new IllegalArgumentException(
					"There is no edge between last pc (" + this.lastPc + ") and pc (" + pc + ") in the control graph."
					);

		// Check if the value is already true. Only proceed if it is false at the moment.
		if (!covered[0]) {
			// It now is covered. Set that.
			covered[0] = true;
			covered[1] = true; // The overall coverage might already be true.
			potentialEdges.put(pc, covered);

			// Generate the CGCoverageTrailElement.
			ChoicePoint choicePoint = this.vm.getSearchAlgorithm().getCurrentChoicePoint();
			if (choicePoint != null && choicePoint.hasTrail()) {
				CGCoverageTrailElement trailElement =
					new CGCoverageTrailElement(this, this.nextTrailElement, this.lastPc, pc);
				this.nextTrailElement++;
				choicePoint.addToTrail(trailElement);
			}
		}

		// Finally set the pc to be the last pc.
		this.lastPc = pc;
	}

	/**
	 * Set the coverage of the definition specified by the supplied pc values to false.
	 * 
	 * This method has package visibility only. It is intended to be used by the restore() method of
	 * the CGCoverageTrailElement.
	 * 
	 * @param number A number indicating how many elements have been confirmed to be covered, yet.
	 * @param lastPc The last pc value.
	 * @param pc The current pc.
	 */
	void revertCoverage(long number, int lastPc, int pc) {
		Map<Integer, boolean[]> potentialEdges = this.coveredEdges.get(lastPc);
		if (number > this.committedTill) {
			// Just overwrite it.
			potentialEdges.put(pc, new boolean[2]);
		} else {
			// Get the current coverage and revert just the current coverage.
			boolean[] coverage = potentialEdges.get(pc);
			coverage[0] = false;
			potentialEdges.put(pc, coverage);
		}
		// Set the lastPC;
		this.lastPc = lastPc;
	}

	/**
	 * Set the internal last pc to the value supplied. This is needed for method invocation and for
	 * backtracking. The revertCoverage-Method will only revert the coverage of those edges that
	 * were not covered before. In many cases the execution of a choice point generating instruction
	 * will not result in additional coverage. Hence, the last pc value has to be reverted
	 * individually.
	 *
	 * @param lastPc The last pc value.
	 */
	public void revertLastPc(int lastPc) {
		this.lastPc = lastPc;
	}

	/**
	 * Signalize that a solution has been saved any any total coverages can hence be kept and do not
	 * need to be unset on backtracking.
	 */
	public void commit() {
		this.committedTill = this.nextTrailElement - 1;
	}

	/**
	 * Check if every control graph edge is covered.
	 * @return true, if full coverage has been reached, false otherwise.
	 */
	public boolean isEverythingCovered() {
		// Iterate through the HashMaps and check if every edge is covered.
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (!edges.get(key2)[1]) return false;
			}
		}

		// If this point is reached, full coverage is guaranteed.
		return true;
	}

	/**
	 * Get the number of covered control graph edges. Resetting edges while backtracking is taken
	 * into account. The number could however decrease, if currently some branches are covered that
	 * will be reseted later.
	 * 
	 * @return The number of covered control graph edges.
	 */
	public long getNumberOfCoveredCGEdges() {
		long number = 0;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (edges.get(key2)[1]) number++;
			}
		}
		return number;
	}

	/**
	 * Get the number of currently covered control graph edges.
	 *
	 * @return The number of currently covered control graph edges.
	 */
	public long getNumberOfCurrentlyCoveredCGEdges() {
		long number = 0;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (edges.get(key2)[0]) number++;
			}
		}
		return number;
	}

	/**
	 * Get the total number of control graph edges. The result of
	 * this method will be cached.
	 *
	 * @return The total of control graph edges.
	 */
	public long getNumberOfEdges() {
		// Have a cached result?
		if (this.numberOfEdges != -1)
			return this.numberOfEdges;

		// Calculate the number.
		int number = 0;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			number += this.coveredEdges.get(key).size();
		}
		this.numberOfEdges = number;
		return number;
	}

	/**
	 * Get the expected number of control graph edges as determined by the corresponding control
	 * graph. The result of this method will be cached.
	 * 
	 * @return The expected number of control graph edges.
	 */
	public long getExpectedNumberOfEdges() {
		// Have a cached result?
		if (this.expectedNumberOfEdges != -1)
			return this.expectedNumberOfEdges;

		// Calculate the number.
		Map<Integer, Set<Integer>> controlGraph = this.controlGraph.getControlGraph();
		int number = 0;
		Set<Integer> potentialEdgesKeyset = controlGraph.keySet();
		for (Integer key : potentialEdgesKeyset) {
			number += controlGraph.get(key).size();
		}
		this.expectedNumberOfEdges = number;
		return number;
	}

	/**
	 * Get the coverage of control graph edges. This will return a Map with Integer keys and a Map
	 * objects. The Map objects have Integer keys and a two-dimensional boolean array. The first
	 * Integer key represents the pc of the node the edge comes from. The second Integer key
	 * represents the pc of the node the edge goes to. A value of true indicates that the
	 * corresponding control graph edge is covered.
	 * 
	 * @return The coverage control graph edges.
	 */
	public Map<Integer, Map<Integer, boolean[]>> getControlGraphCoverage() {
		return this.coveredEdges;
	}

	/**
	 * Get an array of boolean values indicating which control graph edges are covered.
	 * A value of true indicates that the corresponding edge is covered.
	 *
	 * The array is build in the ascending order of the keys.
	 *
	 * @return An array of boolean values indicating which control flow edges have been covered.
	 */
	public boolean[] getCoverage() {
		// First step: Allocate the array.
		boolean[] coverage = new boolean[(int) getNumberOfEdges()];

		// Second step: Fill the array.
		int a = 0;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (edges.get(key2)[1]) {
					coverage[a] = true;
				} else {
					coverage[a] = false;
				}
				a++;
			}
		}

		// Finished.
		return coverage;
	}

	/**
	 * Get an array of boolean values indicating which control graph edges are currently covered.
	 * A value of true indicates that the corresponding edge is covered.<br />
	 * <br />
	 * The array is build in the ascending order of the keys.
	 *
	 * @return An array of boolean values indicating which control flow edges have been covered currently.
	 */
	public boolean[] getCurrentCoverage() {
		// First step: Allocate the array.
		boolean[] coverage = new boolean[(int) getNumberOfEdges()];

		// Second step: Fill the array.
		int a = 0;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (edges.get(key2)[0]) {
					coverage[a] = true;
				} else {
					coverage[a] = false;
				}
				a++;
			}
		}

		// Finished.
		return coverage;
	}
	
	/**
	 * Get a String representation of the control graph edges. This method is an alias for
	 * toString(false).
	 *
	 * @return A String representation of the control graph edges.
	 * @see #toString(boolean)
	 * @see #showEdges(boolean, boolean)
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Get a String representation of the control graph edges.<br />
	 * <br />
	 * Please note that the overall coverage reflects backtracking. However, for this reason not all
	 * covered edges might stay covered. If currently a branch is processed for that no solution is
	 * found, even edges marked as globally covered could be reset.
	 * 
	 * @param showCurrentCoverage Show the current coverage instead of the overall coverage.
	 * @return A String representation of the control graph edges.
	 * @see ControlGraph#toString()
	 * @see #showEdges(boolean, boolean)
	 */
	public String toString(boolean showCurrentCoverage) {
		String edgesString = "Control graph edges coverage for Method "
			+ this.controlGraph.getMethod().getFullNameWithParameterTypesAndNames() + ".\n"
			+ "Scheme: Instruction number from -> instruction number to: coverage\n"
			+ "Instead of a key \"return\" or \"Except.\" might be shown. "
			+ "This means, that the instruction at the pc might finish the method or throw an uncaught exception.\n";
		if (showCurrentCoverage) edgesString += "Only the current coverage is shown.\n";
		edgesString += "Instruction numbers include additional bytes.\n\n";
		boolean firstOne = true;
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			for (Integer key2 : edgesKeyset) {
				if (!firstOne) edgesString += "\n";
				firstOne = false;
				edgesString += key + "\t->\t";
				if (key2 == ControlGraph.CG_RETURN_METHOD) {
					edgesString += "return";
				} else if (key2 == ControlGraph.CG_EXCECPTION_METHOD) {
					edgesString += "Except.";
				} else {
					edgesString += key2;
				}
				edgesString += "\t: ";
				if ((showCurrentCoverage && edges.get(key2)[0])
						|| (!showCurrentCoverage && edges.get(key2)[1])) {
					edgesString += "true";
				} else {
					edgesString += "false";
				}
			}
		}
		return edgesString;
	}

	/**
	 * Get a more sophisticated String representation of the control graph edges' coverage. The
	 * output will be similar to this:<br />
	 * 1<br />
	 * | (covered)<br />
	 * 2<br />
	 * | (covered)<br />
	 * 3 -> 5 (covered)<br />
	 * | (covered)<br />
	 * 4<br />
	 * | (uncovered)<br />
	 * 5<br />
	 * <br />
	 * Please note that the overall coverage reflects backtracking. However, for this reason not all
	 * covered edges might stay covered. If currently a branch is processed for that no solution is
	 * found, even edges marked as globally covered could be reset.
	 * 
	 * @param showInstructionMnemorics Do not only display the instructions positions but also their
	 *        mnemonics.
	 * @param showCurrentCoverage Show the current coverage instead of the overall coverage.
	 * @return A more sophisticated String representation of the control graph edges.
	 * @see #toString(boolean)
	 * @see ControlGraph#showEdges(boolean)
	 */
	public String showEdges(boolean showInstructionMnemorics, boolean showCurrentCoverage) {
		Method method = this.controlGraph.getMethod();
		Instruction[] instructions;
		try {
			instructions = method.getInstructionsAndOtherBytes();
		} catch (InvalidInstructionInitialisationException e) {
			return "Could not generate a graphical representation of the control graph due to a problem loading the "
				+ "instructions of method " + method.getFullNameWithParameterTypesAndNames() + ".";
		}

		String edgesString = "Control graph edges coverage for method "
			+ method.getFullNameWithParameterTypesAndNames() + ".\n";
		if (showCurrentCoverage) edgesString += "Only the current coverage is shown.\n";
		edgesString += "Instruction numbers include additional bytes.\n\n";
		Set<Integer> potentialEdgesKeyset = this.coveredEdges.keySet();
		int lastInstruction = instructions.length - 1;
		for (Integer key : potentialEdgesKeyset) {
			// Add the current key.
			edgesString += key;
			if (showInstructionMnemorics) {
				edgesString += " " + instructions[key].getName();
			}

			// Add the edges.
			int nextKey = key + 1 + instructions[key].getNumberOfOtherBytes();
			Map<Integer, boolean[]> edges = this.coveredEdges.get(key);
			Set<Integer> edgesKeyset = edges.keySet();
			String normalEdge = "";
			if (key != lastInstruction) normalEdge += "\n";
			String jumping = "";
			for (Integer key2 : edgesKeyset) {
				// Is it a jump?
				if (key2 != nextKey) {
					jumping += " -> ";
					if (key2 == ControlGraph.CG_RETURN_METHOD) {
						jumping += "return";
					} else if (key2 == ControlGraph.CG_EXCECPTION_METHOD) {
						jumping += "Exception";
					} else {
						jumping += key2;
					}
					jumping += " ";
					if ((showCurrentCoverage && edges.get(key2)[0])
							|| (!showCurrentCoverage && edges.get(key2)[1])) {
						jumping += "(covered)";
					} else {
						jumping += "(uncovered)";
					}
				} else {
					// Normal control flow.
					normalEdge += "|";
					if ((showCurrentCoverage && edges.get(key2)[0])
							|| (!showCurrentCoverage && edges.get(key2)[1])) {
						normalEdge += "\t\t(covered)";
					} else {
						normalEdge += "\t\t(uncovered)";
					}
				}
			}
			edgesString += jumping + normalEdge;
			if (key != lastInstruction) edgesString += "\n";
		}
		return edgesString;
	}
}
