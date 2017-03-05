package de.wwu.muggl.solvers.solver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import de.wwu.muggl.solvers.SolverManager;

/**
 * Contains solver specific information like its name and priority
 * and is used to compare solvers by priority.
 */
@SuppressWarnings("all")
public class SolverInfo implements Comparable<SolverInfo>, Cloneable{

    protected String className;

    protected boolean enabled;

    /**
     * Positive integers only. Lower number means less prioritized.
     */
    protected int priority;

    protected Solver solver;

    public SolverInfo(String className, int priority, boolean enabled){
	this.priority = priority;
	this.enabled = enabled;
	this.className = className;
    }

    @Override
    public Object clone(){
	try{
	    SolverInfo result = (SolverInfo)super.clone();
	    result.solver = null;
	    return result;
	} catch (CloneNotSupportedException cnse){
	    throw new InternalError(cnse.toString());
	}
    }

    @Override
    public int compareTo(SolverInfo info){
	return priority - info.priority;
    }

    public int getPriority(){
	return priority;
    }

    public Solver getSolver(SolverManager solverManager){
	if (solver == null){
	    try{
		Class classRef = Class.forName(className);
		Class[] argTypes = {SolverManager.class};
		Method newInstanceMethod = classRef.getMethod("newInstance", argTypes);
		Object[] args = {solverManager};
		solver = (Solver)newInstanceMethod.invoke(null, args);
	    } catch (ClassNotFoundException cnfe){
		throw new InternalError(cnfe.toString());
	    } catch (IllegalAccessException iae){
		throw new InternalError(iae.toString());
	    } catch (NoSuchMethodException nsme){
		throw new InternalError("Method newInstance(SolverManager) is missing in class " + className);
	    } catch (InvocationTargetException ite){
	    	ite.printStackTrace();
		throw new InternalError(ite.toString());
	    }
	}
	return solver;
    }

    public String getSolverClassName(){
	return className;
    }

    public boolean isEnabled(){
	return enabled;
    }

    public void setEnabled(boolean flag){
	enabled = flag;
    }

    @Override
    public String toString(){
	return "(" + className + " " + priority + " "  + enabled + ")";
    }
}