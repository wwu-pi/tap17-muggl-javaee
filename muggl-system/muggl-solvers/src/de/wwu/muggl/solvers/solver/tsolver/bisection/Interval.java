package de.wwu.muggl.solvers.solver.tsolver.bisection;

import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph Lembeck
 */
public class Interval {

    public NumberWrapper start;

    public NumberWrapper end;

    public Interval(NumberWrapper start, NumberWrapper end){
	this.start = start;
	this.end = end;
    }

    public NumberWrapper getStart(){
	return start;
    }

    public void setStart(NumberWrapper newStart){
	this.start = newStart;
    }

    public void setEnd(NumberWrapper newEnd){
	this.end = newEnd;
    }

    public NumberWrapper getEnd(){
	return end;
    }

    @Override
    public String toString(){
	return "[" + start + "; " + end + "]";
    }

    public NumberWrapper getWidth(){
	return end.sub(start);
    }

    public Interval[] divide(boolean isInteger){
	NumberWrapper divisor = end.getFactory().getInstance(3);
	if (isInteger){
	    end = end.floor();
	    start = start.ceil();
	    if (start.equals(end)){
		Interval[] result = {new Interval(start, start)};
		return result;
	    }
	    NumberWrapper dist = end.sub(start);
	    if (dist.equals(dist.getFactory().getOne())){
		Interval[] result = {new Interval(start, start)/*, new Interval(end, end)*/};
		return result;
	    }
	    if (dist.equals(dist.getFactory().getTwo())){
		NumberWrapper middle = start.add(start.getFactory().getOne());
		Interval[] result = {new Interval(start, start), new Interval(middle, middle)/*, new Interval(end, end)*/};
		return result;
	    }
	}
	NumberWrapper stepSize = end.div(divisor).sub(start.div(divisor));
	NumberWrapper border1 = start.add(stepSize);
	NumberWrapper border2 = end.sub(stepSize);
	if (isInteger){
	    border1 = border1.ceil();
	    border2 = border2.floor();
	}
	Interval[] result = new Interval[3];
	result[0] = new Interval(start, border1);
	result[1] = new Interval(border2, end);
	result[2] = new Interval(border1, border2);
	return result;
    }
}
