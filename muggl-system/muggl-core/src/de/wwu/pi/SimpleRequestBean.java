//package de.wwu.pi;
//
//import javax.annotation.PostConstruct;
//import javax.ejb.Stateful;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.criteria.CriteriaBuilder;
//
//import de.wwu.pi.entity.Player;
//import de.wwu.pi.entity.Team;
//
//@Stateful
//public class SimpleRequestBean {
//
//    private CriteriaBuilder cb;
//    
//    @PersistenceContext
//    private EntityManager em;
//
//    @PostConstruct
//    private void init() {
//        cb = em.getCriteriaBuilder();
//    }
//	
//    public void createPlayer(String id, String name, String position, double salary) {
//        Player player = new Player(id, name, position, salary);
//        em.persist(player);
//    }
//
//    public void addPlayer(String playerId, String teamId) {
//        Player player = em.find(Player.class, playerId);
//        Team team = em.find(Team.class, teamId);
//
//        team.addPlayer(player);
//        player.addTeam(team);
//    }
//
//    public void removePlayer(String playerId) {
//        Player player = em.find(Player.class, playerId);
//
//        Collection<Team> teams = player.getTeams();
//        Iterator<Team> i = teams.iterator();
//
//        while (i.hasNext()) {
//            Team team = i.next();
//            team.dropPlayer(player);
//        }
//
//        em.remove(player);
//    }
//
//    @Override
//    public void dropPlayer(String playerId, String teamId) {
//        Player player = em.find(Player.class, playerId);
//        Team team = em.find(Team.class, teamId);
//
//        team.dropPlayer(player);
//        player.dropTeam(team);
//    }
//
//    public Player getPlayer(String playerId) {
//        return em.find(Player.class, playerId);
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
//        List<PlayerDetails> playerList = null;
//
//        try {
//            Team team = em.find(Team.class, teamId);
//            playerList = this.copyPlayersToDetails(
//                        (List<Player>) team.getPlayers());
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        return playerList;
//    }
//
//    @Override
//    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
//        logger.info("getTeamsOfLeague");
//
//        List<TeamDetails> detailsList = new ArrayList<TeamDetails>();
//        Collection<Team> teams = null;
//
//        try {
//            League league = em.find(League.class, leagueId);
//            teams = league.getTeams();
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        Iterator<Team> i = teams.iterator();
//
//        while (i.hasNext()) {
//            Team team = (Team) i.next();
//            TeamDetails teamDetails = new TeamDetails(
//                        team.getId(),
//                        team.getName(),
//                        team.getCity());
//            detailsList.add(teamDetails);
//        }
//
//        return detailsList;
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersByPosition(String position) {
//        logger.info("getPlayersByPosition");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            player.get(Player_.position),
//                            position));
//                cq.select(player);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
//        logger.info("getPlayersByHigherSalary");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player1 = cq.from(Player.class);
//                Root<Player> player2 = cq.from(Player.class);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player1.getModel();
//
//                // create a Predicate object that finds players with a salary
//                // greater than player1
//                Predicate gtPredicate = cb.greaterThan(
//                            player1.get(Player_.salary),
//                            player2.get(Player_.salary));
//
//                // create a Predicate object that finds the player based on
//                // the name parameter
//                Predicate equalPredicate = cb.equal(
//                            player2.get(Player_.name),
//                            name);
//                // set the where clause with the predicates
//                cq.where(gtPredicate, equalPredicate);
//                // set the select clause, and return only unique entries
//                cq.select(player1)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersBySalaryRange(
//        double low,
//        double high) {
//        logger.info("getPlayersBySalaryRange");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.between(
//                            player.get(Player_.salary),
//                            low,
//                            high));
//                // set the select clause
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
//        logger.info("getPlayersByLeagueId");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//                Join<Player, Team> team = player.join(Player_.teams);
//                Join<Team, League> league = team.join(Team_.league);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            league.get(League_.id),
//                            leagueId));
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersBySport(String sport) {
//        logger.info("getPlayersByLeagueId");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//                Join<Player, Team> team = player.join(Player_.teams);
//                Join<Team, League> league = team.join(Team_.league);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            league.get(League_.sport),
//                            sport));
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersByCity(String city) {
//        logger.info("getPlayersByCity");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//                Join<Player, Team> team = player.join(Player_.teams);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            team.get(Team_.city),
//                            city));
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getAllPlayers() {
//        logger.info("getAllPlayers");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//
//                cq.select(player);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersNotOnTeam() {
//        logger.info("getPlayersNotOnTeam");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(cb.isEmpty(player.get(Player_.teams)));
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<PlayerDetails> getPlayersByPositionAndName(
//        String position,
//        String name) {
//        logger.info("getPlayersByPositionAndName");
//
//        List<Player> players = null;
//
//        try {
//            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            player.get(Player_.position),
//                            position),
//                        cb.equal(
//                            player.get(Player_.name),
//                            name));
//                cq.select(player)
//                  .distinct(true);
//
//                TypedQuery<Player> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return copyPlayersToDetails(players);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
//        logger.info("getLeaguesOfPlayer");
//
//        List<LeagueDetails> detailsList = new ArrayList<LeagueDetails>();
//        List<League> leagues = null;
//
//        try {
//            CriteriaQuery<League> cq = cb.createQuery(League.class);
//
//            if (cq != null) {
//                Root<League> league = cq.from(League.class);
//
//                //EntityType<League> League_ = league.getModel();
//                Join<League, Team> team = league.join(League_.teams);
//
//                //EntityType<Team> Team_ = team.getModel();
//                Join<Team, Player> player = team.join(Team_.players);
//
//                cq.where(
//                        cb.equal(
//                            player.get(Player_.id),
//                            playerId));
//                cq.select(league)
//                  .distinct(true);
//
//                TypedQuery<League> q = em.createQuery(cq);
//                leagues = q.getResultList();
//            }
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        Iterator<League> i = leagues.iterator();
//
//        while (i.hasNext()) {
//            League league = (League) i.next();
//            LeagueDetails leagueDetails = new LeagueDetails(
//                        league.getId(),
//                        league.getName(),
//                        league.getSport());
//            detailsList.add(leagueDetails);
//        }
//
//        return detailsList;
//    }
//
//    @Override
//    public List<String> getSportsOfPlayer(String playerId) {
//        logger.info("getSportsOfPlayer");
//
//        List<String> sports = new ArrayList<String>();
//
//        try {
//            CriteriaQuery<String> cq = cb.createQuery(String.class);
//
//            if (cq != null) {
//                Root<Player> player = cq.from(Player.class);
//                Join<Player, Team> team = player.join(Player_.teams);
//                Join<Team, League> league = team.join(Team_.league);
//
//                // Get MetaModel from Root
//                //EntityType<Player> Player_ = player.getModel();
//
//                // set the where clause
//                cq.where(
//                        cb.equal(
//                            player.get(Player_.id),
//                            playerId));
//                cq.select(league.get(League_.sport))
//                  .distinct(true);
//
//                TypedQuery<String> q = em.createQuery(cq);
//                sports = q.getResultList();
//            }
//
//            //        Player player = em.find(Player.class, playerId);
//            //        Iterator<Team> i = player.getTeams().iterator();
//            //        while (i.hasNext()) {
//            //            Team team = i.next();
//            //            League league = team.getLeague();
//            //            sports.add(league.getSport());
//            //        }
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        return sports;
//    }
//
//    @Override
//    public void createTeamInLeague(
//        TeamDetails teamDetails,
//        String leagueId) {
//        logger.info("createTeamInLeague");
//
//        try {
//            League league = em.find(League.class, leagueId);
//            Team team = new Team(
//                        teamDetails.getId(),
//                        teamDetails.getName(),
//                        teamDetails.getCity());
//            em.persist(team);
//            team.setLeague(league);
//            league.addTeam(team);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public void removeTeam(String teamId) {
//        logger.info("removeTeam");
//
//        try {
//            Team team = em.find(Team.class, teamId);
//
//            Collection<Player> players = team.getPlayers();
//            Iterator<Player> i = players.iterator();
//
//            while (i.hasNext()) {
//                Player player = (Player) i.next();
//                player.dropTeam(team);
//            }
//
//            em.remove(team);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public TeamDetails getTeam(String teamId) {
//        logger.info("getTeam");
//
//        TeamDetails teamDetails = null;
//
//        try {
//            Team team = em.find(Team.class, teamId);
//            teamDetails = new TeamDetails(
//                        team.getId(),
//                        team.getName(),
//                        team.getCity());
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        return teamDetails;
//    }
//
//    @Override
//    public void createLeague(LeagueDetails leagueDetails) {
//        logger.info("createLeague");
//
//        try {
//            if (leagueDetails.getSport()
//                                 .equalsIgnoreCase("soccer")
//                    || leagueDetails.getSport()
//                                        .equalsIgnoreCase("swimming")
//                    || leagueDetails.getSport()
//                                        .equalsIgnoreCase("basketball")
//                    || leagueDetails.getSport()
//                                        .equalsIgnoreCase("baseball")) {
//                SummerLeague league = new SummerLeague(
//                            leagueDetails.getId(),
//                            leagueDetails.getName(),
//                            leagueDetails.getSport());
//                em.persist(league);
//            } else if (leagueDetails.getSport()
//                                        .equalsIgnoreCase("hockey")
//                    || leagueDetails.getSport()
//                                        .equalsIgnoreCase("skiing")
//                    || leagueDetails.getSport()
//                                        .equalsIgnoreCase("snowboarding")) {
//                WinterLeague league = new WinterLeague(
//                            leagueDetails.getId(),
//                            leagueDetails.getName(),
//                            leagueDetails.getSport());
//                em.persist(league);
//            } else {
//                throw new IncorrectSportException(
//                        "The specified sport is not valid.");
//            }
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public void removeLeague(String leagueId) {
//        logger.info("removeLeague");
//
//        try {
//            League league = em.find(League.class, leagueId);
//            em.remove(league);
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//    }
//
//    @Override
//    public LeagueDetails getLeague(String leagueId) {
//        logger.info("getLeague");
//
//        LeagueDetails leagueDetails = null;
//
//        try {
//            League league = em.find(League.class, leagueId);
//            leagueDetails = new LeagueDetails(
//                        league.getId(),
//                        league.getName(),
//                        league.getSport());
//        } catch (Exception ex) {
//            throw new EJBException(ex);
//        }
//
//        return leagueDetails;
//    }
//
//    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
//        List<PlayerDetails> detailsList = new ArrayList<PlayerDetails>();
//        Iterator<Player> i = players.iterator();
//
//        while (i.hasNext()) {
//            Player player = (Player) i.next();
//            PlayerDetails playerDetails = new PlayerDetails(
//                        player.getId(),
//                        player.getName(),
//                        player.getPosition(),
//                        player.getSalary());
//            detailsList.add(playerDetails);
//        }
//
//        return detailsList;
//    }
//}
