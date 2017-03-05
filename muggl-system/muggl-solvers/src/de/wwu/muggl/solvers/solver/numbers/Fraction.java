package de.wwu.muggl.solvers.solver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;

/**
 * @author Christoph Lembeck
 */
public class Fraction extends NumberFactory implements NumberWrapper{

    public static boolean roundStringOutput = false;
    
    /**
     * The constant -1.
     */
    public static final Fraction MINUSONE = new Fraction(new BigInteger("-1"), BigInteger.ONE);

    /**
     * The constant 1.
     */
    public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);

    /**
     * The constant 2.
     */
    public static final Fraction TWO = new Fraction(new BigInteger("2"), BigInteger.ONE);

    /**
     * The constant 0.
     */
    public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);

    protected static HashMap<Double, Fraction> values;

    static{
	values = new HashMap<Double, Fraction>();
	for (int i = -2; i < 10; i++)
	    values.put(new Double(i), new Fraction(i));
    }

    /**
     * Stores the denominator of the fraction.
     */
    protected BigInteger denominator;
    
    public BigInteger getDenominator() {
	return denominator;
    }
        
    protected final int maxBitLength = 150;

    /**
     * Stoers the numerator of the fraction.
     */
    protected BigInteger numerator;

    /**
     * Constructor for the Fraction representing the number zero. Only used for
     * creating a new NumberFactory using the reflection API.
     */
    public Fraction(){
	this.denominator = BigInteger.ONE;
	this.numerator = BigInteger.ZERO;
    }

    /**
     * Creates a new Fraction of the form (numerator/denominator) and divides both
     * of them by their common greatest divisor to keep the numbers as short as
     * possible.
     * @param numerator the numerator of the fraction
     * @param denominator the denominator of the fraction
     */
    public Fraction(BigInteger numerator, BigInteger denominator){
	if (numerator.equals(BigInteger.ZERO)){
	    this.numerator = BigInteger.ZERO;
	    this.denominator = BigInteger.ONE;
	} else {
	    BigInteger gcd = numerator.gcd(denominator);
	    if (gcd.equals(BigInteger.ONE)){
		this.numerator = numerator;
		this.denominator = denominator;
	    } else {
		this.numerator = numerator.divide(gcd);
		this.denominator = denominator.divide(gcd);
	    }
	    if (this.denominator.signum() == -1){
		this.numerator = this.numerator.negate();
		this.denominator = this.denominator.negate();
	    }
	}
	int shift = this.denominator.bitLength() - maxBitLength;
	if (shift > 0){
	    this.denominator = this.denominator.shiftRight(shift);
	    this.numerator = this.numerator.shiftRight(shift);
	}
    }

    /**
     * Creates a new Fraction object that represents exact the same value as the
     * passed parameter.
     * @param val the value that should be represented by this fraction.
     */
    public Fraction(double val){
	init(val);
    }

    public Fraction(NumericConstant constant){
	if (constant instanceof IntConstant){
	    this.numerator = new BigInteger(Integer.toString(constant.getIntValue()));
	    this.denominator = BigInteger.ONE;
	}
	if (constant instanceof LongConstant){
	    this.numerator = new BigInteger(Long.toString(constant.getLongValue()));
	    this.denominator = BigInteger.ONE;
	}
	init(constant.getDoubleValue());
    }

    @Override
    public NumberWrapper abs(){
	if (numerator.signum() == -1)
	    return new Fraction(numerator.negate(), denominator);
	else
	    return this;
    }

    @Override
    public NumberWrapper add(NumberWrapper addend){
	Fraction addendF = (Fraction)addend;
	return new Fraction(numerator.multiply(addendF.denominator).add(denominator.multiply(addendF.numerator)), denominator.multiply(addendF.denominator));
    }

    @Override
    public NumberWrapper ceil(){
	if (denominator.compareTo(BigInteger.ONE) == 0)
	    return this;
	BigInteger[] bi = numerator.divideAndRemainder(denominator);
	if (numerator.compareTo(BigInteger.ZERO) < 0){
	    return new Fraction(bi[0], BigInteger.ONE);
	} else
	    return new Fraction(bi[0].add(BigInteger.ONE), BigInteger.ONE);
    }

    /**
     * Returns the quotient of the actual fraction and the passed number (which
     * has to be of the type Fraction too).
     */
    public NumberWrapper div(NumberWrapper divisor){
	if (divisor.equals(ZERO))
	    throw new ArithmeticException("Fraction divide by zero");
	Fraction divisorF = (Fraction) divisor;
	return new Fraction(numerator.multiply(divisorF.denominator), denominator.multiply(divisorF.numerator));
    }

    @Override
    public Fraction div2(){
	return new Fraction(numerator, denominator.shiftLeft(1));
    }

    @Override
    public double doubleValue(){
	return new BigDecimal(numerator).divide(new BigDecimal(denominator), 300, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    @Override
    public boolean equals(NumberWrapper other){
	if (other instanceof Fraction){
	    Fraction otherF = (Fraction)other;
	    return otherF.numerator.equals(numerator) && otherF.denominator.equals(denominator);
	} else
	    return false;
    }

    @Override
    public boolean equals(Object obj){
	return (obj == this) || (obj instanceof NumberWrapper && equals((NumberWrapper)obj));
    }

    @Override
    public float floatValue(){
	return (float)doubleValue();
    }

    @Override
    public NumberWrapper floor(){
	if (denominator.compareTo(BigInteger.ONE) == 0)
	    return this;
	BigInteger[] bi = numerator.divideAndRemainder(denominator);
	if (numerator.compareTo(BigInteger.ZERO) < 0){
	    return new Fraction(bi[0].subtract(BigInteger.ONE), BigInteger.ONE);
	} else
	    return new Fraction(bi[0], BigInteger.ONE);
    }

    @Override
    public NumberWrapper gcd(NumberWrapper other) {
	Fraction otherF = (Fraction)other;
	return new Fraction(numerator.multiply(otherF.denominator).gcd(denominator.multiply(otherF.numerator)), denominator.multiply(otherF.denominator));
    }

    @Override
    public NumberWrapper getBinomial(int n, int k){
	return new Fraction(Binomials.getInstance().getBinomial(n, k), BigInteger.ONE);
    }

    @Override
    public NumberFactory getFactory(){
	return this;
    }
    /**
     * Creates a new Fraction object that represents exact the same value as the
     * passed parameter and returns it.
     * @return the Fraction with the same value as the passed double argument.
     */
    @Override
    public NumberWrapper getInstance(double value){
	Fraction result = values.get(value);
	if (result != null)
	    return result;
	return new Fraction(value);
    }

    @Override
    public NumberWrapper getInstance(NumericConstant value) {
	if (value instanceof IntConstant)
	    return new Fraction(new BigInteger(Integer.toString(value.getIntValue())), BigInteger.ONE);
	if (value instanceof LongConstant)
	    return new Fraction(new BigInteger(Long.toString(value.getLongValue())), BigInteger.ONE);
	return new Fraction(value.getDoubleValue());
    }

    @Override
    public NumberWrapper getMinusOne() {
	return MINUSONE;
    }

    @Override
    public NumberWrapper getOne() {
	return ONE;
    }

    @Override
    public NumberWrapper getTwo() {
	return TWO;
    }

    @Override
    public NumberWrapper getZero() {
	return ZERO;
    }

    @Override
    public boolean greaterOrEqual(NumberWrapper other){
	Fraction x = (Fraction)other;
	if (denominator.compareTo(BigInteger.ZERO) > 0){
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) >= 0;
		else
		    return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) <= 0;
	} else {
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) <= 0;
	    else
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) >= 0;
	}
    }

    @Override
    public boolean greaterThan(NumberWrapper other){
	Fraction x = (Fraction)other;
	if (denominator.compareTo(BigInteger.ZERO) > 0){
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) > 0;
		else
		    return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) < 0;
	} else {
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) < 0;
	    else
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) > 0;
	}
    }

    /**
     * Returns a hash code for this object.
     * @return the hash code value of this fraction.
     */
    @Override
    public int hashCode(){
	return numerator.hashCode() * denominator.hashCode();
    }

    @Override
    public NumberWrapper inc(){
	return new Fraction(numerator.add(denominator), denominator);
    }

    @Override
    public int intValue(){
	return (int)doubleValue();
    }

    @Override
    public boolean isGreaterOrEqualZero(){
	return numerator.signum() >= 0;
    }

    @Override
    public boolean isGreaterThanZero(){
	return numerator.signum() == 1;
    }

    @Override
    public boolean isInfinite(){
	return false;
    }

    @Override
    public boolean isInteger(){
	return denominator.compareTo(BigInteger.ONE) == 0;
    }

    @Override
    public boolean isInvalid(){
	return false;
    }

    @Override
    public boolean isLessOrEqualZero(){
	return numerator.signum() <= 0;
    }

    @Override
    public boolean isLessThanOne(){
	return numerator.signum() == -1 || numerator.compareTo(denominator) < 0;
    }

    @Override
    public boolean isLessThanZero(){
	return numerator.signum() == -1;
    }

    @Override
    public boolean isMinusOne() {
	return numerator.intValue() == -1 && denominator.equals(BigInteger.ONE);
    }

    @Override
    public boolean isNegativeInfinity(){
	return false;
    }

    @Override
    public boolean isOne() {
	return numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE);
    }

    @Override
    public boolean isPositiveInfinity(){
	return false;
    }

    @Override
    public boolean isZero() {
	return numerator.equals(BigInteger.ZERO);
    }

    @Override
    public boolean lessOrEqual(NumberWrapper other){
	Fraction x = (Fraction)other;
	if (denominator.compareTo(BigInteger.ZERO) > 0){
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) <= 0;
	    else
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) >= 0;
	} else {
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) >= 0;
		else
		    return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) <= 0;
	}
    }

    @Override
    public boolean lessThan(NumberWrapper other){
	Fraction x = (Fraction)other;
	if (denominator.compareTo(BigInteger.ZERO) > 0){
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) < 0;
	    else
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) > 0;
	} else {
	    if (x.denominator.compareTo(BigInteger.ZERO) > 0)
		return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) > 0;
		else
		    return numerator.multiply(x.denominator).compareTo(denominator.multiply(x.numerator)) < 0;
	}
    }

    @Override
    public long longValue(){
	return (long)doubleValue();
    }

    /**
     * Returns the product of the actual fraction and the passed number (which has
     * to be of the type Fraction too).
     * @param factor the fraction that should multiplied with this fraction.
     * @return the product of the actual fraction and the passed number.
     */
    @Override
    public NumberWrapper mult(NumberWrapper factor){
	Fraction factorF = (Fraction)factor;
	return new Fraction(numerator.multiply(factorF.numerator), denominator.multiply(factorF.denominator));
    }

    @Override
    public NumberWrapper negate(){
	return new Fraction(numerator.negate(), denominator);
    }

    @Override
    public NumberWrapper pow(int exponent){
	if (exponent == 1)
	    return this;
	return new Fraction(numerator.pow(exponent), denominator.pow(exponent));
    }

    @Override
    public Fraction square(){
	return new Fraction(numerator.pow(2), denominator.pow(2));
    }

    @Override
    public NumberWrapper sub(NumberWrapper subtrahend){
	Fraction subtrahendF = (Fraction)subtrahend;
	return new Fraction(numerator.multiply(subtrahendF.denominator).subtract(denominator.multiply(subtrahendF.numerator)), denominator.multiply(subtrahendF.denominator));
    }

    @Override
    public NumericConstant toNumericConstant(byte type) {
	switch (type){
	case Expression.INT:
	    return IntConstant.getInstance(intValue());
	case Expression.LONG:
	    return LongConstant.getInstance(longValue());
	case Expression.FLOAT:
	    return FloatConstant.getInstance(floatValue());
	default:
	    return DoubleConstant.getInstance(doubleValue());
	}
    }

    /**
     * Returns a string representation of the fraction.
     * @return the string representation of the fraction.
     */
    @Override
    public String toString(){
	if (roundStringOutput)
	    return Double.toString( (numerator.doubleValue() / denominator.doubleValue()) );
	
	if (numerator.compareTo(BigInteger.ZERO) == 0)
	    return "0";
	if (denominator.compareTo(BigInteger.ONE) == 0)
	    return numerator.toString();
	return numerator.toString() + '/' + denominator.toString();
    }

    @Override
    public String toTexString() {
	return toString();
    }

    private void init(double val){
	long l = Double.doubleToLongBits(val);
	if ((l == 0x0000000000000000L) || (l == 0x8000000000000000L)){
	    this.numerator = BigInteger.ZERO;
	    this.denominator = BigInteger.ONE;
	    return;
	}
	int exponent = (int)(((l & 0x7ff0000000000000L) >>> 52) - 1023);
	long mantisse = (l & 0x000fffffffffffffL) | 0x0010000000000000L;
	numerator = new BigInteger("" + mantisse);
	if (52-exponent >= 0)
	    denominator = BigInteger.ONE.shiftLeft(52 - exponent);
	else {
	    denominator = BigInteger.ONE;
	    numerator = numerator.shiftLeft(exponent - 52);
	}
	BigInteger gcd = numerator.gcd(denominator);
	if (!gcd.equals(BigInteger.ONE)){
	    this.numerator = numerator.divide(gcd);
	    this.denominator = denominator.divide(gcd);
	}
	if ((l & 0x8000000000000000L) != 0)
	    numerator = numerator.negate();
    }

}
