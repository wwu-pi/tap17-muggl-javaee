package de.wwu.muggl.solvers.solver.numbers;

import de.wwu.muggl.solvers.expressions.NumericConstant;

/**
 * @author Marko Ernsting
 *
 */
public class DeltaWrapper extends NumberFactory implements NumberWrapper {
    protected NumberWrapper c;
    protected NumberWrapper k;
    protected boolean positiveInfinite;
    protected boolean negativeInfinite;
    protected boolean infinite;    
    
    public enum Delta {
	    ZERO, POSITIVE_INFINITY, NEGATIVE_INFINITY 
    }
    
    // remove delta.
    static protected NumberWrapper delta;
    
    public NumberWrapper getC() {return c;}
    public NumberWrapper getK() {return k;}
    
    public static void setDelta(NumberWrapper delta) {DeltaWrapper.delta = delta;}
    public static NumberWrapper getDelta() {return delta;}
    
    
    private NumberWrapper value(){
	//Return NaN. Remove delta.
	return c.add(k.mult(delta));
    }
    
    public NumberWrapper getValue(NumberWrapper delta){
	return c.add(k.mult(delta));
    }
    
    public DeltaWrapper() {}
    
    public DeltaWrapper(Delta delta){
	infinite = (delta != Delta.ZERO);
	positiveInfinite = (delta == Delta.POSITIVE_INFINITY);
	negativeInfinite = (delta == Delta.NEGATIVE_INFINITY);
    }

    public DeltaWrapper(NumberWrapper value){
	infinite = false;
	positiveInfinite = false;
	negativeInfinite = false;
	c = value;
	k = value.getFactory().getZero();
    }
    
    public DeltaWrapper(NumberWrapper c, NumberWrapper k){
	infinite = false;
	positiveInfinite = false;
	negativeInfinite = false;	
	this.c = c;
	this.k = k;
    }

    @Override
    public DeltaWrapper add(NumberWrapper addend) {
	if (addend instanceof DeltaWrapper){
	    return new DeltaWrapper(c.add( ((DeltaWrapper) addend).c), k.add( ((DeltaWrapper) addend).k) );
	} else {
	    return new DeltaWrapper( c.add(c), k );	    
	}
    }

    @Override
    public DeltaWrapper sub(NumberWrapper subtrahend) {
	if (subtrahend instanceof DeltaWrapper){
	    if (infinite || subtrahend.isInfinite()){
		throw new RuntimeException("Infinite Values");
	    }
	    return new DeltaWrapper( c.sub( ((DeltaWrapper)subtrahend).c ), k.sub(((DeltaWrapper)subtrahend).k));
	} else {
	    return new DeltaWrapper( c.sub(subtrahend), k );
	}
    }
    
    @Override
    public DeltaWrapper mult(NumberWrapper factor) { 
	return new DeltaWrapper( c.mult(factor), k.mult(factor) );
    }
    
    @Override
    public DeltaWrapper div(NumberWrapper divisor) {
	return new DeltaWrapper( c.div(divisor), k.div(divisor) );
	// return mult(divisor.getFactory().getOne().div(divisor));
    }
    
    @Override
    public boolean equals(NumberWrapper other) {
	if (other instanceof DeltaWrapper) {
	    if ( (this.positiveInfinite && ((DeltaWrapper)other).positiveInfinite) 
		 || (this.negativeInfinite && ((DeltaWrapper)other).negativeInfinite) )
		return true;
	    if ( (this.infinite ^ ((DeltaWrapper)other).infinite )
		 || (this.positiveInfinite && ((DeltaWrapper)other).negativeInfinite) 
		 || (this.negativeInfinite && ((DeltaWrapper)other).positiveInfinite) )
		return false;
	    return ( c.equals( ((DeltaWrapper) other).c ) && k.equals( ((DeltaWrapper) other).k ) );
	} else {
	    return c.equals(other) && k.isZero();
	}
    }

    @Override
    public boolean greaterOrEqual(NumberWrapper other) {
	if (other instanceof DeltaWrapper) {
	    if (positiveInfinite || ((DeltaWrapper)other).negativeInfinite ) return true;
	    if (negativeInfinite || ((DeltaWrapper)other).positiveInfinite ) return false;
	    return c.greaterThan( ((DeltaWrapper)other).c ) || ( c.equals( ((DeltaWrapper)other).c ) && k.greaterOrEqual( ((DeltaWrapper)other).k ) );
	} else {
	    if (negativeInfinite) return false;
	    return positiveInfinite || c.greaterThan(other) || ( c.equals(other) && k.greaterOrEqual(other.getFactory().getZero()));
	} 
    }

    @Override
    public boolean lessOrEqual(NumberWrapper other) {
	if (other instanceof DeltaWrapper) {
	    if (positiveInfinite || ((DeltaWrapper)other).negativeInfinite ) return false;
	    if (negativeInfinite || ((DeltaWrapper)other).positiveInfinite ) return true;
	    return c.lessThan( ((DeltaWrapper)other).c ) || ( c.equals( ((DeltaWrapper)other).c ) && k.lessOrEqual( ((DeltaWrapper)other).k ) );
	} else {
	    if (negativeInfinite) return true;
	    if (positiveInfinite) return false;
	    return c.lessThan(other) || ( c.equals(other) && k.lessOrEqual(other.getFactory().getZero()));
	} 
    }

    @Override
    public boolean greaterThan(NumberWrapper other) {
	return ! lessOrEqual(other);
    }

    @Override
    public boolean lessThan(NumberWrapper other) {
	return ! greaterOrEqual(other);
    }
    
    @Override
    public boolean isLessOrEqualZero() {
	if (positiveInfinite) return false;
	if (negativeInfinite) return true;
	return c.isLessThanZero() || ( c.isZero() && k.isLessOrEqualZero() );
    }

    @Override
    public boolean isGreaterOrEqualZero() {
	if (positiveInfinite) return true;
	if (negativeInfinite) return false;
	return c.isGreaterThanZero() || ( c.isZero() && k.isGreaterOrEqualZero() );
    }
    
    @Override
    public boolean isLessThanZero() {
	return ! isGreaterOrEqualZero();
    }
    
    @Override
    public boolean isGreaterThanZero() {
	return ! isLessThanZero();
    }

    @Override
    public boolean isLessThanOne() {
	// bad because it uses getOne()
	return lessThan(getOne());
    }
    
    @Override
    public NumberWrapper abs() {
	return value().abs();
    }

    @Override
    public DeltaWrapper ceil() {
	return new DeltaWrapper( value().ceil() );
    }
    
    @Override
    public DeltaWrapper floor() {
	return new DeltaWrapper( value().floor() );
    }

    @Override
    public NumberWrapper div2() {
	return null;
    }

    @Override
    public double doubleValue() { 
	return value().doubleValue();
    }

    @Override
    public float floatValue() {
	return value().floatValue();
    }

    @Override
    public NumberWrapper gcd(NumberWrapper other) {
	return null;
    }

    @Override
    public NumberFactory getFactory() {
	return this;
    }

    @Override
    public NumberWrapper inc() {
	return null;
    }

    @Override
    public int intValue() {
	return value().intValue();
    }

    @Override
    public boolean isInfinite() {
	return infinite;
    }

    @Override
    public boolean isInteger() {
	return value().isInteger();
    }

    @Override
    public boolean isInvalid() {
	return false;
    }

    @Override
    public boolean isMinusOne() {
	return value().isMinusOne();
    }

    @Override
    public boolean isNegativeInfinity() {
	return negativeInfinite;
    }

    @Override
    public boolean isOne() {
	return value().isOne();
    }

    @Override
    public boolean isPositiveInfinity() {
	return positiveInfinite;
    }

    @Override
    public boolean isZero() {
	return value().isZero();
    }

    @Override
    public long longValue() {
	return value().longValue();
    }

    @Override
    public DeltaWrapper negate() {
	return new DeltaWrapper(c.negate(), k.negate());
    }

    @Override
    public NumberWrapper pow(int exponent) {
	return value().pow(exponent);
    }

    @Override
    public NumberWrapper square() {
	return value().square();
    }

    @Override
    public NumericConstant toNumericConstant(byte type) {
	return value().toNumericConstant(type);
    }

    @Override
    public String toTexString() {
	return "(" + c.toTexString() + "," + k.toTexString() + ")";
    }

    @Override
    public String toString() {
	if (infinite) {
	    if (positiveInfinite) return "oo";
	    else return "-oo";
 	}	
	return "("+c+", "+k+")";
    }
    
    @Override
    public DeltaWrapper getInstance(double value) {
	return new DeltaWrapper( c.getFactory().getInstance(value) );
    }

    @Override
    public NumberWrapper getInstance(NumericConstant value) {
	return new DeltaWrapper( c.getFactory().getInstance(value) );
    }

    @Override
    public NumberWrapper getMinusOne() {
	return new DeltaWrapper( c.getFactory().getMinusOne() );
    }

    @Override
    public NumberWrapper getOne() {
	return new DeltaWrapper( c.getFactory().getOne() );
    }

    @Override
    public NumberWrapper getTwo() {
	return new DeltaWrapper( c.getFactory().getTwo() );
    }

    @Override
    public NumberWrapper getZero() {
	return new DeltaWrapper( c.getFactory().getZero() );
    }

    @Override
    public NumberWrapper getBinomial(int n, int k) {
	return null;
    }
}
