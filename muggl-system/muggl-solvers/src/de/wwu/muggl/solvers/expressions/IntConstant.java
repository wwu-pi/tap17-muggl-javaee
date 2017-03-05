package de.wwu.muggl.solvers.expressions;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class IntConstant extends NumericConstant implements Comparable{

    /**
     * The constant representing the int value 5.
     */
    public static final IntConstant FIVE = new IntConstant(5);

    /**
     * The constant representing the int value 4.
     */
    public static final IntConstant FOUR = new IntConstant(4);

    /**
     * The constant representing the int value -1.
     */
    public static final IntConstant MINUSONE = new IntConstant(-1);

    /**
     * The constant representing the int value 1.
     */
    public static final IntConstant ONE = new IntConstant(1);

    /**
     * The constant representing the int value 3.
     */
    public static final IntConstant THREE = new IntConstant(3);

    /**
     * The constant representing the int value 2.
     */
    public static final IntConstant TWO = new IntConstant(2);

    /**
     * The constant representing the int value 0.
     */
    public static final IntConstant ZERO = new IntConstant(0);

    /**
     * Creates a new IntConstant object representing the passed int value.
     * @param value the number that should be represented by this object.
     * @return the new IntConstant object representing the passed number.
     */
    static public IntConstant getInstance(int value){
	switch (value){
	case 0: return ZERO;
	case 1: return ONE;
	case 2: return TWO;
	case 3: return THREE;
	case 4: return FOUR;
	default: return new IntConstant(value);
	}
    }

    /**
     * Stores the internal int value of this constant.
     */
    protected int value;

    /**
     * Creates a new IntConstant object representing the passed int value.
     * This constructor should only be called using the static method
     * getInstance to avoid multiple generation of constants representing the same
     * values.
     * @param value the number that should be represented by this constant object.
     * @see #getInstance(int)
     */
    protected IntConstant(int value){
	this.value = value;
    }

    @Override
    public NumericConstant abs(){
	if (value < 0)
	    return getInstance(-value);
	return this;
    }

    @Override
    public NumericConstant add(NumericConstant addend) {
	if ((addend instanceof FloatConstant) || (addend instanceof LongConstant) || (addend instanceof DoubleConstant))
	    return addend.add(this);
	else
	    return getInstance(this.value + addend.getIntValue());
    }

    @Override
    public NumericConstant and(NumericConstant constant){
	if (constant instanceof IntConstant)
	    return IntConstant.getInstance(value & constant.getIntValue());
	if (constant instanceof LongConstant)
	    return LongConstant.getInstance(getLongValue() & constant.getLongValue());
	throw new InternalError("& can not be applied to int and " + Term.getTypeName(constant.getType()));
    }

    @Override
    public NumericConstant arithmeticShiftRight(int range) {
	return getInstance(value >> range);
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
	    return this;
	case Expression.LONG:
	    return LongConstant.getInstance(value);
	case Expression.FLOAT:
	    return FloatConstant.getInstance(value);
	case Expression.DOUBLE:
	    return DoubleConstant.getInstance(value);
	default:
	    throw new IllegalArgumentException("unkown type");
	}
    }

    /**
     * Compares the internal stored value of this IntConstant with the passed
     * object.
     * @param obj the object this constant should be compared with.
     * @return a negative number if the internally stored value of this constant
     * is smaller than the stored value inside the passed object, zero if both
     * values are equal and a positive number if this number is greater than the
     * value of the passed constant.
     */
    @Override
    public int compareTo(Object obj) {
	if (obj instanceof IntConstant){
	    return new Integer(value).compareTo(new Integer(((IntConstant)obj).value));
	} else
	    throw new ClassCastException();
    }

    @Override
    public NumericConstant divide(NumericConstant divisor) {
	if (divisor instanceof FloatConstant)
	    return FloatConstant.getInstance(getFloatValue() / divisor.getFloatValue());
	if (divisor instanceof LongConstant)
	    return LongConstant.getInstance(getLongValue() / divisor.getLongValue());
	if (divisor instanceof DoubleConstant)
	    return DoubleConstant.getInstance(getDoubleValue() / divisor.getDoubleValue());
	else
	    return getInstance(this.value / divisor.getIntValue());
    }

    /**
     * Checks whether the passed object is equal to this constant.
     * @param obj the other object this constant should be compared to.
     * @return <i>true</i> if the passed object is a IntConstant too and
     * represents the same number as this constant.
     */
    @Override
    public boolean equals(Object obj){
	return (obj == this) || ((obj instanceof IntConstant) && (((IntConstant)obj).value == value));
    }

    @Override
    public double getDoubleValue() {
	return value;
    }

    @Override
    public float getFloatValue() {
	return value;
    }

    @Override
    public int getIntValue(){
	return value;
    }

    @Override
    public long getLongValue() {
	return value;
    }

    /**
     * Returns the type of the actual expression. Here it will be
     * <i>Expression.INT</i>.
     * @return <i>Expression.INT</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#INT
     */
    @Override
    public byte getType() {
	return Expression.INT;
    }

    /**
     * Returns the internal stored int value of this constant.
     * @return the internal stored int value of this constant.
     */
    public int getValue(){
	return value;
    }

    @Override
    public int hashCode(){
	return value;
    }

    @Override
    public boolean isEqualTo(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value == arg.getIntValue();
	if (arg instanceof LongConstant)
	    return value == arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value == arg.getFloatValue();
	return value == arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterOrEqual(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value >= arg.getIntValue();
	    if (arg instanceof LongConstant)
		return value >= arg.getLongValue();
		if (arg instanceof FloatConstant)
		    return value >= arg.getFloatValue();
		    return value >= arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterThan(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value > arg.getIntValue();
	    if (arg instanceof LongConstant)
		return value > arg.getLongValue();
		if (arg instanceof FloatConstant)
		    return value > arg.getFloatValue();
		    return value > arg.getDoubleValue();
    }

    /**
     * Returns <code>true</code> because int values are always integer.
     * @return <code>true</code>.
     */
    @Override
    public boolean isInteger(){
	return true;
    }

    @Override
    public boolean isLesserOrEqual(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value <= arg.getIntValue();
	if (arg instanceof LongConstant)
	    return value <= arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value <= arg.getFloatValue();
	return value <= arg.getDoubleValue();
    }

    /**
     * Checks whether the constant is lesser than the passed argument.
     * @param arg the constant this constant should be compared to.
     * @return <i>true</i> if this constant is lesser than the passed argument,
     * <i>false</i> otherwise.
     */
    @Override
    public boolean isLesserThan(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value < arg.getIntValue();
	if (arg instanceof LongConstant)
	    return value < arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value < arg.getFloatValue();
	return value < arg.getDoubleValue();
    }

    @Override
    public boolean isMinusOne() {
	return value == -1;
    }

    @Override
    public boolean isNegative() {
	return value < 0;
    }

    @Override
    public boolean isNegativeOrZero() {
	return value <= 0;
    }

    @Override
    public boolean isNotEqualTo(NumericConstant arg){
	if (arg instanceof IntConstant)
	    return value != arg.getIntValue();
	if (arg instanceof LongConstant)
	    return value != arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value != arg.getFloatValue();
	return value != arg.getDoubleValue();
    }

    @Override
    public boolean isOne() {
	return value == 1;
    }

    @Override
    public boolean isPositive() {
	return value > 0;
    }

    @Override
    public boolean isPositiveOrZero() {
	return value >= 0;
    }
    
    @Override
    public boolean isZero() {
	return value == 0;
    }

    @Override
    public NumericConstant modulo(NumericConstant divisor){
	if (divisor instanceof IntConstant)
	    return (IntConstant.getInstance(value % divisor.getIntValue()));
	if (divisor instanceof LongConstant)
	    return (LongConstant.getInstance(value % divisor.getLongValue()));
	if (divisor instanceof FloatConstant)
	    return (FloatConstant.getInstance(value % divisor.getFloatValue()));
	if (divisor instanceof DoubleConstant)
	    return (DoubleConstant.getInstance(value % divisor.getDoubleValue()));
	return null;
    }

    @Override
    public NumericConstant multiply(NumericConstant factor) {
	if ((factor instanceof FloatConstant) || (factor instanceof LongConstant) || (factor instanceof DoubleConstant))
	    return factor.multiply(this);
	else
	    return getInstance(this.value * factor.getIntValue());
    }

    @Override
    public NumericConstant negate() {
	return getInstance(-value);
    }

    @Override
    public NumericConstant next() {
	return getInstance(value + 1);
    }

    @Override
    public NumericConstant or(NumericConstant constant){
	if (constant instanceof IntConstant)
	    return IntConstant.getInstance(value | constant.getIntValue());
	if (constant instanceof LongConstant)
	    return LongConstant.getInstance(getLongValue() | constant.getLongValue());
	throw new InternalError("| can not be applied to int and " + Term.getTypeName(constant.getType()));
    }

    @Override
    public NumericConstant pred() {
	return getInstance(value - 1);
    }

    @Override
    public NumericConstant shiftLeft(int range) {
	return getInstance(value << range);
    }

    @Override
    public NumericConstant subtract(NumericConstant subtrahend) {
	if (subtrahend instanceof FloatConstant)
	    return FloatConstant.getInstance(getFloatValue() - subtrahend.getFloatValue());
	if (subtrahend instanceof LongConstant)
	    return LongConstant.getInstance(getLongValue() - subtrahend.getLongValue());
	if (subtrahend instanceof DoubleConstant)
	    return DoubleConstant.getInstance(getDoubleValue() - subtrahend.getDoubleValue());
	else
	    return getInstance(this.value - subtrahend.getIntValue());
    }

    @Override
    public String toHaskellString(){
	return "(Constant (IntConst " + value + "))";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return Integer.toString(value);
    }

    @Override
    public NumericConstant xor(NumericConstant constant){
	if (constant instanceof IntConstant)
	    return IntConstant.getInstance(value ^ constant.getIntValue());
	if (constant instanceof LongConstant)
	    return LongConstant.getInstance(getLongValue() ^ constant.getLongValue());
	throw new InternalError("^ can not be applied to int and " + Term.getTypeName(constant.getType()));
    }
}
