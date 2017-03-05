package de.wwu.muggl.solvers.solver.tsolver.bisection;

import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class MultiIndexVariablesReference{

    protected NumericVariable[] indexToVariable;

    protected TreeMap<NumericVariable, Integer> variablesToIndex;

    public MultiIndexVariablesReference(){
	variablesToIndex = new TreeMap<NumericVariable, Integer>();
	indexToVariable = new NumericVariable[0];
    }

    public MultiIndexVariablesReference(NumericVariable var){
	variablesToIndex = new TreeMap<NumericVariable, Integer>();
	indexToVariable = new NumericVariable[1];
	variablesToIndex.put(var, new Integer(0));
	indexToVariable[0] = var;
    }

    public MultiIndexVariablesReference(SortedSet<NumericVariable> vars){
	variablesToIndex = new TreeMap<NumericVariable, Integer>();
	indexToVariable = new NumericVariable[vars.size()];
	int idx = 0;
	for (NumericVariable var : vars){
	    variablesToIndex.put(var, new Integer(idx));
	    indexToVariable[idx] = var;
	    idx++;
	}
    }

    @Override
    public boolean equals(Object o){
	if (!(o instanceof MultiIndexVariablesReference))
	    return false;
	MultiIndexVariablesReference other = (MultiIndexVariablesReference)o;
	return variablesToIndex.equals(other.variablesToIndex);
    }

    public int getDimension(){
	return variablesToIndex.size();
    }

    public int getIndex(NumericVariable var){
	Integer i = variablesToIndex.get(var);
	if (i == null)
	    return -1;
	else
	    return i.intValue();
    }

    public NumericVariable getVariable(int index){
	return indexToVariable[index];
    }

    @Override
    public int hashCode(){
	return variablesToIndex.hashCode();
    }

    public MultiIndexVariablesReference join(MultiIndexVariablesReference varRef2){
	TreeSet<NumericVariable> variables = new TreeSet<NumericVariable>();
	variables.addAll(variablesToIndex.keySet());
	variables.addAll(varRef2.variablesToIndex.keySet());
	return new MultiIndexVariablesReference(variables);
    }

    public MultiIndexVariablesReference join(NumericVariable var){
	TreeSet<NumericVariable> variables = new TreeSet<NumericVariable>();
	variables.addAll(variablesToIndex.keySet());
	variables.add(var);
	return new MultiIndexVariablesReference(variables);
    }

    public MultiIndexVariablesReference remove(int index){
	MultiIndexVariablesReference result = new MultiIndexVariablesReference();
	result.indexToVariable = new NumericVariable[getDimension() - 1];
	int insertPos = 0;
	for (int i = 0; i < indexToVariable.length; i++){
	    if (i != index){
		NumericVariable var = indexToVariable[i];
		result.indexToVariable[insertPos] = var;
		result.variablesToIndex.put(var, new Integer(insertPos));
	    }
	    insertPos++;
	}
	return result;
    }

    @Override
    public String toString(){
	StringBuffer sb = new StringBuffer();
	sb.append("(");
	if (variablesToIndex.size() > 0){
	    sb.append(indexToVariable[0].toString());
	}
	for (int i = 1; i < variablesToIndex.size(); i++){
	    sb.append(",");
	    sb.append(indexToVariable[i].toString());
	}
	sb.append(")");
	return sb.toString();
    }
}
