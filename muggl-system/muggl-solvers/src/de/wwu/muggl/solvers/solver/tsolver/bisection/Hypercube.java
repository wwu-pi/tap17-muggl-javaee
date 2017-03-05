package de.wwu.muggl.solvers.solver.tsolver.bisection;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Hypercube {

    protected Interval[] intervals;

    protected MultiIndexVariablesReference variablesRef;

    public Hypercube(MultiIndexVariablesReference varRef){
	this.variablesRef = varRef;
	this.intervals = new Interval[variablesRef.getDimension()];
    }

    public NumberWrapper[] getCenter(){
	NumberWrapper[] result = new NumberWrapper[intervals.length];
	NumberWrapper two = intervals[0].start.getFactory().getTwo();
	for (int i = 0; i < intervals.length; i++){
	    Interval interval = intervals[i];
	    result[i] = interval.start.div(two).add(interval.end.div(two));
	    if (variablesRef.getVariable(i).isInteger()){
		result[i] = result[i].floor();
		if (result[i].lessThan(interval.getStart())){
		    result[i] = result[i].inc();
		    if (result[i].greaterThan(interval.getEnd()))
			return null;
		}
	    }
	}
	return result;
    }

    public int getDimension(){
	return variablesRef.getDimension();
    }

    public Interval getInterval(int index){
	return intervals[index];
    }

    public Interval getInterval(NumericVariable var){
	return intervals[variablesRef.getIndex(var)];
    }

    public NumericVariable getVariable(int index){
	return variablesRef.getVariable(index);
    }

    public void setEnd(NumericVariable var, NumberWrapper value){
	intervals[variablesRef.getIndex(var)].setEnd(value);
    }

    public void setInterval(NumericVariable var, Interval interval){
	intervals[variablesRef.getIndex(var)] = interval;
    }

    public void setStart(NumericVariable var, NumberWrapper value){
	intervals[variablesRef.getIndex(var)].setStart(value);
    }

    @Override
    public String toString(){
	StringBuffer sb = new StringBuffer();
	sb.append("{");
	if (getDimension() > 0){
	    sb.append(intervals[0].start);
	    sb.append("<=");
	    sb.append(variablesRef.getVariable(0));
	    sb.append("<=");
	    sb.append(intervals[0].end);
	}
	for (int i = 1; i < getDimension(); i++){
	    sb.append(", ");
	    sb.append(intervals[i].start);
	    sb.append("<=");
	    sb.append(variablesRef.getVariable(i));
	    sb.append("<=");
	    sb.append(intervals[i].end);
	}
	sb.append("}");
	return sb.toString();
    }

    public Hypercube[] divide(NumberWrapper epsilon){
	int maxIndex = 0;
	NumberWrapper maxWidth = intervals[0].getWidth();
	int maxFloatIndex = -1;
	NumberWrapper maxFloatWidth;
	if (variablesRef.getVariable(0).isInteger()){
	    maxFloatWidth = epsilon.getFactory().getInstance(0d);
	} else {
	    maxFloatWidth = maxWidth;
	    maxFloatIndex = 0;
	}
	for (int i = 1; i < intervals.length; i++){
	    NumberWrapper currentWidth = intervals[i].getWidth();
	    if (currentWidth.greaterThan(maxWidth)){
		maxWidth = currentWidth;
		maxIndex = i;
	    }
	    if (!variablesRef.getVariable(i).isInteger()){
		if (currentWidth.greaterThan(maxFloatWidth)){
		    maxFloatWidth = currentWidth;
		    maxFloatIndex = i;
		}
	    }
	}
	int splitIndex = maxIndex;
	if (variablesRef.getVariable(maxIndex).isInteger() && maxWidth.isLessThanOne()){
	    splitIndex = maxFloatIndex;
	}
	if (splitIndex == -1 || (!variablesRef.getVariable(splitIndex).isInteger() && maxFloatWidth.lessThan(epsilon)))
	    return null;
	Interval[] newIntervals = intervals[splitIndex].divide(variablesRef.getVariable(splitIndex).isInteger());
	Hypercube[] result = new Hypercube[newIntervals.length];
	for (int i = 0; i < result.length; i++){
	    result[i] = new Hypercube(variablesRef);
	    for (int j = 0; j < getDimension(); j++)
		if (j == splitIndex)
		    result[i].intervals[j] = newIntervals[i];
		else
		    result[i].intervals[j] = intervals[j];
	}
	return result;
    }
}
