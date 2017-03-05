package de.wwu.muggl.symbolic.flow.defUseChains;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.vm.classfile.structures.Method;

/**
 * This class triggers the {@link DUGenerator} to find def-use chains for the specified initial
 * method. It will not only store them for this method, but for any other method chains were
 * discovered for.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class DefUseChainsInitial extends DefUseChains {
	// Mapping of def-use chains to methods.
	private Map<Method, DefUseChains> defUseChainsMap;

	/**
	 * Construct the def-use chains by finding them for the supplied method.
	 *
	 * @param method The Method to generate the def-use chains for.
	 * @throws NullPointerException If the supplied Method is null.
	 */
	public DefUseChainsInitial(Method method) {
		super(method);
		DUGenerator generator = new DUGenerator(this, method);
		try {
			this.defUseChainsMap = generator.findChains();
			// TODO generator.omitIrrelevantChains();
		} catch (DUGenerationException e) {
			e.printStackTrace();
			/*
			 * This exception is only thrown if class files are malicious or corrupt, or if there is
			 * a serious misconfiguration. Log it as a warning.
			 */
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("Def-use chains for method "
								+ this.method.getFullNameWithParameterTypesAndNames()
								+ " were not generated successfully due to a "
								+ "DUGenerationException. This is not "
								+ "expected to happen and might hint to serious problems.");
		} catch (InvalidInstructionInitialisationException e) {
			/*
			 * This execution is thrown when the parsing of the bytecode representation to
			 * instruction objects fails. This should not happen as the Method has been parsed
			 * earlier during the execution. Log it as a warning.
			 */
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("Def-use chains for method "
								+ this.method.getFullNameWithParameterTypesAndNames()
								+ " were not generated successfully due to a "
								+ "InvalidInstructionInitialisationException. This is not "
								+ "expected to happen and might hint to serious problems.");
		}

		// Finished.
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger
					.trace("Finished finding def-use chains for method "
							+ this.method.getFullNameWithParameterTypesAndNames() + ".");
	}
		
	/**
	 * Returns the set of methods covered by def-use chains saved by this object.
	 *
	 * @return The set of methods covered by def-use chains saved by this object.
	 */
	public Set<Method> getCoveredMethods() {
		return this.defUseChainsMap.keySet();
	}
	
	/**
	 * Get the mapping of methods to def-use chains for the initial methods they were found for.
	 *
	 * @return The mapping of methods to def-use chains.
	 */
	public Map<Method, DefUseChains> getDefUseChainsMapping() {
		return this.defUseChainsMap;
	}
	
}
