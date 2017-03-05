package de.wwu.muggl.solvers.solver.tsolver.bisection;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class MultiIndexMap{

    protected TreeMap<MultiIndex, NumberWrapper> map;

    protected MultiIndexVariablesReference variablesRef;

    protected NumberFactory factory;

    public MultiIndexMap(NumberFactory factory){
	this.factory = factory;
	this.map = new TreeMap<MultiIndex, NumberWrapper>();
	variablesRef = new MultiIndexVariablesReference();
    }

    private MultiIndexMap(NumberFactory factory,
	    MultiIndexVariablesReference varRef1, MultiIndexVariablesReference varRef2){
	this.factory = factory;
	this.map = new TreeMap<MultiIndex, NumberWrapper>();
	if (varRef1.equals(varRef2))
	    this.variablesRef = varRef1;
	else
	    this.variablesRef = varRef1.join(varRef2);
    }

    private MultiIndexMap(NumberFactory factory,
	    MultiIndexVariablesReference varRef, NumericVariable var){
	this.factory = factory;
	this.map = new TreeMap<MultiIndex, NumberWrapper>();
	if (varRef.getIndex(var) == -1){
	    this.variablesRef = varRef.join(var);
	} else
	    this.variablesRef = varRef;
    }

    public MultiIndexMap(NumberFactory factory,
	    MultiIndexVariablesReference varRef,
	    TreeMap<MultiIndex, NumberWrapper> map){
	this.factory = factory;
	this.variablesRef = varRef;
	this.map = new TreeMap<MultiIndex, NumberWrapper>();
	this.map.putAll(map);
    }

    public MultiIndexMap(NumberFactory factory, NumberWrapper value){
	this(factory);
	map.put(new MultiIndex(), value);
    }

    public MultiIndexMap add(MultiIndexMap map2){
	return addOrSub(true, map2);
    }

    private void addOrSub(boolean add, MultiIndex index, NumberWrapper value){
	if (value.isZero())
	    return;
	NumberWrapper old = map.get(index);
	if (old != null){
	    if (add)
		old = old.add(value);
	    else
		old = old.sub(value);
	    if (old.isZero())
		map.remove(index);
	    else
		map.put(index, old);
	} else
	    map.put(index, value);
    }

    private MultiIndexMap addOrSub(boolean add, MultiIndexMap map2){
	MultiIndexMap result = new MultiIndexMap(factory, variablesRef,
		map2.variablesRef);
	for (MultiIndex index : map.keySet()){
	    NumberWrapper coeff = map.get(index);
	    int[] newIndex = new int[result.getDimension()];
	    for (int i = 0; i < this.getDimension(); i++)
		newIndex[result.variablesRef.getIndex(variablesRef.getVariable(i))] = index
		.getValue(i);
	    result.addOrSub(true, new MultiIndex(newIndex), coeff);
	}
	for (MultiIndex index : map2.map.keySet()){
	    NumberWrapper coeff = map2.map.get(index);
	    int[] newIndex = new int[result.getDimension()];
	    for (int i = 0; i < map2.getDimension(); i++)
		newIndex[result.variablesRef.getIndex(map2.variablesRef.getVariable(i))] = index
		.getValue(i);
	    result.addOrSub(add, new MultiIndex(newIndex), coeff);
	}
	return result;
    }

    public boolean containsInfiniteValues(){
	for (NumberWrapper number : map.values()){
	    if (number.isInfinite())
		return true;
	}
	return false;
    }

    public NumberWrapper get(MultiIndex index){
	return map.get(index);
    }

    public MultiIndexMap getBernsteinCoefficientsDirect(){
	int dim = getDimension();
	MultiIndex degree = getDegree();
	int[] outerCounter = new int[dim];
	int[] innerCounter = new int[dim];
	TreeMap<MultiIndex, NumberWrapper> resultMap = new TreeMap<MultiIndex, NumberWrapper>();
	do{
	    int[] currentIndex = new int[dim];
	    System.arraycopy(outerCounter, 0, currentIndex, 0, dim);
	    MultiIndex mIndex = new MultiIndex(currentIndex);
	    Arrays.fill(innerCounter, 0);
	    NumberWrapper coeff = null;
	    do{
		NumberWrapper addend = map.get(new MultiIndex(innerCounter));
		if (addend != null){
		    NumberWrapper factor = factory.getBinomial(currentIndex[0],
			    innerCounter[0]);
		    for (int i = 1; i < dim; i++)
			factor = factor.mult(factory.getBinomial(currentIndex[i],
				innerCounter[i]));
		    for (int i = 0; i < dim; i++)
			factor = factor.div(factory.getBinomial(degree.getValue(i),
				innerCounter[i]));
		    if (coeff == null)
			coeff = factor.mult(addend);
		    else
			coeff = coeff.add(factor.mult(addend));
		}
	    } while (incCounter(innerCounter, mIndex));
	    if (coeff != null)
		resultMap.put(mIndex, coeff);
	} while (incCounter(outerCounter, degree));
	return new MultiIndexMap(factory, variablesRef, resultMap);
    }

    public MultiIndex getDegree(){
	int dim = getDimension();
	int[] result = new int[dim];
	for (MultiIndex index : map.keySet()){
	    for (int i = 0; i < dim; i++){
		int exp = index.getValue(i);
		if (exp > result[i])
		    result[i] = exp;
	    }
	}
	return new MultiIndex(result);
    }

    public int getDimension(){
	return variablesRef.getDimension();
    }

    public NumberWrapper[] getMinAndMaxValue(){
	NumberWrapper[] result = new NumberWrapper[2];
	if (map.size() == 0){
	    result[0] = factory.getZero();
	    result[1] = result[0];
	} else
	    for (NumberWrapper value : map.values()){
		if (result[0] == null || result[0].greaterThan(value))
		    result[0] = value;
		if (result[1] == null || result[1].lessThan(value))
		    result[1] = value;
	    }
	return result;
    }

    public NumericVariable getVariable(int index){
	return variablesRef.getVariable(index);
    }

    public boolean incCounter(int[] counter, MultiIndex bounds){
	int pos = counter.length - 1;
	while (pos >= 0 && counter[pos] == bounds.getValue(pos)){
	    counter[pos] = 0;
	    pos--;
	}
	if (pos >= 0){
	    counter[pos]++;
	    return true;
	} else
	    return false;
    }

    public Set<MultiIndex> keySet(){
	return map.keySet();
    }

    public MultiIndexMap mult(MultiIndexMap map2){
	MultiIndexMap result = new MultiIndexMap(factory, variablesRef,
		map2.variablesRef);
	final int dim = result.getDimension();
	for (MultiIndex index1 : map.keySet()){
	    NumberWrapper coeff1 = map.get(index1);
	    for (MultiIndex index2 : map2.map.keySet()){
		NumberWrapper coeff = coeff1.mult(map2.map.get(index2));
		int[] newIndex = new int[dim];
		for (int i = 0; i < dim; i++){
		    NumericVariable var = result.variablesRef.getVariable(i);
		    int pos1 = this.variablesRef.getIndex(var);
		    int pos2 = map2.variablesRef.getIndex(var);
		    int exp = 0;
		    if (pos1 != -1)
			exp = index1.getValue(pos1);
		    if (pos2 != -1)
			exp += index2.getValue(pos2);
		    newIndex[i] = exp;
		}
		result.addOrSub(true, new MultiIndex(newIndex), coeff);
	    }
	}
	return result;
    }

    public MultiIndexMap mult(NumericVariable var, int exponent){
	MultiIndexMap result = new MultiIndexMap(factory, variablesRef, var);
	final int dim = result.getDimension();
	for (MultiIndex index : map.keySet()){
	    NumberWrapper coeff = map.get(index);
	    int[] newIndex = new int[dim];
	    for (int i = 0; i < index.getDimension(); i++)
		newIndex[result.variablesRef.getIndex(variablesRef.getVariable(i))] = index
		.getValue(i);
	    newIndex[result.variablesRef.getIndex(var)] += exponent;
	    result.map.put(new MultiIndex(newIndex), coeff);
	}
	return result;
    }

    public MultiIndexMap pow(int exponent){
	if (exponent == 1)
	    return this;
	MultiIndexMap result = null;
	MultiIndexMap square = this;
	if ((exponent & 1) != 0)
	    result = square;
	exponent /= 2;
	while (exponent > 0){
	    square = square.mult(square);
	    if ((exponent & 1) != 0){
		if (result == null)
		    result = square;
		else
		    result = result.mult(square);
	    }
	    exponent /= 2;
	}
	return result;
    }

    public void removeUnusedVariables(){
	boolean[] used = new boolean[getDimension()];
	for (MultiIndex index : map.keySet()){
	    for (int i = 0; i < used.length; i++){
		if (index.getValue(i) > 0)
		    used[i] = true;
	    }
	}
	for (int i = used.length - 1; i >= 0; i--){
	    if (!used[i]){
		this.variablesRef = variablesRef.remove(i);
		TreeMap<MultiIndex, NumberWrapper> newMap = new TreeMap<MultiIndex, NumberWrapper>();
		for (MultiIndex index : map.keySet()){
		    newMap.put(index.remove(i), map.get(index));
		}
		this.map = newMap;
	    }
	}
    }

    public MultiIndexMap scale(Hypercube cube){
	MultiIndexMap result = new MultiIndexMap(factory);
	for (MultiIndex oldIndex : keySet()){
	    MultiIndexMap translatedMonomial = new MultiIndexMap(factory, get(oldIndex));
	    for (int i = 0; i < oldIndex.getDimension(); i++){
		int exp = oldIndex.getValue(i);
		if (exp > 0){
		    NumericVariable var = getVariable(i);
		    Interval interval = cube.getInterval(var);
		    MultiIndexMap factor = null;
		    if (interval == null){
			translatedMonomial = translatedMonomial.mult(var, exp);
		    } else{
			TreeMap<MultiIndex, NumberWrapper> factorElements = new TreeMap<MultiIndex, NumberWrapper>();
			int[] tmp = new int[1];
			factorElements.put(new MultiIndex(tmp), interval.start);
			tmp = new int[1];
			tmp[0] = 1;
			factorElements.put(new MultiIndex(tmp), interval.getWidth());
			factor = new MultiIndexMap(factory,
				new MultiIndexVariablesReference(var), factorElements);
			factor = factor.pow(exp);
			translatedMonomial = translatedMonomial.mult(factor);
		    }
		}
	    }
	    result = result.add(translatedMonomial);
	}
	return result;
    }

    public MultiIndexMap sub(MultiIndexMap map2){
	return addOrSub(false, map2);
    }

    @Override
    public String toString(){
	return variablesRef.toString() + " " + map.toString();
    }
}
