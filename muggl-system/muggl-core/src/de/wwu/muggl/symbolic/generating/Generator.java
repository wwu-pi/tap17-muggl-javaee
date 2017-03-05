package de.wwu.muggl.symbolic.generating;

/**
 * Interface for generators that the symbolic virtual machine may use.
 *
 * Generators are used to provide means of generation input parameters for methods where
 * random inputs are not feasible. Generated values are useful in two cases:
 * <ul>
 * <li>If unbound variables can be replaced with constrained ones or even with constant values.
 *   Any constraint to the parameters of a methods will speed up execution as the number of
 *   branches in the search tree will be reduced. With the possibility to generate choice points,
 *   an arbitrary number of constrained variables or constants can be tested.</li>
 * <li>If complex data structures are needed. Imagine an algorithm that processes some kind
 *   of trees. Let us say the tree is a binary one. Random symbolic execution would mean that
 *   most inputs for the tree parameter will be useless (yet syntactically correct). To save
 *   execution time and to get good results, a generator could be used to generate a symbolic
 *   binary tree. Should it be too complex to generate a symbolic tree, the generator could be
 *   used to generate partly or non symbolic binary trees. Execution will be done with those
 *   trees and whenever execution with one tree is finished, tracking back will take place and
 *   the next (probably different) tree can be used for execution. Eventually, satisfying
 *   results will be found. These results might be found with random input either but this
 *   could take significantly longer or the found parameters would be strange. To complete the
 *   example: Non binary trees could be used to correctly test a binary tree algorithm but the
 *   test cases generated for that would not be comprehensible for humans as they would look
 *   weird.</li>
 * </ul>
 * <br />
 *
 * @see de.wwu.muggl.symbolic.generating.ArrayElementsGenerator
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public interface Generator extends GenericGenerator {

	/**
	 * Check if this generator allows a CoicePoint to be generated. Generators that
	 * provide more than one distinct value are suitable to be tracked back to. To
	 * make this possible, a ChoicePoint of type GeneratorChoicePoint is needed.
	 * @see de.wwu.muggl.symbolic.generating.GeneratorChoicePoint
	 *
	 * @return true, if a CoicePoint is allowed, false otherwise.
	 */
	boolean allowsChoicePoint();

	/**
	 * Check if this generator can provide another object.
	 * @return true, if this generator can provide another object, false otherwise.
	 */
	boolean hasAnotherObject();

	/**
	 * Provide a generated object.
	 * @return A generated object
	 * @throws IllegalStateException
	 *             If no more elements are available. This cannot happen if checking the availability with
	 *             hasAnotherElement() first and not having more than one thread access this generator.
	 */
	Object provideObject();

	/**
	 * Check if this generator supplies java objects that are no wrappers for primitive types. In
	 * that case they have to be converted to Muggl reference values (object or array references)
	 * before they can be used. This is done automatically if this method returns true.
	 *
	 * @return true, if the supplied objects of this generator require a conversion; false
	 *         otherwise.
	 */
	boolean objectNeedsConversion();

	/**
	 * If the generator implements a deterministic strategy of generating object,
	 * reset it.
	 */
	void reset();

}
