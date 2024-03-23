package de.herrmann.tippkick.spielrundenverwaltung.model;

public class CompetitionTeamsRelationDAO {

    private Integer id;
    private Integer competitionId;
    private Integer teamId;

    public CompetitionTeamsRelationDAO() {
    }

    public CompetitionTeamsRelationDAO(Integer id, Integer competitionId, Integer teamId) {
        this.id = id;
        this.competitionId = competitionId;
        this.teamId = teamId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Integer competitionId) {
        this.competitionId = competitionId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }
}
