package de.wwu.muggl.symbolic.searchAlgorithms;

import javax.persistence.criteria.CriteriaQuery;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.list.SymbolicHasNext;
import de.wwu.muggl.instructions.bytecode.Getfield;
import de.wwu.muggl.instructions.bytecode.LCmp;
import de.wwu.muggl.instructions.general.CompareFp;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.If_acmp;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.general.Switch;
import de.wwu.muggl.instructions.jpa.JPAInvokeInterface;
import de.wwu.muggl.jpa.FindResult;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.ArrayRestore;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.FieldPut;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Restore;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.type.IObjectreference;

/**
 * This interface is implemented by any class that offers a search algorithm. It
 * specifies the methods there classes have to offer, so that the search algorithm
 * can be used in the symbolic execution of a program. Especially the backtracking
 * functionality is of importance.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-29
 */
public interface SearchAlgorithm {
	
	NoExceptionHandlerFoundException getTrackBackExpectedException();

	/**
	 * Get the current ChoicePoint of this search algorithm. It reflects the
	 * last node in the searching tree visited.
	 * @return The current choice point.
	 */
	ChoicePoint getCurrentChoicePoint();

	/**
	 * Execution has reached a solution or a point where further execution
	 * should not take part (at least for the moment). This method will track
	 * back according to the search algorithm implemented, setting the
	 * SymbolicalVirtualMachine to the state of the ChoicePoint tracked back
	 * to.
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @return true, if tracking back was successfull and the execution can be continued, false, if there was no possibility for tracking back and then execution should hence be stopped.
	 */
	boolean trackBack(SymbolicVirtualMachine vm);

	/**
	 * Recover that state at the ChoicePoint currentChoicePoint.
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 */
	void recoverState(SymbolicVirtualMachine vm);

	/**
	 * Generate a new GeneratorChoicePoint or ArrayInitializationChoicePoint for a local variable.
	 * Set it as the current choice point and mark that the jumping branch has been visited already
	 * (since execution just continues there.)
	 * 
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param localVariableIndex The index into the local variable table to store the generated
	 *        array at.
	 * @param generator A variable Generator. May be null to indicate no custom variable generator
	 *        is used.
	 * @throws ConversionException If converting the first provided object failed.
	 * 
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm, int localVariableIndex,
			Generator generator) throws ConversionException, SymbolicExecutionException;

	/**
	 * Generate a new ArrayInitializationChoicePoint for instruction <code>anewarray</code>. Set it
	 * as the current choice point and mark that the jumping branch has been visited already (since
	 * execution just continues there.)
	 * 
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param type A String representation of the type.
	 * 
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm, String type)
			throws SymbolicExecutionException;

	/**
	 * Generate a new ConditionalJumpChoicePoint. Set it as the current choice point and mark that
	 * the jumping branch has been visited already (since execution just continues there.)
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param constraintExpression The ConstraintExpression describing the choice at this
	 *        conditional jump Instruction.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction, ConstraintExpression constraintExpression);

	/**
	 * Generate a new LongComparisonChoicePoint. Set it as the current choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param leftTerm The term of long variables and constants of the left hand side of the comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point generation.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm, LCmp instruction,
			Term leftTerm, Term rightTerm) throws SymbolicExecutionException;

	/**
	 * Generate a new FpComparisonChoicePoint. Set it as the current choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param less If set to true, the choice point will have the behaviour of dcmpl / fcmpl;
	 *        otherwise, it will behave like dcmpg / fcmpg.
	 * @param leftTerm The term of long variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm, CompareFp instruction,
			boolean less, Term leftTerm, Term rightTerm) throws SymbolicExecutionException;

	/**
	 * Generate a new SwitchingComparisonChoicePoint. Set it as the current choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @param low The "low" boundary of the tableswitch instruction; or null, if the choice point is
	 *        generated for a lookupswitch instruction.
	 * @param high The "high" boundary of the tableswitch instruction; or null, if the choice point
	 *        is generated for a lookupswitch instruction.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets or if there are no choices at all.
	 * @throws NullPointerException If either of the specified arrays is null, or if the instruction
	 *         is tableswitch and at least one of the boundaries is null.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 */
	void generateNewChoicePoint(SymbolicVirtualMachine vm, Switch instruction, Term termFromStack,
			IntConstant[] keys, int[] pcs, IntConstant low, IntConstant high)
			throws SymbolicExecutionException;

	/**
	 * Return a String representation of this search algorithms name.
	 *
	 * @return A String representation of this search algorithms name.
	 */
	String getName();

	/**
	 * Get the information whether this search algorithm requires a field
	 * value to be stored (at this exakt moment of execution).
	 * @return true, if the field value should be stored, false otherwise.
	 */
	boolean savingFieldValues();

	/**
	 * Store a field value for use by the search algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation Either a InstanceFieldPut or a StaticfieldPut object.
	 */
	void saveFieldValue(FieldPut valueRepresentation);

	/**
	 * Get the information whether this search algorithm requires a local
	 * variable value to be stored (at this exakt moment of execution).
	 * @return true, if the local variable value should be stored, false otherwise.
	 */
	 boolean savingLocalVariableValues();

	/**
	 * Store a local variable value for use by the search algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation A Restore object.
	 */
	void saveLocalVariableValue(Restore valueRepresentation);

	/**
	 * Get the information whether this search algorithm requires an array
	 * value to be stored (at this exakt moment of execution).
	 * @return true, if the array value should be stored, false otherwise.
	 */
	boolean savingArrayValues();

	/**
	 * Store a array value for use by the search algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation An ArrayRestore object.
	 */
	void saveArrayValue(ArrayRestore valueRepresentation);

	/**
	 * Return statistical information about the execution. The information is
	 * returned as an two-dimensional String array. Its first dimension
	 * represents the distinct statistical informations that can be supplied.
	 * The second dimension has two elements. Element 0 is the name of the
	 * statistical criterion, element 1 is the string representation of its
	 * value.
	 * @return Statistical information about the execution.
	 */
	String[][] getStatisticalInformation();
	
	
	void generateNewDBChoicePoint(SymbolicVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction, ConstraintExpression constraintExpression);
	
//	void generateNewEntityManagerFindChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, ConstraintExpression constraintExpression);
	
	void generateFindCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, String entityName, Variable idVariable);
	
	void generateResultListCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery);
	
//	void generateResultListCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery);

	void generateNewFindResultChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, FindResult findResultObject, int jumpTarget);
	
	void generateNewCollectionChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, CollectionVariable<DatabaseObject> collection);

	void generateNewChoicePoint(SymbolicVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, Object reference1, Object reference2, boolean compareEquality);
	
	void generateNewStringComparisonChoicePoint(SymbolicVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, Object reference1, Object reference2);
	
	void generateEntityManagerFindChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, String entityName, Object idReference)  throws VmRuntimeException;
	
	void generateEntityManagerPersistChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, Objectref entityObject) throws VmRuntimeException;
	
	void generateIteratorHasNextChoicePoint(SymbolicVirtualMachine vm, SymbolicIterator symbolicIterator, GeneralInstructionWithOtherBytes instruction);
	
	void generateGetQueryResultListChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException;
	
	void generateGetSingleResultChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException;

	void generateSymbolicIterationChoicePoint(Invoke instruction, SymbolicIterator symbolicIterator);

	void generateNewObjectrefIsNullChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, Objectref objectReference, boolean jumpToIsNull);
	
	void generateNewGetFieldOfNullableObjectChoicePoint(JPAVirtualMachine vm, Getfield instruction, Objectref objectReference)  throws VmRuntimeException;

}
