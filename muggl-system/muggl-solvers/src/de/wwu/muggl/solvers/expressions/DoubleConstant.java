package de.wwu.muggl.solvers.expressions;

/**
 * @author Christoph Lembeck
 */
public class DoubleConstant extends NumericConstant implements Comparable<NumericConstant>{

    /**
     * The constant representing the double value -1.
     */
    public static final DoubleConstant MINUSONE = new DoubleConstant(-1d);

    /**
     * The constant representing the double value 1.
     */
    public static final DoubleConstant ONE = new DoubleConstant(1d);

    /**
     * The constant representing the double value 1.
     */
    public static final DoubleConstant THREE = new DoubleConstant(3d);

    /**
     * The constant representing the double value 2.
     */
    public static final DoubleConstant TWO = new DoubleConstant(2d);

    /**
     * The constant representing the double value 0.
     */
    public static final DoubleConstant ZERO = new DoubleConstant(0d);

    /**
     * Creates a new DoubleConstant object representing the passed double value.
     * @param value the number that should be represented by this object.
     * @return the new DoubleConstant object representing the passed number.
     */
    public static DoubleConstant getInstance(double value){
	return new DoubleConstant(value);
    }

    /**
     * Stores the internal double value of this constant.
     */
    protected double value;

    /**
     * Creates a new DoubleConstant object representing the passed double value.
     * This constructor should only be called using the static method
     * getInstance to avoid multiple generation of constants representing the same
     * values.
     * @param value the number that should be represented by this constant object.
     * @see #getInstance(double)
     */
    protected DoubleConstant(double value){
	this.value = value;
    }


    @Override
    public NumericConstant abs(){
	if (value < 0)
	    return getInstance(-value);
	return this;
    }

    /**
     * Adds the passed constant to this constant.
     * @param addend the constant that should be added.
     * @return the sum of the two constants.
     */
    @Override
    public NumericConstant add(NumericConstant addend) {
	return getInstance(this.value + addend.getDoubleValue());
    }

    /**
     * Throws an InternalError because there is no conjunction operation for
     * floating point values defined in the java virtual machine specification.
     * @param constant unused.
     * @return nothing.
     * @throws InternalError
     */
    @Override
    public NumericConstant and(NumericConstant constant){
	throw new InternalError("& can not be applied to double and " + Term.getTypeName(constant.getType()));
    }

    @Override
    public NumericConstant arithmeticShiftRight(int range) {
	throw new InternalError("Shifting not supported on floating point values");
    }

    @Override
    public NumericConstant castTo(byte type) {
	switch (type){
	case Expression.BYTE:
	    return IntConstant.getInstance((byte)value);
	case Expression.CHAR:
	    return IntConstant.getInstance((char)value);
	case Expression.SHORT:
	    return IntConstant.getInstance((short)value);
	case Expression.INT:
	    return IntConstant.getInstance((int)value);
	case Expression.LONG:
	    return LongConstant.getInstance((long)value);
	case Expression.FLOAT:
	    return FloatConstant.getInstance((float)value);
	case Expression.DOUBLE:
	    return this;
	default:
	    throw new IllegalArgumentException("unkown type");
	}
    }

    /**
     * Compares the internal stored value of this DoubleConstant with the passed
     * object.
     * @param obj the object this constant should be compared with.
     * @return a negative number if the internally stored value of this constant
     * is smaller than the stored value inside the passed object, zero if both
     * values are equal and a positive number if this numbre is greater than the
     * value of the passed constant.
     */
    @Override
    public int compareTo(NumericConstant obj) {
	if (obj instanceof DoubleConstant){
	    return new Double(value).compareTo(new Double(((DoubleConstant)obj).value));
	} else
	    throw new ClassCastException();
    }

    @Override
    public NumericConstant divide(NumericConstant divisor) {
	return getInstance(this.value / divisor.getDoubleValue());
    }

    /**
     * Checks whether the passed object is equal to this constant.
     * @param obj the other object this constant should be compared to.
     * @return <i>true</i> if the passed object is a DoubleConstant too and
     * represents the same number as this constant.
     */
    @Override
    public boolean equals(Object obj){
	return (obj == this) || ((obj instanceof DoubleConstant) && (((DoubleConstant)obj).value == value));
    }

    @Override
    public double getDoubleValue(){
	return value;
    }

    @Override
    public float getFloatValue() {
	return (float)value;
    }

    @Override
    public int getIntValue() {
	return (int)value;
    }

    @Override
    public long getLongValue() {
	return (long)value;
    }

    /**
     * Returns the type of the actual expression. Here it will be
     * <i>Expression.DOUBLE</i>.
     * @return <i>Expression.DOUBLE</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#DOUBLE
     */
    @Override
    public byte getType() {
	return Expression.DOUBLE;
    }

    /**
     * Returns the internal stored double value of this constant.
     * @return the internal stored double value of this constant.
     */
    public double getValue(){
	return value;
    }
    
    @Override
    public int hashCode(){
	long v = Double.doubleToLongBits(value);
	return (int)(v^(v>>>32));
    }

    @Override
    public boolean isEqualTo(NumericConstant arg){
	return value == arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterOrEqual(NumericConstant arg){
	return value >= arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterThan(NumericConstant arg){
	return value > arg.getDoubleValue();
    }

    /**
     * Returns <code>false</code> because double values have no integer type.
     * @return <code>false</code>.
     */
    @Override
    public boolean isInteger(){
	return false;
    }

    @Override
    public boolean isLesserOrEqual(NumericConstant arg){
	return value <= arg.getDoubleValue();
    }

    @Override
    public boolean isLesserThan(NumericConstant arg){
	return value < arg.getDoubleValue();
    }

    @Override
    public boolean isMinusOne() {
	return value == -1d;
    }

    @Override
    public boolean isNegative() {
	return value < 0d;
    }

    @Override
    public boolean isNegativeOrZero() {
	return value <= 0d;
    }

    @Override
    public boolean isNotEqualTo(NumericConstant arg){
	return value != arg.getDoubleValue();
    }

    @Override
    public boolean isOne() {
	return value == 1d;
    }

    @Override
    public boolean isPositive() {
	return value > 0d;
    }

    @Override
    public boolean isPositiveOrZero() {
	return value >= 0d;
    }

    @Override
    public boolean isZero() {
	return (value == 0d) || (value == -0d);
    }

    @Override
    public NumericConstant modulo(NumericConstant divisor){
	return getInstance(value % divisor.getDoubleValue());
    }

    @Override
    public NumericConstant multiply(NumericConstant factor) {
	return getInstance(this.value * factor.getDoubleValue());
    }

    @Override
    public NumericConstant negate() {
	return getInstance(-value);
    }

    @Override
    public NumericConstant next() {
	return getInstance(Double.longBitsToDouble((Double.doubleToLongBits(value) + 1)));
    }

    /**
     * Throws an InternalError because there is no or operation for
     * floating point values defined in the java virtual machine specification.
     * @param constant unused.
     * @return nothing.
     * @throws InternalError
     */
    @Override
    public NumericConstant or(NumericConstant constant){
	throw new InternalError("| can not be applied to double and " + Term.getTypeName(constant.getType()));
    }

    @Override
    public NumericConstant pred() {
	return getInstance(Double.longBitsToDouble((Double.doubleToLongBits(value) - 1)));
    }

    @Override
    public NumericConstant shiftLeft(int range) {
	throw new InternalError("Shifting not supported on floating point values");
    }

    @Override
    public NumericConstant subtract(NumericConstant subtrahend) {
	return getInstance(this.value - subtrahend.getDoubleValue());
    }

    @Override
    public String toHaskellString(){
	return "(Constant (DoubleConst " + value + "))";
    }

    /**
     * Returns a string representation of the internally stored value of this
     * constant.
     */
    @Override
    public String toString(boolean useInternalVariableNames){
	return Double.toString(value);
    }


    /**
     * Throws an InternalError because there is no or operation for
     * floating point values defined in the java virtual machine specification.
     * @param constant unused.
     * @return nothing.
     * @throws InternalError
     */
    @Override
    public NumericConstant xor(NumericConstant constant){
	throw new InternalError("^ can not be applied to double and " + Term.getTypeName(constant.getType()));
    }

}
