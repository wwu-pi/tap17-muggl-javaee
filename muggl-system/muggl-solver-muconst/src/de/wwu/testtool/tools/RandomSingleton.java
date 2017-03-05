package de.wwu.testtool.tools;

import java.util.Random;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class RandomSingleton extends Random{

    protected static RandomSingleton instance;

    private RandomSingleton(){
	super();
    }

    public static RandomSingleton getInstance(){
	if (instance == null)
	    instance = new RandomSingleton();
	return instance;
    }

    public static boolean staticNextBoolean(){
	if (instance == null)
	    instance = new RandomSingleton();
	return instance.nextBoolean();
    }

}
