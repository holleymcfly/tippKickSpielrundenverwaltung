package de.herrmann.tippkick.spielrundenverwaltung.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompetitionDAO {

    private Integer id;
    private CompetitionType competitionType;
    private String name;
    private Integer numberOfTeams = 0;
    private boolean isStarted = false;
    private Date startedAt;

    private final List<CompetitionTeamsRelationDAO> teamRelations = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CompetitionDAO() {
    }

    public CompetitionDAO(Integer id, CompetitionType competitionType, String name,
                          Integer numberOfTeams, boolean isStarted, Date startedAt) {
        this.id = id;
        this.competitionType = competitionType;
        this.name = name;
        this.numberOfTeams = numberOfTeams;
        this.isStarted = isStarted;
        this.startedAt = startedAt;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(Integer numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public String toString() {
        return name + " (" + competitionType.toString() + ")";
    }

    public List<CompetitionTeamsRelationDAO> getTeamRelations() {
        return teamRelations;
    }

    public void addTeamRelation(CompetitionTeamsRelationDAO relation) {
        this.teamRelations.add(relation);
    }

    public void addTeamRelations(List<CompetitionTeamsRelationDAO> relations) {
        this.teamRelations.addAll(relations);
    }
}
