package de.wwu.muggl.solvers.solver.numbers;

import java.math.BigInteger;


/**
 * 
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Binomials{

    protected static Binomials instance;

    protected BigInteger[][] binomials;

    private Binomials(){
	binomials = new BigInteger[0][];
    }

    public final static BigInteger MAX_LONG = new BigInteger(Long.toString(Long.MAX_VALUE));

    public static Binomials getInstance(){
	if (instance == null)
	    instance = new Binomials();
	return instance;
    }

    public BigInteger getBinomial(int n, int k){
	if (k > n/2)
	    k = n - k;
	if (n > binomials.length - 1){
	    BigInteger[][] old = binomials;
	    binomials = new BigInteger[n + 1][];
	    System.arraycopy(old, 0, binomials, 0, old.length);
	    for (int line = old.length; line < binomials.length; line++){
		binomials[line] = new BigInteger[line / 2 + 1];
		binomials[line][0] = BigInteger.ONE;
	    }
	}
	BigInteger result = binomials[n][k];
	if (result == null){
	    result = getBinomial(n-1, k-1).add(getBinomial(n-1, k));
	    binomials[n][k] = result;
	}
	return result;
    }

    public long getLongBinomial(int n, int k){
	BigInteger b = getBinomial(n, k);
	if (b.compareTo(MAX_LONG) > 0)
	    throw new InternalError("Overflow during calculation of binomial(" + n + ", " + k + ")");
	return b.longValue();
    }

}
