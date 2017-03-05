/**
 * Provides interfaces for the byte code instructions. While the interface Instruction has to be
 * implemented by each instruction class, it should implement one (or more) of the Jump* interfaces
 * from the control subpackage and it may implement the LocalVariableAccess interface to signal that
 * it will access the local variables if executed. Marker interface with regard to the data access
 * are provided by the data subpackage.Studentenbetreuung
 * 
 * @author Tim Majchrzak
 */
package de.wwu.muggl.instructions.interfaces;