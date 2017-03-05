package de.wwu.muggl.solvers.expressions;

/**
 * @author Christoph Lembeck
 */
public class LongConstant extends NumericConstant implements Comparable<Constant>{

    /**
     * The constant representing the long value 4.
     */
    public static final LongConstant FOUR = new LongConstant(4);

    /**
     * The constant representing the long value 1.
     */
    public static final LongConstant ONE = new LongConstant(1);

    /**
     * The constant representing the long value 3.
     */
    public static final LongConstant THREE = new LongConstant(3);

    /**
     * The constant representing the long value 2.
     */
    public static final LongConstant TWO = new LongConstant(2);

    /**
     * The constant representing the long value 0.
     */
    public static final LongConstant ZERO = new LongConstant(0);

    /**
     * Creates a new LongConstant object representing the passed long value.
     * @param value the number that should be represented by this object.
     * @return the new LongConstant object representing the passed number.
     */
    public static LongConstant getInstance(long value){
	if (value == 0)
	    return ZERO;
	if (value == 1)
	    return ONE;
	if (value == 2)
	    return TWO;
	if (value == 3)
	    return THREE;
	if (value == 4)
	    return FOUR;
	return new LongConstant(value);
    }

    /**
     * Stores the internal long value of this constant.
     */
    protected long value;

    /**
     * Creates a new LongConstant object representing the passed long value.
     * This constructor should only be called using the static method
     * getInstance to avoid multiple generation of constants representing the same
     * values.
     * @param value the number that should be represented by this constant object.
     * @see #getInstance(long)
     */
    protected LongConstant(long value){
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
	if ((addend instanceof DoubleConstant) || (addend instanceof FloatConstant))
	    return addend.add(this);
	else
	    return getInstance(this.value + addend.getLongValue());
    }

    @Override
    public NumericConstant and(NumericConstant constant){
	if (constant instanceof LongConstant || constant instanceof IntConstant)
	    return LongConstant.getInstance(value & constant.getLongValue());
	throw new InternalError("& can not be applied to long and " + Term.getTypeName(constant.getType()));
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
	    return IntConstant.getInstance((int)value);
	case Expression.LONG:
	    return this;
	case Expression.FLOAT:
	    return FloatConstant.getInstance(value);
	case Expression.DOUBLE:
	    return DoubleConstant.getInstance(value);
	default:
	    throw new IllegalArgumentException("unkown type");
	}
    }
    
    @Override
    public int compareTo(Constant obj) {
	if (obj instanceof LongConstant){
	    return new Long(value).compareTo(new Long(((LongConstant)obj).value));
	} else
	    throw new ClassCastException();
    }

    @Override
    public NumericConstant divide(NumericConstant divisor) {
	if (divisor instanceof DoubleConstant)
	    return DoubleConstant.getInstance(getDoubleValue() / divisor.getDoubleValue());
	if (divisor instanceof FloatConstant)
	    return FloatConstant.getInstance(getFloatValue() / divisor.getFloatValue());
	else
	    return getInstance(this.value / divisor.getLongValue());
    }

    @Override
    public boolean equals(Object obj){
	return (obj == this) || ((obj instanceof LongConstant) && (((LongConstant)obj).value == value));
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
	return (int)value;
    }

    @Override
    public long getLongValue() {
	return value;
    }

    /**
     * Returns the type of the actual expression. Here it will be
     * <i>Expression.LONG</i>.
     * @return <i>Expression.LONG</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#LONG
     */
    @Override
    public byte getType() {
	return Expression.LONG;
    }

    /**
     * Returns the internal stored long value of this constant.
     * @return the internal stored long value of this constant.
     */
    public long getValue(){
	return value;
    }

    @Override
    public int hashCode(){
	return (int)value;
    }

    @Override
    public boolean isEqualTo(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value == arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value == arg.getFloatValue();
	return value == arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterOrEqual(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value >= arg.getLongValue();
	    if (arg instanceof FloatConstant)
		return value >= arg.getFloatValue();
		return value >= arg.getDoubleValue();
    }

    @Override
    public boolean isGreaterThan(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value > arg.getLongValue();
	    if (arg instanceof FloatConstant)
		return value > arg.getFloatValue();
		return value > arg.getDoubleValue();
    }

    /**
     * Returns <code>true</code> because long values are always integer.
     * @return <code>true</code>.
     */
    @Override
    public boolean isInteger(){
	return true;
    }

    @Override
    public boolean isLesserOrEqual(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value <= arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value <= arg.getFloatValue();
	return value <= arg.getDoubleValue();
    }

    @Override
    public boolean isLesserThan(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value < arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value < arg.getFloatValue();
	return value < arg.getDoubleValue();
    }

    @Override
    public boolean isMinusOne() {
	return value == -1l;
    }

    @Override
    public boolean isNegative() {
	return value < 0l;
    }

    @Override
    public boolean isNegativeOrZero() {
	return value <= 0l;
    }

    @Override
    public boolean isNotEqualTo(NumericConstant arg){
	if (arg instanceof IntConstant || arg instanceof LongConstant)
	    return value != arg.getLongValue();
	if (arg instanceof FloatConstant)
	    return value != arg.getFloatValue();
	return value != arg.getDoubleValue();
    }

    @Override
    public boolean isOne() {
	return value == 1l;
    }

    @Override
    public boolean isPositive() {
	return value > 0l;
    }

    @Override
    public boolean isPositiveOrZero() {
	return value >= 0l;
    }

    @Override
    public boolean isZero() {
	return value == 0l;
    }

    @Override
    public NumericConstant modulo(NumericConstant divisor){
	if (divisor instanceof LongConstant || divisor instanceof IntConstant)
	    return LongConstant.getInstance(value % divisor.getLongValue());
	if (divisor instanceof FloatConstant)
	    return FloatConstant.getInstance(value % divisor.getFloatValue());
	if (divisor instanceof DoubleConstant)
	    return DoubleConstant.getInstance(value % divisor.getDoubleValue());
	return null;
    }

    @Override
    public NumericConstant multiply(NumericConstant factor) {
	if ((factor instanceof DoubleConstant) || (factor instanceof FloatConstant))
	    return factor.multiply(this);
	else
	    return getInstance(this.value * factor.getLongValue());
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
	if (constant instanceof LongConstant || constant instanceof IntConstant)
	    return LongConstant.getInstance(value | constant.getLongValue());
	throw new InternalError("| can not be applied to long and " + Term.getTypeName(constant.getType()));
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
	if (subtrahend instanceof DoubleConstant)
	    return DoubleConstant.getInstance(getDoubleValue() - subtrahend.getDoubleValue());
	if (subtrahend instanceof FloatConstant)
	    return FloatConstant.getInstance(getFloatValue() - subtrahend.getFloatValue());
	else
	    return getInstance(this.value - subtrahend.getLongValue());
    }

    @Override
    public String toHaskellString(){
	return "(Constant (LongConst " + value + "))";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return Long.toString(value);
    }

    @Override
    public NumericConstant xor(NumericConstant constant){
	if (constant instanceof LongConstant || constant instanceof IntConstant)
	    return LongConstant.getInstance(value ^ constant.getLongValue());
	throw new InternalError("^ can not be applied to long and " + Term.getTypeName(constant.getType()));
    }
}
