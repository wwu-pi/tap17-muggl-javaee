/**
 * Provides abstract superclasses for the instruction classes.
 * 
 * As many insctrutions have a similar functionality and are only distinguished by some
 * particularities, most of their functionality can be re-used. Hence, each instruction inherits a
 * class from this package. Each abstract class inherits either GeneralInstruction or
 * GeneralInstructionWithOtherBytes. GeneralInstructionWithOtherBytes also inherits
 * GeneralInstruction.
 * 
 * @author Tim Majchrzak
 */
package de.wwu.muggl.instructions.general;