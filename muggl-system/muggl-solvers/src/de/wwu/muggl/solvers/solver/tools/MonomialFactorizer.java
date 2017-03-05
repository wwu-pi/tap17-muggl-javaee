package de.wwu.muggl.solvers.solver.tools;

import java.util.Arrays;
import java.util.Vector;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
@Deprecated
public class MonomialFactorizer{

    /**
     * Calculates all prime factors of the given positive number and returns
     * them in a two-dimensional array. Each array element consists of an
     * array with two elements, the prime factor at position 0 and the
     * occurence of the prime factor at position 1. The prime factors are
     * ordered ascending.
     * @param x the number that should be factorized.
     * @return the resulting prime factors in the form mentioned above.
     */
    public static long[][] getFactors(long x){
	if (x < 1)
	    return new long[0][];
	if (x == 1){
	    long[][] one = {{1,1}};
	    return one;
	}
	long[][] result = new long[10][2];
	long divisor = 2;
	int idx = 0;
	boolean written = false;
	while (x > 1){
	    if (x % divisor == 0){
		if (idx == result.length){
		    long[][] tmp = result;
		    result = new long[result.length * 2][2];
		    System.arraycopy(tmp, 0, result, 0, idx);
		}
		result[idx][0] = divisor;
		result[idx][1]++;
		x /= divisor;
		written = true;
	    } else {
		divisor++;
		if (written){
		    written = false;
		    idx++;
		}
	    }
	}
	if (written)
	    idx++;
	if (idx < result.length){
	    long[][] tmp = result;
	    result = new long[idx][2];
	    System.arraycopy(tmp, 0, result, 0, idx);
	}
	return result;
    }

    /**
     * Stores the exponents of the variables of the monomial.
     */
    protected int[] exponents;

    /**
     * Stores the prime factors of the target value as produced by the method
     * getFactors(int).
     * @see #getFactors(long)
     */
    protected long[][] factors;

    /**
     * Stores the results of the method getSolutions().
     * @see #getSolutions()
     */
    protected Vector<long[]> result;

    /**
     * The target value of the monomial;
     */
    protected long rhs;

    /**
     * Temporary variable for the transport of the intermediary results of the
     * recursive calls of the findSolution-methods.
     */
    private long[] tmp;

    /**
     * Generates a new MonomialFactorizer for the given target number and the
     * exponents of the variables inside the monomial. The monomial Factorizer
     * will be able to generate all combinations of integer values for each
     * variable of the monomial so that the product of the variables exponentiated
     * with their exponents will be equal to the target number.
     * @param target the target value of the monomial.
     * @param exponents the exponents of the participating variables.
     */
    public MonomialFactorizer(long target, int[] exponents){
	this.rhs = target;
	this.factors = getFactors(rhs);
	this.exponents = exponents;
	tmp = new long[exponents.length];
	Arrays.fill(tmp, 1);
    }

    private void findSolutions(int varIndex){
	if (varIndex == 0){
	    for (int i = 0; i < factors.length; i++){
		if (factors[i][1] % exponents[0] == 0)
		    tmp[0] *= Math.pow(factors[i][0], factors[i][1] / exponents[0]);
		else {
		    tmp[0] = 1;
		    return;
		}
	    }
	    long[] copy = new long[tmp.length];
	    System.arraycopy(tmp, 0, copy, 0, tmp.length);
	    result.add(copy);
	    tmp[0] = 1;
	    return;
	}
	findSolutions(varIndex, factors.length - 1);
    }

    private void findSolutions(int varIndex, int factorIndex){
	if (factorIndex > 0)
	    findSolutions(varIndex, factorIndex - 1);
	else
	    findSolutions(varIndex - 1);
	long tmpold = tmp[varIndex];
	long fold = factors[factorIndex][1];
	while (factors[factorIndex][1] >= exponents[varIndex]){
	    tmp[varIndex] *= factors[factorIndex][0];
	    factors[factorIndex][1] -= exponents[varIndex];
	    if (factorIndex > 0)
		findSolutions(varIndex, factorIndex - 1);
	    else
		findSolutions(varIndex - 1);
	}
	tmp[varIndex] = tmpold;
	factors[factorIndex][1] = fold;
    }

    /**
     * Generates all combinations of integer values for each variable of the
     * monomial so that the product of the variables exponentiated
     * with their exponents will be equal to the target number.
     * @return a Vector of possible values for the variables in the order their
     * exponents were defined in the constructor of the class.
     */
    public synchronized Vector<long[]> getSolutions(){
	if (result == null){
	    result = new Vector<long[]>();
	    findSolutions(exponents.length - 1);
	}
	return result;
    }

    /**
     * Checks whether at least one combinations of integer values exists so that
     * the product of the values exponentiated with the exponents of the variables
     * will be equal to the traget number.
     * @return <code>true</code> if at least one solution exists, <code>false</code>
     * if definitively no such a solution exists.
     */
    public synchronized boolean hasSolutions(){
	if (result != null)
	    return result.size() != 0;
	else{
	    try{
		findSolutions(exponents.length - 1);
	    } catch (NullPointerException npe){
		return true;
	    }
	    return false;
	}
    }

}
