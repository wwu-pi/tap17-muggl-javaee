package de.wwu.testtool.solver.tsolver;

/**
 * The to be statistics class which encapsulates all common statistics that could be desirable.
 * @author Marko Ernsting
 *
 */
@SuppressWarnings("all")
public class SolverStatistics {
    
    private long addConstraintTotalTime = 0;
    private long addConstraintLastTime = 0;
    private long addConstraintLastTimeTemp = 0;
    private long addConstraintCount = 0;
    
    private long getSolutionTotalTime = 0;
    private long getSolutionLastTime = 0;
    private long getSolutionLastTimeTemp = 0;
    private long getSolutionCount = 0;

    
    public long addConstraintTotalTime(){
	return addConstraintTotalTime;
    }
    
    public long addConstraintLastTime() {
	return addConstraintLastTime;
    }
    
    public long addConstraintCount(){
	return addConstraintCount;
    }
    
    public void addConstraintTimeStart(){
	addConstraintLastTimeTemp = System.nanoTime();;
    }
    
    public void addConstraintTimeStop(){
	addConstraintLastTime = System.nanoTime() - addConstraintLastTimeTemp;
	addConstraintTotalTime += addConstraintLastTime;
	addConstraintCount++;
    }
}
