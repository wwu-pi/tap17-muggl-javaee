package de.wwu.muggl.symbolic.flow.defUseChains;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.wwu.muggl.symbolic.flow.defUseChains.structures.Use;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This Class represents the def-use chains generated for a method and any invoked method (according
 * to the options set). There can be multiple DefUseChains containing chains for a Method, but only
 * one per distinct initial Method which is presented by the sub class {@link DefUseChainsInitial}.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class DefUseChains {
	/**
	 * The method these def-use chains belong to.
	 */
	protected Method method;
	private Set<DefUseChain> defUseChains;

	/**
	 * Construct the def-use chains.
	 *
	 * @param method The Method to store the def-use chains for.
	 * @throws NullPointerException If the supplied Method is null.
	 */
	public DefUseChains(Method method) {
		if (method == null)
			throw new NullPointerException("The supplied Method must not be null.");
		this.method = method;
		this.defUseChains = new TreeSet<DefUseChain>();
	}

	/**
	 * Add a def use chain. If it already is included, it will not be included twice and false
	 * will be returned.<br />
	 * <br />
	 * This method has package visibility, as only the DuGenerator should add def-use chains.
	 *
	 * @param defUseChain The def-use chain to add.
	 * @return true, if it was added, false if it already is added.
	 */
	boolean addDefUseChain(DefUseChain defUseChain) {
		return this.defUseChains.add(defUseChain);
	}
	
	/**
	 * Remove a def use chain.<br />
	 * <br />
	 * This method has package visibility, as only the DuGenerator should add def-use chains.
	 *
	 * @param defUseChain The def-use chain to remove.
	 */
	void removeDefUseChain(DefUseChain defUseChain) {
		this.defUseChains.remove(defUseChain);
	}

	/**
	 * Get the Method.
	 *
	 * @return The Method these def-use chains are for.
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Get the set of def-use chains (they are ascendingly sorted by variable indexes,
	 * definition instruction numbers and usage instruction numbers).
	 *
	 * @return The TreeSet of def-use chains.
	 */
	public Set<DefUseChain> getDefUseChains() {
		return this.defUseChains;
	}

	/**
	 * Get a String representation of the def-use chains. The output will also include the method
	 * name and information about the scheme the def-use chains are presented in.
	 *
	 * @return A String representation of the def-use chains.
	 */
	@Override
	public String toString() {
		String duString = "Def-use chains for method " + this.method.getFullNameWithParameterTypesAndNames() + ".\n"
						+ "Scheme: [variable number, def instruction number, use instruction number]\n"
						+ "Instruction numbers include additional bytes.\n\n";
		Iterator<DefUseChain> iterator = this.defUseChains.iterator();
		while (iterator.hasNext()) {
			DefUseChain defUseChain = iterator.next();
			Use use = defUseChain.getUse();
			duString += "["
					 + use.getVariable() + ", "
					 + defUseChain.getDef().getInstructionNumber() + ", "
					 + use.getInstructionNumber() + "]\n";
		}
		return duString;
	}
}
