//package de.wwu.pi.entity;
//
//import java.io.Serializable;
//import java.util.Collection;
//
//import javax.persistence.CascadeType;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
//import javax.persistence.Table;
//
//@Entity
//@Table(name = "PERSISTENCE_ROSTER_LEAGUE")
//public abstract class LeagueInt implements Serializable {
//    private static final long serialVersionUID = 5060910864394673463L;
//    protected Collection<TeamInt> teams;
//    protected int id;
//    protected int name;
//    protected int sport;
//
//    @Id
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
//    public int getSport() {
//        return sport;
//    }
//
//    public void setSport(int sport) {
//        this.sport = sport;
//    }
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "league")
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