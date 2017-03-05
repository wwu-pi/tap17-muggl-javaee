//package de.wwu.pi.entity;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collection;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.JoinTable;
//import javax.persistence.ManyToMany;
//import javax.persistence.ManyToOne;
//
//
//@Entity
//public class TeamInt implements Serializable {
//
//    @Id
//    private int id;
//    private int name;
//    private int city;
//    
//    @ManyToMany
//    @JoinTable(
//            name="PERSISTENCE_ROSTER_TEAM_PLAYER",
//            joinColumns=
//                @JoinColumn(name="TEAM_ID", referencedColumnName="ID"),
//            inverseJoinColumns=
//                @JoinColumn(name="PLAYER_ID", referencedColumnName="ID")
//        )
//    private Collection<PlayerInt> players;
//    @ManyToOne
//    private LeagueInt league;
//    
//    /** Creates a new instance of Team */
//    public TeamInt() {
//    }
//    
//    public TeamInt(int id, int name, int city) {
//        this.id = id;
//        this.name = name;
//        this.city = city;
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
//    public int getCity() {
//        return city;
//    }
//
//    public void setCity(int city) {
//        this.city = city;
//    }
//    
//    public Collection<PlayerInt> getPlayers() {
//    	if(players == null) {
//    		return new ArrayList<>();
//    	}
//        return players;
//    }
//
//    public void setPlayers(Collection<PlayerInt> players) {
//        this.players = players;
//    }
//    
//    public void addPlayer(PlayerInt player) {
////    	if(players == null) {
////    		players = new ArrayList<>();
////    	}
//    	this.players.add(player);
//    }
//    
//    public void dropPlayer(PlayerInt player) {
//        this.getPlayers().remove(player);
//    }
//    
//    public LeagueInt getLeague() {
//        return league;
//    }
//
//    public void setLeague(LeagueInt league) {
//        this.league = league;
//    }
//}
