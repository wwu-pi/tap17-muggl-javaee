package de.wwu.muggl.symbolic.flow.controlflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpAlways;

/**
 * Provides static methods for working with control graphs.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class CGUtilities {

	/**
	 * Protected default constructor.
	 */
	protected CGUtilities() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Determine if pc is on a path between <code>from</code> and <code>to</code> on the control graph specified. Pc
	 * explicitly has to be between the two nodes. If is actually is one of these nodes, this method
	 * will not consider it to be on the path!
	 * 
	 * @param controlGraph The control graph to search through.
	 * @param from The pc that denotes the node search starts from.
	 * @param to The pc that denotes the node search ends at.
	 * @param pc The pc that denotes the node being searched for.
	 * @return true, if the pc is on the path; false otherwise.
	 */
	public static boolean isOnPath(ControlGraph controlGraph, int from, int to, int pc) {
		if (from == ControlGraph.CG_INVOKED_METHOD) {
			from = 0;
		}
		Integer fromInt = Integer.valueOf(from);
		Integer toInt = Integer.valueOf(to);
		Integer pcInt = Integer.valueOf(pc);

		// Load the edges.
		Map<Integer, Map<Integer, Boolean>> edges = new HashMap<Integer, Map<Integer, Boolean>>();
		Map<Integer, Set<Integer>> cgCoverageMap = controlGraph.getControlGraph();
		for (Entry<Integer, Set<Integer>> cgCoverageMapTo : cgCoverageMap.entrySet()) {
			HashMap<Integer, Boolean> edgesTo = new HashMap<Integer, Boolean>();
			for (Integer toValue : cgCoverageMapTo.getValue()) {
				edgesTo.put(toValue, Boolean.FALSE);
			}
			edges.put(cgCoverageMapTo.getKey(), edgesTo);
		}

		// Set up a stack of edges to process.
		Stack<Integer> stack = new Stack<Integer>();
		Map<Integer, Boolean> edgesTo = edges.get(fromInt);
		for (Integer toKey : edgesTo.keySet()) {
			stack.add(toKey);
			edgesTo.put(toKey, Boolean.TRUE);
		}

		// Process the stack until it is empty.
		while (!stack.isEmpty()) {
			Integer currentNode = stack.pop();
			// No need to follow this path any further if we have reached to or from.
			if (currentNode != toInt && currentNode != fromInt) {
				// Did we reach pc?
				if (currentNode == pcInt) {
					return true;
				}

				/*
				 * Determine any edges from this node and put them onto the stack if they have not
				 * been visited yet.
				 */
				if (currentNode >= 0) {
					edgesTo = edges.get(currentNode);
					for (Entry<Integer, Boolean> toEntry : edgesTo.entrySet()) {
						if (toEntry.getValue() != Boolean.TRUE) {
							stack.add(toEntry.getKey());
							edgesTo.put(toEntry.getKey(), Boolean.TRUE);
						}
					}
				}
			}
		}

		// Nothing found.
		return false;
	}

	/**
	 * Control graphs found by the {@link CGGenerator} are not reduced to edges between
	 * basic blocks. In fact, they contain any edge between nodes whereas nodes are all instructions
	 * of a method. If a control graph is desired that only contains edges between basic blocks and
	 * thus treats every basic block as an edge, this method can be used to determine the basic
	 * blocks.
	 * 
	 * The start of the basic block is the last instruction in a sequence of instructions that can
	 * be reached by jumps. The following blocks never are jump targets and will always be processed
	 * in sequence. The last instruction in the block is either an conditional jump (at this place
	 * including switching instructions and instructions that may throw exceptions) or the
	 * instruction prior to a jump target. Basic blocks must not contain a sequence of instructions.
	 * In fact, they can be made up from one instruction only.
	 * 
	 * @param controlGraph The control graph to extract basic blocks from.
	 * @return The basic blocks as a Map of integers to integers where the first one is pc the block
	 *         starts at and the second one is the pc the block ends at.
	 */
	public static Map<Integer, Integer> getBasicBlocks(ControlGraph controlGraph) {
		// Preparation.
		Map<Integer, Integer> basicBlocks = new HashMap<Integer, Integer>();
		Map<Integer, Set<Integer>> cgCoverageMap = controlGraph.getControlGraph();
		
		Instruction[] instructions;
		try {
			instructions = controlGraph.getMethod().getInstructionsAndOtherBytes();
		} catch (InvalidInstructionInitialisationException e) {
			// This cannot happen. At this point, the Method has been read already.
			return null;
		}
		
		/*
		 * Get the basic blocks. For performance reasons, the strategy is rather simple: Process the
		 * graph by following its edges. Records basic blocks while doing so. Whenever a instruction
		 * with more than one edge is encountered, branch and save the basic block. Even though the
		 * control-flow is not interrupted, unconditional jumps will also end a basic block. On any
		 * jump that has a target with a lower pc than the current pc is, check whether the target
		 * already is the beginning instruction of a basic block. If it is, discard hat branch. If
		 * it is not, proceed, but check whether a basic block has been split into two by doing so.
		 */
		Stack<Integer> stack = new Stack<Integer>();
		stack.push(0);
		while (!stack.isEmpty()) {
			Integer startedAt = stack.pop();
			Integer pcCur = startedAt;
			Set<Integer> edges;
			// Continue until an instruction with multiple edges was found.
			for (edges = cgCoverageMap.get(pcCur); edges.size() == 1 && !(instructions[pcCur] instanceof JumpAlways);) {
				pcCur = edges.iterator().next();
			}
			
			// Save the basic block.
			basicBlocks.put(startedAt, pcCur);
			
			// Branch.
			for (Integer target : edges) {
				// Only continue when the jump target is not the start of a basic block already.
				if (basicBlocks.containsKey(target)) {
					// Check whether it is a jump into a basic block.
					Integer basicBlockStart = inBasicBlock(target, basicBlocks);
					if (basicBlockStart != null) {
						// Split the block.
						Integer basicBlockEnd = basicBlocks.get(basicBlockStart);
						basicBlocks.remove(basicBlockStart);
						Integer precedeTarget;
						int a = target - 1;
						while (instructions[a] == null) { a--; }
						precedeTarget = a;
						basicBlocks.put(basicBlockStart, precedeTarget);
						basicBlocks.put(target, basicBlockEnd);
					} else {
						// Process that branch.
						stack.push(target);
					}
				}
			}
		}
		
		// Finished.
		return basicBlocks;
	}
	
	/**
	 * Check whether <code>pc</code> is within a basic block.
	 *
	 * @param pc The pc to check.
	 * @param basicBlocks the basic blocks yet found in the code of a method.
	 * @return The starting pc of a basic block if pc is found to split a basic block; null otherwise. 
	 */
	private static Integer inBasicBlock(Integer pc, Map<Integer, Integer> basicBlocks) {
		Set<Entry<Integer, Integer>> entries = basicBlocks.entrySet();
		for (Entry<Integer, Integer> entry : entries) {
			if (entry.getKey() < pc && entry.getValue() >= pc) {
				return entry.getKey();
			}
		}
		
		return null;
	}

}
