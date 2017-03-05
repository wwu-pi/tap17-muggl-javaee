package de.wwu.muggl.symbolic.flow.defUseChains;

import de.wwu.muggl.symbolic.flow.defUseChains.structures.Def;
import de.wwu.muggl.symbolic.flow.defUseChains.structures.Use;

/**
 * This class represents a single def use chain in a Method. It implements Comparable to enable
 * sorting of def-use chains.<br />
 * <br />
 * 
 * A def-use chains can be used to check the data-flow coverage. It describes at which point a
 * variable was defined (def) and where it was used (use). Def-use chains can hence be used to
 * optimize testing. It can be chosen if the symbolic execution of a program will finish once all
 * def-use chains have been covered. Another option is to filter solutions by the def-use chains
 * covered and delete redundant ones.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class DefUseChain implements Comparable<DefUseChain> {
	// Elements of a def-use chain.
	private Def def;
	private Use use;

	/**
	 * Initialize the def-use chain.
	 *
	 * @param def The definition.
	 * @param use The usage.
	 */
	public DefUseChain(Def def, Use use) {
		this.def = def;
		this.use = use;
	}

	/**
	 * Getter for the Def object, representing the Definition.
	 * @return The Def object.
	 */
	public Def getDef() {
		return this.def;
	}

	/**
	 * Getter for the Use object, representing the Usage.
	 * @return The Use object.
	 */
	public Use getUse() {
		return this.use;
	}

	/**
	 * Compare this def-use chain to the one supplied.<br />
	 * <br />
	 *
	 * Def-use chain are sorted in ascending order by their definition first. Should it be equal,
	 * the usage is compared. Only if the usage is equal, either, two def-use chains are completely
	 * equal.
	 *
	 * @param arg0 The def-use chain to compare this one to.
	 * @return -1 if this def-use chain is "less", 1 if it is "greater" and 0 if it is equal.
	 */
	public int compareTo(DefUseChain arg0) {
		int defComparison = this.def.compareTo(arg0.def);
		if (defComparison != 0) return defComparison;

		int useComparison = this.use.compareTo(arg0.use);
		if (useComparison != 0) return useComparison;

		return 0;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 * 
	 * @param obj The object to check for equality.
	 * @return true, if the supplied object is of type DefUseChain and its definition and usage
	 *         equals the ones of this; false otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DefUseChain) {
			DefUseChain defUseChain = (DefUseChain) obj;
			if (defUseChain.def.equals(this.def) && defUseChain.use.equals(this.use))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns a hash code value for the object.
	 * 
     * @return  a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.def.hashCode() + this.use.hashCode();
	}
	
	/**
	 * Get a String representation of the def-use chains. The output will also include the method
	 * name and information about the scheme the def-use chains are presented in.
	 *
	 * @return A String representation of the def-use chains.
	 */
	@Override
	public String toString() {
			return "["
					 + this.use.getVariable() + ", "
					 + this.def.getInstructionNumber() + ", "
					 + this.use.getInstructionNumber() + "]";
	}

}
