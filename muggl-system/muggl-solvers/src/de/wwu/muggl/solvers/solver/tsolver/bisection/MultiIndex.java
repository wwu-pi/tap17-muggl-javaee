package de.wwu.muggl.solvers.solver.tsolver.bisection;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class MultiIndex implements Comparable{

    protected int[] index;

    public MultiIndex(){
	this.index = new int[0];
    }

    public MultiIndex(int[] index){
	this.index = index;
    }

    public int compareTo(Object o){
	MultiIndex other = (MultiIndex)o;
	if (other.index.length != index.length)
	    return index.length - other.index.length;
	for (int i = 0; i < index.length; i++){
	    if (other.index[i] != index[i])
		return index[i] - other.index[i];
	}
	return 0;
    }

    @Override
    public boolean equals(Object o){
	if (!(o instanceof MultiIndex))
	    return false;
	MultiIndex other = (MultiIndex)o;
	if (other.index.length != index.length)
	    return false;
	for (int i = 0; i < index.length; i++)
	    if (other.index[i] != index[i])
		return false;
	return true;
    }

    public int getDimension(){
	return index.length;
    }

    public int getValue(int pos){
	return index[pos];
    }

    @Override
    public int hashCode(){
	int result = index.length;
	for (int i = 0; i < index.length; i++)
	    result += (i+1) * index[i];
	return result;
    }

    public MultiIndex remove(int idx){
	int[] newIndex = new int[index.length - 1];
	System.arraycopy(index, 0, newIndex, 0, idx);
	System.arraycopy(index, idx+1, newIndex, idx, newIndex.length - idx);
	return new MultiIndex(newIndex);
    }

    @Override
    public String toString(){
	StringBuffer sb = new StringBuffer("(");
	if (index.length > 0)
	    sb.append(Integer.toString(index[0]));
	for (int i = 1; i < index.length; i++){
	    sb.append(",");
	    sb.append(Integer.toString(index[i]));
	}
	sb.append(")");
	return sb.toString();
    }
}
