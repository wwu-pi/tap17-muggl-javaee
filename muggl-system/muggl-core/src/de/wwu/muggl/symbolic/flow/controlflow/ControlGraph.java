package de.wwu.muggl.symbolic.flow.controlflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This class provides a control graph for a method.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class ControlGraph {
	// Fields.
	private Method method;
	private Map<Integer, Set<Integer>> edges;
	private Set<String> uncaughtExceptionTypes;
	
	// Constants.
	/**
	 * The magic number to mark an edge in the control graph denoting that the method was invoked.
	 * The predecessor of a node marked with this always is 0 i.e. the first instruction. This magic
	 * number should only be used by the def-use generator.
	 */
	public static final int CG_INVOKED_METHOD = 0;
	/**
	 * The magic number to mark an edge in the control graph for returning from a method.
	 */
	public static final int CG_RETURN_METHOD = -2;
	/**
	 * The magic number to mark an edge in the control graph for leaving a method because an
	 * uncaugbht exception was thrown.
	 */
	public static final int CG_EXCECPTION_METHOD = -3;
	
	/**
	 * Construct the control graph for a method.
	 * 
	 * @param method The method to generate the control graph for.
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instructions.
	 * @throws NullPointerException If the supplied Method is null.
	 */
	public ControlGraph(Method method)
		throws InvalidInstructionInitialisationException {
		if (method == null) throw new NullPointerException("The supplied Method must not be null.");
		
		this.method = method;
		this.edges = new HashMap<Integer, Set<Integer>>();
		this.uncaughtExceptionTypes = new HashSet<String>();
		CGGenerator.findEdges(method, this.edges, this.uncaughtExceptionTypes);
	}
	
	/**
	 * Get the method this control graph is for.
	 *
	 * @return The method this control graph is for.
	 */
	public Method getMethod() {
		return this.method;
	}
	
	/**
	 * Get the edges of the control graph.
	 *
	 * @return The edges of the control graph.
	 */
	public Map<Integer, Set<Integer>> getControlGraph() {
		return this.edges;
	}

	/**
	 * Get the edges of the control graph for the instruction <code>index</code>.
	 * 
	 * @param index The index into the instructions to get the edges for; or null, if the index in
	 *        invalid.
	 * @return A set of edges of the control graph.
	 */
	public Set<Integer> getControlGraphFor(int index) {
		return this.edges.get(index);
	}

	/**
	 * Get a String representation of the control graph edges.
	 *
	 * @return A String representation of the control graph edges.
	 */
	@Override
	public String toString() {
		String edgesString = "Control graph edges coverage for Method " + this.method.getFullNameWithParameterTypesAndNames() + ".\n"
		+ "Scheme: Instruction number from -> instruction number to\n"
		+ "Instead of a key \"return\" or \"Except.\" might be shown. "
		+ "This means, that the instruction at the pc might finish the method or throw an uncaught exception.\n";
		edgesString += "Instruction numbers include additional bytes.\n\n";
		boolean firstOne = true;
		Set<Integer> potentialEdgesKeyset = this.edges.keySet();
		for (Integer key : potentialEdgesKeyset) {
			Set<Integer> edgesKeyset = this.edges.get(key);
			for (Integer key2 : edgesKeyset) {
				if (!firstOne) edgesString += "\n";
				firstOne = false;
				edgesString += key + "\t->\t";
				if (key2 == CG_RETURN_METHOD) {
					edgesString += "return";
				} else if (key2 == CG_EXCECPTION_METHOD) {
					edgesString += "Except.";
				} else {
					edgesString += key2;
				}
			}
		}
		return edgesString;
	}

	/**
	 * Get a more sophisticated String representation of the control graph edges.
	 * The output will be similar to this:<br />
	 * 1<br />
	 * |<br />
	 * 2<br />
	 * |<br />
	 * 3 -> 5<br />
	 * |<br />
	 * 4<br />
	 * |<br />
	 * 5<br />
	 *
	 * @param showInstructionMnemorics Do not only display the instructions positions but also their mnemonics.
	 * @return A more sophisticated String representation of the control graph edges.
	 * @see #toString()
	 */
	public String showEdges(boolean showInstructionMnemorics) {
		Instruction[] instructions;
		try {
			instructions = this.method.getInstructionsAndOtherBytes();
		} catch (InvalidInstructionInitialisationException e) {
			return "Could not generate a graphical representation of the control graph due to a problem loaing the "
				+ "instructions of method " + this.method.getFullNameWithParameterTypesAndNames() + ".";
		}

		String edgesString = "Control graph edges for method " + this.method.getFullNameWithParameterTypesAndNames() + ".\n";
		edgesString += "Instruction numbers include additional bytes.\n\n";
		Set<Integer> potentialEdgesKeyset = this.edges.keySet();
		int lastInstruction = instructions.length - 1;
		for (Integer key : potentialEdgesKeyset) {
			// Add the current key.
			edgesString += key;
			if (showInstructionMnemorics) {
				edgesString += " " + instructions[key].getName();
			}

			// Add the edges.
			int nextKey = key + 1 + instructions[key].getNumberOfOtherBytes();
			Set<Integer> edgesKeyset = this.edges.get(key);
			String normalEdge = "";
			if (key != lastInstruction) normalEdge += "\n";
			String jumping = "";
			for (Integer key2 : edgesKeyset) {
				// Is it a jump?
				if (key2 != nextKey) {
					jumping += " -> ";
					if (key2 == CG_RETURN_METHOD) {
						jumping += "return";
					} else if (key2 == CG_EXCECPTION_METHOD) {
						jumping += "Exception";
					} else {
						jumping += key2;
					}
				} else {
					// Normal control flow.
					normalEdge += "|";
				}
			}
			edgesString += jumping + normalEdge;
			if (key != lastInstruction) edgesString += "\n";
		}
		return edgesString;
	}
	
	/**
	 * Get the exception types which are thrown but not caught by this method.
	 *
	 * @return The uncaught exception types of this method.
	 */
	public Set<String> getUncaughtExceptionTypes() {
		return this.uncaughtExceptionTypes;
	}

	/**
	 * Show the exception types which are thrown but not caught by this method. The representation
	 * will be line by line, i.e. exceptions are separated by \n. The result will be ordered
	 * alphabetically.
	 * 
	 * @return The uncaught exception types of this method.
	 */
	public String showUncaughtExceptionTypes() {
		Set<String> orderedExceptions = new TreeSet<String>();
		orderedExceptions.addAll(this.uncaughtExceptionTypes);
		StringBuilder exceptions = new StringBuilder();
		boolean firstOne = true;
		for (String exceptionString : orderedExceptions) {
			if (firstOne) {
				exceptions.append("\n");
				firstOne = false;
			}
			exceptions.append(exceptionString);
		}
		return exceptions.toString();
	}

}
