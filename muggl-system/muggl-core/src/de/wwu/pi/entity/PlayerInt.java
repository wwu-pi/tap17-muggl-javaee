//package de.wwu.pi.entity;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collection;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToMany;
//import javax.validation.constraints.Min;
//
//@Entity
//public class PlayerInt implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//	@Id
//    private int id;
//    private int name;
//    
//    @Min(value=3)
//    private int position;
//    
//    @Min(value=20)
//    private int salary;
//    
//    @ManyToMany(mappedBy="players")
//    private Collection<TeamInt> teams;
//    
//    /** Creates a new instance of Player */
//    public PlayerInt() {
//    }
//    
//    public PlayerInt(int id, int name, int position, int salary) {
//        this.id = id;
//        this.name = name;
//        this.position = position;
//        this.salary = salary;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public int getName() {
//        return name;
//    }
//
//    public void setName(int name) {
//        this.name = name;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public int getSalary() {
//        return salary;
//    }
//
//    public void setSalary(int salary) {
//        this.salary = salary;
//    }
//    
//    public Collection<TeamInt> getTeams() {
////    	if(teams == null) {
////    		return new ArrayList<>();
////    	}
//        return teams;
//    }
//
//    public void setTeams(Collection<TeamInt> teams) {
//        this.teams = teams;
//    }
//    
//    public void addTeam(TeamInt team) {
//        this.getTeams().add(team);
//    }
//    
//    public void dropTeam(TeamInt team) {
//        this.getTeams().remove(team);
//    }
//}
