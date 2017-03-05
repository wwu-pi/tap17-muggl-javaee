//package de.wwu.pi.entity;
//
//import java.io.Serializable;
//import java.util.Collection;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToMany;
//import javax.persistence.Table;
//
//
//@Entity
//@Table(name = "PERSISTENCE_ROSTER_PLAYER")
//public class NewPlayer implements Serializable {
//    private static final long serialVersionUID = -2760127516426049966L;
//    private Collection<TeamInt> teams;
//    private String id;
//    private String name;
//    private String position;
//    private double salary;
//
//    /** Creates a new instance of Player */
//    public NewPlayer() {
//    }
//
//    public NewPlayer(
//        String id,
//        String name,
//        String position,
//        double salary) {
//        this.id = id;
//        this.name = name;
//        this.position = position;
//        this.salary = salary;
//    }
//
//    @Id
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getPosition() {
//        return position;
//    }
//
//    public void setPosition(String position) {
//        this.position = position;
//    }
//
//    public double getSalary() {
//        return salary;
//    }
//
//    public void setSalary(double salary) {
//        this.salary = salary;
//    }
//
//    @ManyToMany(mappedBy = "players")
//    public Collection<TeamInt> getTeams() {
//        return teams;
//    }
//
//    public void setTeams(Collection<TeamInt> teams) {
//        this.teams = teams;
//    }
//
//    public void addTeam(TeamInt team) {
//        this.getTeams()
//            .add(team);
//    }
//
//    public void dropTeam(TeamInt team) {
//        this.getTeams()
//            .remove(team);
//    }
//}