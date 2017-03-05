package de.wwu.muggl.symbolic.flow.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.symbolic.flow.defUseChains.DefUseChain;
import de.wwu.muggl.symbolic.flow.defUseChains.DefUseChains;
import de.wwu.muggl.symbolic.flow.defUseChains.DefUseChainsInitial;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;

/**
 * This class is used to keep track of the def-use ({@link DefUseChain}) coverage of a
 * distinct method. When instantiated, maps which map instruction numbers to definition and uses are
 * calculated. After doing so, recording coverage information is possible without consuming much
 * execution time.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class DUCoverage {
	// Fields for references.
	private SymbolicVirtualMachine vm;
	private DefUseChainsInitial defUseChains;

	// The Mapping of definitions and uses to line numbers.
	private Map<Method, List<List<Integer>>> methodDefMapping;
	private Map<Method, List<List<Integer>>> methodUseMapping;

	// Boolean arrays to keep track of the covered definitions and uses.
	private boolean[] coveredDef;
	private boolean[] coveredDefCurrentBranch;
	private boolean[] coveredUse;
	private boolean[] coveredUseCurrentBranch;

	// Counter to keep track of which total coverages are committed (i.e. lead to a solution).
	private long nextTrailElement;
	private long committedTill;
	
	// Counter for the total number of def-use chains.
	private int totalDUChains;

	/**
	 * Initialize this DUCoverage object.
	 *
	 * @param vm The SymbolicalVirtualMachine execution currently runs in.
	 * @param defUseChains The DefUseChainsInitial to process.
	 * @throws NullPointerException If the supplied SymbolicalVirtualMachine or DefUseChains is null.
	 */
	public DUCoverage(SymbolicVirtualMachine vm, DefUseChainsInitial defUseChains) {
		if (vm == null) throw new NullPointerException("The SymbolicalVirtualMachine supplied  must not be null.");
		if (defUseChains == null) throw new NullPointerException("The supplied DefUseChains must not be null.");
		this.vm = vm;
		this.defUseChains = defUseChains;
		int numberOfChains = 0;
		
		// Initialize the maps.
		this.methodDefMapping = new HashMap<Method, List<List<Integer>>>();
		this.methodUseMapping = new HashMap<Method, List<List<Integer>>>();
		
		// Fill the maps.
		Map<Method, DefUseChains> defUseChainsMap = defUseChains.getDefUseChainsMapping();
		int counter = 0;
		for (Method method : defUseChains.getCoveredMethods()) {
			List<List<Integer>> defMapping = new ArrayList<List<Integer>>();
			List<List<Integer>> useMapping = new ArrayList<List<Integer>>();
			this.methodDefMapping.put(method, defMapping);
			this.methodUseMapping.put(method, useMapping);
			
			// Set an empty mapping for each instruction number.
			int number = method.getInstructionsNumber();
			for (int a = 0; a < number; a++) {
				defMapping.add(a, null);
				useMapping.add(a, null);
			}
			
			// Now fill the lists.
			this.nextTrailElement = 0L;
			this.committedTill = -1L;

			// Fill the maps according to the instruction numbers of the def-use chains.
			for (DefUseChain defUseChain : defUseChainsMap.get(method).getDefUseChains()) {
				// Increase the number of chains.
				numberOfChains += defUseChainsMap.size();
				
				// Get current mapping.
				int instructionNumber = defUseChain.getDef().getInstructionNumber();
				List<Integer> defIndices = defMapping.get(instructionNumber);
				List<Integer> useIndices = useMapping.get(instructionNumber);
				
				if (defIndices == null)
					defIndices = new ArrayList<Integer>();
				if (useIndices == null)
						useIndices = new ArrayList<Integer>();
				
				// Update mapping.
				defIndices.add(Integer.valueOf(counter));
				defMapping.set(instructionNumber, defIndices);
				useIndices.add(Integer.valueOf(counter));
				useMapping.set(instructionNumber, useIndices);
				
				counter++;
			}
		}
		this.totalDUChains = numberOfChains;
		
		// Boolean arrays for the coverage have the size of the number of def-use chains.
		this.coveredDef = new boolean[numberOfChains];
		this.coveredDefCurrentBranch = new boolean[numberOfChains];
		this.coveredUse = new boolean[numberOfChains];
		this.coveredUseCurrentBranch = new boolean[numberOfChains];
	}

	/**
	 * Update the coverage according to the instruction number.<br />
	 * <br />
	 * In order to do so, check if there is any mapping for the given pc. If a mapping
	 * is found, update the definition coverage at the given index. Update the use
	 * coverage either if the definition required to form a def-use chain is already
	 * covered.
	 * 
	 * @param method The method currently executed in the virtual machine.
	 * @param pc The current pc of the virtual machine.
	 */
	public void updateCoverage(Method method, int pc) {
		// Definition coverage....
		List<List<Integer>> defMapping = this.methodDefMapping.get(method);
		if (defMapping != null) {
			List<Integer> defIndices = defMapping.get(pc);
			if (defIndices != null) {
				boolean covering = false;
				for (int index : defIndices) {
					// Set the all time flag to true regardless of its current state.
					this.coveredDef[index] = true;

					// Just proceed with the current branch information it it is not already true.
					if (!this.coveredDefCurrentBranch[index]) {
						this.coveredDefCurrentBranch[index] = true;
						covering = true;

						// Save backtracking information.
						ChoicePoint choicePoint = this.vm.getSearchAlgorithm()
								.getCurrentChoicePoint();
						if (choicePoint != null && choicePoint.hasTrail()) {
							DUCoverageTrailElement dUCoverageTrailElement = new DUCoverageTrailElement(
									this, this.nextTrailElement, index, false);
							this.nextTrailElement++;
							choicePoint.addToTrail(dUCoverageTrailElement);
						}
					}
				}
				// Logging?
				if (covering && Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger
							.trace("Def-use chains: Now covering def at pc " + pc + ".");
			}
		}

		// Use coverage...
		List<List<Integer>> useMapping = this.methodUseMapping.get(method);
		if (useMapping != null) {
			List<Integer> useIndices = useMapping.get(pc);
			if (useIndices != null) {
				boolean covering = false;
				for (int index : useIndices) {
					// Set the all time flag to true if the definition is met in the current branch.
					if (this.coveredDefCurrentBranch[index]) {
						this.coveredUse[index] = true;

						// Just proceed with the current branch information it it is not already
						// true.
						if (!this.coveredUseCurrentBranch[index]) {
							this.coveredUseCurrentBranch[index] = true;
							covering = true;

							// Save backtracking information.
							ChoicePoint choicePoint = this.vm.getSearchAlgorithm()
									.getCurrentChoicePoint();
							if (choicePoint != null && choicePoint.hasTrail()) {
								DUCoverageTrailElement dUCoverageTrailElement = new DUCoverageTrailElement(
										this, this.nextTrailElement, index, true);
								this.nextTrailElement++;
								choicePoint.addToTrail(dUCoverageTrailElement);
							}
						}
					}
				}
				// Logging?
				if (covering && Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger
							.trace("Def-use chains: Now covering use at pc " + pc + ".");
			}
		}
	}

	/**
	 * Set the coverage of the definition or usage specified by the supplied index to false. If the
	 * number is higher then the point currently committed to, the total coverage will also be unset.
	 * This is needed since covering all chains might lead to the abortion of the execution.
	 * However, backtracking might take place without a solution saved. In such a case, coverage
	 * achieved would not be included in any solution, and yet has to be met again to fulfill the
	 * abortion criterion.
	 *
	 * This method has package visibility only. It is intended to be used by the restore() method of
	 * the {@link DUCoverageTrailElement}.
	 *
	 * @param number The number of the def-use coverage trail element.
	 * @param coveredIndex The index of the definition to unset.
	 * @param defOrUse Indicates whether the definition or the usage is unset. False means def, true
	 *        means use.
	 */
	void revertCoverage(long number, int coveredIndex, boolean defOrUse) {
		if (defOrUse) {
			this.coveredUseCurrentBranch[coveredIndex] = false;
			if (number > this.committedTill)
				this.coveredUse[coveredIndex] = false;
		} else {
			this.coveredDefCurrentBranch[coveredIndex] = false;
			if (number > this.committedTill)
				this.coveredDef[coveredIndex] = false;
		}
	}

	/**
	 * Signalize that a solution has been saved any any total coverages can hence be kept and do not
	 * need to be unset on backtracking.
	 */
	public void commit() {
		this.committedTill = this.nextTrailElement - 1;
	}

	/**
	 * Check if every def-use chain is covered.
	 * @return true, if full coverage has been reached, false otherwise.
	 */
	public boolean isEverythingCovered() {
		// Cycle through both coverage arrays and check if any element is false.
		for (int a = 0; a < this.coveredDef.length; a++) {
			if (!this.coveredDef[a] || !this.coveredUse[a]) return false;
		}
		// If this point is reached, full coverage is guaranteed.
		return true;
	}

	/**
	 * Get the number of total def-use chains. 
	 * 
	 * @return The number of covered def-use chains.
	 */
	public int getNumberOfTotaldDUChains() {
		return this.totalDUChains;
	}


	/**
	 * Get the number of covered def-use chains. Resetting definitions and usages while backtracking
	 * is taken into account. The number could however decrease, if currently some branches are
	 * covered that will be reseted later.
	 * 
	 * @return The number of covered def-use chains.
	 */
	public int getNumberOfCoveredDUChains() {
		int number = 0;
		for (int a = 0; a < this.coveredUse.length; a++) {
			if (this.coveredUse[a]) number++;
		}
		return number;
	}

	/**
	 * Get the number of currently covered def-use chains.
	 * 
	 * @return The number of currently covered def-use chains.
	 */
	public int getNumberOfCurrentlyCoveredDUChains() {
		int number = 0;
		for (int a = 0; a < this.coveredUseCurrentBranch.length; a++) {
			if (this.coveredUseCurrentBranch[a]) number++;
		}
		return number;
	}

	/**
	 * Get an array of boolean values indicating which def-use chains have been covered. A value of
	 * true indicates that the corresponding def-use chain is covered. Resetting definitions and
	 * usages while backtracking is taken into account. However, some covered chains could be set to
	 * be uncovered if no solution for the currently processed branch is found.
	 * 
	 * @return An array of boolean values indicating which def-use chains have been covered.
	 */
	public boolean[] getCoverage() {
		return this.coveredUse;
	}

	/**
	 * Get an array of boolean values indicating which def-use chains are currently covered.
	 * A value of true indicates that the corresponding def-use chain is covered.
	 *
	 * @return An array of boolean values indicating which def-use chains are currently covered.
	 */
	public boolean[] getCurrentCoverage() {
		return this.coveredUseCurrentBranch;
	}
	
	/**
	 * Get a String representation of the def-use chains coverage.
	 *
	 * @return A String representation of the def-use chains.
	 */
	@Override
	public String toString() {
		return "Tracking coverage for:\n" + this.defUseChains.toString();
	}

}
