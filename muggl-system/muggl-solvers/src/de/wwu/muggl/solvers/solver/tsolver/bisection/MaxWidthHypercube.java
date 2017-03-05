package de.wwu.muggl.solvers.solver.tsolver.bisection;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class MaxWidthHypercube extends Hypercube{

    protected NumberWrapper maxWidth;

    protected int maxIndex;

    private MaxWidthHypercube(MultiIndexVariablesReference varRef){
	super(varRef);
	maxIndex = -1;
	maxWidth = null;
    }

    @Override
    public void setStart(NumericVariable var, NumberWrapper value){
	int index = variablesRef.getIndex(var);
	NumberWrapper oldValue = intervals[index].start;
	super.setStart(var, value);
	if (index == maxIndex){
	    if (value.greaterThan(oldValue))
		recalculateMaximum();
	    else
		maxWidth = intervals[index].getWidth();
	} else{
	    NumberWrapper newWidth = intervals[index].getWidth();
	    if (newWidth.greaterThan(maxWidth)){
		maxWidth = newWidth;
		maxIndex = index;
	    }
	}
    }

    @Override
    public void setEnd(NumericVariable var, NumberWrapper value){
	int index = variablesRef.getIndex(var);
	NumberWrapper oldValue = intervals[index].end;
	super.setEnd(var, value);
	if (index == maxIndex){
	    if (value.lessThan(oldValue))
		recalculateMaximum();
	    else
		maxWidth = intervals[index].getWidth();
	} else {
	    NumberWrapper newWidth = intervals[index].getWidth();
	    if (newWidth.greaterThan(maxWidth)){
		maxWidth = newWidth;
		maxIndex = index;
	    }
	}
    }

    @Override
    public void setInterval(NumericVariable var, Interval interval){
	int index = variablesRef.getIndex(var);
	super.setInterval(var, interval);
	NumberWrapper newWidth = intervals[index].getWidth();
	if (maxIndex == -1){
	    maxIndex = index;
	    maxWidth = newWidth;
	    return;
	}
	if (index == maxIndex){
	    if (newWidth.greaterThan(maxWidth))
		maxWidth = newWidth;
	    else
		recalculateMaximum();
	} else {
	    if (newWidth.greaterThan(maxWidth)){
		maxWidth = newWidth;
		maxIndex = index;
	    }
	}
    }

    protected void recalculateMaximum(){
	maxWidth = intervals[0].getWidth();
	maxIndex = 0;
	for (int i = 1; i < intervals.length; i++){
	    NumberWrapper width = intervals[i].getWidth();
	    if (width.greaterThan(maxWidth)){
		maxWidth = width;
		maxIndex = i;
	    }
	}
    }

    public int getMaxWidthIndex(){
	return maxIndex;
    }

    public Interval getMaxWidthInterval(){
	return intervals[maxIndex];
    }

    @Override
    public MaxWidthHypercube[] divide(NumberWrapper epsilon){
	MaxWidthHypercube[] result = new MaxWidthHypercube[3];
	int indexToDivide = getMaxWidthIndex();
	Interval[] newIntervals = intervals[indexToDivide].divide(getVariable(indexToDivide).isInteger());
	for (int i = 0; i < 3; i++){
	    result[i] = new MaxWidthHypercube(variablesRef);
	    for (int j = 0; j < getDimension(); j++)
		if (j != indexToDivide)
		    result[i].intervals[j] = intervals[j];
		else
		    result[i].intervals[j] = newIntervals[i];
	    result[i].recalculateMaximum();
	}
	return result;
    }
}
