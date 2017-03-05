package de.wwu.testtool.tools;

/**
 * A timer that waits the given amount of time to set its timeout state to
 * <i>true</i>. Can be used implement a timeout for some algorithms without
 * calling System.currentTimeInMillis() frequently.
 * @author Marko Ernsting
 */
public class Timer {

    /**
     * Stores the number of milliseconds the timer should wait.
     */
    private long timeToWaitInMillis;
    
    /**
     * Stores the time when the timer was started.
     */
    private long startTime;

    /**
     * Creates a new timer object that waits the given amount of time to set its
     * timeout state to <i>true</i>.
     * @param timeToWaitInMillis the time, the timer should wait.
     */
    public Timer(long timeToWaitInMillis){
	this.timeToWaitInMillis = timeToWaitInMillis;
    }

    /**
     * Waits for the specified amount of time and sets the timeout state to true
     * before the thread dies.
     */
    public void run(){
	//startTime = System.nanoTime() * 1000;
	startTime = System.currentTimeMillis();
    }

    /**
     * Returns whether the time, the timer should wait, is up or not.
     * @return <i>true</i> if the time if up, <i>false</i> if the timer still
     * has to wait.
     */
    public boolean timeout(){
	if (timeToWaitInMillis == 0) {
	    return false;
	} else {
	    return (getRemainingTime() > timeToWaitInMillis);
	}
    }

    public String toString() {
	return Long.toString(getRemainingTime());
    }

    public long getRemainingTime() {
	return System.currentTimeMillis() - startTime;
    }
}
