package de.wwu.muggl.symbolic.testCases;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This class encapsulates the functionality to eliminate solutions before test cases are generated.
 * It offers one method that works according to the currently set options.<br />
 * <br />
 * The class has package visibility only as it is meant to be utilized by the SolutionProcessor.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-08
 */
class TestCaseEliminationAlgorithms {
	// Fields.
	private SolutionProcessor solutionProcessor;
	private TestCaseSolution firstSolutionFound;
	private TestCaseSolution newFirstSolutionFound;
	private volatile boolean eliminationStarted;
	private volatile boolean eliminationCompleted;
	private long totalNumberOfDefUseChains;
	private long totalNumberOfControlGraphEdges;
	private long totalNumberOfDefUseChainsCovered;
	private long totalNumberOfControlGraphEdgesCovered;

	/**
	 * Construct the elimination algorithm object.
	 *
	 * @param solutionProcessor The SolutionProcessor utilizing the algorithm.
	 * @param firstSolutionFound The first solution found, representation the first item in the chain.
	 */
	TestCaseEliminationAlgorithms(SolutionProcessor solutionProcessor, TestCaseSolution firstSolutionFound) {
		this.solutionProcessor = solutionProcessor;
		this.firstSolutionFound = firstSolutionFound;
		this.eliminationStarted = false;
		this.eliminationCompleted = false;
		this.totalNumberOfDefUseChains = 0L;
		this.totalNumberOfControlGraphEdges = 0L;
		this.totalNumberOfDefUseChainsCovered = 0L;
		this.totalNumberOfControlGraphEdgesCovered = 0L;
	}

	/**
	 * Eliminate solutions by their coverage of def-use chains and/or control graph edges. Basically, the set
	 * of test cases just has to include a number of tests that will have any def-use chain and/or control
	 * flow edge covered at least once during execution. This method tries to find the minimal set of test
	 * cases.
	 *
	 * In order not to waste ressources, it operates a 'greedy' algorithm. A heuristical algorithm will be
	 * used. It picks the solution first that covers the most def-use chains (this strategy is called "greedy"
	 * in the literature). If there is more than one solution with and equal number of def-use chains covered,
	 * the one is picked that covers more control-flow edges. Then the solutions is picked that covers most of
	 * the remaining def-use-chains, again applying the control-flow coverage as the second criterion. This is
	 * continued until all chains are covered. If any control-flow edges are not picked by then, the algorithm
	 * will continue to add those solutions that offer the coverage of yet not picked control flow edges, again
	 * working in a "greedy" way until the set of solution picks all chains and edges.
	 *
	 * Solutions not picked are discarded, as there will not be any reference to them.
	 *
	 * If either elimination by def-use chain or control flow coverage is disabled, only the other criterion
	 * will be used.
	 *
	 * This method will occasionally check the SolutionProcessor for its interruption status. Should it have
	 * been interrupted, processing will stop and a InterruptedException will be thrown.
	 *
	 * PLEASE NOTE: If you decide to free solutions from redundancy (i.e. delete solutions with same
	 * parameters and return values), this should be done before invoking this algorithm. There is no need to
	 * do it after it was run as redundant solutions will not survive the elimination process anyway.
	 *
	 * @return The (probably) new first item in the chain.
	 * @throws IllegalStateException
	 *             If this method is invoked while it its currently running.
	 * @throws InterruptedException
	 *             If the solution processor has received the command to interrupt.
	 * @throws TestCaseEliminationException
	 *             On fatal problems while eliminating the test cases.
	 */
	@SuppressWarnings("null")
	TestCaseSolution eliminateSolutions() throws InterruptedException, TestCaseEliminationException {
		// Already running?
		if (this.eliminationStarted) {
			throw new IllegalStateException("Solution elimination is currently running.");
		}
		if (this.eliminationCompleted) {
			return this.newFirstSolutionFound;
		}
		this.eliminationStarted = true;

		// Anything to do at all?
		if (Options.getInst().eliminateSolutionsByCoverage == 0) {
			return this.firstSolutionFound;
		}

		// The new first solutions.
		TestCaseSolution newFirstSolution = null;
		TestCaseSolution lastSolutionChained = null;

		/*
		 * First step: Put all solutions into a Set. This saves us from re-chaining while processing and
		 * it helps processing subsets.
		 */
		TreeSet<TestCaseSolutionEliminationWrap> solutionTreeSet = new TreeSet<TestCaseSolutionEliminationWrap>();
		TestCaseSolution solution = this.firstSolutionFound;
		long number = 0;
		while (solution != null) {
			// Add it.
			solutionTreeSet.add(new TestCaseSolutionEliminationWrap(solution, number));

			// Get the next solution.
			solution = solution.getSuccessor();
			number++;
		}

		// Check if test case generation has been interrupted.
		if (this.solutionProcessor.isInterrupted())
			throw new InterruptedException("Interrupted");

		// Second step: Determine the total possible coverage.
		long[] maximumCoverageNumber = determineMaximumCoverageNumbers(solutionTreeSet);
		this.totalNumberOfDefUseChainsCovered = maximumCoverageNumber[0];
		this.totalNumberOfControlGraphEdgesCovered = maximumCoverageNumber[1];

		// Third step: Run the main loop.
		long totalNumberOfDefUseChainsCovered = 0L;
		long totalNumberOfControlGraphEdgesCovered = 0L;
		Options options = Options.getInst();
		while (totalNumberOfDefUseChainsCovered < maximumCoverageNumber[0]
				|| totalNumberOfControlGraphEdgesCovered < maximumCoverageNumber[1]) {
			// Check if test case generation has been interrupted.
			if (this.solutionProcessor.isInterrupted())
				throw new InterruptedException("Interrupted");

			// The best solution yet found in this pass.
			TestCaseSolutionEliminationWrap bestSolution;

			// First sub-step: Get the best solutions.
			TreeSet<TestCaseSolutionEliminationWrap> bestSolutions;
			// First use def-use coverage if it is not explicitly disabled or full coverage reached.
			if (totalNumberOfDefUseChainsCovered == maximumCoverageNumber[0]
					|| options.eliminateSolutionsByCoverage == 2) {
				bestSolutions = getBestControlFlowCoveringSolutions(solutionTreeSet);
			} else {
				bestSolutions = getBestDefUseCoveringSolutions(solutionTreeSet);
				/*
				 * If there is more than one solution, get the ones that have the best control-flow coverage.
				 * Of course, only do so if full control flow coverage has not been reached, yet.
				 */
				if (bestSolutions.size() > 1 && totalNumberOfControlGraphEdgesCovered < maximumCoverageNumber[1]) {
					bestSolutions = getBestControlFlowCoveringSolutions(bestSolutions);
				}
			}

			/*
			 * Second sub-step: If there is more than one solution equally good, just pick the first one. There
			 * is no real need to pick a random solution as there is no reason why any solution should be
			 * "better" (or "worse") than the first one. Actually, this is not decidable at this point.
			 */
			if (bestSolutions.size() > 0) {
				bestSolution = bestSolutions.first();
			} else {
				// This should not happen and hints to a bug in the algorithm.
				throw new TestCaseEliminationException(
					  "There were no more suitable solutions even though the possible coverage was not reached."
					);
			}

			// Third sub-step: Chain the solution and remove it from the TreeSet.
			if (newFirstSolution == null) {
				newFirstSolution = bestSolution.getTestCaseSolution();
				newFirstSolution.setPredecessor(null);
				newFirstSolution.setSuccessor(null);
				lastSolutionChained = newFirstSolution;
			} else {
				lastSolutionChained.setSuccessor(bestSolution.getTestCaseSolution());
				bestSolution.getTestCaseSolution().setPredecessor(lastSolutionChained);
				lastSolutionChained = bestSolution.getTestCaseSolution();
			}
			solutionTreeSet.remove(bestSolution);

			// Mark that another solution is kept.
			this.solutionProcessor.increaseEliminationSolutionsKept();

			/*
			 * Fourth sub-step: Remove the now covered chains and edges.
			 */
			removeNowCoveredChainsAndEdges(bestSolution, solutionTreeSet);
			totalNumberOfDefUseChainsCovered += bestSolution.getNumberOfCoveredDefUseChains();
			totalNumberOfControlGraphEdgesCovered += bestSolution.getNumberOfCoveredControlGraphEdges();
		}
		
		// No solutions chained?
		if (lastSolutionChained == null) {
			// TODO: is this really what we want here?
			this.eliminationCompleted = true;
			this.newFirstSolutionFound = this.firstSolutionFound;
			return this.firstSolutionFound;
		}

		// Fourth step: Close the now completed chain.
		lastSolutionChained.setSuccessor(null);

		// Check if test case generation has been interrupted.
		if (this.solutionProcessor.isInterrupted())
			throw new InterruptedException("Interrupted");

		// Fifth step: Unchain the not picked solutions. This will speed up garbage collection.
		Iterator<TestCaseSolutionEliminationWrap> solutionIterator = solutionTreeSet.iterator();
		while (solutionIterator.hasNext()) {
			solution = solutionIterator.next().getTestCaseSolution();
			solution.setPredecessor(null);
			solution.setSuccessor(null);
		}
		solutionIterator = null;
		solutionTreeSet.clear();

		// Sixth step: Return the (new) first solution.
		this.eliminationCompleted = true;
		this.newFirstSolutionFound = newFirstSolution;
		return newFirstSolution;
	}

	/**
	 * Determine the maximum number of def-use chains and control flow edges that can be covered. While
	 * doing so, each TestCaseSolutionEliminationWrap is augmented with the information how many yet
	 * uncovered chains and edges could by covered by it.
	 *
	 * Solutions not covering any def-use chain or control graph edge will be removed. If only one of
	 * these is tracked, solutions not covering anything of the criterion will be removed.
	 *
	 * @param solutionTreeSet A TreeSet of solutions to process.
	 * @return An array of long containing the maximum number of covered def-use chains and of control graph edges.
	 * @throws TestCaseEliminationException If the number of chains/ edges for the same method is different in two solutions.
	 */
	private long[] determineMaximumCoverageNumbers(
			TreeSet<TestCaseSolutionEliminationWrap> solutionTreeSet
			) throws TestCaseEliminationException {
		// Variables needed.
		long maximumNumberOfDefUseChainsCovered = 0L;
		long maximumNumberControlGraphEdgesCovered = 0L;
		boolean[] combinedDUCoverage = new boolean[solutionTreeSet.first().getDUCoverage().length];
		Map<Method, boolean[]> combinedControlFlowCoverageMapping = new HashMap<Method, boolean[]>();

		// Options.
		boolean countDefUse = false;
		boolean countControlFlow = false;
		switch (Options.getInst().eliminateSolutionsByCoverage) {
			case 1:
				countDefUse = true;
				break;
			case 2:
				countControlFlow = true;
				break;
			case 3:
				countDefUse = true;
				countControlFlow = true;
				break;
		}

		// Build new hash maps that have the coverage set to true if at least on solutions satisfies that coverage.
		Iterator<TestCaseSolutionEliminationWrap> solutionIterator = solutionTreeSet.iterator();
		while (solutionIterator.hasNext()) {
			TestCaseSolutionEliminationWrap solution = solutionIterator.next();
			long numberOfDefUseChainsCovered = 0L;
			long numberControlGraphEdgesCovered = 0L;

			// Iterate over all def-use chain coverages.
			if (countDefUse) {
				boolean[] dUCoverage = solution.getDUCoverage();
				// Checking the length.
				if (combinedDUCoverage.length != dUCoverage.length)
					throw new TestCaseEliminationException(
							"The def-use chain mapping is faulty and cannot be processed.");

				// Merge it with the current coverage.
				for (int a = 0; a < combinedDUCoverage.length; a++) {
					combinedDUCoverage[a] = combinedDUCoverage[a] | dUCoverage[a];
					if (dUCoverage[a])
						numberOfDefUseChainsCovered++;
				}
					
			}

			// Build new hash map that has the control flow coverage set to true if at least on solutions satisfies that coverage.
			if (countControlFlow) {
				Map<Method, boolean[]> controlFlowCoverageMapping = solution.getCFCoverageMap();
				Set<Entry<Method, boolean[]>> controlFlowCoverageSet = controlFlowCoverageMapping.entrySet();
				Iterator<Entry<Method, boolean[]>> controlFlowCoverageIterator = controlFlowCoverageSet.iterator();
				while (controlFlowCoverageIterator.hasNext()) {
					Entry<Method, boolean[]> entry = controlFlowCoverageIterator.next();
					Method method = entry.getKey();
					boolean[] controlFlowCoverage = entry.getValue();
					boolean anythingCovered = false;

					// Is the method already contained?
					if (combinedControlFlowCoverageMapping.containsKey(method)) {
						// Get it.
						boolean[] combinedControlFlowCoverage = combinedControlFlowCoverageMapping.get(method);

						// Checking the length.
						if (combinedControlFlowCoverage.length != controlFlowCoverage.length)
							throw new TestCaseEliminationException(
									"The def-use chain mapping is faulty and cannot be processed.");

						// Merge it with the current coverage.
						for (int a = 0; a < combinedControlFlowCoverage.length; a++) {
							combinedControlFlowCoverage[a] = combinedControlFlowCoverage[a] | controlFlowCoverage[a];
							if (controlFlowCoverage[a]) {
								anythingCovered = true;
								numberControlGraphEdgesCovered++;
							}
						}
					} else {
						// Just clone the current coverage and put it.
						boolean[] controlFlowCoverageCloned = new boolean[controlFlowCoverage.length];
						for (int a = 0; a < controlFlowCoverage.length; a++) {
							controlFlowCoverageCloned[a] = controlFlowCoverage[a];
							if (controlFlowCoverage[a]) {
								anythingCovered = true;
								numberControlGraphEdgesCovered++;
							}
						}
						combinedControlFlowCoverageMapping.put(method, controlFlowCoverageCloned);
					}

					// To make further processing faster, remove any method that is not covered at all.
					if (!anythingCovered) {
						controlFlowCoverageIterator.remove();
					}
				}
			}

			// Remove the solution if it does not cover anything.
			if (numberOfDefUseChainsCovered == 0 && numberControlGraphEdgesCovered == 0) {
				solutionIterator.remove();
				this.solutionProcessor.increaseEliminationSolutionsDropped();
			} else {
				// Set the coverage numbers for the solution.
				solution.setNumberOfCoveredDefUseChains(numberOfDefUseChainsCovered);
				solution.setNumberOfCoveredControlGraphEdges(numberControlGraphEdgesCovered);
			}
		}

		// Count all covered chains and edges.
		long totalNumberOfDefUseChains = 0L;
		long totalNumberOfControlGraphEdges = 0L;
		if (countDefUse) {
			for (boolean chain : combinedDUCoverage) {
				if (chain) maximumNumberOfDefUseChainsCovered++;
				totalNumberOfDefUseChains++;
			}
		}

		if (countControlFlow) {
			Set<Method> combinedControlFlowCoverageSet = combinedControlFlowCoverageMapping.keySet();
			for (Method method : combinedControlFlowCoverageSet) {
				boolean[] combinedControlFlowCoverage = combinedControlFlowCoverageMapping.get(method);
				for (boolean edge : combinedControlFlowCoverage) {
					if (edge) maximumNumberControlGraphEdgesCovered++;
					totalNumberOfControlGraphEdges++;
				}
			}
		}

		// Set the fields for the total def-use chains and control graph edges
		this.totalNumberOfDefUseChains = totalNumberOfDefUseChains;
		this.totalNumberOfControlGraphEdges = totalNumberOfControlGraphEdges;

		// Return the results.
		long[] maximumCoverage = {maximumNumberOfDefUseChainsCovered, maximumNumberControlGraphEdgesCovered};
		return maximumCoverage;
	}

	/**
	 * Get the currently best solutions based on the def-use chain coverage. This method will return a Set of those
	 * solutions that have the highest number of def-use chains not yet covered by the already picked solutions.
	 * @param solutionTreeSet The Set of solutions to choose the beast ones from.
	 * @return A Set of the best solutions based on the def-use coverage.
	 */
	private TreeSet<TestCaseSolutionEliminationWrap> getBestDefUseCoveringSolutions(
			TreeSet<TestCaseSolutionEliminationWrap> solutionTreeSet) {
		// TreeSet for the best solutions.
		TreeSet<TestCaseSolutionEliminationWrap> bestSolutions = new TreeSet<TestCaseSolutionEliminationWrap>();

		// Starting at zero.
		long highestDefUseChainsCoverageCount = 0;
		// Iterate through all solutions.
		Iterator<TestCaseSolutionEliminationWrap> solutionIterator = solutionTreeSet.iterator();
		while (solutionIterator.hasNext()) {
			TestCaseSolutionEliminationWrap solution = solutionIterator.next();
			// Better or equal to the yet best solution?
			long defUseChainsCoverageCount = solution.getNumberOfCoveredDefUseChains();
			if (defUseChainsCoverageCount >= highestDefUseChainsCoverageCount) {
				// Is it even better?
				if (defUseChainsCoverageCount > highestDefUseChainsCoverageCount) {
					// Remove all solutions from the TreeSet.
					bestSolutions.clear();

					// Make this solutions the best one, yet.
					highestDefUseChainsCoverageCount = defUseChainsCoverageCount;
				}
				// Add the found to the TreeSet of the best solutions.
				bestSolutions.add(solution);
			}
		}

		// Return the best solutions found.
		return bestSolutions;
	}

	/**
	 * Get the currently best solutions based on the control graph edge coverage. This method will return a Set
	 * of those solutions that have the highest number of control graph edges not yet covered by the already
	 * picked solutions.
	 * @param solutionTreeSet The Set of solutions to choose the beast ones from.
	 * @return A Set of the best solutions based on the control graph coverage.
	 */
	private TreeSet<TestCaseSolutionEliminationWrap> getBestControlFlowCoveringSolutions(
			TreeSet<TestCaseSolutionEliminationWrap> solutionTreeSet) {
		// TreeSet for the best solutions.
		TreeSet<TestCaseSolutionEliminationWrap> bestSolutions = new TreeSet<TestCaseSolutionEliminationWrap>();

		// Starting at zero.
		long highestControlGraphEdgeCoverageCount = 0;
		// Iterate through all solutions.
		Iterator<TestCaseSolutionEliminationWrap> solutionIterator = solutionTreeSet.iterator();
		while (solutionIterator.hasNext()) {
			TestCaseSolutionEliminationWrap solution = solutionIterator.next();
			// Better or equal to the yet best solution?
			long controlGraphEdgeCoverageCount = solution.getNumberOfCoveredControlGraphEdges();
			if (controlGraphEdgeCoverageCount >= highestControlGraphEdgeCoverageCount) {
				// Is it even better?
				if (controlGraphEdgeCoverageCount > highestControlGraphEdgeCoverageCount) {
					// Remove all solutions from the TreeSet.
					bestSolutions.clear();

					// Make this solutions the best one, yet.
					highestControlGraphEdgeCoverageCount = controlGraphEdgeCoverageCount;
				}
				// Add the found to the TreeSet of the best solutions.
				bestSolutions.add(solution);
			}
		}

		// Return the best solutions found.
		return bestSolutions;
	}

	/**
	 * Remove the now covered def-use chains and control graph edges from the solutions not yet picked.
	 *
	 * Removing the chains and edges covered by the picked solution from any not yet picked solution is vital
	 * to the algorithm and its greedy nature. Not yet picked solutions get less "attractive" with any
	 * coverage already in the picked solutions set removed from them. Hence, by removing the covered chains
	 * and edges the solution picked next will be the one with the most yet uncovered chains and/or edges.
	 *
	 * This method will delete any solution that has no more uncovered chains and edges left after beeing
	 * processed. Especially with high numbers of solutions, this will speed up the overall process as the
	 * number of solutions checked will not decrease by one with each iteration of the algorithm but much
	 * faster ideally.
	 *
	 * @param bestSolution The best solution chosen priorly to invoking this method.
	 * @param solutionTreeSet The Set of not yet picked solutions.
	 */
	private void removeNowCoveredChainsAndEdges(TestCaseSolutionEliminationWrap bestSolution,
			TreeSet<TestCaseSolutionEliminationWrap> solutionTreeSet) {
		boolean[] dUCoverage = bestSolution.getDUCoverage();
	
		// Iterate over all other solutions.
		for (TestCaseSolutionEliminationWrap solution : solutionTreeSet) {
			boolean[] removingDUCoverage = solution.getDUCoverage();
			long numberOfDefUseChainsNowCovered = 0L;

			for (int a = 0; a < dUCoverage.length; a++) {
				// Set every element to false that is true in the chosen solution.
				if (dUCoverage[a]) {
					// Is it true currently?
					if (removingDUCoverage[a]) {
						// One more chain that was uncovered but is covered now.
						numberOfDefUseChainsNowCovered++;
					}

					// Set it.
					removingDUCoverage[a] = false;
				}
			}

			// Reduce the number of uncovered chains the solution could cover.
			solution.decreaseNumberOfCoveredDefUseChainsBy(numberOfDefUseChainsNowCovered);
		}

		// Iterate over all control-flow edges.
		Map<Method, boolean[]> controlFlowCoverageMapping = bestSolution.getCFCoverageMap();
		Set<Method> controlFlowCoverageSet = controlFlowCoverageMapping.keySet();
		for (Method method : controlFlowCoverageSet) {
			boolean[] controlFlowCoverage = controlFlowCoverageMapping.get(method);
			// Iterate over all other solutions.
			for (TestCaseSolutionEliminationWrap solution : solutionTreeSet) {
				Map<Method, boolean[]> removingControlFlowCoverageMapping = solution.getCFCoverageMap();
				long numberOfControlGraphEdgesNowCovered = 0L;

				// Is the method contained?
				if (removingControlFlowCoverageMapping.containsKey(method)) {
					boolean[] removingControlFlowCoverage = removingControlFlowCoverageMapping.get(method);
					boolean coverageLeft = false;
					for (int a = 0; a < controlFlowCoverage.length; a++) {
						// Set every element to false that is true in the chosen solution.
						if (controlFlowCoverage[a]) {
							// Is it true currently?
							if (removingControlFlowCoverage[a]) {
								// One more edge that was uncovered but is covered now.
								numberOfControlGraphEdgesNowCovered++;
							}

							// Set it.
							removingControlFlowCoverage[a] = false;
						} else if (removingControlFlowCoverage[a]) {
							// Note that there was a "true" left.
							coverageLeft = true;
						}
					}

					// Does the solution cover any yet uncovered edge at all?
					if (!coverageLeft) {
						// Remove it to make further processing faster.
						removingControlFlowCoverageMapping.remove(method);
					}
				}

				// Reduce the number of uncovered edges the solution could cover.
				solution.decreaseNumberOfCoveredControlGraphEdges(numberOfControlGraphEdgesNowCovered);

				// Check if that solution can be removed completely. This would speed up the further processing.
				if (solution.getNumberOfCoveredDefUseChains() == 0
						&& solution.getNumberOfCoveredControlGraphEdges() == 0) {
					this.solutionProcessor.increaseEliminationSolutionsDropped();
				}
			}
		}
	}

	/**
	 * Getter for the total number of def-use chains coverable in the methods contained in the
	 * processed solutions. The actual number of covered def-use chains may be lower. It can be
	 * retrieved with {@link #getTotalNumberOfDefUseChainsCovered()}.
	 *
	 * @return The total number of def-use chains.
	 * @throws IllegalStateException If elimination has not been run or is not completed, yet.
	 */
	long getTotalNumberOfDefUseChains() {
		if (this.eliminationCompleted) return this.totalNumberOfDefUseChains;
		throw new IllegalStateException("Solution elimination has not been run or is not completed, yet.");
	}

	/**
	 * Getter for the total number of control graph edges coverable in the methods contained in
	 * the processed solutions. The actual number of covered def-use chains may be lower. It can be
	 * retrieved with {@link #getTotalNumberOfControlGraphEdgesCovered()}.
	 *
	 * @return The total number of control graph edges.
	 * @throws IllegalStateException If elimination has not been run or is not completed, yet.
	 */
	long getTotalNumberOfControlGraphEdges() {
		if (this.eliminationCompleted) return this.totalNumberOfControlGraphEdges;
		throw new IllegalStateException("Solution elimination has not been run or is not completed, yet.");
	}

	/**
	 * Getter for the total number of def-use chains covered by the solutions picked.
	 *
	 * @return The total number of def-use chains covered.
	 * @throws IllegalStateException If elimination has not been run or is not completed, yet.
	 */
	long getTotalNumberOfDefUseChainsCovered() {
		if (this.eliminationCompleted) return this.totalNumberOfDefUseChainsCovered;
		throw new IllegalStateException("Solution elimination has not been run or is not completed, yet.");
	}

	/**
	 * Getter for the total number of control graph edges covered by the solutions picked.
	 *
	 * @return The total number of control graph edges covered.
	 * @throws IllegalStateException If elimination has not been run or is not completed, yet.
	 */
	long getTotalNumberOfControlGraphEdgesCovered() {
		if (this.eliminationCompleted) return this.totalNumberOfControlGraphEdgesCovered;
		throw new IllegalStateException("Solution elimination has not been run or is not completed, yet.");
	}

}
