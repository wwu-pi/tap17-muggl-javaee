package de.wwu.pi.entity;

import java.io.Serializable;


public class PlayerDetails implements Serializable {
	
    private static final long serialVersionUID = -5352446961599198526L;
    
    private int id;
    private int name;
    private int position;
    private int salary;

    public PlayerDetails() {
    }

    public PlayerDetails(
        int id,
        int name,
        int position,
        int salary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        String s = id + " " + name + " " + position + " " + salary;

        return s;
    }
}