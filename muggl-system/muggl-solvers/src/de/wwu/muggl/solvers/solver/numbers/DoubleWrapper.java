package de.wwu.muggl.solvers.solver.numbers;

import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;

/**
 * @author Christoph Lembeck
 */
public class DoubleWrapper extends NumberFactory implements NumberWrapper{

    /**
     * the constant -1.
     */
    public static final DoubleWrapper MINUSONE = new DoubleWrapper(-1d);

    /**
     * the constant 1.
     */
    public static final DoubleWrapper ONE = new DoubleWrapper(1d);

    /**
     * the constant 2.
     */
    public static final DoubleWrapper TWO = new DoubleWrapper(2d);

    /**
     * the constant 0.
     */
    public static final DoubleWrapper ZERO = new DoubleWrapper(0d);

    
    /**
     * the constant negative infinity
     */
    public static final DoubleWrapper NEGATIVE_INFINITY = new DoubleWrapper(Double.NEGATIVE_INFINITY);

    /**
     * the constant positive infinity
     */
    public static final DoubleWrapper POSITIVE_INFINITY = new DoubleWrapper(Double.POSITIVE_INFINITY);
    
    /**
     * The internal value of the current wrapper.
     */
    protected double value;

    /**
     * Constructor for the DoubleWrapper representing the number zero. Only used for
     * creating a new NumberFactory using the reflection API.
     */
    public DoubleWrapper(){
	this.value = 0d;
    }

    /**
     * Creates a new DoubleWrapper representing the passed double value.
     * @param value the value that should be represented by this wrapper.
     */
    public DoubleWrapper(double value){
	this.value = value;
    }

    public NumberWrapper abs(){
	if (value < 0)
	    return new DoubleWrapper(-value);
	else
	    return this;
    }

    @Override
    public NumberWrapper add(NumberWrapper addend) {
	DoubleWrapper da = (DoubleWrapper)addend;
	return new DoubleWrapper(value + da.value);
    }


    @Override
    public NumberWrapper ceil() {
	return new DoubleWrapper(Math.ceil(value));
    }

    @Override
    public NumberWrapper div(NumberWrapper divisor) {
	DoubleWrapper dd = (DoubleWrapper)divisor;
	return new DoubleWrapper(value/dd.value);
    }

    @Override
    public NumberWrapper div2() {
	return new DoubleWrapper(value / 2);
    }

    @Override
    public double doubleValue() {
	return value;
    }

    @Override
    public boolean equals(NumberWrapper other){
	return ((other instanceof DoubleWrapper) && (((DoubleWrapper)other).value == value));
    }

    @Override
    public float floatValue(){
	return (float)value;
    }

    @Override
    public NumberWrapper floor() {
	return new DoubleWrapper(Math.floor(value));
    }

    @Override
    public NumberWrapper gcd(NumberWrapper other){
	double x = this.value;
	double y = ((DoubleWrapper)other).value;
	if (x < y){
	    double h = x;
	    x = y;
	    y = h;
	}
	while (y != 0){
	    double h = x % y;
	    x = y;
	    y = h;
	}
	return new DoubleWrapper(x);
    }

    @Override
    public NumberWrapper getBinomial(int n, int k){
	return new DoubleWrapper(Binomials.getInstance().getLongBinomial(n, k));
    }

    @Override
    public NumberFactory getFactory(){
	return this;
    }

    /**
     * Returns a DoubleWrapper object representing the passed argument.
     */
    @Override
    public NumberWrapper getInstance(double newValue) {
	return new DoubleWrapper(newValue);
    }

    @Override
    public NumberWrapper getInstance(NumericConstant val) {
	return new DoubleWrapper(val.getDoubleValue());
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
    public boolean greaterOrEqual(NumberWrapper other) {
	DoubleWrapper dx = (DoubleWrapper)other;
	return value >= dx.value;
    }

    @Override
    public boolean greaterThan(NumberWrapper other) {
	DoubleWrapper dx = (DoubleWrapper)other;
	return value > dx.value;
    }

    @Override
    public int hashCode(){
	long bits = Double.doubleToLongBits(value);
	return (int)(bits ^ (bits >>> 32));
    }

    @Override
    public NumberWrapper inc(){
	return new DoubleWrapper(value++);
    }

    @Override
    public int intValue(){
	return (int)value;
    }

    @Override
    public boolean isGreaterOrEqualZero(){
	return value >= 0d;
    }

    @Override
    public boolean isGreaterThanZero(){
	return value > 0d;
    }

    @Override
    public boolean isInfinite(){
	return value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isInteger() {
	return value == Math.floor(value);
    }

    @Override
    public boolean isInvalid(){
	return value == Double.NaN;
    }

    @Override
    public boolean isLessOrEqualZero(){
	return value <= 0d;
    }

    @Override
    public boolean isLessThanOne(){
	return value < 1;
    }

    @Override
    public boolean isLessThanZero(){
	return value < 0d;
    }

    @Override
    public boolean isMinusOne() {
	return value == -1;
    }

    @Override
    public boolean isNegativeInfinity(){
	return value == Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isOne() {
	return value == 1;
    }

    @Override
    public boolean isPositiveInfinity(){
	return value == Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean isZero() {
	return value == 0d;
    }

    @Override
    public boolean lessOrEqual(NumberWrapper other) {
	DoubleWrapper dx = (DoubleWrapper)other;
	return value <= dx.value;
    }

    @Override
    public boolean lessThan(NumberWrapper other) {
	DoubleWrapper dx = (DoubleWrapper)other;
	return value < dx.value;
    }

    @Override
    public long longValue(){
	return (long)value;
    }

    /**
     * Returns the product of the actual and the passed number (wich has to
     * be of the type DoubleWrapper too).
     */
    @Override
    public NumberWrapper mult(NumberWrapper factor) {
	DoubleWrapper df = (DoubleWrapper)factor;
	return new DoubleWrapper(value * df.value);
    }

    /**
     * Returns a DoubleWrapper that represents the negated value of this wrapper.
     * @return the DoubleWrapper representing the negated value of this wrapper.
     */
    @Override
    public NumberWrapper negate() {
	return new DoubleWrapper(-value);
    }

    @Override
    public NumberWrapper pow(int exponent){
	return new DoubleWrapper(Math.pow(value, exponent));
    }

    @Override
    public DoubleWrapper square(){
	return new DoubleWrapper(value * value);
    }

    /**
     * Returns the difference of the actual and the passed number (wich has to
     * be of the type DoubleWrapper too).
     * @param subtrahend the number that should be subtracted from this number.
     * @return the difference of the actual number and the passed number.
     */
    @Override
    public NumberWrapper sub(NumberWrapper subtrahend) {
	DoubleWrapper ds = (DoubleWrapper)subtrahend;
	return new DoubleWrapper(value - ds.value);
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
     * Returns a string representation of this double value.
     * @return the string representation of this double value.
     */
    @Override
    public String toString(){
	return Double.toString(value);
    }

    @Override
    public String toTexString() {
	return toString();
    }
}
